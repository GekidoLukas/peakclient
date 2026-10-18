package net.gekidolukas.peakclient.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class PeakClientConnection {
    private PeakClientConnection() {}

    public static boolean hasChannel(CustomPacketPayload.Type<?> type) {
        var connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.hasChannel(type);
    }
}
