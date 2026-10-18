package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.helper.PeakServerTask;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;
import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.server.PeakMenuNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class PeakMenuTestRunner implements PeakServerTask {
    private enum Phase { RESETTING, WAITING }

    private static final Map<UUID, PeakMenuTestRunner> ACTIVE = new ConcurrentHashMap<>();
    private static final int MIN_RESET_TICKS = 6;
    private static final int MAX_RESET_TICKS = 80;

    private final UUID playerId;
    private final List<PeakMenuTest> tests;
    private final int isolatedStep;
    private int testIndex = -1;
    private int stepIndex = -1;
    private int checkIndex;
    private int ticks;
    private int phaseTicks;
    private Phase phase = Phase.RESETTING;
    private PeakMenuTestContext context;
    private int passed;
    private int failed;
    private int skipped;
    private int totalPassed;
    private int totalFailed;
    private int totalSkipped;
    private volatile boolean running;

    private PeakMenuTestRunner(UUID playerId, List<PeakMenuTest> tests, int isolatedStep) {
        this.playerId = playerId;
        this.tests = tests;
        this.isolatedStep = isolatedStep;
    }

    static void registerListeners() {
        PeakClient.onMenuAction(event -> {
            PeakMenuTestRunner runner = ACTIVE.get(event.player().getUUID());
            if (runner == null) return;
            String actionId = event.actionId();
            if (actionId.equals(PeakMenuTestKit.VERDICT_OK)) {
                if (!confirm(event.player(), true)) say(event.player(), "§7  nothing to confirm in this step");
            } else if (actionId.equals(PeakMenuTestKit.VERDICT_FAIL)) {
                if (!confirm(event.player(), false)) say(event.player(), "§7  no step is running");
            } else if (actionId.equals(PeakMenuTestKit.VERDICT_SKIP)) {
                skip(event.player());
            } else {
                runner.process(event.player(), event);
            }
        });
        PeakClient.onMenuNavigation(event -> {
            PeakMenuTestRunner runner = ACTIVE.get(event.player().getUUID());
            if (runner == null) return;
            say(event.player(), "§7  navigation: current=" + event.menuId() + " history=" + event.history() + " reason=" + event.reason());
            runner.process(event.player(), event);
        });
    }

    static void startSingle(ServerPlayer player, PeakMenuTest test, int step) {
        start(player, List.of(test), step);
    }

    static void startAll(ServerPlayer player, List<PeakMenuTest> tests) {
        start(player, tests, -1);
    }

    private static void start(ServerPlayer player, List<PeakMenuTest> tests, int step) {
        cancel(player.getUUID());
        PeakMenuTestRunner runner = new PeakMenuTestRunner(player.getUUID(), tests, tests.size() == 1 ? step : -1);
        runner.running = true;
        ACTIVE.put(player.getUUID(), runner);
        PeakTaskRegistry.register(runner);
        say(player, "§7Starting menu test run (" + tests.size() + " test(s)). Confirm visual checks with the OK button at the bottom of "
                + "every test menu, report problems with Fail, skip a step with Skip.");
    }

    static void cancel(UUID playerId) {
        PeakMenuTestRunner existing = ACTIVE.remove(playerId);
        if (existing == null) return;
        existing.running = false;
        PeakTaskRegistry.unregister(existing);
        ServerPlayer player = PeakClientServer.resolvePlayer(playerId);
        if (player != null) PeakMenuTestSuite.resetPlayer(player);
    }

    static boolean confirm(ServerPlayer player, boolean ok) {
        PeakMenuTestRunner runner = ACTIVE.get(player.getUUID());
        if (runner == null || runner.phase != Phase.WAITING) return false;
        PeakMenuTestStep step = runner.currentStep();
        if (runner.checkIndex >= step.checks().size()) return false;
        if (!ok) {
            runner.failStep(player, "reported as failed by you");
            return true;
        }
        PeakMenuCheck check = step.checks().get(runner.checkIndex);
        if (!check.manual()) return false;
        runner.checkPassed(player, check);
        return true;
    }

    static boolean skip(ServerPlayer player) {
        PeakMenuTestRunner runner = ACTIVE.get(player.getUUID());
        if (runner == null || runner.testIndex < 0 || runner.testIndex >= runner.tests.size()) return false;
        runner.finishStep(player, 0, "skipped");
        return true;
    }

    @Override
    public UUID getTargetPlayerId() {
        return playerId;
    }

    @Override
    public void tick() {
        if (!running) return;
        ServerPlayer player = PeakClientServer.resolvePlayer(playerId);
        if (player == null) {
            stop();
            return;
        }
        if (testIndex < 0) {
            nextTest(player);
            return;
        }
        if (testIndex >= tests.size()) return;

        phaseTicks++;
        if (phase == Phase.RESETTING) {
            boolean settled = phaseTicks >= MIN_RESET_TICKS && PeakClient.currentMenu(playerId) == null;
            if (settled || phaseTicks >= MAX_RESET_TICKS) runSetup(player);
            return;
        }

        ticks++;
        process(player, null);
        if (running && phase == Phase.WAITING && ticks >= currentStep().timeoutTicks()) {
            failStep(player, "timed out after " + (currentStep().timeoutTicks() / 20) + " seconds");
        }
    }

    private PeakMenuTestStep currentStep() {
        return tests.get(testIndex).steps().get(stepIndex);
    }

    private void process(ServerPlayer player, Object event) {
        if (!running || phase != Phase.WAITING || testIndex < 0 || testIndex >= tests.size()) return;
        PeakMenuTestStep step = currentStep();

        if (event != null) {
            for (var guard : step.guards()) {
                if (guard.test(event)) {
                    failStep(player, "something happened that must not happen: " + describe(event));
                    return;
                }
            }
        }
        if (checkIndex >= step.checks().size()) return;
        PeakMenuCheck check = step.checks().get(checkIndex);
        if (check.manual()) return;

        PeakMenuCheck.Outcome outcome;
        try {
            outcome = check.matcher().test(context, event);
        } catch (Exception e) {
            failStep(player, "check threw " + e);
            return;
        }
        switch (outcome) {
            case YES -> checkPassed(player, check);
            case FAIL -> failStep(player, "unexpected: " + describe(event));
            case NO -> { }
        }
    }

    private static String describe(Object event) {
        if (event instanceof PeakMenuActionEvent action) {
            return "action '" + action.actionId() + "' from '" + action.elementId() + "' args=" + action.args();
        }
        if (event instanceof PeakMenuNavigationEvent navigation) {
            return "navigation to " + navigation.menuId() + " history=" + navigation.history();
        }
        return String.valueOf(event);
    }

    private void checkPassed(ServerPlayer player, PeakMenuCheck check) {
        say(player, "§a  ✔ §7" + check.instruction());
        checkIndex++;
        PeakMenuTestStep step = currentStep();
        if (checkIndex >= step.checks().size()) {
            finishStep(player, 1, "passed");
        } else {
            announceCheck(player, step.checks().get(checkIndex));
        }
    }

    private void announceCheck(ServerPlayer player, PeakMenuCheck check) {
        if (check.manual()) {
            say(player, "§e  → " + check.instruction() + " §7(OK or Fail at the bottom of the menu)");
        } else {
            say(player, "§e  → " + check.instruction());
        }
    }

    private void failStep(ServerPlayer player, String reason) {
        say(player, "§c  ✘ " + reason);
        finishStep(player, -1, "failed");
    }

    private void finishStep(ServerPlayer player, int result, String word) {
        PeakMenuTest test = tests.get(testIndex);
        PeakMenuTestStep step = currentStep();
        if (result > 0) passed++;
        else if (result < 0) failed++;
        else skipped++;
        say(player, (result > 0 ? "§a" : result < 0 ? "§c" : "§e") + "[" + test.name() + "] " + step.label() + ": " + word);

        if (isolatedStep >= 0) {
            finishTest(player, false);
            finishRun(player);
            return;
        }
        stepIndex++;
        if (stepIndex >= test.steps().size()) {
            finishTest(player, true);
            nextTest(player);
        } else {
            beginStep(player);
        }
    }

    private void finishTest(ServerPlayer player, boolean cleanUp) {
        PeakMenuTest test = tests.get(testIndex);
        say(player, "§7[" + test.name() + "] §a" + passed + " passed§7, §c" + failed + " failed§7, §e" + skipped + " skipped");
        totalPassed += passed;
        totalFailed += failed;
        totalSkipped += skipped;
        passed = 0;
        failed = 0;
        skipped = 0;
        if (cleanUp) PeakMenuTestSuite.resetPlayer(player);
    }

    private void nextTest(ServerPlayer player) {
        testIndex++;
        if (testIndex >= tests.size()) {
            finishRun(player);
            return;
        }
        PeakMenuTest test = tests.get(testIndex);
        say(player, "§e== " + test.name() + " §7- " + test.description());
        stepIndex = isolatedStep >= 0 ? Math.min(isolatedStep, test.steps().size() - 1) : 0;
        if (test.steps().isEmpty()) {
            finishTest(player, false);
            nextTest(player);
            return;
        }
        beginStep(player);
    }

    private void beginStep(ServerPlayer player) {
        PeakMenuTestSuite.resetPlayer(player);
        PeakMenuTest test = tests.get(testIndex);
        PeakMenuTestStep step = currentStep();
        say(player, "§b[" + test.name() + "] (" + (stepIndex + 1) + "/" + test.steps().size() + ") " + step.label());
        phase = Phase.RESETTING;
        phaseTicks = 0;
        ticks = 0;
        checkIndex = 0;
    }

    private void runSetup(ServerPlayer player) {
        context = new PeakMenuTestContext(player);
        PeakMenuTestStep step = currentStep();
        try {
            step.setup().accept(player);
        } catch (Exception e) {
            failStep(player, "setup threw " + e);
            return;
        }
        phase = Phase.WAITING;
        ticks = 0;
        checkIndex = 0;
        if (step.checks().isEmpty()) {
            finishStep(player, 1, "passed");
            return;
        }
        announceCheck(player, step.checks().get(0));
    }

    private void finishRun(ServerPlayer player) {
        say(player, "§7Menu test run finished: §a" + totalPassed + " passed§7, §c" + totalFailed + " failed§7, §e"
                + totalSkipped + " skipped");
        stop();
    }

    private void stop() {
        running = false;
        ACTIVE.remove(playerId, this);
        PeakTaskRegistry.unregister(this);
    }

    @Override
    public void forceStopInternal() {
        running = false;
        ACTIVE.remove(playerId, this);
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§d[menu-test] §f" + text));
    }
}
