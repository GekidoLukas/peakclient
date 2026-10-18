package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class ExampleHudElementTest implements PeakElementTest {
    @Override public String name() { return "examplehud"; }

    @Override
    public String description() {
        return "Docs example \"A static HUD\": a panel in the top right corner, sent once and removed again";
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("static HUD from the examples page: shown for 6 seconds, then removed with removeUi", 160,
                        (player, docId) -> {
                            List<ServerPlayer> players = List.of(player);

                            PeakDocument hud = PeakDocument.create(docId);
                            hud.add(PeakPanel.create("peaktest:example/panel")
                                    .absolute(PeakAnchor.TOP_RIGHT, -10, 10)
                                    .padding(6).background(PeakPaint.BLACK.withAlpha(0.6f)).border(1, PeakPaint.GRAY)
                                    .add(PeakLabel.create("peaktest:example/title", "§6Capture the Flag"),
                                         PeakLabel.create("peaktest:example/rules", "First to 3 captures wins")));

                            PeakClient.to(players).sendUi(hud);
                            PeakTestTimeline.create(player)
                                    .at(120, () -> PeakClient.to(players).removeUi(docId))
                                    .start();
                        })
        );
    }
}
