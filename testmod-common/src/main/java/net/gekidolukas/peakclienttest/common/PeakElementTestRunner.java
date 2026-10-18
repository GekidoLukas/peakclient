package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.helper.PeakServerTask;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class PeakElementTestRunner implements PeakServerTask {
    private static final Map<UUID, PeakElementTestRunner> ACTIVE = new ConcurrentHashMap<>();

    private final UUID targetPlayerId;
    private final List<PeakElementTest> testQueue;
    /** -1 = run every step normally; >= 0 = run ONLY this step and stay on it. */
    private final int isolatedStepIndex;
    private int testIndex = -1;
    private int stepIndex = -1;
    private int tickCounter = 0;
    private volatile boolean running = false;

    private PeakElementTestRunner(UUID targetPlayerId, List<PeakElementTest> testQueue, int isolatedStepIndex) {
        this.targetPlayerId = targetPlayerId;
        this.testQueue = testQueue;
        this.isolatedStepIndex = isolatedStepIndex;
    }

    static void startSingle(ServerPlayer player, PeakElementTest test, int step) {
        start(player, List.of(test), step);
    }

    static void startAll(ServerPlayer player, List<PeakElementTest> tests) {
        start(player, tests, -1);
    }

    private static void start(ServerPlayer player, List<PeakElementTest> tests, int step) {
        cancelIfRunning(player.getUUID());

        int resolvedStep = tests.size() == 1 ? step : -1;

        PeakElementTestRunner runner = new PeakElementTestRunner(player.getUUID(), tests, resolvedStep);
        ACTIVE.put(player.getUUID(), runner);
        runner.running = true;
        PeakTaskRegistry.register(runner);

        String suffix = resolvedStep >= 0 ? " (isolated at step " + (resolvedStep + 1) + ")" : "";
        player.sendSystemMessage(Component.literal("§7[PeakTest] Starting element test run (" + tests.size() + " element(s), " + totalSteps(tests) + " step(s))..." + suffix));
    }

    private static int totalSteps(List<PeakElementTest> tests) {
        int total = 0;
        for (PeakElementTest test : tests) total += test.steps().size();
        return total;
    }

    static void cancelIfRunning(UUID playerId) {
        PeakElementTestRunner existing = ACTIVE.remove(playerId);
        if (existing == null) return;
        existing.running = false;
        PeakTaskRegistry.unregister(existing);

        ServerPlayer player = PeakClientServer.resolvePlayer(playerId);
        if (player != null && existing.testIndex >= 0 && existing.testIndex < existing.testQueue.size()) {
            PeakTestEnvironment.clearDocument(player, existing.testQueue.get(existing.testIndex).documentId());
        }
    }

    @Override
    public UUID getTargetPlayerId() { return targetPlayerId; }

    @Override
    public void tick() {
        if (!running) return;

        ServerPlayer player = PeakClientServer.resolvePlayer(targetPlayerId);
        if (player == null) { stop(); return; }

        if (testIndex == -1) { advanceTest(player); return; }

        PeakElementTest currentTest = testQueue.get(testIndex);
        List<PeakElementTestStep> steps = currentTest.steps();

        if (stepIndex == -1) { advanceStep(player, currentTest, steps); return; }

        tickCounter++;
        if (tickCounter < steps.get(stepIndex).durationTicks()) return;

        PeakTestEnvironment.clearDocument(player, currentTest.documentId());
        PeakTestEnvironment.clearGlobalState(player);
        advanceStep(player, currentTest, steps);
    }

    private void advanceStep(ServerPlayer player, PeakElementTest test, List<PeakElementTestStep> steps) {
        int nextIndex = isolatedStepIndex >= 0 ? isolatedStepIndex : stepIndex + 1;

        if (isolatedStepIndex >= 0 && (nextIndex < 0 || nextIndex >= steps.size())) {
            nextIndex = 0;
        }

        if (isolatedStepIndex < 0 && nextIndex >= steps.size()) {
            stepIndex = -1;
            advanceTest(player);
            return;
        }

        stepIndex = nextIndex;
        tickCounter = 0;
        PeakElementTestStep step = steps.get(stepIndex);
        player.sendSystemMessage(Component.literal(
                "§b[PeakTest] [" + test.name() + "] (" + (stepIndex + 1) + "/" + steps.size() + ") " + step.label()));
        try {
            step.action().accept(player, test.documentId());
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c[PeakTest] FAILED: " + test.name() + "/" + step.label() + " - " + e));
        }

        if (isolatedStepIndex >= 0) {
            running = false;
            ACTIVE.remove(targetPlayerId);
            PeakTaskRegistry.unregister(this);
            player.sendSystemMessage(Component.literal("§7[PeakTest] Step stays visible - use /peaktest clear-all when done."));
        }
    }

    private void advanceTest(ServerPlayer player) {
        testIndex++;
        if (testIndex >= testQueue.size()) {
            player.sendSystemMessage(Component.literal("§7[PeakTest] Element test run finished."));
            stop();
            return;
        }

        PeakElementTest test = testQueue.get(testIndex);
        player.sendSystemMessage(Component.literal("§e[PeakTest] Testing element: " + test.name() + "§7 - " + test.description()));
        stepIndex = -1;
        advanceStep(player, test, test.steps());
    }

    private void stop() {
        running = false;
        ACTIVE.remove(targetPlayerId);
        PeakTaskRegistry.unregister(this);
    }

    @Override
    public void forceStopInternal() { running = false; }
}