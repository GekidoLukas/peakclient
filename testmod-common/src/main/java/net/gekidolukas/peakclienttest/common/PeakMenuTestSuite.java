package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclienttest.common.menus.BasicMenuTest;
import net.gekidolukas.peakclienttest.common.menus.ColorPickerMenuTest;
import net.gekidolukas.peakclienttest.common.menus.FormsMenuTest;
import net.gekidolukas.peakclienttest.common.menus.CacheMenuTest;
import net.gekidolukas.peakclienttest.common.menus.InputMenuTest;
import net.gekidolukas.peakclienttest.common.menus.ItemsMenuTest;
import net.gekidolukas.peakclienttest.common.menus.ScrollMenuTest;
import net.gekidolukas.peakclienttest.common.menus.SectionsMenuTest;
import net.gekidolukas.peakclienttest.common.menus.ServerMenuTest;
import net.gekidolukas.peakclienttest.common.menus.StackMenuTest;
import net.gekidolukas.peakclienttest.common.menus.TemplateMenuTest;
import net.gekidolukas.peakclienttest.common.menus.ThemeMenuTest;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PeakMenuTestSuite {
    private static final Map<String, PeakMenuTest> TESTS = new LinkedHashMap<>();
    private static boolean initialized;

    static {
        register(new BasicMenuTest());
        register(new StackMenuTest());
        register(new CacheMenuTest());
        register(new ServerMenuTest());
        register(new ScrollMenuTest());
        register(new InputMenuTest());
        register(new TemplateMenuTest());
        register(new ThemeMenuTest());
        register(new ColorPickerMenuTest());
        register(new FormsMenuTest());
        register(new ItemsMenuTest());
        register(new SectionsMenuTest());
    }

    private PeakMenuTestSuite() {}

    private static void register(PeakMenuTest test) {
        TESTS.put(test.name(), test);
    }

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        PeakMenuTestRunner.registerListeners();
        for (PeakMenuTest test : TESTS.values()) test.registerHandlers();
    }

    public static boolean runSingle(String name, ServerPlayer player, int step) {
        PeakMenuTest test = TESTS.get(name);
        if (test == null) return false;
        PeakMenuTestRunner.startSingle(player, test, step);
        return true;
    }

    public static void runAll(ServerPlayer player) {
        PeakMenuTestRunner.startAll(player, new ArrayList<>(TESTS.values()));
    }

    public static void cancel(UUID playerId) {
        PeakMenuTestRunner.cancel(playerId);
    }

    public static Set<String> availableTests() {
        return TESTS.keySet();
    }

    static void resetPlayer(ServerPlayer player) {
        for (PeakMenuTest test : TESTS.values()) {
            try {
                test.cleanup(player);
            } catch (Exception ignored) {
            }
        }
        PeakTestTimeline.cancelFor(player.getUUID());
        PeakClient.to(player).menus().clear();
        PeakMenuTestKit.clearHud(player);
    }
}
