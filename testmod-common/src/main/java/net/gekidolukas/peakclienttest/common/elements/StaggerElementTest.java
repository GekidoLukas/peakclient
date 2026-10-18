package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class StaggerElementTest implements PeakElementTest {
    private static final String[] NAMES = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot"};

    @Override public String name() { return "stagger"; }

    @Override
    public String description() {
        return "PeakStagger: delay, animations, durations, start delay, order, reverse, autoPlay, replay and hide";
    }

    private static PeakStagger rows(int count) {
        PeakStagger stagger = PeakStagger.create().column().gap(4).absolute(PeakAnchor.TOP_LEFT, 20, 20);
        for (int i = 0; i < count; i++) {
            stagger.add(PeakPanel.create().size(150, 18).background(0xFF202830).padding(4).border(1, PeakPaint.GRAY)
                    .add(PeakLabel.create((i + 1) + ". " + NAMES[i % NAMES.length])));
        }
        return stagger;
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§e[stagger] §f" + text));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("FADE, 4 ticks between rows: a list that fills one row after the other without moving the layout", 160, doc ->
                        doc.add(rows(6))),
                PeakElementTestStep.document("SLIDE_FROM_LEFT with 3 ticks delay, 12 ticks per row, slide distance 60", 160, doc ->
                        doc.add(rows(6).animations(PeakPopupAnimation.SLIDE_FROM_LEFT, PeakPopupAnimation.FADE)
                                .delayTicks(3).durations(12, 8).slideDistance(60))),
                PeakElementTestStep.document("reverse: the last row comes first; RANDOM on the right", 160, doc -> {
                    doc.add(rows(6).reverse(true).delayTicks(5));
                    doc.add(rows(6).order(PeakCycleOrder.RANDOM).delayTicks(5).absolute(PeakAnchor.TOP_LEFT, 200, 20));
                }),
                PeakElementTestStep.document("SCALE with a 1 s start delay and 8 ticks between rows", 160, doc ->
                        doc.add(rows(5).animations(PeakPopupAnimation.SCALE, PeakPopupAnimation.SCALE)
                                .startDelayTicks(20).delayTicks(8))),
                PeakElementTestStep.document("WIPE_FROM_LEFT: every row is uncovered from the left (left), FLIP_VERTICAL: rows fold open (right)", 160, doc -> {
                    doc.add(rows(5).animations(PeakPopupAnimation.WIPE_FROM_LEFT, PeakPopupAnimation.WIPE_FROM_LEFT)
                            .delayTicks(5).durations(14, 8));
                    doc.add(rows(5).animations(PeakPopupAnimation.FLIP_VERTICAL, PeakPopupAnimation.FLIP_VERTICAL)
                            .delayTicks(5).durations(14, 8).absolute(PeakAnchor.TOP_LEFT, 200, 20));
                }),
                PeakElementTestStep.document("NONE: rows pop in one after the other without animation (every 10 ticks)", 160, doc ->
                        doc.add(rows(5).animations(PeakPopupAnimation.NONE, PeakPopupAnimation.NONE).delayTicks(10))),
                PeakElementTestStep.of("autoPlay off: nothing at first, replay at 2 s, hide at 6 s, replay at 9 s with another order at 11 s", 360,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakStagger stagger = rows(6).autoPlay(false).animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.SLIDE_FROM_LEFT)
                                    .delayTicks(4).slideDistance(40);
                            doc.add(stagger);
                            PeakClient.to(player).sendUi(doc);
                            String id = stagger.getId();
                            PeakTestTimeline.create(player)
                                    .atSeconds(2, () -> {
                                        say(player, "replay()");
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.stagger().replay());
                                    })
                                    .atSeconds(6, () -> {
                                        say(player, "hide(): rows leave one after the other");
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.stagger().hide());
                                    })
                                    .atSeconds(9, () -> {
                                        say(player, "replay()");
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.stagger().replay());
                                    })
                                    .atSeconds(11, () -> {
                                        say(player, "order RANDOM, replay()");
                                        PeakClient.to(player).updateElement(docId, id,
                                                PeakPatch.stagger().order(PeakCycleOrder.RANDOM).replay());
                                    })
                                    .start();
                        }),
                PeakElementTestStep.document("mixed children: items, a player and labels appear as whole groups (no popping)", 160, doc ->
                        doc.add(PeakStagger.create().row().gap(12).alignChildren(PeakAlign.CENTER)
                                .animations(PeakPopupAnimation.SCALE, PeakPopupAnimation.FADE).delayTicks(8).durations(14, 8)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20)
                                .add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(32, 32))
                                .add(PeakItemElement.fixed(new ItemStack(Items.GOLDEN_APPLE, 3)).size(32, 32))
                                .add(PeakChibiPlayer.clientPlayer().size(36, 52))
                                .add(PeakLabel.create("Winner!").color(PeakPaint.YELLOW))))
        );
    }
}
