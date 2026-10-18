package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public final class PeakQuickTestRegistry {
    private enum Kind { HUD, MENU }

    private record Entry(Kind kind, BiFunction<ServerPlayer, PeakDocument, PeakDocument> builder) {}

    private static final Map<String, Entry> TESTS = new LinkedHashMap<>();

    private PeakQuickTestRegistry() {}

    public static void registerHud(String name, BiFunction<ServerPlayer, PeakDocument, PeakDocument> builder) {
        TESTS.put(name, new Entry(Kind.HUD, builder));
    }

    public static void registerMenu(String name, BiFunction<ServerPlayer, PeakDocument, PeakDocument> builder) {
        TESTS.put(name, new Entry(Kind.MENU, builder));
    }

    public static boolean run(String name, ServerPlayer player) {
        Entry entry = TESTS.get(name);
        if (entry == null) return false;

        boolean menu = entry.kind() == Kind.MENU;
        PeakDocument doc = menu
                ? PeakMenu.create(menuId(name), settings -> settings.title(name))
                : new PeakDocument(documentId(name));
        PeakDocument result;
        try {
            result = entry.builder().apply(player, doc);
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c[PeakTest] Quick test '" + name + "' threw an exception: " + e));
            return true;
        }

        if (result == null) {
            player.sendSystemMessage(Component.literal("§c[PeakTest] Quick test '" + name + "' returned null instead of the document - nothing sent."));
            return true;
        }

        if (menu) {
            if (!result.isMenu()) {
                player.sendSystemMessage(Component.literal("§c[PeakTest] Quick menu '" + name + "' returned a document that is not a menu - nothing sent."));
                return true;
            }
            PeakClient.to(player).menus().sendAndOpen(result);
            player.sendSystemMessage(Component.literal("§a[PeakTest] Cached and opened quick menu: " + name));
        } else {
            PeakClient.to(player).sendUi(result);
            player.sendSystemMessage(Component.literal("§a[PeakTest] Ran quick test: " + name));
        }
        return true;
    }

    public static void clear(String name, ServerPlayer player) {
        Entry entry = TESTS.get(name);
        if (entry != null && entry.kind() == Kind.MENU) {
            PeakClient.to(player).menus().remove(menuId(name));
        } else {
            PeakTestEnvironment.clearDocument(player, documentId(name));
        }
    }

    static void clearAllSilently(ServerPlayer player) {
        for (String name : TESTS.keySet()) clear(name, player);
    }

    public static Set<String> availableTests() { return TESTS.keySet(); }

    private static String documentId(String name) { return "peaktest:quick/" + name; }

    private static String menuId(String name) { return "peaktest:quick-menu/" + name; }
}
