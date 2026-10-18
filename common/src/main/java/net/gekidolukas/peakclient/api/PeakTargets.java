package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.helper.PeakHud;
import net.gekidolukas.peakclient.helper.PeakPopupBuilder;
import net.gekidolukas.peakclient.server.PeakClients;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakRef;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.server.PeakSentDocuments;
import net.gekidolukas.peakclient.ui.patch.PeakReelPatch;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.PeakWaypoint;
import net.gekidolukas.peakclient.ui.patch.PeakBasePatch;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PeakTargets {
    private final Supplier<Collection<UUID>> source;

    PeakTargets(Supplier<Collection<UUID>> source) {
        this.source = source;
    }

    static PeakTargets ofIds(Collection<UUID> playerIds) {
        List<UUID> snapshot = List.copyOf(new LinkedHashSet<>(playerIds));
        return new PeakTargets(() -> snapshot);
    }

    public Collection<UUID> ids() {
        return source.get();
    }

    public List<UUID> reachable() {
        List<UUID> result = new ArrayList<>();
        for (UUID id : ids()) {
            if (PeakClientServer.resolvePlayer(id) != null && PeakClients.hasClient(id)) result.add(id);
        }
        return result;
    }

    public int count() {
        return reachable().size();
    }

    private PeakClientApi api() {
        return PeakClientApi.get();
    }

    public void sendUi(PeakDocument document) {
        api().sendUiToClient(document, ids());
    }

    public void removeUi(String documentId) {
        api().sendUiToClient(new PeakDocument(documentId).deleteIfEmpty(true), ids());
    }

    public void sendUiUpdate(String documentId, List<PeakUiOperation> operations) {
        api().sendUiUpdateToClient(documentId, operations, ids());
    }

    public void updateElement(String documentId, String elementId, PeakBasePatch<?> patch) {
        sendUiUpdate(documentId, List.of(new PeakUiOperation.Update(elementId, patch.build().deepCopy())));
    }

    public void updateElementsByClass(String documentId, String className, PeakBasePatch<?> patch) {
        sendUiUpdate(documentId, List.of(new PeakUiOperation.UpdateByClass(className, patch.build().deepCopy())));
    }

    public void deleteElement(String documentId, String elementId) {
        sendUiUpdate(documentId, List.of(new PeakUiOperation.Delete(elementId)));
    }

    public void addElement(String documentId, String parentId, PeakElement<?> element) {
        sendUiUpdate(documentId, List.of(new PeakUiOperation.Add(parentId, element)));
    }

    public void addElement(String documentId, PeakRef<?, ?> parent, PeakElement<?> element) {
        addElement(documentId, parent.id(), element);
    }

    public void spinReel(String documentId, String reelId, String childId) {
        sendUiUpdate(documentId, List.of(PeakReelSpins.spinTo(reelId, childId)));
    }

    public void spinReel(String documentId, PeakRef<PeakReel, PeakReelPatch> reel, String childId) {
        spinReel(documentId, reel.id(), childId);
    }

    public void spinReel(String documentId, String reelId, PeakElement<?> winner) {
        PeakDocument known = PeakSentDocuments.knownDocument(ids(), documentId);
        PeakElement<?> knownReel = known == null ? null : known.findById(reelId);
        sendUiUpdate(documentId, PeakReelSpins.operations(reelId, winner, knownReel, ThreadLocalRandom.current()));
    }

    public void spinReel(String documentId, PeakRef<PeakReel, PeakReelPatch> reel, PeakElement<?> winner) {
        spinReel(documentId, reel.id(), winner);
    }

    public void updateElement(String documentId, PeakElement<?> element, PeakBasePatch<?> patch) {
        updateElement(documentId, element.getId(), patch);
    }

    public void updateElement(PeakDocument document, PeakElement<?> element, PeakBasePatch<?> patch) {
        updateElement(document.getId(), element.getId(), patch);
    }

    public <E extends PeakElement<?>, P extends PeakBasePatch<?>> void updateElement(String documentId, PeakRef<E, P> ref, Consumer<P> patcher) {
        P patch = ref.newPatch();
        patcher.accept(patch);
        updateElement(documentId, ref.id(), patch);
    }

    public void deleteElement(String documentId, PeakElement<?> element) {
        deleteElement(documentId, element.getId());
    }

    public void deleteElement(String documentId, PeakRef<?, ?> ref) {
        deleteElement(documentId, ref.id());
    }

    public void pauseTimer(String documentId, String timerId) {
        updateElement(documentId, timerId, PeakPatch.timer().running(false));
    }

    public void resumeTimer(String documentId, String timerId) {
        updateElement(documentId, timerId, PeakPatch.timer().running(true));
    }

    public void syncTimer(String documentId, String timerId, long value, boolean running) {
        updateElement(documentId, timerId, PeakPatch.timer().value(value).running(running));
    }

    public <T> void setValue(PeakValueKey<T> key, T value) {
        api().setCustomValue(key.namespace(), key.key(), key.format(value), ids());
    }

    public void setValue(String namespacedKey, String value) {
        PeakValueKey<String> key = PeakValueKey.ofString(namespacedKey);
        api().setCustomValue(key.namespace(), key.key(), value, ids());
    }

    public void setValue(String namespacedKey, int value) { setValue(namespacedKey, Integer.toString(value)); }

    public void setValue(String namespacedKey, long value) { setValue(namespacedKey, Long.toString(value)); }

    public void setValue(String namespacedKey, double value) { setValue(namespacedKey, String.valueOf(value)); }

    public void setValue(String namespacedKey, double value, String format) {
        setValue(namespacedKey, String.format(Locale.ROOT, format, value));
    }

    public void setValue(String namespacedKey, boolean value) { setValue(namespacedKey, Boolean.toString(value)); }

    public <T> void clearValue(PeakValueKey<T> key) {
        api().clearCustomValue(key.namespace(), key.key(), ids());
    }

    public void clearValue(String namespacedKey) {
        PeakValueKey<String> key = PeakValueKey.ofString(namespacedKey);
        api().clearCustomValue(key.namespace(), key.key(), ids());
    }

    public void clearValues(String namespace) {
        api().clearCustomNamespace(namespace, ids());
    }

    public PeakVanillaHud vanillaHud() {
        return new PeakVanillaHud(source);
    }

    public PeakMenus menus() {
        return new PeakMenus(source);
    }

    public void upsertWaypoint(PeakWaypoint waypoint) {
        api().upsertWaypoint(waypoint, ids());
    }

    public void removeWaypoint(String waypointId) {
        api().removeWaypoint(waypointId, ids());
    }

    public void clearWaypoints() {
        api().clearWaypoints(ids());
    }

    public void syncWaypointPosition(String waypointId, double x, double y, double z) {
        api().syncWaypointPosition(waypointId, x, y, z, ids());
    }

    public void setCustomWorldBorder(double minX, double minZ, double maxX, double maxZ) {
        api().setCustomWorldBorder(minX, minZ, maxX, maxZ, ids());
    }

    public void setNextCustomWorldBorder(double minX, double minZ, double maxX, double maxZ, int transitionTicks) {
        api().setNextCustomWorldBorder(minX, minZ, maxX, maxZ, transitionTicks, ids());
    }

    public void clearCustomWorldBorder() {
        api().clearCustomWorldBorder(ids());
    }

    public void notify(String text) {
        notify(text, PeakPopupAnimation.SLIDE_FROM_RIGHT, 0xFFFFFF, 60);
    }

    public void notify(String text, PeakPopupAnimation animation) {
        notify(text, animation, 0xFFFFFF, 60);
    }

    public void notify(String text, PeakPopupAnimation animation, PeakPaint color, int timeToLiveTicks) {
        PeakHud.notify(ids(), text, animation, color, timeToLiveTicks);
    }

    public void notify(String text, PeakPopupAnimation animation, int color, int timeToLiveTicks) {
        notify(text, animation, PeakPaint.solid(color), timeToLiveTicks);
    }

    public void notifySuccess(String text) { notify(text, PeakPopupAnimation.SLIDE_FROM_RIGHT, 0x55FF55, 60); }

    public void notifyWarning(String text) { notify(text, PeakPopupAnimation.SLIDE_FROM_RIGHT, 0xFFAA00, 60); }

    public void notifyError(String text) { notify(text, PeakPopupAnimation.SLIDE_FROM_RIGHT, 0xFF5555, 60); }

    public PeakPopupBuilder popup() {
        return new PeakPopupBuilder(this);
    }
}
