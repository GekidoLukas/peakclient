package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class RadialProgressElementTest implements PeakElementTest {
    @Override public String name() { return "radialprogress"; }
    @Override public String description() { return "PeakRadialProgress: arc, start angle, direction, thickness, colors, gradient"; }

    private static PeakRadialProgress radial(double value) {
        PeakRadialProgress radial = PeakRadialProgress.create().source(PeakProgressSource.MANUAL).maxValue(100)
                .fillColor(0x55AAFF).size(60, 60);
        radial.setValue(value);
        return radial;
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("arcDegrees: full 360 at 65%", 60, doc ->
                        doc.add(radial(65).arcDegrees(360).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("arcDegrees: half circle 180", 60, doc ->
                        doc.add(radial(65).arcDegrees(180).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("arcDegrees: quarter circle 90", 60, doc ->
                        doc.add(radial(65).arcDegrees(90).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("clockwise=false with a 270 arc", 60, doc ->
                        doc.add(radial(65).arcDegrees(270).clockwise(false).fillColor(0xFF8800).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("startAngle: 0, 90 and 180 degrees side by side", 80, doc -> {
                    doc.add(radial(25).startAngle(0).absolute(PeakAnchor.CENTER, -80, 0));
                    doc.add(radial(25).startAngle(90).absolute(PeakAnchor.CENTER, 0, 0));
                    doc.add(radial(25).startAngle(180).absolute(PeakAnchor.CENTER, 80, 0));
                }),
                PeakElementTestStep.document("thickness: 3, 8 and 16", 80, doc -> {
                    doc.add(radial(75).thickness(3).absolute(PeakAnchor.CENTER, -80, 0));
                    doc.add(radial(75).thickness(8).absolute(PeakAnchor.CENTER, 0, 0));
                    doc.add(radial(75).thickness(16).absolute(PeakAnchor.CENTER, 80, 0));
                }),
                PeakElementTestStep.document("backgroundColor: a dark ring behind the fill at 40%", 80, doc ->
                        doc.add(radial(40).backgroundColor(0xAA222233).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("fillGradient on a full circle", 60, doc ->
                        doc.add(radial(100).arcDegrees(360).fillColor(PeakPaint.gradient(0xFFFF5555, 0xFF55FF55)).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("autoIncrementPerTick 0.5 fills the ring by itself", 240, doc ->
                        doc.add(radial(0).autoIncrementPerTick(0.5f).absolute(PeakAnchor.CENTER, 0, 0)))
        );
    }
}
