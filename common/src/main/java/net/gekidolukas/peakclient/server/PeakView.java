package net.gekidolukas.peakclient.server;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.PeakServerThread;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentDiff;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.api.PeakReelSpins;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakRef;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.patch.PeakBasePatch;
import net.gekidolukas.peakclient.ui.patch.PeakReelPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class PeakView {
    private final PeakAudience audience;
    private final PeakDocumentModel model;
    private final PeakViewTransport transport;
    private final Set<UUID> known = ConcurrentHashMap.newKeySet();
    private final List<PeakUiOperation> pending = new ArrayList<>();
    private volatile boolean persistent = false;
    private volatile boolean closed = false;

    PeakView(PeakAudience audience, PeakDocument document) {
        this(audience, document, PeakViewTransport.DOCUMENT);
    }

    PeakView(PeakAudience audience, PeakDocument document, PeakViewTransport transport) {
        this.audience = audience;
        this.model = new PeakDocumentModel(document);
        this.transport = transport;
    }

    public String documentId() { return model.document().getId(); }

    public PeakDocument document() { return model.document(); }

    public PeakAudience audience() { return audience; }

    public boolean isPersistent() { return persistent; }

    public boolean isClosed() { return closed; }

    public PeakView persistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public PeakView update(String elementId, PeakBasePatch<?> patch) {
        JsonObject built = patch.build().deepCopy();
        PeakServerThread.run(() -> queue(new PeakUiOperation.Update(elementId, built), elementId));
        return this;
    }

    public PeakView update(PeakElement<?> element, PeakBasePatch<?> patch) {
        return update(element.getId(), patch);
    }

    public <E extends PeakElement<?>, P extends PeakBasePatch<?>> PeakView update(PeakRef<E, P> ref, Consumer<P> patcher) {
        P patch = ref.newPatch();
        patcher.accept(patch);
        JsonObject built = patch.build().deepCopy();
        PeakServerThread.run(() -> {
            if (!matchesType(ref)) return;
            queue(new PeakUiOperation.Update(ref.id(), built), ref.id());
        });
        return this;
    }

    public PeakView updateByClass(String className, PeakBasePatch<?> patch) {
        JsonObject built = patch.build().deepCopy();
        PeakServerThread.run(() -> queue(new PeakUiOperation.UpdateByClass(className, built), null));
        return this;
    }

    public PeakView add(String parentId, PeakElement<?> element) {
        PeakServerThread.run(() -> queue(new PeakUiOperation.Add(parentId, element), parentId));
        return this;
    }

    public PeakView add(PeakElement<?> parent, PeakElement<?> element) {
        return add(parent.getId(), element);
    }

    public PeakView add(PeakRef<?, ?> parent, PeakElement<?> element) {
        PeakServerThread.run(() -> {
            if (!matchesType(parent)) return;
            queue(new PeakUiOperation.Add(parent.id(), element), parent.id());
        });
        return this;
    }

    public PeakView spinReel(String reelId, String childId) {
        PeakServerThread.run(() -> queue(PeakReelSpins.spinTo(reelId, childId), reelId));
        return this;
    }

    public PeakView spinReel(PeakRef<PeakReel, PeakReelPatch> reel, String childId) {
        return spinReel(reel.id(), childId);
    }

    public PeakView spinReel(String reelId, PeakElement<?> winner) {
        PeakServerThread.run(() -> {
            if (!model.contains(reelId)) {
                PeakLog.warn("View '{}': reel '{}' is not in the document - spin ignored", documentId(), reelId);
                return;
            }
            for (PeakUiOperation operation : PeakReelSpins.operations(reelId, winner, model.find(reelId), ThreadLocalRandom.current())) {
                queue(operation, reelId);
            }
        });
        return this;
    }

    public PeakView spinReel(PeakRef<PeakReel, PeakReelPatch> reel, PeakElement<?> winner) {
        return spinReel(reel.id(), winner);
    }

    public PeakView delete(String elementId) {
        PeakServerThread.run(() -> queue(new PeakUiOperation.Delete(elementId), elementId));
        return this;
    }

    public PeakView delete(PeakElement<?> element) {
        return delete(element.getId());
    }

    public PeakView delete(PeakRef<?, ?> ref) {
        PeakServerThread.run(() -> {
            if (!matchesType(ref)) return;
            queue(new PeakUiOperation.Delete(ref.id()), ref.id());
        });
        return this;
    }

    public PeakView replace(PeakDocument newDocument) {
        PeakServerThread.run(() -> {
            if (closed) return;
            for (PeakUiOperation operation : PeakDocumentDiff.diff(model.document(), newDocument)) {
                if (model.applyOperation(operation)) pending.add(operation);
            }
        });
        return this;
    }

    public void flush() {
        PeakServerThread.run(this::doFlush);
    }

    public void close() {
        PeakServerThread.run(() -> {
            if (closed) return;
            doFlush();
            closed = true;
            transport.sendClose(documentId(), audience.members());
            audience.removeView(this);
        });
    }

    void open() {
        var members = audience.members();
        if (members.isEmpty()) return;
        transport.sendDocument(model.document(), members);
        known.addAll(members);
    }

    void onJoin(UUID playerId) {
        if (closed) return;
        doFlush();
        if (persistent || !known.contains(playerId)) {
            transport.sendDocument(model.document(), List.of(playerId));
        }
        known.add(playerId);
    }

    private void doFlush() {
        if (pending.isEmpty() || closed) return;
        List<PeakUiOperation> operations = new ArrayList<>(pending);
        pending.clear();
        transport.sendOperations(documentId(), operations, audience.members());
    }

    private boolean matchesType(PeakRef<?, ?> ref) {
        PeakElement<?> existing = model.find(ref.id());
        if (existing == null || ref.elementClass().isInstance(existing)) return true;
        PeakLog.warn("View '{}': reference '{}' expects a {} but the element is a {} - operation ignored",
                documentId(), ref.id(), ref.elementClass().getSimpleName(), existing.getClass().getSimpleName());
        return false;
    }

    private PeakView queue(PeakUiOperation operation, String requiredElementId) {
        if (closed) return this;
        if (requiredElementId != null && !model.contains(requiredElementId)) {
            PeakLog.warn("View '{}' has no element '{}' - operation ignored", documentId(), requiredElementId);
            return this;
        }
        if (model.applyOperation(operation)) pending.add(operation);
        return this;
    }
}
