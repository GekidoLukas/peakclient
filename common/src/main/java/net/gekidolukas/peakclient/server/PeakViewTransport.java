package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakMenuCommand;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakUiOperation;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PeakViewTransport {
    PeakViewTransport DOCUMENT = new PeakViewTransport() {
        @Override
        public void sendDocument(PeakDocument document, Collection<UUID> targets) {
            PeakClientApi.get().sendUiToClient(document, targets);
        }

        @Override
        public void sendOperations(String documentId, List<PeakUiOperation> operations, Collection<UUID> targets) {
            PeakClientApi.get().sendUiUpdateToClient(documentId, operations, targets);
        }

        @Override
        public void sendClose(String documentId, Collection<UUID> targets) {
            PeakClientApi.get().sendUiToClient(new PeakDocument(documentId).deleteIfEmpty(true), targets);
        }
    };

    PeakViewTransport MENU = new PeakViewTransport() {
        @Override
        public void sendDocument(PeakDocument document, Collection<UUID> targets) {
            PeakClientApi.get().sendMenuToClient(document, targets);
        }

        @Override
        public void sendOperations(String documentId, List<PeakUiOperation> operations, Collection<UUID> targets) {
            PeakClientApi.get().sendMenuUpdateToClient(documentId, operations, targets);
        }

        @Override
        public void sendClose(String documentId, Collection<UUID> targets) {
            PeakClientApi.get().menuCommand(PeakMenuCommand.REMOVE, documentId, 0, targets);
        }
    };

    void sendDocument(PeakDocument document, Collection<UUID> targets);

    void sendOperations(String documentId, List<PeakUiOperation> operations, Collection<UUID> targets);

    void sendClose(String documentId, Collection<UUID> targets);
}
