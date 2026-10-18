package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakTargets;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

public final class PeakTimerSync extends PeakTargetedTask {
    private final String documentId;
    private final String timerId;
    private final LongSupplier sharedValueSupplier;
    private final BooleanSupplier sharedRunningSupplier;
    private final ToLongFunction<UUID> perPlayerValueSupplier;
    private final Predicate<UUID> perPlayerRunningSupplier;

    private long driftToleranceTicks = 5;

    private final Map<UUID, SyncedState> synced = new HashMap<>();

    private record SyncedState(long value, boolean running) {}

    private PeakTimerSync(String documentId, String timerId, Collection<UUID> targetPlayerIds,
                          LongSupplier sharedValueSupplier, BooleanSupplier sharedRunningSupplier,
                          ToLongFunction<UUID> perPlayerValueSupplier, Predicate<UUID> perPlayerRunningSupplier) {
        super(targetPlayerIds, 20);
        this.documentId = documentId;
        this.timerId = timerId;
        this.sharedValueSupplier = sharedValueSupplier;
        this.sharedRunningSupplier = sharedRunningSupplier;
        this.perPlayerValueSupplier = perPlayerValueSupplier;
        this.perPlayerRunningSupplier = perPlayerRunningSupplier;
    }

    public static PeakTimerSync create(String documentId, String timerId, PeakTargets targets,
                                       LongSupplier valueTicksSupplier, BooleanSupplier runningSupplier) {
        PeakTimerSync sync = new PeakTimerSync(documentId, timerId, targets.ids(), valueTicksSupplier, runningSupplier, null, null);
        sync.startTask();
        return sync;
    }

    public static PeakTimerSync createPerPlayer(String documentId, String timerId, PeakTargets targets,
                                                ToLongFunction<UUID> valueTicksSupplier, Predicate<UUID> runningSupplier) {
        PeakTimerSync sync = new PeakTimerSync(documentId, timerId, targets.ids(), null, null, valueTicksSupplier, runningSupplier);
        sync.startTask();
        return sync;
    }

    public PeakTimerSync intervalTicks(int ticks) { this.intervalTicks = Math.max(1, ticks); return this; }
    public PeakTimerSync driftToleranceTicks(long ticks) { this.driftToleranceTicks = Math.max(0, ticks); return this; }

    @Override
    protected void onTargetRemoved(UUID playerId) {
        synced.remove(playerId);
    }

    @Override
    protected void onInterval() {
        if (sharedValueSupplier != null) {
            syncShared();
        } else {
            syncPerPlayer();
        }
    }

    private void syncShared() {
        long value = sharedValueSupplier.getAsLong();
        boolean running = sharedRunningSupplier.getAsBoolean();
        List<UUID> recipients = new ArrayList<>();
        for (UUID id : targets()) {
            if (needsSync(synced.get(id), value, running)) recipients.add(id);
        }
        if (recipients.isEmpty()) return;
        SyncedState state = new SyncedState(value, running);
        for (UUID id : recipients) synced.put(id, state);
        PeakClientApi.get().sendUiUpdateToClient(documentId,
                List.of(new PeakUiOperation.Update(timerId, PeakPatch.timer().value(value).running(running).build())),
                recipients);
    }

    private void syncPerPlayer() {
        for (UUID id : targets()) {
            long value = perPlayerValueSupplier.applyAsLong(id);
            boolean running = perPlayerRunningSupplier.test(id);
            if (!needsSync(synced.get(id), value, running)) continue;
            synced.put(id, new SyncedState(value, running));
            PeakClientApi.get().sendUiUpdateToClient(documentId,
                    List.of(new PeakUiOperation.Update(timerId, PeakPatch.timer().value(value).running(running).build())),
                    List.of(id));
        }
    }

    private boolean needsSync(SyncedState last, long value, boolean running) {
        if (last == null || last.running() != running) return true;
        return Math.abs(value - last.value()) > driftToleranceTicks;
    }
}
