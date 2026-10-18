package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class ImageElementTest implements PeakElementTest {
    private static final String DIAMOND = "minecraft:textures/item/diamond.png";
    private static final String TITLE = "minecraft:textures/gui/title/minecraft.png";

    @Override public String name() { return "image"; }
    @Override public String description() { return "PeakImage: size, textures, invalid texture, sourceSize, objectFit, aspectRatio"; }

    private static PeakPanel framed(PeakImage image, int width, int height) {
        PeakPanel box = PeakPanel.create().size(width, height).background(0xFF303030);
        box.add(image.size(width, height));
        return box;
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("size: small icon 16x16", 50, doc ->
                        doc.add(PeakImage.create(DIAMOND).absolute(PeakAnchor.CENTER, 0, 0).size(16, 16))),
                PeakElementTestStep.document("size: stretched 64x64", 50, doc ->
                        doc.add(PeakImage.create(DIAMOND).absolute(PeakAnchor.CENTER, 0, 0).size(64, 64))),
                PeakElementTestStep.document("texture: different texture (iron ingot)", 50, doc ->
                        doc.add(PeakImage.create("minecraft:textures/item/iron_ingot.png").absolute(PeakAnchor.CENTER, 0, 0).size(32, 32))),
                PeakElementTestStep.document("texture: invalid texture renders nothing and does not crash", 50, doc -> {
                    doc.add(PeakLabel.create("No image below is expected here").color(0xFFAA00).absolute(PeakAnchor.CENTER, 0, -20));
                    doc.add(PeakImage.create("peaktest:definitely/not/a/real/texture.png").absolute(PeakAnchor.CENTER, 0, 0).size(32, 32));
                }),
                PeakElementTestStep.document("objectFit FILL (default): the wide texture is stretched into a tall box", 60, doc ->
                        doc.add(framed(PeakImage.create(TITLE, 1024, 256).objectFit(PeakObjectFit.FILL), 60, 100).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("objectFit CONTAIN: the wide texture fits inside the tall box", 60, doc ->
                        doc.add(framed(PeakImage.create(TITLE, 1024, 256).objectFit(PeakObjectFit.CONTAIN), 60, 100).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("objectFit COVER: the same texture is cropped to fill the box", 60, doc ->
                        doc.add(framed(PeakImage.create(TITLE, 1024, 256).objectFit(PeakObjectFit.COVER), 60, 100).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("sourceSize: the texture is 1024x256 (left) vs. a wrong 256x256 (right, squeezed region)", 60, doc -> {
                    doc.add(framed(PeakImage.create(TITLE, 1024, 256).objectFit(PeakObjectFit.CONTAIN), 120, 40).absolute(PeakAnchor.CENTER, -70, 0));
                    doc.add(framed(PeakImage.create(TITLE, 256, 256).objectFit(PeakObjectFit.CONTAIN), 120, 40).absolute(PeakAnchor.CENTER, 70, 0));
                }),
                PeakElementTestStep.document("aspectRatio 2 with only the width set (64 wide, 32 high expected)", 60, doc ->
                        doc.add(PeakImage.create(DIAMOND).aspectRatio(2f).width(64).background(0xFF303030).absolute(PeakAnchor.CENTER, 0, 0))),
                PeakElementTestStep.document("aspectRatio 0.5 with only the height set (32 wide, 64 high expected)", 60, doc ->
                        doc.add(PeakImage.create(DIAMOND).aspectRatio(0.5f).height(64).background(0xFF303030).absolute(PeakAnchor.CENTER, 0, 0)))
        );
    }
}
