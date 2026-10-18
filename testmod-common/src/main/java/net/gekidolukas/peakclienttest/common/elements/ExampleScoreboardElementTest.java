package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ExampleScoreboardElementTest implements PeakElementTest {
    private static final PeakValueKey<Integer> KILLS = PeakValueKey.ofInt("peaktest:example/kills");

    @Override public String name() { return "examplescoreboard"; }

    @Override
    public String description() {
        return "Docs example \"A live scoreboard\": audience, persistent view, a patched score and a per-player value";
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("live scoreboard from the examples page: the score changes every 2 seconds, kills are bound per player", 360,
                        (player, docId) -> {
                            List<ServerPlayer> players = List.of(player);
                            AtomicInteger red = new AtomicInteger();
                            AtomicInteger blue = new AtomicInteger();
                            AtomicInteger kills = new AtomicInteger();

                            PeakDocument hud = PeakDocument.create(docId);
                            hud.add(PeakPanel.create("peaktest:example/panel")
                                    .absolute(PeakAnchor.TOP_RIGHT, -10, 10)
                                    .padding(6).background(PeakPaint.BLACK.withAlpha(0.6f))
                                    .column().gap(2)
                                    .add(PeakLabel.create("peaktest:example/score", "Red 0 : 0 Blue"),
                                         PeakLabel.create("peaktest:example/kills-label", "Kills: " + KILLS.placeholder("0"))));

                            PeakAudience audience = PeakClient.audience(players);
                            PeakView view = audience.open(hud).persistent(true);
                            audience.values().bindPerPlayer(KILLS, uuid -> kills.get(), 10);

                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            for (int round = 1; round <= 8; round++) {
                                int current = round;
                                timeline.at(current * 40, () -> {
                                    if (current % 2 == 1) red.incrementAndGet(); else blue.incrementAndGet();
                                    kills.addAndGet(2);
                                    view.update("peaktest:example/score",
                                            PeakPatch.label().text("Red " + red.get() + " : " + blue.get() + " Blue"));
                                });
                            }
                            timeline.at(340, () -> {});
                            timeline.onCancel(audience::close);
                            timeline.start();
                        })
        );
    }
}
