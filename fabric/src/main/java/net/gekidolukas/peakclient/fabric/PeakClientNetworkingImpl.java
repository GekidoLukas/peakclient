package net.gekidolukas.peakclient.fabric;


import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.gekidolukas.peakclient.network.PeakUiDocumentPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class PeakClientNetworkingImpl {

    private PeakClientNetworkingImpl() {}

    public static void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static boolean canSend(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, PeakUiDocumentPayload.TYPE);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static boolean canSendToServer(CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type);
    }
}
