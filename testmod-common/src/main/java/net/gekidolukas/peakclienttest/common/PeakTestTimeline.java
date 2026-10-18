package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.helper.PeakServerTask;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakTestTimeline implements PeakServerTask {
    private static final Map<UUID, PeakTestTimeline> ACTIVE = new ConcurrentHashMap<>();

    private record Entry(int tick, Runnable action) {}

    private final ServerPlayer player;
    private final List<Entry> entries = new ArrayList<>();
    private final List<Runnable> cancelActions = new ArrayList<>();
    private int elapsed = 0;
    private int lastTick = 0;

    private PeakTestTimeline(ServerPlayer player) {
        this.player = player;
    }

    public static PeakTestTimeline create(ServerPlayer player) {
        cancelFor(player.getUUID());
        PeakTestTimeline timeline = new PeakTestTimeline(player);
        ACTIVE.put(player.getUUID(), timeline);
        return timeline;
    }

    public PeakTestTimeline at(int tick, Runnable action) {
        entries.add(new Entry(tick, action));
        lastTick = Math.max(lastTick, tick);
        return this;
    }

    public PeakTestTimeline onCancel(Runnable action) {
        cancelActions.add(action);
        return this;
    }

    public PeakTestTimeline atSeconds(double seconds, Runnable action) {
        return at((int) Math.round(seconds * 20), action);
    }

    public void start() {
        PeakTaskRegistry.register(this);
    }

    static void cancelFor(UUID playerId) {
        PeakTestTimeline existing = ACTIVE.remove(playerId);
        if (existing == null) return;
        PeakTaskRegistry.unregister(existing);
        for (Runnable action : existing.cancelActions) {
            try {
                action.run();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public UUID getTargetPlayerId() { return player.getUUID(); }

    @Override
    public void tick() {
        elapsed++;
        for (Entry entry : entries) {
            if (entry.tick() != elapsed) continue;
            try {
                entry.action().run();
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§c[PeakTest] Timeline step failed at tick " + elapsed + ": " + e));
            }
        }
        if (elapsed >= lastTick) cancelFor(player.getUUID());
    }

    @Override
    public void forceStopInternal() {
        ACTIVE.remove(player.getUUID(), this);
    }
}
