package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;

import java.util.ArrayList;
import java.util.List;

public final class ProgressBarElementTest implements PeakElementTest {
    @Override public String name() { return "progressbar"; }
    @Override public String description() { return "PeakProgressBar: sources, colors, gradients, textures, maxValue, transition, auto increment, hide thresholds"; }

    private static PeakProgressBar bar(double value) {
        PeakProgressBar bar = PeakProgressBar.create().source(PeakProgressSource.MANUAL).maxValue(100)
                .fillColor(0x55AAFF).size(120, 10).absolute(PeakAnchor.TOP_CENTER, 0, 26);
        bar.setValue(value);
        return bar;
    }

    private static PeakLabel caption(String text) {
        return PeakLabel.create(text).color(0xAAAAAA).absolute(PeakAnchor.TOP_CENTER, 0, 10);
    }

    private static PeakElementTestStep sourceStep(PeakProgressSource source, String label) {
        return PeakElementTestStep.document("source " + label, 80, doc -> {
            doc.add(caption(label));
            PeakProgressBar bar = PeakProgressBar.create().source(source).fillColor(0x55AAFF).size(120, 10)
                    .absolute(PeakAnchor.TOP_CENTER, 0, 26);
            if (source == PeakProgressSource.MANUAL) {
                bar.maxValue(100);
                bar.setValue(65);
            }
            doc.add(bar);
        });
    }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();
        steps.add(sourceStep(PeakProgressSource.MANUAL, "manual (fixed 65%)"));
        steps.add(sourceStep(PeakProgressSource.HEALTH, "health (live)"));
        steps.add(sourceStep(PeakProgressSource.HUNGER, "hunger (live)"));
        steps.add(sourceStep(PeakProgressSource.ARMOR, "armor (live)"));
        steps.add(sourceStep(PeakProgressSource.EXPERIENCE_PROGRESS, "experience progress (live)"));

        steps.add(PeakElementTestStep.document("colors: backgroundColor red and fillColor green at 40%", 80, doc -> {
            doc.add(caption("backgroundColor 0xAA3333, fillColor 0x33CC55"));
            doc.add(bar(40).backgroundColor(0xAA3333).fillColor(0x33CC55));
        }));
        steps.add(PeakElementTestStep.document("gradients: fill gradient (top) and background gradient (bottom)", 80, doc -> {
            doc.add(caption("fillGradient / backgroundGradient"));
            doc.add(bar(60).fillColor(PeakPaint.gradient(0xFFFF5555, 0xFF55FF55)));
            doc.add(bar(60).backgroundColor(PeakPaint.gradient(0xFF553355, 0xFF335555)).fillColor(0x55AAFF).absolute(PeakAnchor.TOP_CENTER, 0, 44));
        }));
        steps.add(PeakElementTestStep.document("textures: fill texture (stone) on a background texture (dirt)", 80, doc -> {
            doc.add(caption("fillTexture / backgroundTexture"));
            doc.add(bar(70).fillTexture("minecraft:textures/block/stone.png").backgroundTexture("minecraft:textures/block/dirt.png").size(120, 16));
        }));
        steps.add(PeakElementTestStep.document("maxValue 200 with value 50 shows 25%", 80, doc -> {
            doc.add(caption("maxValue 200, value 50"));
            PeakProgressBar bar = bar(0).maxValue(200);
            bar.setValue(50);
            doc.add(bar);
        }));
        steps.add(PeakElementTestStep.document("hideIfEmpty: the bar at value 0 is invisible", 80, doc -> {
            doc.add(caption("The bar below must be invisible"));
            doc.add(bar(0).hideIfEmpty(true));
        }));
        steps.add(PeakElementTestStep.document("hideIfFull: the bar at the maximum is invisible", 80, doc -> {
            doc.add(caption("The bar below must be invisible"));
            doc.add(bar(100).hideIfFull(true));
        }));
        steps.add(PeakElementTestStep.of("transition: a patch moves the value from 20 to 80 over 40 ticks", 160, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(caption("Value 20 now, 80 after one second, smooth over 2 seconds"));
            PeakProgressBar transition = PeakProgressBar.create("pb-transition").source(PeakProgressSource.MANUAL).maxValue(100)
                    .fillColor(0x55AAFF).transitionTicks(40).size(120, 10).absolute(PeakAnchor.TOP_CENTER, 0, 26);
            transition.setValue(20);
            doc.add(transition);
            PeakClient.to(player).sendUi(doc);
            PeakTestTimeline.create(player)
                    .atSeconds(1, () -> PeakClient.to(player).updateElement(docId, "pb-transition", PeakPatch.progressBar().value(80)))
                    .atSeconds(5, () -> PeakClient.to(player).updateElement(docId, "pb-transition", PeakPatch.progressBar().value(20)))
                    .start();
        }));
        steps.add(PeakElementTestStep.document("autoIncrementPerTick 0.5 fills the bar by itself", 240, doc -> {
            doc.add(caption("autoIncrementPerTick 0.5, full after 10 seconds"));
            doc.add(bar(0).autoIncrementPerTick(0.5f));
        }));
        return steps;
    }
}
