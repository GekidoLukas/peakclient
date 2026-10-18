package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakRemotePages;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;
import net.gekidolukas.peakclient.server.PeakAudiences;
import net.gekidolukas.peakclient.server.PeakMenuSessions;
import net.gekidolukas.peakclient.server.PeakPlayerStates;
import net.gekidolukas.peakclient.server.PeakRecipients;
import net.gekidolukas.peakclient.server.PeakSentDocuments;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class PeakClientServer {
    private static volatile MinecraftServer INSTANCE;

    private PeakClientServer() {}

    public static void set(MinecraftServer server) {
        INSTANCE = server;
    }

    public static void clear() {
        INSTANCE = null;
        PeakPlayerStates.clearAll();
        PeakAudiences.clear();
        PeakTaskRegistry.clear();
        PeakMenuSessions.clearAll();
        PeakRemotePages.clearAll();
        PeakSentDocuments.clearAll();
        PeakRecipients.clearAll();
    }

    public static MinecraftServer server() {
        return INSTANCE;
    }

    public static long currentTick() {
        MinecraftServer server = INSTANCE;
        return server == null ? 0L : server.getTickCount();
    }

    public static ServerPlayer resolvePlayer(UUID playerId) {
        MinecraftServer server = INSTANCE;
        if (server == null) return null;
        return server.getPlayerList().getPlayer(playerId);
    }
}
