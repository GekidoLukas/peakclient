package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class TooltipLabelElementTest implements PeakElementTest {
    @Override public String name() { return "tooltiplabel"; }
    @Override public String description() { return "PeakTooltipLabel: background styles, border color, padding, color, gradient, translation, anchors"; }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("backgroundStyle VANILLA (default)", 60, doc ->
                        doc.add(PeakTooltipLabel.create("Vanilla-style tooltip").absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("backgroundStyle DRAWN_BOX with an orange borderColor", 60, doc ->
                        doc.add(PeakTooltipLabel.create("Custom drawn box")
                                .backgroundStyle(PeakTooltipBackgroundStyle.DRAWN_BOX)
                                .borderColor(PeakPaint.rgba(255, 100, 0, 220))
                                .absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("backgroundStyle CUSTOM_TEXTURE", 60, doc ->
                        doc.add(PeakTooltipLabel.create("Custom texture background")
                                .backgroundTexture("minecraft:textures/gui/title/minecraft.png")
                                .absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("tooltipPadding: 0/0 (top), 4/2 (middle) and 16/10 (bottom)", 60, doc -> {
                    doc.add(PeakTooltipLabel.create("Padding 0 0").tooltipPadding(0, 0).absolute(PeakAnchor.CENTER, 0, -40));
                    doc.add(PeakTooltipLabel.create("Padding 4 2").tooltipPadding(4, 2).absolute(PeakAnchor.CENTER, 0, 0));
                    doc.add(PeakTooltipLabel.create("Padding 16 10").tooltipPadding(16, 10).absolute(PeakAnchor.CENTER, 0, 40));
                }),
                PeakElementTestStep.document("color: green text on the DRAWN_BOX style", 60, doc ->
                        doc.add(PeakTooltipLabel.create("Green text").color(0x55FF55)
                                .backgroundStyle(PeakTooltipBackgroundStyle.DRAWN_BOX).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("textGradient on the vanilla style", 60, doc ->
                        doc.add(PeakTooltipLabel.create("Gradient tooltip text")
                                .color(PeakPaint.gradient(0xFFFF5555, 0xFF5555FF)).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("translation: key without and with an argument", 60, doc -> {
                    doc.add(PeakTooltipLabel.translatable("gui.done").absolute(PeakAnchor.CENTER, 0, -20));
                    doc.add(PeakTooltipLabel.translatable("commands.seed.success", "1234").absolute(PeakAnchor.CENTER, 0, 20));
                }),
                PeakElementTestStep.document("anchors: a tooltip at the right and bottom edges is not clipped", 80, doc -> {
                    doc.add(PeakTooltipLabel.create("Top right").absolute(PeakAnchor.TOP_RIGHT, 0, 0));
                    doc.add(PeakTooltipLabel.create("Bottom right").absolute(PeakAnchor.BOTTOM_RIGHT, 0, 0));
                    doc.add(PeakTooltipLabel.create("Centered right").absolute(PeakAnchor.CENTER_RIGHT, 0, 0));
                })
        );
    }
}
