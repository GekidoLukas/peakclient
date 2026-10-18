package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.PeakServerThread;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class PeakTargetedTask implements PeakServerTask, PeakTask {
    private final Set<UUID> targets = new CopyOnWriteArraySet<>();
    private int tickCounter = 0;
    protected int intervalTicks;
    protected volatile boolean running = false;
    private volatile boolean keepAlive = false;

    protected PeakTargetedTask(Collection<UUID> initialTargets, int defaultIntervalTicks) {
        this.targets.addAll(initialTargets);
        this.intervalTicks = defaultIntervalTicks;
    }

    public final void addTarget(UUID playerId) {
        if (targets.add(playerId)) {
            PeakTaskRegistry.targetAdded(this, playerId);
        }
    }

    public final void addTargets(Collection<UUID> playerIds) {
        for (UUID id : playerIds) addTarget(id);
    }

    @Override
    public final boolean removeTarget(UUID playerId) {
        if (!PeakServerThread.isServerThread()) {
            PeakServerThread.run(() -> removeTarget(playerId));
            return false;
        }
        if (targets.remove(playerId)) {
            onTargetRemoved(playerId);
            PeakTaskRegistry.targetRemoved(this, playerId);
        }
        return targets.isEmpty() && !keepAlive;
    }

    public final Collection<UUID> targets() { return List.copyOf(targets); }

    public final boolean hasTargets() { return !targets.isEmpty(); }

    public final PeakTargetedTask keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    @Override
    public final Collection<UUID> getTargetPlayerIds() { return targets(); }

    protected void onTargetRemoved(UUID playerId) {}

    protected abstract void onInterval();

    @Override
    public final void tick() {
        if (!running) return;
        tickCounter++;
        if (tickCounter < intervalTicks) return;
        tickCounter = 0;
        if (targets.isEmpty()) {
            if (!keepAlive) stop();
            return;
        }
        onInterval();
    }

    protected final void startTask() {
        PeakServerThread.run(() -> {
            if (running) return;
            running = true;
            tickCounter = intervalTicks;
            PeakTaskRegistry.register(this);
        });
    }

    @Override
    public final void stop() {
        PeakServerThread.run(() -> {
            running = false;
            PeakTaskRegistry.unregister(this);
        });
    }

    @Override
    public void forceStopInternal() { running = false; }

    @Override
    public final boolean isRunning() { return running; }
}
