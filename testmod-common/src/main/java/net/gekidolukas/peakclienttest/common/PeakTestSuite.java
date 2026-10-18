package net.gekidolukas.peakclienttest.common;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PeakTestSuite {
    private PeakTestSuite() {}

    public static void clearAll(ServerPlayer player) {
        PeakElementTestRunner.cancelIfRunning(player.getUUID());
        PeakMenuTestSuite.cancel(player.getUUID());
        clearAllSilently(player);
        player.sendSystemMessage(Component.literal("§7[PeakTest] Cleared all test documents, waypoints, and the custom world border."));
    }

    static void clearAllSilently(ServerPlayer player) {
        for (PeakElementTest elementTest : PeakElementTestSuite.allTests()) {
            PeakTestEnvironment.clearDocument(player, elementTest.documentId());
        }
        PeakQuickTestRegistry.clearAllSilently(player);
        PeakTestEnvironment.clearGlobalState(player);
    }
}
