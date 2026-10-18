package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakDocumentActionEvent;
import net.gekidolukas.peakclient.server.PeakDocumentEvents;

import java.util.function.Consumer;

public final class PeakDocumentHandlers {
    private final String documentId;

    PeakDocumentHandlers(String documentId) {
        this.documentId = documentId;
    }

    public PeakDocumentHandlers on(String actionId, Consumer<PeakDocumentActionEvent> handler) {
        PeakDocumentEvents.on(documentId, actionId, handler);
        return this;
    }
}
