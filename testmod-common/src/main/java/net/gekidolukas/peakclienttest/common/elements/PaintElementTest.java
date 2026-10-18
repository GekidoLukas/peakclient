package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakBindingHelper;
import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class PaintElementTest implements PeakElementTest {
    private static final PeakLinearGradient WARM = PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55);
    private static final PeakLinearGradient COOL = PeakPaint.gradient(0xFF55FFFF, 0xFF5555FF).vertical();
    private static final PeakLinearGradient RAINBOW = PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF5555FF);
    private static final PeakLinearGradient ANIMATED = RAINBOW.animated(80);

    @Override public String name() { return "paints"; }

    @Override
    public String description() {
        return "PeakPaint: gradients on backgrounds, borders, shadows, text, bars, charts and bindings";
    }

    private static PeakPanel row(int x, int y) {
        return PeakPanel.create().absolute(PeakAnchor.TOP_LEFT, x, y).row().gap(10).padding(0).background(PeakPaint.NONE);
    }

    private static PeakPanel box(String caption) {
        return PeakPanel.create().size(76, 46).padding(4).background(0xFF202830)
                .add(PeakLabel.create(caption).color(PeakPaint.WHITE));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("backgrounds: solid, left to right, top to bottom, five stops, animated rainbow", 160, doc ->
                        doc.add(row(20, 20)
                                .add(box("solid").background(0xFF3366CC))
                                .add(box("warm").background(WARM))
                                .add(box("cool").background(COOL))
                                .add(box("rainbow").background(RAINBOW))
                                .add(box("animated").background(ANIMATED)))),
                PeakElementTestStep.document("rounded backgrounds: radius 12 plain, with a solid border and with a gradient border", 160, doc ->
                        doc.add(row(20, 20)
                                .add(box("radius").background(WARM).borderRadius(12))
                                .add(box("solid border").background(WARM).borderRadius(12).border(3, PeakPaint.WHITE))
                                .add(box("gradient border").background(0xFF202830).borderRadius(12).border(3, RAINBOW))
                                .add(box("both").background(COOL).borderRadius(12).border(3, WARM)))),
                PeakElementTestStep.document("borders: rectangular gradient frames, 2 and 5 px; animated on the right", 160, doc ->
                        doc.add(row(20, 20)
                                .add(box("2 px").border(2, WARM))
                                .add(box("5 px").border(5, COOL))
                                .add(box("animated").border(3, ANIMATED)))),
                PeakElementTestStep.document("shadows: hard gradient shadow, blurred gradient shadow, rounded blurred", 160, doc ->
                        doc.add(row(30, 30)
                                .add(box("hard").boxShadow(WARM, 5, 5, 0))
                                .add(box("blur 6").boxShadow(COOL, 4, 4, 6))
                                .add(box("rounded").borderRadius(10).boxShadow(RAINBOW, 3, 3, 8)))),
                PeakElementTestStep.document("text: label, timer and tooltip label with gradient colors, animated on the second row", 160, doc -> {
                    doc.add(row(20, 20)
                            .add(PeakLabel.create("Warm gradient label").color(WARM))
                            .add(PeakTimer.countDownFrom(1200).color(COOL))
                            .add(PeakTooltipLabel.create("Gradient tooltip text").color(RAINBOW)));
                    doc.add(row(20, 60)
                            .add(PeakLabel.create("Animated rainbow label with a long text").color(ANIMATED))
                            .add(PeakLabel.create("No shadow").color(WARM).shadow(false)));
                }),
                PeakElementTestStep.document("bars: progress bar fill and background, radial ring, icon bar fallbacks", 160, doc -> {
                    PeakProgressBar bar = PeakProgressBar.create().source(PeakProgressSource.MANUAL).maxValue(100)
                            .fillColor(WARM).backgroundColor(PeakPaint.gradient(0xFF202020, 0xFF505050))
                            .size(160, 12).absolute(PeakAnchor.TOP_LEFT, 20, 20);
                    bar.setValue(70);
                    PeakRadialProgress ring = PeakRadialProgress.create().source(PeakProgressSource.MANUAL).maxValue(100)
                            .fillColor(RAINBOW).size(60, 60).absolute(PeakAnchor.TOP_LEFT, 20, 50);
                    ring.setValue(75);
                    PeakIconBar icons = PeakIconBar.create().source(PeakProgressSource.MANUAL).maxValue(20).maxIcons(10)
                            .value(13).useHalfIcons(true)
                            .fallbackFullColor(WARM).fallbackHalfColor(COOL).fallbackEmptyColor(PeakPaint.gradient(0xFF303030, 0xFF707070))
                            .absolute(PeakAnchor.TOP_LEFT, 100, 70);
                    doc.add(bar, ring, icons);
                }),
                PeakElementTestStep.document("charts: bar chart and pie chart entries with gradients, translucent background", 160, doc -> {
                    doc.add(PeakBarChart.create()
                            .addEntry("Warm", 12, WARM)
                            .addEntry("Cool", 8, COOL)
                            .addEntry("Rainbow", 15, RAINBOW)
                            .backgroundColor(PeakPaint.gradient(0x80000000, 0x80303060).vertical())
                            .size(160, 80).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(PeakPieChart.create()
                            .addEntry("Warm", 40, WARM)
                            .addEntry("Cool", 30, COOL)
                            .addEntry("Rainbow", 30, RAINBOW)
                            .size(100, 100).absolute(PeakAnchor.TOP_LEFT, 200, 20));
                }),
                PeakElementTestStep.of("bindings: a value carrying a whole paint, stops from placeholders (second box falls back to blue)", 200,
                        (player, docId) -> {
                            PeakValueKey<String> whole = PeakValueKey.ofString("peaktest:paint");
                            PeakValueKey<String> first = PeakValueKey.ofString("peaktest:first");
                            PeakValueKey<String> second = PeakValueKey.ofString("peaktest:second");
                            PeakClient.to(player).clearValues("peaktest");
                            PeakClient.to(player).setValue(whole, PeakBindingHelper.gradient()
                                    .stop(0f, PeakPaint.RED).stop(1f, PeakPaint.YELLOW).vertical().expression());
                            PeakClient.to(player).setValue(first, "#55FF55");

                            PeakDocument doc = PeakDocument.create(docId);
                            doc.add(row(20, 20)
                                    .add(box("whole paint").background(0xFF000000)
                                            .bind(PeakBindingHelper.Paths.STYLE_BACKGROUND, PeakBindingHelper.placeholder(whole)))
                                    .add(box("stops").background(0xFF000000)
                                            .bind(PeakBindingHelper.Paths.STYLE_BACKGROUND, PeakBindingHelper.gradient()
                                                    .stop(0f, first, 0xFFFFFFFF).stop(1f, second, 0xFF5555FF).expression()))
                                    .add(box("missing").background(0xFF664400)
                                            .bind(PeakBindingHelper.Paths.STYLE_BACKGROUND, PeakBindingHelper.placeholder("peaktest:nothing"))));
                            PeakClient.to(player).sendUi(doc);
                        }),
                PeakElementTestStep.document("angles: 0, 30, 45, 90, 135 and 180 degrees on rectangles (the arrow of the gradient points from red to blue)", 200, doc ->
                        doc.add(row(20, 20)
                                .add(box("0").background(angled(0f)))
                                .add(box("30").background(angled(30f)))
                                .add(box("45").background(angled(45f)))
                                .add(box("90").background(angled(90f)))
                                .add(box("135").background(angled(135f)))
                                .add(box("180").background(angled(180f))))),
                PeakElementTestStep.document("angles on rounded boxes, with solid and angled borders, shadows and frames; the corner edges must stay smooth", 200, doc -> {
                    doc.add(row(20, 20)
                            .add(box("radius").background(angled(45f)).borderRadius(12))
                            .add(box("border").background(angled(45f)).borderRadius(12).border(3, PeakPaint.WHITE))
                            .add(box("gradient border").background(0xFF202830).borderRadius(12).border(3, angled(30f)))
                            .add(box("both").background(angled(135f)).borderRadius(12).border(3, angled(315f))));
                    doc.add(row(20, 90)
                            .add(box("shadow").background(0xFF303840).boxShadow(angled(45f), 4, 4, 6))
                            .add(box("frame").border(3, angled(60f)))
                            .add(box("animated").background(angled(30f).animated(60)).borderRadius(8)));
                }),
                PeakElementTestStep.document("text with angled gradients: 0, 45 and 90 degrees (90 keeps the old left-to-right look), animated on the second row", 200, doc -> {
                    doc.add(row(20, 20)
                            .add(PeakLabel.create("Angle 0").color(angled(0f)))
                            .add(PeakLabel.create("Angle 45").color(angled(45f)))
                            .add(PeakLabel.create("Angle 90").color(angled(90f))));
                    doc.add(row(20, 50)
                            .add(PeakLabel.create("Animated angled rainbow text").color(RAINBOW.angle(30f).animated(80))));
                }),
                PeakElementTestStep.document("radial: centered, off-center, small radius, animated, a wide box (the circle stays round)", 200, doc -> {
                    doc.add(row(20, 20)
                            .add(box("center").background(radial()))
                            .add(box("corner").background(PeakRadialGradient.builder().stop(0f, 0xFFFFFF55).stop(1f, 0xFF5555FF).center(0f, 0f).build()))
                            .add(box("r 0.5").background(PeakRadialGradient.builder().stop(0f, 0xFFFFFF55).stop(1f, 0xFF5555FF).radius(0.5f).build()))
                            .add(box("animated").background(PeakRadialGradient.of(0xFFFF5555, 0xFFFFFF55, 0xFF55FF55, 0xFF5555FF,0xFFFF5555).animated(60))));
                    doc.add(row(20, 90)
                            .add(PeakPanel.create().size(220, 40).padding(4).background(radial()).add(PeakLabel.create("wide box")))
                            .add(box("rounded").background(radial()).borderRadius(14))
                            .add(box("border").background(radial()).borderRadius(14).border(3, angled(45f))));
                }),
                PeakElementTestStep.document("radial and angles in shapes and charts: circle, hexagon, star, a pie slice and a radial frame", 200, doc -> {
                    doc.add(PeakShape.circle().size(70, 70).background(radial()).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(PeakShape.hexagon().size(80, 70).background(angled(45f)).absolute(PeakAnchor.TOP_LEFT, 110, 20));
                    doc.add(PeakShape.star().size(80, 76).background(radial()).absolute(PeakAnchor.TOP_LEFT, 210, 20));
                    doc.add(PeakPieChart.create()
                            .addEntry("Radial", 50, radial())
                            .addEntry("Angled", 50, angled(45f))
                            .size(100, 100).absolute(PeakAnchor.TOP_LEFT, 310, 20));
                    doc.add(PeakPanel.create().size(120, 40).padding(4).background(PeakPaint.NONE).border(4, radial())
                            .absolute(PeakAnchor.TOP_LEFT, 20, 110).add(PeakLabel.create("radial frame")));
                }),
                PeakElementTestStep.of("bindings: a bound angled gradient, a bound radial gradient and one with a stop from a placeholder", 200,
                        (player, docId) -> {
                            PeakValueKey<String> angledKey = PeakValueKey.ofString("peaktest:angled");
                            PeakValueKey<String> radialKey = PeakValueKey.ofString("peaktest:radial");
                            PeakValueKey<String> stop = PeakValueKey.ofString("peaktest:stop");
                            PeakClient.to(player).clearValues("peaktest");
                            PeakClient.to(player).setValue(angledKey, PeakBindingHelper.gradient()
                                    .stop(0f, PeakPaint.RED).stop(1f, PeakPaint.YELLOW).angle(60f).expression());
                            PeakClient.to(player).setValue(radialKey, PeakBindingHelper.gradient().radial()
                                    .stop(0f, PeakPaint.YELLOW).stop(1f, PeakPaint.BLUE).radius(0.6f).expression());
                            PeakClient.to(player).setValue(stop, "#55FF55");

                            PeakDocument doc = PeakDocument.create(docId);
                            doc.add(row(20, 20)
                                    .add(box("angled").background(0xFF000000)
                                            .bind(PeakBindingHelper.Paths.STYLE_BACKGROUND, PeakBindingHelper.placeholder(angledKey)))
                                    .add(box("radial").background(0xFF000000)
                                            .bind(PeakBindingHelper.Paths.STYLE_BACKGROUND, PeakBindingHelper.placeholder(radialKey)))
                                    .add(box("stop").background(0xFF000000)
                                            .bind(PeakBindingHelper.Paths.STYLE_BACKGROUND, PeakBindingHelper.gradient().radial()
                                                    .stop(0f, stop, 0xFFFFFFFF).stop(1f, PeakPaint.BLUE).expression())));
                            PeakClient.to(player).sendUi(doc);
                        }));
    }

    private static PeakLinearGradient angled(float degrees) {
        return PeakPaint.gradient(0xFFFF5555, 0xFF5555FF).angle(degrees);
    }

    private static PeakRadialGradient radial() {
        return PeakPaint.radial(0xFFFFFF55, 0xFF5555FF);
    }
}
