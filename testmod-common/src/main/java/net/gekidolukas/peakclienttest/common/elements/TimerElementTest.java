package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;

import java.util.ArrayList;
import java.util.List;

public final class TimerElementTest implements PeakElementTest {
    @Override public String name() { return "timer"; }
    @Override public String description() { return "PeakTimer: every format, count up and down, target, pause, prefix and suffix, color, shadow, gradient"; }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();
        for (PeakTimerFormat format : PeakTimerFormat.values()) {
            steps.add(PeakElementTestStep.document("format " + format.name(), 80, doc -> {
                doc.add(PeakLabel.create(format.name()).color(0xAAAAAA).absolute(PeakAnchor.TOP_CENTER, 0, 10));
                doc.add(PeakTimer.countDownFrom(200_000).format(format).color(0xFFFFFF).absolute(PeakAnchor.TOP_CENTER, 0, 26));
            }));
        }
        steps.add(PeakElementTestStep.document("mode: count up from 0 (top) and count down from 30 s (bottom)", 120, doc -> {
            doc.add(PeakTimer.countUpFrom(0).format(PeakTimerFormat.MM_SS_U).absolute(PeakAnchor.TOP_CENTER, 0, 10));
            doc.add(PeakTimer.countDownFrom(600).format(PeakTimerFormat.MM_SS_U).absolute(PeakAnchor.TOP_CENTER, 0, 26));
        }));
        steps.add(PeakElementTestStep.document("target: counts down from 15 s and stops at 5 s", 400, doc ->
                doc.add(PeakTimer.countDownFrom(300).target(100).format(PeakTimerFormat.MM_SS_U).absolute(PeakAnchor.TOP_CENTER, 0, 10))));
        steps.add(PeakElementTestStep.document("running=false: the value stays at 1:00", 100, doc ->
                doc.add(PeakTimer.countDownFrom(1200).running(false).format(PeakTimerFormat.MM_SS).absolute(PeakAnchor.TOP_CENTER, 0, 10))));
        steps.add(PeakElementTestStep.document("prefix and suffix", 80, doc ->
                doc.add(PeakTimer.countDownFrom(1200).prefix("Ends in ").suffix(" (hurry)").format(PeakTimerFormat.MM_SS)
                        .absolute(PeakAnchor.TOP_CENTER, 0, 10))));
        steps.add(PeakElementTestStep.document("color 0xFF5555 with shadow off (top) and a gradient (bottom)", 80, doc -> {
            doc.add(PeakTimer.countDownFrom(1200).color(0xFF5555).shadow(false).absolute(PeakAnchor.TOP_CENTER, 0, 10));
            doc.add(PeakTimer.countDownFrom(1200).color(PeakPaint.gradient(0xFFFF5555, 0xFF5555FF)).absolute(PeakAnchor.TOP_CENTER, 0, 26));
        }));
        steps.add(PeakElementTestStep.of("API: paused at 2 s, resumed at 4 s, set to 0:10 and running at 6 s", 240, (player, docId) -> {
            PeakTimer timer = PeakTimer.countUpFrom(0).format(PeakTimerFormat.MM_SS_U).absolute(PeakAnchor.TOP_CENTER, 0, 26);
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakLabel.create("pauseTimer at 2 s, resumeTimer at 4 s, syncTimer(10 s, running) at 6 s")
                    .color(0xAAAAAA).absolute(PeakAnchor.TOP_CENTER, 0, 10));
            doc.add(timer);
            PeakClient.to(player).sendUi(doc);
            PeakTestTimeline.create(player)
                    .atSeconds(2, () -> PeakClient.to(player).pauseTimer(docId, timer.getId()))
                    .atSeconds(4, () -> PeakClient.to(player).resumeTimer(docId, timer.getId()))
                    .atSeconds(6, () -> PeakClient.to(player).syncTimer(docId, timer.getId(), 200, true))
                    .start();
        }));
        return steps;
    }
}
