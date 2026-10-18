package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.helper.PeakServerTask;
import net.gekidolukas.peakclient.helper.PeakTask;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;

public final class PeakScheduler {
    public interface Handle extends PeakTask {
        void cancel();
        boolean isCancelled();

        @Override
        default void stop() { cancel(); }

        @Override
        default boolean isRunning() { return !isCancelled(); }
    }

    private PeakScheduler() {}

    public static Handle after(int ticks, Runnable action) {
        return schedule(Math.max(1, ticks), 0, action);
    }

    public static Handle every(int ticks, Runnable action) {
        int period = Math.max(1, ticks);
        return schedule(period, period, action);
    }

    private static Handle schedule(int firstDelay, int period, Runnable action) {
        ScheduledTask task = new ScheduledTask(firstDelay, period, action);
        PeakTaskRegistry.register(task);
        return task;
    }

    private static final class ScheduledTask implements PeakServerTask, Handle {
        private int remaining;
        private final int period;
        private final Runnable action;
        private volatile boolean cancelled = false;

        ScheduledTask(int remaining, int period, Runnable action) {
            this.remaining = remaining;
            this.period = period;
            this.action = action;
        }

        @Override
        public void tick() {
            if (cancelled) return;
            if (--remaining > 0) return;
            try {
                action.run();
            } catch (Exception e) {
                PeakLog.error("A scheduled task failed", e);
            }
            if (period > 0) {
                remaining = period;
            } else {
                cancel();
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            PeakTaskRegistry.unregister(this);
        }

        @Override
        public boolean isCancelled() { return cancelled; }

        @Override
        public void forceStopInternal() { cancelled = true; }
    }
}
