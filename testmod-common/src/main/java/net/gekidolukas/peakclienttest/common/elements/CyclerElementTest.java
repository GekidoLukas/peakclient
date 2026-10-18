package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class CyclerElementTest implements PeakElementTest {
    private static final int[] COLORS = {0xFFAA3333, 0xFFCC7722, 0xFF33AA55, 0xFF3366CC, 0xFF8844BB};
    private static final String[] NAMES = {"Red", "Orange", "Green", "Blue", "Purple"};

    @Override public String name() { return "cycler"; }

    @Override
    public String description() {
        return "PeakCycler: interval, order, transition mode, enter and exit animations, durations, pause, next and index patches";
    }

    private static PeakPanel slide(int index, int width, int height) {
        return PeakPanel.create().size(width, height).background(COLORS[index % COLORS.length]).padding(6)
                .add(PeakLabel.create(NAMES[index % NAMES.length]).color(PeakPaint.WHITE));
    }

    private static PeakCycler display(int count) {
        PeakCycler cycler = PeakCycler.create().size(160, 60).background(0x60000000)
                .alignChildren(PeakAlign.CENTER).mainAxisAlign(PeakMainAxisAlign.CENTER);
        for (int i = 0; i < count; i++) cycler.add(slide(i, 120, 36));
        return cycler;
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("FADE in and out, 2 s interval, one card after the other with a short gap in between", 300, doc ->
                        doc.add(display(4).intervalSeconds(2).absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("enter SLIDE_FROM_RIGHT, exit SLIDE_FROM_LEFT, durations 14 and 14 ticks", 300, doc ->
                        doc.add(display(4).intervalSeconds(2).animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.SLIDE_FROM_LEFT)
                                .durations(14, 14).slideDistance(60).absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("SCALE in and out from the center", 300, doc ->
                        doc.add(display(4).intervalSeconds(2).animations(PeakPopupAnimation.SCALE, PeakPopupAnimation.SCALE)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("WIPE: the card is revealed from the left by a moving edge and wiped away in reverse (top), from the top (bottom)", 300, doc -> {
                    doc.add(display(4).intervalSeconds(2).animations(PeakPopupAnimation.WIPE_FROM_LEFT, PeakPopupAnimation.WIPE_FROM_LEFT)
                            .durations(16, 16).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(display(4).intervalSeconds(2).animations(PeakPopupAnimation.WIPE_FROM_TOP, PeakPopupAnimation.WIPE_FROM_TOP)
                            .durations(16, 16).absolute(PeakAnchor.TOP_LEFT, 20, 100));
                }),
                PeakElementTestStep.document("WIPE_FROM_RIGHT and WIPE_FROM_BOTTOM with OVERLAP: the new card is uncovered while the old one disappears", 300, doc -> {
                    doc.add(display(4).intervalSeconds(2).transition(PeakCyclerTransition.OVERLAP)
                            .animations(PeakPopupAnimation.WIPE_FROM_RIGHT, PeakPopupAnimation.WIPE_FROM_RIGHT)
                            .durations(20, 20).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(display(4).intervalSeconds(2).transition(PeakCyclerTransition.OVERLAP)
                            .animations(PeakPopupAnimation.WIPE_FROM_BOTTOM, PeakPopupAnimation.WIPE_FROM_BOTTOM)
                            .durations(20, 20).absolute(PeakAnchor.TOP_LEFT, 20, 100));
                }),
                PeakElementTestStep.document("FLIP_HORIZONTAL (top) and FLIP_VERTICAL (bottom): the card squeezes to a line and the next one unfolds like a flipped card", 300, doc -> {
                    doc.add(display(4).intervalSeconds(2).animations(PeakPopupAnimation.FLIP_HORIZONTAL, PeakPopupAnimation.FLIP_HORIZONTAL)
                            .durations(12, 12).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(display(4).intervalSeconds(2).animations(PeakPopupAnimation.FLIP_VERTICAL, PeakPopupAnimation.FLIP_VERTICAL)
                            .durations(12, 12).absolute(PeakAnchor.TOP_LEFT, 20, 100));
                }),
                PeakElementTestStep.document("flip and wipe with items and a player as cards (the whole group must squeeze and clip, not pop)", 300, doc ->
                        doc.add(PeakCycler.create().size(120, 80).background(0x60000000).intervalSeconds(2)
                                .animations(PeakPopupAnimation.FLIP_HORIZONTAL, PeakPopupAnimation.FLIP_HORIZONTAL)
                                .alignChildren(PeakAlign.CENTER).mainAxisAlign(PeakMainAxisAlign.CENTER)
                                .add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(48, 48))
                                .add(PeakChibiPlayer.clientPlayer().size(40, 70))
                                .add(PeakItemElement.fixed(new ItemStack(Items.GOLDEN_APPLE)).size(48, 48))
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("OVERLAP: the old card leaves while the new one enters (compare with SEQUENTIAL below)", 300, doc -> {
                    doc.add(display(4).intervalSeconds(2).transition(PeakCyclerTransition.OVERLAP)
                            .animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.SLIDE_FROM_LEFT).slideDistance(80)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(display(4).intervalSeconds(2).transition(PeakCyclerTransition.SEQUENTIAL)
                            .animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.SLIDE_FROM_LEFT).slideDistance(80)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 100));
                }),
                PeakElementTestStep.document("order RANDOM with a 1 s interval: five colors in a shuffled order, never the same twice in a row", 300, doc ->
                        doc.add(display(5).intervalSeconds(1).order(PeakCycleOrder.RANDOM).absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("animations NONE: a hard cut every second", 200, doc ->
                        doc.add(display(3).intervalSeconds(1).animations(PeakPopupAnimation.NONE, PeakPopupAnimation.NONE)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("different child sizes: the box keeps the size of the biggest child, children are centered", 300, doc ->
                        doc.add(PeakCycler.create().background(0x60000000).alignChildren(PeakAlign.CENTER)
                                .mainAxisAlign(PeakMainAxisAlign.CENTER).intervalSeconds(2)
                                .add(slide(0, 60, 24))
                                .add(slide(1, 140, 30))
                                .add(slide(2, 90, 70))
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("items and a player as children: they must fade and scale as a whole, not pop", 300, doc ->
                        doc.add(PeakCycler.create().size(120, 64).background(0x60000000).alignChildren(PeakAlign.CENTER)
                                .mainAxisAlign(PeakMainAxisAlign.CENTER).intervalSeconds(2)
                                .animations(PeakPopupAnimation.SCALE, PeakPopupAnimation.FADE)
                                .add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(40, 40))
                                .add(PeakChibiPlayer.clientPlayer().size(40, 56))
                                .add(PeakItemElement.fixed(new ItemStack(Items.GOLDEN_APPLE, 3)).size(40, 40))
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.of("controls: next at 2 s, showIndex(3) at 4 s, paused at 6 s, resumed at 9 s, hidden child skipped from 10 s", 400,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakCycler cycler = display(5).intervalSeconds(3).absolute(PeakAnchor.TOP_LEFT, 20, 20);
                            doc.add(cycler);
                            String id = cycler.getId();
                            String hiddenId = cycler.getChildren().get(1).getId();
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(2, () -> {
                                        player.sendSystemMessage(Component.literal("§e[cycler] §fnext()"));
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.cycler().next());
                                    })
                                    .atSeconds(4, () -> {
                                        player.sendSystemMessage(Component.literal("§e[cycler] §fshowIndex(3) shows Blue"));
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.cycler().showIndex(3));
                                    })
                                    .atSeconds(6, () -> {
                                        player.sendSystemMessage(Component.literal("§e[cycler] §fpaused"));
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.cycler().paused(true));
                                    })
                                    .atSeconds(9, () -> {
                                        player.sendSystemMessage(Component.literal("§e[cycler] §fresumed"));
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.cycler().paused(false));
                                    })
                                    .atSeconds(10, () -> {
                                        player.sendSystemMessage(Component.literal("§e[cycler] §fOrange is hidden and skipped from now on"));
                                        PeakClient.to(player).updateElement(docId, hiddenId, PeakPatch.panel().hidden(true));
                                    })
                                    .start();
                        })
        );
    }
}
