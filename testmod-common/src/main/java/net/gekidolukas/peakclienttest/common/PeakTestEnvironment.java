package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakTargets;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.minecraft.server.level.ServerPlayer;

public class PeakTestEnvironment {
    private PeakTestEnvironment() {}

    static void clearDocument(ServerPlayer player, String documentId) {
        if (documentId == null) return;
        PeakClient.to(player).sendUi(new PeakDocument(documentId).deleteIfEmpty(true));
    }

    static void clearGlobalState(ServerPlayer player) {
        PeakTestTimeline.cancelFor(player.getUUID());
        PeakTargets self = PeakClient.to(player);
        self.clearWaypoints();
        self.clearCustomWorldBorder();
        self.clearValues("peaktest");
        self.vanillaHud().reset();
        PeakMenuTestSuite.resetPlayer(player);
        PeakClient.forgetPlayer(player.getUUID());
    }
}
