package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.PeakClientNetworking;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.api.PeakMenuCommand;
import net.gekidolukas.peakclient.network.PeakMenuPayload;
import net.gekidolukas.peakclient.network.PeakProtocol;
import net.gekidolukas.peakclient.network.PeakUiDocumentPayload;
import net.gekidolukas.peakclient.network.PeakUiUpdatePayload;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentValidator;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.serialize.PeakGzip;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

final class PeakUiSender {
    private PeakUiSender() {}

    static void sendDocument(PeakDocument document, List<UUID> playerIds) {
        String json = PeakUiSerializer.toJson(document);
        byte[] compressed = PeakGzip.compress(json);
        PeakDocumentValidator.validateAndLog(document, json.length(), compressed.length);
        if (!fits("Document '" + document.getId() + "'", compressed.length)) return;
        for (UUID id : playerIds) {
            PeakSentDocuments.rememberDocument(id, document.getId(), document);
        }

        List<ServerPlayer> players = PeakRecipients.resolve(playerIds);
        if (players.isEmpty()) return;
        sendAll(players, new PeakUiDocumentPayload(compressed));
    }

    static void sendDocumentUpdate(String documentId, List<PeakUiOperation> operations, List<UUID> playerIds) {
        PeakDocument known = PeakSentDocuments.knownDocument(playerIds, documentId);
        sendUpdate("Update for document '" + documentId + "'", documentId, known, operations, playerIds,
                compressed -> new PeakUiUpdatePayload(documentId, compressed));
    }

    static void sendMenu(PeakDocument menu, List<UUID> playerIds) {
        String menuId = menu.getId();
        if (menuId.length() > PeakMenuPayload.MAX_ID_LENGTH) {
            PeakLog.error("Menu '" + menuId + "' was not sent: its id is longer than " + PeakMenuPayload.MAX_ID_LENGTH + " characters");
            return;
        }
        String json = PeakUiSerializer.toJson(menu);
        byte[] compressed = PeakGzip.compress(json);
        PeakDocumentValidator.validateAndLog(menu, json.length(), compressed.length);
        if (!fits("Menu '" + menuId + "'", compressed.length)) return;

        PeakMenuPayload payload = new PeakMenuPayload(PeakMenuPayload.Kind.SEND, menuId, 0, compressed);
        for (ServerPlayer player : PeakRecipients.resolve(playerIds)) {
            UUID id = player.getUUID();
            PeakSentDocuments.rememberMenu(id, menuId, menu);
            PeakMenuSessions.markSent(id, menuId);
            PeakClientNetworking.sendToClient(player, payload);
        }
    }

    static void sendMenuUpdate(String menuId, List<PeakUiOperation> operations, List<UUID> playerIds) {
        PeakDocument known = PeakSentDocuments.knownMenu(playerIds, menuId);
        sendUpdate("Update for menu '" + menuId + "'", menuId, known, operations, playerIds,
                compressed -> new PeakMenuPayload(PeakMenuPayload.Kind.UPDATE, menuId, 0, compressed));
    }

    static void menuCommand(PeakMenuCommand command, String menuId, int flags, List<UUID> playerIds) {
        List<ServerPlayer> players = PeakRecipients.resolve(playerIds);
        if (players.isEmpty()) return;
        PeakMenuPayload.Kind kind = switch (command) {
            case OPEN -> PeakMenuPayload.Kind.OPEN;
            case CLOSE -> PeakMenuPayload.Kind.CLOSE;
            case BACK -> PeakMenuPayload.Kind.BACK;
            case REMOVE -> PeakMenuPayload.Kind.REMOVE;
            case CLEAR -> PeakMenuPayload.Kind.CLEAR;
        };
        PeakMenuPayload payload = PeakMenuPayload.command(kind, menuId, flags);
        int unknown = 0;
        for (ServerPlayer player : players) {
            UUID id = player.getUUID();
            if (command == PeakMenuCommand.OPEN && !PeakMenuSessions.isKnown(id, menuId)) {
                unknown++;
                continue;
            }
            if (command == PeakMenuCommand.REMOVE) {
                PeakMenuSessions.markRemoved(id, menuId);
                PeakSentDocuments.forgetMenu(id, menuId);
            } else if (command == PeakMenuCommand.CLEAR) {
                PeakMenuSessions.markCleared(id);
                PeakSentDocuments.forgetMenus(id);
            }
            PeakClientNetworking.sendToClient(player, payload);
        }
        if (unknown > 0) {
            PeakLog.warn("Menu '{}' was not opened for {} player(s) because it was never sent to them, send it first", menuId, unknown);
        }
    }

    private static void sendUpdate(String what, String id, PeakDocument known, List<PeakUiOperation> operations,
                                   List<UUID> playerIds, Function<byte[], CustomPacketPayload> payload) {
        PeakDocumentValidator.validateOperationsAndLog(id, operations, known);

        List<ServerPlayer> players = PeakRecipients.resolve(playerIds);
        if (players.isEmpty()) return;
        byte[] compressed = PeakGzip.compress(PeakUiSerializer.operationsToJson(operations));
        if (!fits(what, compressed.length)) return;
        sendAll(players, payload.apply(compressed));
    }

    private static boolean fits(String what, int compressedBytes) {
        if (!PeakDocumentValidator.exceedsPayloadLimit(compressedBytes)) return true;
        PeakLog.error(what + " was not sent: " + compressedBytes + " bytes compressed exceed the payload limit of "
                + PeakProtocol.MAX_PAYLOAD_BYTES + " bytes");
        return false;
    }

    private static void sendAll(List<ServerPlayer> players, CustomPacketPayload payload) {
        for (ServerPlayer player : players) PeakClientNetworking.sendToClient(player, payload);
    }
}
