package net.gekidolukas.peakclient.helper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class PeakPlayers {
    private PeakPlayers() {}

    public static List<UUID> ids(Collection<? extends ServerPlayer> players) {
        List<UUID> ids = new ArrayList<>(players.size());
        for (ServerPlayer player : players) ids.add(player.getUUID());
        return ids;
    }

    public static List<UUID> of(ServerPlayer... players) {
        return ids(List.of(players));
    }

    public static List<UUID> all(MinecraftServer server) {
        return ids(server.getPlayerList().getPlayers());
    }
}
