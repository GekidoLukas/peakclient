package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.PeakServerThread;
import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakScheduler;
import net.gekidolukas.peakclient.server.PeakValueKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PeakTaskRegistryTest {
    private static final PeakValueKey<Integer> KEY = PeakValueKey.ofInt("test:value");

    @AfterEach
    void cleanUp() {
        PeakServerThread.reset();
        PeakTaskRegistry.clear();
    }

    private static final class ScriptedTask implements PeakServerTask {
        private final boolean[] failures;
        int ticks;
        boolean stopped;

        ScriptedTask(boolean... failures) {
            this.failures = failures;
        }

        @Override
        public void tick() {
            int index = ticks++;
            if (index < failures.length ? failures[index] : false) throw new IllegalStateException("scripted failure");
        }

        @Override
        public void forceStopInternal() {
            stopped = true;
        }
    }

    private static boolean[] repeat(boolean value, int count) {
        boolean[] result = new boolean[count];
        java.util.Arrays.fill(result, value);
        return result;
    }

    @Test
    void aFailingTaskDoesNotStopTheOthersAndIsRemovedAfterThreeFailures() {
        ScriptedTask failing = new ScriptedTask(repeat(true, 10));
        ScriptedTask healthy = new ScriptedTask();
        PeakTaskRegistry.register(failing);
        PeakTaskRegistry.register(healthy);

        for (int i = 0; i < 6; i++) PeakTaskRegistry.tickTasks();

        assertEquals(6, healthy.ticks);
        assertEquals(PeakTaskRegistry.MAX_CONSECUTIVE_FAILURES, failing.ticks);
        assertTrue(failing.stopped);
        assertFalse(healthy.stopped);
    }

    @Test
    void aSuccessfulTickResetsTheFailureCount() {
        ScriptedTask flaky = new ScriptedTask(true, true, false, true, true, false);
        PeakTaskRegistry.register(flaky);

        for (int i = 0; i < 6; i++) PeakTaskRegistry.tickTasks();

        assertEquals(6, flaky.ticks);
        assertFalse(flaky.stopped);
    }

    @Test
    void clearStopsEveryTask() {
        ScriptedTask task = new ScriptedTask();
        PeakTaskRegistry.register(task);

        PeakTaskRegistry.clear();
        PeakTaskRegistry.tickTasks();

        assertTrue(task.stopped);
        assertEquals(0, task.ticks);
    }

    @Test
    void clearCancelsScheduledHandles() {
        int[] runs = {0};
        PeakScheduler.Handle handle = PeakScheduler.every(1, () -> runs[0]++);

        PeakTaskRegistry.tickTasks();
        PeakTaskRegistry.clear();
        PeakTaskRegistry.tickTasks();

        assertEquals(1, runs[0]);
        assertTrue(handle.isCancelled());
        assertFalse(handle.isRunning());
    }

    @Test
    void factoriesReturnRunningTasksThatCanBeStopped() {
        PeakCustomValueWatcher<Integer> watcher = PeakCustomValueWatcher.watch(KEY, PeakClient.to(List.<UUID>of()), () -> 1);

        assertTrue(watcher.isRunning());
        watcher.stop();
        assertFalse(watcher.isRunning());
    }

    @Test
    void startAndStopFromAnotherThreadKeepTheirOrder() {
        Queue<Runnable> queue = new ArrayDeque<>();
        boolean[] onServerThread = {false};
        PeakServerThread.install(() -> onServerThread[0], queue::add);

        PeakCustomValueWatcher<Integer> watcher = PeakCustomValueWatcher.watch(KEY, PeakClient.to(List.<UUID>of()), () -> 1);
        assertFalse(watcher.isRunning());
        watcher.stop();

        onServerThread[0] = true;
        while (!queue.isEmpty()) queue.poll().run();

        assertFalse(watcher.isRunning());
    }

    @Test
    void aTaskWithoutTargetsStopsItselfUnlessKeptAlive() {
        PeakCustomValueWatcher<Integer> ending = PeakCustomValueWatcher.watch(KEY, PeakClient.to(List.<UUID>of()), () -> 1);
        PeakCustomValueWatcher<Integer> kept = PeakCustomValueWatcher.watch(KEY, PeakClient.to(List.<UUID>of()), () -> 1);
        kept.keepAlive(true);

        for (int i = 0; i < 40; i++) PeakTaskRegistry.tickTasks();

        assertFalse(ending.isRunning());
        assertTrue(kept.isRunning());
    }
}
