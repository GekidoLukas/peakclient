package net.gekidolukas.peakclienttest.common;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class PeakMenuTestContext {
    private final ServerPlayer player;
    private final Map<String, Integer> counters = new HashMap<>();

    PeakMenuTestContext(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }

    public int increment(String key) {
        return counters.merge(key, 1, Integer::sum);
    }

    public int counter(String key) {
        return counters.getOrDefault(key, 0);
    }
}
