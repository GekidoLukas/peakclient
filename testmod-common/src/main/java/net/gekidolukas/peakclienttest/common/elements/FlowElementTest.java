package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class FlowElementTest implements PeakElementTest {
    private static final String[] TAGS = {"PvP", "Survival", "Minigame", "Build", "Skyblock", "Hardcore", "Events", "Creative",
            "Parkour", "Factions", "Bedwars", "Lobby", "Tournament", "Trading"};
    private static final int[] COLORS = {0xFFAA3333, 0xFFCC7722, 0xFF33AA55, 0xFF3366CC, 0xFF8844BB, 0xFF228899};

    @Override public String name() { return "flow"; }

    @Override
    public String description() {
        return "PeakFlow: wrapping, gaps, line alignment, cross alignment, line distribution, reverse, maximum lines, line breaks";
    }

    private static PeakPanel tag(int index) {
        return PeakPanel.create().padding(2, 5, 2, 5).background(COLORS[index % COLORS.length])
                .add(PeakLabel.create(TAGS[index % TAGS.length]).color(PeakPaint.WHITE));
    }

    private static PeakFlow tags(int count) {
        PeakFlow flow = PeakFlow.create().background(0x60000000).padding(4).spacing(4);
        for (int i = 0; i < count; i++) flow.add(tag(i));
        return flow;
    }

    private static PeakPanel box(int index, int width, int height) {
        return PeakPanel.create().size(width, height).background(COLORS[index % COLORS.length]);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("fixed width 180: tags of different widths wrap into lines, the height follows the content", 300, doc ->
                        doc.add(tags(14).width(180).absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("automatic width: the flow wraps at wrapSize 150 and is only as wide as its longest line", 300, doc ->
                        doc.add(tags(10).wrapSize(150).absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("percent width 50 of the window: the flow wraps at half of the window width", 300, doc ->
                        doc.add(tags(14).widthPercent(50).absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("mainAxisAlign per line: START, CENTER, END and SPACE_BETWEEN (the last line is spread too)", 300, doc -> {
                    PeakMainAxisAlign[] aligns = {PeakMainAxisAlign.START, PeakMainAxisAlign.CENTER, PeakMainAxisAlign.END,
                            PeakMainAxisAlign.SPACE_BETWEEN};
                    for (int i = 0; i < aligns.length; i++) {
                        doc.add(tags(6).width(170).mainAxisAlign(aligns[i]).absolute(PeakAnchor.TOP_LEFT, 20 + (i % 2) * 190, 20 + (i / 2) * 90));
                    }
                }),
                PeakElementTestStep.document("alignChildren inside a line: START, CENTER, END with boxes of different heights", 300, doc -> {
                    PeakAlign[] aligns = {PeakAlign.START, PeakAlign.CENTER, PeakAlign.END};
                    for (int i = 0; i < aligns.length; i++) {
                        PeakFlow flow = PeakFlow.create().width(150).background(0x60000000).padding(4).spacing(4)
                                .alignChildren(aligns[i]).absolute(PeakAnchor.TOP_LEFT, 20 + i * 170, 20);
                        for (int n = 0; n < 6; n++) flow.add(box(n, 30, 14 + (n % 3) * 12));
                        doc.add(flow);
                    }
                }),
                PeakElementTestStep.document("lineAlign in a box of height 110: START, CENTER, END, SPACE_BETWEEN, SPACE_AROUND", 300, doc -> {
                    PeakMainAxisAlign[] aligns = {PeakMainAxisAlign.START, PeakMainAxisAlign.CENTER, PeakMainAxisAlign.END,
                            PeakMainAxisAlign.SPACE_BETWEEN, PeakMainAxisAlign.SPACE_AROUND};
                    for (int i = 0; i < aligns.length; i++) {
                        PeakFlow flow = PeakFlow.create().size(100, 110).background(0x60000000).padding(3).spacing(3)
                                .lineAlign(aligns[i]).absolute(PeakAnchor.TOP_LEFT, 20 + i * 112, 20);
                        for (int n = 0; n < 5; n++) flow.add(box(n, 40, 16));
                        doc.add(flow);
                    }
                }),
                PeakElementTestStep.document("COLUMN direction: children run downwards and wrap into columns at height 90", 300, doc -> {
                    PeakFlow flow = PeakFlow.create().direction(PeakLayoutDirection.COLUMN).height(90).background(0x60000000)
                            .padding(4).spacing(4).absolute(PeakAnchor.TOP_LEFT, 20, 20);
                    for (int n = 0; n < 9; n++) flow.add(box(n, 24 + (n % 3) * 10, 20));
                    doc.add(flow);
                }),
                PeakElementTestStep.document("maxLines 2 shows only the first two lines; the second flow is reversed (last child first, bottom to top)", 300, doc -> {
                    doc.add(tags(14).width(180).maxLines(2).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(tags(8).width(180).reverse(true).absolute(PeakAnchor.TOP_LEFT, 220, 20));
                }),
                PeakElementTestStep.document("line breaks: a Flow.lineBreak() child forces a new line (Alpha | Beta Gamma | Delta)", 300, doc ->
                        doc.add(PeakFlow.create().width(240).background(0x60000000).padding(4).spacing(4)
                                .add(tag(0), tag(1), PeakFlow.lineBreak(), tag(2), tag(3), tag(4), PeakFlow.lineBreak(), tag(5))
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("percent children: three boxes of 45 percent in a flow of width 200 give one box in the second row", 300, doc -> {
                    PeakFlow flow = PeakFlow.create().width(200).background(0x60000000).padding(4).spacing(4)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20);
                    for (int n = 0; n < 3; n++) flow.add(box(n, 10, 18).widthPercent(45));
                    doc.add(flow);
                }),
                PeakElementTestStep.document("items and players as children: eight icons wrap into rows of four", 300, doc -> {
                    PeakFlow flow = PeakFlow.create().width(100).background(0x60000000).padding(4).spacing(4)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20);
                    for (int n = 0; n < 6; n++) {
                        flow.add(PeakItemElement.fixed(new ItemStack(n % 2 == 0 ? Items.DIAMOND_SWORD : Items.GOLDEN_APPLE)).size(18, 18));
                    }
                    flow.add(PeakPlayerIcon.clientPlayer().size(18, 18), PeakPlayerIcon.clientPlayer().size(18, 18));
                    doc.add(flow);
                }),
                PeakElementTestStep.document("flow inside an automatic panel with wrapSize, next to a second panel (neither overlaps)", 300, doc ->
                        doc.add(PeakPanel.create().row().gap(10).absolute(PeakAnchor.TOP_LEFT, 20, 20)
                                .add(PeakPanel.create().background(0x40FFFFFF).padding(4).add(tags(8).wrapSize(140)))
                                .add(PeakPanel.create().background(0x40FFFFFF).padding(4).add(tags(5).wrapSize(100))))),
                PeakElementTestStep.document("flow with a border, padding 8 and a rounded background", 300, doc ->
                        doc.add(tags(9).width(200).padding(8).borderRadius(8).border(2, 0xFFFFFFFF)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20)))
        );
    }
}
