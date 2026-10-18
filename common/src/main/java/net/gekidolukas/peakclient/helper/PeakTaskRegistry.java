package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.server.PeakAudiences;
import net.gekidolukas.peakclient.server.PeakPlayerStates;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeakTaskRegistry {
    static final int MAX_CONSECUTIVE_FAILURES = 3;

    private static final List<PeakServerTask> ACTIVE = new CopyOnWriteArrayList<>();
    private static final Map<UUID, List<PeakServerTask>> BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<PeakServerTask, Integer> FAILURES = new ConcurrentHashMap<>();

    private PeakTaskRegistry() {}

    public static void register(PeakServerTask task) {
        ACTIVE.add(task);
        for (UUID id : task.getTargetPlayerIds()) {
            BY_PLAYER.computeIfAbsent(id, key -> new CopyOnWriteArrayList<>()).add(task);
        }
    }

    public static void unregister(PeakServerTask task) {
        ACTIVE.remove(task);
        FAILURES.remove(task);
        for (List<PeakServerTask> list : BY_PLAYER.values()) list.remove(task);
    }

    static void targetAdded(PeakServerTask task, UUID playerId) {
        if (!ACTIVE.contains(task)) return;
        List<PeakServerTask> list = BY_PLAYER.computeIfAbsent(playerId, key -> new CopyOnWriteArrayList<>());
        if (!list.contains(task)) list.add(task);
    }

    static void targetRemoved(PeakServerTask task, UUID playerId) {
        List<PeakServerTask> list = BY_PLAYER.get(playerId);
        if (list != null) list.remove(task);
    }

    public static void stopAllForPlayer(UUID playerId) {
        List<PeakServerTask> list = BY_PLAYER.remove(playerId);
        if (list == null) return;
        for (PeakServerTask task : list) {
            if (task.removeTarget(playerId)) {
                ACTIVE.remove(task);
                FAILURES.remove(task);
                task.forceStopInternal();
            }
        }
    }

    public static void clear() {
        for (PeakServerTask task : ACTIVE) {
            try {
                task.forceStopInternal();
            } catch (Exception e) {
                PeakLog.error("Failed to stop task " + task.getClass().getName(), e);
            }
        }
        ACTIVE.clear();
        BY_PLAYER.clear();
        FAILURES.clear();
    }

    static void tickTasks() {
        for (PeakServerTask task : ACTIVE) {
            try {
                task.tick();
                if (!FAILURES.isEmpty()) FAILURES.remove(task);
            } catch (Exception e) {
                onTaskFailure(task, e);
            }
        }
    }

    private static void onTaskFailure(PeakServerTask task, Exception error) {
        int failures = FAILURES.merge(task, 1, Integer::sum);
        String name = task.getClass().getName();
        if (failures < MAX_CONSECUTIVE_FAILURES) {
            PeakLog.error("Task " + name + " failed (" + failures + "/" + MAX_CONSECUTIVE_FAILURES + ")", error);
            return;
        }
        PeakLog.error("Task " + name + " was stopped after " + failures + " consecutive failures", error);
        unregister(task);
        try {
            task.forceStopInternal();
        } catch (Exception e) {
            PeakLog.error("Failed to stop task " + name, e);
        }
    }

    public static void tickAll() {
        long tick = PeakClientServer.currentTick();
        guarded("player state", () -> PeakPlayerStates.tick(tick));
        tickTasks();
        guarded("audience", () -> PeakAudiences.tick(tick));
    }

    private static void guarded(String what, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            PeakLog.error("The " + what + " tick failed", e);
        }
    }
}
