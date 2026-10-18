package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakId;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class PeakDocumentEvents {
    private static final List<Consumer<PeakDocumentActionEvent>> LISTENERS = new CopyOnWriteArrayList<>();
    private static final Map<String, Map<String, List<Consumer<PeakDocumentActionEvent>>>> HANDLERS = new ConcurrentHashMap<>();

    private PeakDocumentEvents() {}

    public static void onAction(Consumer<PeakDocumentActionEvent> listener) {
        LISTENERS.add(listener);
    }

    public static void on(String documentId, String actionId, Consumer<PeakDocumentActionEvent> handler) {
        PeakId.require(documentId, "Document");
        PeakId.require(actionId, "Action");
        HANDLERS.computeIfAbsent(documentId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(actionId, id -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    static void dispatchAction(PeakDocumentActionEvent event) {
        Map<String, List<Consumer<PeakDocumentActionEvent>>> forDocument = HANDLERS.get(event.documentId());
        if (forDocument != null) {
            List<Consumer<PeakDocumentActionEvent>> handlers = forDocument.get(event.actionId());
            if (handlers != null) for (Consumer<PeakDocumentActionEvent> handler : handlers) call(handler, event);
        }
        for (Consumer<PeakDocumentActionEvent> listener : LISTENERS) call(listener, event);
    }

    private static void call(Consumer<PeakDocumentActionEvent> handler, PeakDocumentActionEvent event) {
        try {
            handler.accept(event);
        } catch (Exception e) {
            PeakLog.error("A document action handler failed for '" + event.documentId() + "/" + event.actionId() + "'", e);
        }
    }

    public static void clearHandlers() {
        LISTENERS.clear();
        HANDLERS.clear();
    }
}
