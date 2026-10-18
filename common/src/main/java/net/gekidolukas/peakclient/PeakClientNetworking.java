package net.gekidolukas.peakclient;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.gekidolukas.peakclient.network.PeakHelloPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class PeakClientNetworking {

    private PeakClientNetworking() {}

    @ExpectPlatform
    public static void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        throw new AssertionError("Not Implemented");
    }

    @ExpectPlatform
    public static boolean canSend(ServerPlayer player) {
        throw new AssertionError("Not Implemented");
    }

    @ExpectPlatform
    public static void sendToServer(CustomPacketPayload payload) {
        throw new AssertionError("Not Implemented");
    }

    @ExpectPlatform
    public static boolean canSendToServer(CustomPacketPayload.Type<?> type) {
        throw new AssertionError("Not Implemented");
    }

    public static void sendHello() {
        if (canSendToServer(PeakHelloPayload.TYPE)) {
            sendToServer(PeakHelloPayload.current());
        }
    }
}
