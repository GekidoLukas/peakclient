package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class ShapeElementTest implements PeakElementTest {
    @Override public String name() { return "shape"; }

    @Override
    public String description() {
        return "PeakShape: children clipped to a circle or rounded rectangle, border, nested containers, opacity";
    }

    private static PeakPanel stripes(int width, int height) {
        PeakPanel panel = PeakPanel.create().size(width, height).row();
        int[] colors = {0xFFCC3333, 0xFFCC9933, 0xFF33AA55, 0xFF3366CC};
        for (int color : colors) panel.add(PeakPanel.create().size(width / colors.length + 1, height).background(color));
        return panel;
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("circle avatar: stripes and the player head are cut to a circle, the wide one is a pill", 300, doc -> {
                    doc.add(PeakShape.circle().size(64, 64).absolute(PeakAnchor.TOP_LEFT, 20, 20).add(stripes(64, 64)));
                    doc.add(PeakShape.circle().size(64, 64).background(0xFF202020).absolute(PeakAnchor.TOP_LEFT, 100, 20)
                            .add(PeakPlayerIcon.clientPlayer().size(64, 64)));
                    doc.add(PeakShape.circle().size(90, 40).absolute(PeakAnchor.TOP_LEFT, 180, 20).add(stripes(90, 40)));
                }),
                PeakElementTestStep.document("rounded rectangle with radius 4, 12 and 30 (clamped to half the height)", 300, doc -> {
                    doc.add(PeakShape.rounded(4).size(70, 50).absolute(PeakAnchor.TOP_LEFT, 20, 20).add(stripes(70, 50)));
                    doc.add(PeakShape.rounded(12).size(70, 50).absolute(PeakAnchor.TOP_LEFT, 110, 20).add(stripes(70, 50)));
                    doc.add(PeakShape.rounded(30).size(70, 50).absolute(PeakAnchor.TOP_LEFT, 200, 20).add(stripes(70, 50)));
                }),
                PeakElementTestStep.document("border 3 px: the border follows the curve and the content is clipped at the inner edge", 300, doc -> {
                    doc.add(PeakShape.rounded(14).size(80, 60).border(3, 0xFFFFFFFF).background(0xFF101010)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20).add(stripes(80, 60)));
                    doc.add(PeakShape.circle().size(64, 64).border(4, 0xFFFFD700).absolute(PeakAnchor.TOP_LEFT, 120, 20)
                            .add(PeakPlayerIcon.clientPlayer().size(64, 64)));
                }),
                PeakElementTestStep.document("opacity 0.5 on shapes with a chibi child and with stripes", 300, doc -> {
                    doc.add(PeakShape.circle().size(70, 70).opacity(0.5f).background(0xFF336699)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20).add(PeakChibiPlayer.clientPlayer().size(60, 70)));
                    doc.add(PeakShape.rounded(16).size(70, 70).opacity(0.5f).background(0xFF993366)
                            .absolute(PeakAnchor.TOP_LEFT, 110, 20).add(stripes(70, 70)));
                }),
                PeakElementTestStep.document("polygon shapes: triangle, diamond, pentagon, hexagon, flat hexagon, octagon, star (all sharp, stripes cut to the outline)", 300, doc -> {
                    PeakShape[] shapes = {PeakShape.triangle(), PeakShape.diamond(), PeakShape.pentagon(), PeakShape.hexagon(),
                            PeakShape.hexagonFlat(), PeakShape.octagon(), PeakShape.star()};
                    for (int i = 0; i < shapes.length; i++) {
                        doc.add(shapes[i].size(52, 52).background(0xFF202830).absolute(PeakAnchor.TOP_LEFT, 20 + i * 62, 20)
                                .add(stripes(52, 52)));
                    }
                }),
                PeakElementTestStep.document("polygons with cornerRadius 6, with a 3 px border, and with a gradient background", 300, doc -> {
                    doc.add(PeakShape.hexagon().cornerRadius(6).size(70, 70).absolute(PeakAnchor.TOP_LEFT, 20, 20)
                            .add(stripes(70, 70)));
                    doc.add(PeakShape.star().cornerRadius(4).size(70, 70).border(3, 0xFFFFD700).background(0xFF101010)
                            .absolute(PeakAnchor.TOP_LEFT, 110, 20).add(stripes(70, 70)));
                    doc.add(PeakShape.triangle().cornerRadius(8).size(70, 70).border(2, 0xFFFFFFFF)
                            .background(PeakPaint.gradient(0xFF3366CC, 0xFFCC3366)).absolute(PeakAnchor.TOP_LEFT, 200, 20)
                            .add(PeakPlayerIcon.clientPlayer().size(70, 70)));
                }),
                PeakElementTestStep.document("popOutTop: the chibi in the circle is cut at the bottom, everything above the middle is not clipped", 300, doc -> {
                    doc.add(PeakShape.circle().size(70, 70).background(0xFF336699).popOutTop(0.5f)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 60)
                            .add(PeakChibiPlayer.clientPlayer().size(60, 110).absolute(PeakAnchor.TOP_LEFT, 5, -40)));
                    doc.add(PeakShape.circle().size(70, 70).background(0xFF336699)
                            .absolute(PeakAnchor.TOP_LEFT, 120, 60)
                            .add(PeakChibiPlayer.clientPlayer().size(60, 110).absolute(PeakAnchor.TOP_LEFT, 5, -40)));
                    doc.add(PeakShape.rounded(14).size(80, 60).background(0xFF993366).popOutTop(0.4f).border(2, 0xFFFFFFFF)
                            .absolute(PeakAnchor.TOP_LEFT, 220, 60)
                            .add(PeakChibiPlayer.clientPlayer().size(50, 100).absolute(PeakAnchor.TOP_LEFT, 15, -45)));
                }),
                PeakElementTestStep.document("popOutBottom, popOutLeft, popOutRight and top plus bottom together (hexagon and square)", 300, doc -> {
                    doc.add(PeakShape.hexagon().size(60, 70).background(0xFF336699).popOutBottom(0.5f)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 40)
                            .add(stripes(60, 110)));
                    doc.add(PeakShape.rounded(10).size(70, 50).background(0xFF336699).popOutLeft(0.5f)
                            .absolute(PeakAnchor.TOP_LEFT, 150, 60)
                            .add(PeakPanel.create().size(130, 50).background(0xFFCC9933).absolute(PeakAnchor.TOP_LEFT, -60, 0)));
                    doc.add(PeakShape.circle().size(60, 60).background(0xFF336699).popOutTop(0.5f).popOutBottom(0.5f)
                            .absolute(PeakAnchor.TOP_LEFT, 270, 50)
                            .add(PeakPanel.create().size(40, 100).background(0xFFCC3333).absolute(PeakAnchor.TOP_LEFT, 10, -20)));
                }),
                PeakElementTestStep.document("shapes inside a reel with edge fade and a shape inside a shape (nested masks fall back to a plain clip)", 300, doc -> {
                    PeakReel reel = PeakReel.create().movement(PeakCarouselDirection.UP).size(80, 80).fade(16)
                            .alignChildren(PeakAlign.CENTER).gap(4).absolute(PeakAnchor.TOP_LEFT, 20, 20);
                    for (int i = 0; i < 4; i++) reel.add(PeakShape.circle().size(44, 44).add(stripes(44, 44)));
                    doc.add(reel);
                    doc.add(PeakShape.rounded(20).size(90, 90).background(0xFF202830).absolute(PeakAnchor.TOP_LEFT, 130, 20)
                            .alignChildren(PeakAlign.CENTER).mainAxisAlign(PeakMainAxisAlign.CENTER)
                            .add(PeakShape.circle().size(60, 60).add(stripes(60, 60))));
                })
        );
    }
}
