package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class IconBarElementTest implements PeakElementTest {
    @Override public String name() { return "iconbar"; }
    @Override public String description() { return "PeakIconBar: full and half icons, layering, size, spacing, textures, live source, hide thresholds"; }

    private PeakIconBar bar(double value) {
        return PeakIconBar.create().source(PeakProgressSource.MANUAL).maxValue(20).maxIcons(10)
                .value(value)
                .fallbackFullColor(PeakPaint.rgb(220, 60, 60))
                .fallbackHalfColor(PeakPaint.rgb(160, 40, 40))
                .fallbackEmptyColor(PeakPaint.rgba(60, 60, 60, 160))
                .absolute(PeakAnchor.TOP_CENTER, 0, 10);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("full icons only (value 20 of 20)", 60, doc -> doc.add(bar(20))),
                PeakElementTestStep.document("useHalfIcons: value 13 shows 6 full and 1 half", 60, doc -> doc.add(bar(13).useHalfIcons(true))),
                PeakElementTestStep.document("emptyBehindFullAndHalf: empty icons are layered behind", 60, doc ->
                        doc.add(bar(13).useHalfIcons(true).emptyBehindFullAndHalf(true))),
                PeakElementTestStep.document("maxIcons 5 with maxValue 10 at value 7", 60, doc ->
                        doc.add(bar(7).maxValue(10).maxIcons(5).useHalfIcons(true))),
                PeakElementTestStep.document("iconSize 12 and iconSpacing 4 (top) vs. iconSize 6 and iconSpacing 0 (bottom)", 60, doc -> {
                    doc.add(bar(14).iconSize(12).iconSpacing(4));
                    doc.add(bar(14).iconSize(6).iconSpacing(0).absolute(PeakAnchor.TOP_CENTER, 0, 40));
                }),
                PeakElementTestStep.document("textures: full, half and empty heart sprites", 80, doc ->
                        doc.add(bar(13).useHalfIcons(true).emptyBehindFullAndHalf(true)
                                .fullIconTexture("minecraft:textures/gui/sprites/hud/heart/full.png")
                                .halfIconTexture("minecraft:textures/gui/sprites/hud/heart/half.png")
                                .emptyIconTexture("minecraft:textures/gui/sprites/hud/heart/container.png"))),
                PeakElementTestStep.document("source HEALTH follows your health live", 100, doc ->
                        doc.add(PeakIconBar.create().source(PeakProgressSource.HEALTH).useHalfIcons(true).absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("hideIfEmpty: the bar at value 0 is invisible", 60, doc -> {
                    doc.add(PeakLabel.create("The bar above must be invisible").color(0xFFAA00).absolute(PeakAnchor.TOP_CENTER, 0, 25));
                    doc.add(bar(0).hideIfEmpty(true));
                }),
                PeakElementTestStep.document("hideIfFull: the bar at the maximum is invisible", 60, doc -> {
                    doc.add(PeakLabel.create("The bar above must be invisible").color(0xFFAA00).absolute(PeakAnchor.TOP_CENTER, 0, 25));
                    doc.add(bar(20).hideIfFull(true));
                })
        );
    }
}
