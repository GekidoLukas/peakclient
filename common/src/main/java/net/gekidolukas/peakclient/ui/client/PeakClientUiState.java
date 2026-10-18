package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.render.PeakCameraState;
import net.gekidolukas.peakclient.render.PeakElementLifecycleRegistry;
import net.gekidolukas.peakclient.ui.PeakAbstractProgress;
import net.gekidolukas.peakclient.ui.PeakAnimated;
import net.gekidolukas.peakclient.ui.PeakCarousel;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakCycler;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPopUpContainer;
import net.gekidolukas.peakclient.ui.PeakFeed;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.PeakStagger;
import net.gekidolukas.peakclient.ui.PeakTimer;
import net.gekidolukas.peakclient.ui.PeakUiOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakClientUiState {
    private static final Map<String, DocumentState> ACTIVE_DOCUMENTS = new ConcurrentHashMap<>();
    private static final Map<String, String> ELEMENT_TO_DOCUMENT = new ConcurrentHashMap<>();
    private PeakClientUiState() {}

    public static void applyDocument(PeakDocument document) {
        primeStatefulElements(document);
        DocumentState previous = ACTIVE_DOCUMENTS.get(document.getId());
        DocumentState current = new DocumentState(document);
        ACTIVE_DOCUMENTS.put(document.getId(), current);
        PeakClientVanillaHudState.setDocumentHides(document.getId(), document.getHiddenVanilla());
        if (previous == null) return;
        for (String id : previous.model.ids()) {
            if (current.model.contains(id)) continue;
            ELEMENT_TO_DOCUMENT.remove(id, document.getId());
            PeakElementLifecycleRegistry.notifyRemoved(id);
        }
    }
    private static void primeStatefulElements(PeakElement<?> element) {
        resetAnchors(element);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) primeStatefulElements(child);
        }
    }

    private static void resetAnchors(PeakElement<?> element) {
        PeakEventRuntime.reset(element);
        if (element instanceof PeakTimer timer) timer.resetAnchor();
        if (element instanceof PeakAbstractProgress<?> progress) progress.resetAnchor();
        if (element instanceof PeakPopUpContainer popup) popup.resetAnchor();
        if (element instanceof PeakLabel label) label.resetAnchor();
        if (element instanceof PeakCarousel carousel) carousel.resetAnchor();
        if (element instanceof PeakCycler cycler) cycler.resetAnchor();
        if (element instanceof PeakAnimated animated) animated.resetAnchor();
        if (element instanceof PeakStagger stagger) stagger.resetAnchor();
        if (element instanceof PeakFeed feed) feed.resetAnchor();
        if (element instanceof PeakSortedList sortedList) sortedList.resetAnchor();
        if (element instanceof PeakReel reel) reel.resetAnchor();
    }

    public static void removeDocument(String documentId) {
        removeDocumentAndIndex(documentId);
    }

    private static void removeDocumentAndIndex(String docId) {
        DocumentState state = ACTIVE_DOCUMENTS.remove(docId);
        if (state == null) return;
        PeakClientVanillaHudState.removeDocument(docId);
        for (String id : state.model.ids()) {
            ELEMENT_TO_DOCUMENT.remove(id);
            PeakElementLifecycleRegistry.notifyRemoved(id);
        }
    }

    public static void clearAll() {
        for (DocumentState state : ACTIVE_DOCUMENTS.values()) {
            for (String id : state.model.ids()) PeakElementLifecycleRegistry.notifyRemoved(id);
        }
        ACTIVE_DOCUMENTS.clear();
        ELEMENT_TO_DOCUMENT.clear();
        PeakClientVanillaHudState.clearDocumentHides();
        PeakCameraState.clear();
        PeakWorldAnchorTargets.clear();
    }

    public static PeakDocumentModel model(String documentId) {
        DocumentState state = ACTIVE_DOCUMENTS.get(documentId);
        return state == null ? null : state.model;
    }

    public static Collection<PeakDocument> getDocuments() {
        return ACTIVE_DOCUMENTS.values().stream().map(state -> state.model.document()).toList();
    }

    public static void removeElementById(String elementId) {
        String docId = ELEMENT_TO_DOCUMENT.get(elementId);
        if (docId == null) return;
        applyOperations(docId, List.of(new PeakUiOperation.Delete(elementId)));
    }

    public static void applyOperations(String documentId, List<PeakUiOperation> operations) {
        DocumentState state = ACTIVE_DOCUMENTS.get(documentId);
        if (state == null) {
            PeakLog.warn("UI update received for unknown document '{}' - ignored", documentId);
            return;
        }
        state.lastTouchedTick = PeakClientTickCounter.current();

        for (PeakUiOperation operation : operations) {
            try {
                state.model.applyOperation(operation);
            } catch (Exception e) {
                PeakLog.error("Failed to apply a UI operation in document '" + documentId + "'", e);
            }
        }
    }

    public static void tickDocumentLifecycles() {
        long now = PeakClientTickCounter.current();
        List<String> toRemove = new ArrayList<>();
        for (var entry : ACTIVE_DOCUMENTS.entrySet()) {
            DocumentState state = entry.getValue();
            PeakDocument doc = state.model.document();
            if (doc.getDocumentTimeToLiveTicks() > 0 && now - state.lastTouchedTick >= doc.getDocumentTimeToLiveTicks()) {
                toRemove.add(entry.getKey());
                continue;
            }
            if (doc.isDeleteIfEmpty() && doc.getChildren().isEmpty()) {
                toRemove.add(entry.getKey());
            }
        }
        for (String id : toRemove) removeDocumentAndIndex(id);
    }

    private static final class DocumentState {
        final PeakDocumentModel model;
        long lastTouchedTick;

        DocumentState(PeakDocument document) {
            this.lastTouchedTick = PeakClientTickCounter.current();
            String documentId = document.getId();
            this.model = new PeakDocumentModel(document, new PeakDocumentModel.Listener() {
                @Override
                public void onIndexed(PeakElement<?> element) {
                    ELEMENT_TO_DOCUMENT.put(element.getId(), documentId);
                }

                @Override
                public void onRemoved(PeakElement<?> element) {
                    ELEMENT_TO_DOCUMENT.remove(element.getId(), documentId);
                    PeakElementLifecycleRegistry.notifyRemoved(element.getId());
                }

                @Override
                public void onAdded(PeakElement<?> element) {
                    resetAnchors(element);
                }
            });
        }
    }
}
