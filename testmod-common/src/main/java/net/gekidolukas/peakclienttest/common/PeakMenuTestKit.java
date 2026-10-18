package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PeakMenuTestKit {
    public static final String HUD_ID = "peaktest:menu-test/hud";

    private PeakMenuTestKit() {}

    public static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§d[menu] §f" + text));
    }

    public static final String VERDICT_OK = "peaktest-ok";
    public static final String VERDICT_FAIL = "peaktest-fail";
    public static final String VERDICT_SKIP = "peaktest-skip";

    public static PeakPanel frame(String title, PeakElement<?>... children) {
        PeakPanel panel = PeakPanel.create()
                .absolute(PeakAnchor.CENTER, 0, 0)
                .column().gap(6).padding(10).width(200).borderRadius(5)
                .background(0xFF202030).border(1, 0xFF888888);
        panel.add(PeakLabel.create(title).color(0xFFD700));
        for (PeakElement<?> child : children) panel.add(child);
        panel.add(verdictRow());
        return panel;
    }

    private static PeakPanel verdictRow() {
        PeakPanel row = PeakPanel.create("peaktest:verdict").row().gap(4).padding(0).background(0);
        row.add(PeakLabel.create("peaktest:verdict/label", "Test:").color(0x888888));
        row.add(PeakButton.text("peaktest:verdict/ok", "OK").onClick(PeakAction.send(VERDICT_OK))
                .tooltip("Confirms the manual check of this step"));
        row.add(PeakButton.text("peaktest:verdict/fail", "Fail").onClick(PeakAction.send(VERDICT_FAIL))
                .tooltip("Marks this step as failed"));
        row.add(PeakButton.text("peaktest:verdict/skip", "Skip").onClick(PeakAction.send(VERDICT_SKIP))
                .tooltip("Skips this step"));
        return row;
    }

    public static PeakButton button(String id, String text, PeakAction... actions) {
        return PeakButton.text(id, text).width(180).onClick(actions);
    }

    public static void showHudLabel(ServerPlayer player, String text) {
        PeakDocument hud = PeakDocument.create(HUD_ID);
        hud.add(PeakLabel.create(HUD_ID + "/label", text).color(0xFFFF55).absolute(PeakAnchor.TOP_CENTER, 0, 10));
        PeakClient.to(player).sendUi(hud);
    }

    public static void clearHud(ServerPlayer player) {
        PeakClient.to(player).removeUi(HUD_ID);
    }
}
