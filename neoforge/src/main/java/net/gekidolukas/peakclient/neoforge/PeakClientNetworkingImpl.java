package net.gekidolukas.peakclient.neoforge;

import net.gekidolukas.peakclient.network.PeakUiDocumentPayload;
import net.gekidolukas.peakclient.neoforge.client.PeakClientConnection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PeakClientNetworkingImpl {

    private PeakClientNetworkingImpl() {}

    public static void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static boolean canSend(ServerPlayer player) {
        return player.connection.hasChannel(PeakUiDocumentPayload.TYPE);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static boolean canSendToServer(CustomPacketPayload.Type<?> type) {
        return PeakClientConnection.hasChannel(type);
    }
}
