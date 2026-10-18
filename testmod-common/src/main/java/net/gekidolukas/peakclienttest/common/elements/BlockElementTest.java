package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class BlockElementTest implements PeakElementTest {
    @Override public String name() { return "block"; }
    @Override public String description() { return "PeakBlockElement: FIXED block, rotation, TARGETED_BLOCK"; }

    private PeakBlockElement withSize(PeakBlockElement block) {
        return block.absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                .size(PeakSize.of(PeakSize.PeakDimension.pixels(48), PeakSize.PeakDimension.pixels(48)));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("FIXED diamond block", 50, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakBlockElement.fixed("minecraft:diamond_block")));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("FIXED oak log, rotated 45deg", 50, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakBlockElement.fixed("minecraft:oak_log").yaw(45f)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("TARGETED_BLOCK (look at any block)", 50, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Look at any block").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, -30)));
                    doc.add(withSize(PeakBlockElement.targetedBlock()));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.document("yaw: 0, 90 and 180 degrees side by side", 60, doc -> {
                    for (int i = 0; i < 3; i++) {
                        doc.add(PeakBlockElement.fixed("minecraft:oak_log").yaw(i * 90f).size(48, 48).absolute(PeakAnchor.CENTER, (i - 1) * 70, 0));
                    }
                }),
                PeakElementTestStep.document("size: 24, 48 and 96 pixel boxes keep the block centered", 60, doc -> {
                    int[] sizes = { 24, 48, 96 };
                    int[] offsets = { -90, 0, 100 };
                    for (int i = 0; i < 3; i++) {
                        doc.add(PeakBlockElement.fixed("minecraft:diamond_block").size(sizes[i], sizes[i])
                                .background(0xFF303030).absolute(PeakAnchor.CENTER, offsets[i], 0));
                    }
                }),
                PeakElementTestStep.document("blocks with different render types: glass, leaves and grass", 60, doc -> {
                    String[] blocks = { "minecraft:glass", "minecraft:oak_leaves", "minecraft:grass_block" };
                    for (int i = 0; i < 3; i++) {
                        doc.add(PeakBlockElement.fixed(blocks[i]).size(48, 48).absolute(PeakAnchor.CENTER, (i - 1) * 70, 0));
                    }
                }),
                PeakElementTestStep.document("opacity 0.5: glass, leaves, grass and diamond over a striped background", 80, doc -> {
                    doc.add(PeakPanel.create().size(300, 20).background(0xFFCC3333).absolute(PeakAnchor.CENTER, 0, -12));
                    doc.add(PeakPanel.create().size(300, 20).background(0xFF33CC33).absolute(PeakAnchor.CENTER, 0, 12));
                    String[] blocks = { "minecraft:glass", "minecraft:oak_leaves", "minecraft:grass_block", "minecraft:diamond_block" };
                    for (int i = 0; i < blocks.length; i++) {
                        doc.add(PeakBlockElement.fixed(blocks[i]).size(48, 48).opacity(0.5f)
                                .absolute(PeakAnchor.CENTER, i * 64 - 96, 0));
                    }
                })
        );
    }
}