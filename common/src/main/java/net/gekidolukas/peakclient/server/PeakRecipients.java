package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.PeakClientNetworking;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakRecipients {
    private static final Set<UUID> WARNED_WITHOUT_CLIENT = ConcurrentHashMap.newKeySet();

    private PeakRecipients() {}

    public static List<ServerPlayer> resolve(Collection<UUID> playerIds) {
        List<ServerPlayer> players = new ArrayList<>(playerIds.size());
        for (UUID id : playerIds) {
            ServerPlayer player = PeakClientServer.resolvePlayer(id);
            if (player == null) continue;
            if (PeakClientNetworking.canSend(player)) {
                players.add(player);
            } else if (WARNED_WITHOUT_CLIENT.add(id)) {
                PeakLog.warn("Player {} has no PeakClient channel (vanilla client or not connected yet), packets are skipped. "
                        + "Use PeakClient.onClientReady to send after the client has joined", player.getGameProfile().getName());
            }
        }
        return players;
    }

    public static void send(Collection<UUID> playerIds, CustomPacketPayload payload) {
        for (ServerPlayer player : resolve(playerIds)) {
            PeakClientNetworking.sendToClient(player, payload);
        }
    }

    public static void forget(UUID playerId) {
        WARNED_WITHOUT_CLIENT.remove(playerId);
    }

    public static void clearAll() {
        WARNED_WITHOUT_CLIENT.clear();
    }
}
