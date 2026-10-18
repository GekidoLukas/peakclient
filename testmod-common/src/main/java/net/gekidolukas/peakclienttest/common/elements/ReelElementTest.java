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
import java.util.function.Supplier;

public final class ReelElementTest implements PeakElementTest {
    private static final int[] COLORS = {0xFFAA3333, 0xFFCC7722, 0xFF33AA55, 0xFF3366CC, 0xFF8844BB, 0xFF228899};
    private static final String[] NAMES = {"Red", "Orange", "Green", "Blue", "Purple", "Teal"};

    @Override public String name() { return "reel"; }

    @Override
    public String description() {
        return "PeakReel: spinTo lands exactly on the chosen child, minimum loops, directions, easing, edge fade, jump and stop";
    }

    private static PeakPanel card(int index, int width, int height) {
        return PeakPanel.create().size(width, height).background(COLORS[index % COLORS.length]).padding(6)
                .add(PeakLabel.create(NAMES[index % NAMES.length]).color(PeakPaint.WHITE));
    }

    private static PeakReel reel(int count, PeakCarouselDirection movement, int width, int height) {
        PeakReel reel = PeakReel.create().movement(movement).size(width, height).background(0x60000000)
                .alignChildren(PeakAlign.CENTER).gap(4);
        boolean horizontal = movement == PeakCarouselDirection.LEFT || movement == PeakCarouselDirection.RIGHT;
        for (int i = 0; i < count; i++) {
            reel.add(horizontal ? card(i, 56, height - 12) : card(i, width - 12, 28));
        }
        return reel;
    }

    private static PeakElementTestStep spinStep(String label, int secondsPerSpin, int[] targets, Supplier<PeakReel> factory) {
        return PeakElementTestStep.of(label, 400, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            PeakReel reel = factory.get();
            doc.add(reel);
            String id = reel.getId();
            PeakClient.to(player).sendUi(doc);
            PeakTestTimeline timeline = PeakTestTimeline.create(player);
            for (int i = 0; i < targets.length; i++) {
                int target = targets[i];
                timeline.atSeconds(1 + i * secondsPerSpin, () -> {
                    player.sendSystemMessage(Component.literal("§e[reel] §fspinTo(" + target + ") must stop on " + NAMES[target % NAMES.length]));
                    PeakClient.to(player).updateElement(docId, id, PeakPatch.reel().spinTo(target));
                });
            }
            timeline.start();
        });
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                spinStep("vertical slot, 4 s spins: Blue, Red, Teal; the marker is the middle of the box and the card must be centered", 6,
                        new int[]{3, 0, 5}, () -> reel(6, PeakCarouselDirection.UP, 100, 90).spinSeconds(4)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20)),
                spinStep("movement DOWN with edge fade 20 px: Green, Purple, Orange, cards come from the top", 6, new int[]{2, 4, 1},
                        () -> reel(6, PeakCarouselDirection.DOWN, 100, 120).spinSeconds(4).fade(20)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20)),
                spinStep("horizontal LEFT with fade 24 px, 3 s spins: Purple, Orange, Blue", 5, new int[]{4, 1, 3},
                        () -> reel(6, PeakCarouselDirection.LEFT, 200, 60).spinSeconds(3).fade(24)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20)),
                spinStep("horizontal RIGHT, easing SETTLE: a small overshoot before it settles on Teal, Green, Red", 5, new int[]{5, 2, 0},
                        () -> reel(6, PeakCarouselDirection.RIGHT, 200, 60).spinSeconds(3).easing(PeakReelEasing.SETTLE)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20)),
                spinStep("minLoops 0 and a short spin: it only travels the shortest way forward (Orange, Green, Purple)", 4, new int[]{1, 2, 4},
                        () -> reel(6, PeakCarouselDirection.UP, 100, 90).spinSeconds(1.5).minLoops(0)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20)),
                PeakElementTestStep.document("spinOnStart(4) in the builder: no patch at all, it spins by itself and lands on Purple after 4 s; the second reel waits 3 s first and lands on Green", 300, doc -> {
                    doc.add(reel(6, PeakCarouselDirection.UP, 100, 90).spinSeconds(4).spinOnStart(4)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(reel(6, PeakCarouselDirection.UP, 100, 90).spinSeconds(2).spinOnStart(2, 60)
                            .absolute(PeakAnchor.TOP_LEFT, 140, 20));
                }),
                PeakElementTestStep.document("spinOnStart with an element that is not in the list: it is thrown in at a random position and the reel lands on the gold Jackpot card (reload the step to see another position)", 300, doc ->
                        doc.add(reel(6, PeakCarouselDirection.UP, 100, 90).spinSeconds(4)
                                .spinOnStart(PeakPanel.create().size(88, 28).background(0xFFD4AF37).padding(6)
                                        .add(PeakLabel.create("Jackpot").color(PeakPaint.WHITE)))
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.of("by id and by element at runtime: spinTo(\"Blue\" id) at 1 s, spinReel(new Jackpot element) at 6 s, spinTo(\"Blue\" id) again at 11 s", 400,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakReel reel = reel(5, PeakCarouselDirection.UP, 100, 90).spinSeconds(3).fade(12)
                                    .absolute(PeakAnchor.TOP_LEFT, 20, 20);
                            String blueId = reel.getChildren().get(3).getId();
                            doc.add(reel);
                            String reelId = reel.getId();
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(1, () -> {
                                        player.sendSystemMessage(Component.literal("§e[reel] §fspinTo(id) must stop on Blue"));
                                        PeakClient.to(player).updateElement(docId, reelId, PeakPatch.reel().spinTo(blueId));
                                    })
                                    .atSeconds(6, () -> {
                                        player.sendSystemMessage(Component.literal("§e[reel] §fspinReel(element) adds the gold Jackpot card at a random place and stops on it"));
                                        PeakClient.to(player).spinReel(docId, reelId, PeakPanel.create().size(88, 28)
                                                .background(0xFFD4AF37).padding(6)
                                                .add(PeakLabel.create("Jackpot").color(PeakPaint.WHITE)));
                                    })
                                    .atSeconds(11, () -> {
                                        player.sendSystemMessage(Component.literal("§e[reel] §fspinTo(id) must stop on Blue again, the list now has six cards"));
                                        PeakClient.to(player).updateElement(docId, reelId, PeakPatch.reel().spinTo(blueId));
                                    })
                                    .start();
                        }),
                PeakElementTestStep.document("different child sizes: items and a player in a reel, the chosen one is centered, edge fade 16 px", 300, doc ->
                        doc.add(PeakReel.create().size(100, 100).background(0x60000000).alignChildren(PeakAlign.CENTER).gap(6)
                                .fade(16).startIndex(1)
                                .add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(32, 32))
                                .add(PeakChibiPlayer.clientPlayer().size(36, 52))
                                .add(PeakItemElement.fixed(new ItemStack(Items.GOLDEN_APPLE, 3)).size(40, 40))
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.of("controls: spinTo(2) at 1 s, stop() at 2 s (settles on the next card), jumpTo(4) at 5 s, spinTo(0) at 7 s", 400,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakReel reel = reel(6, PeakCarouselDirection.UP, 100, 90).spinSeconds(5).fade(12)
                                    .absolute(PeakAnchor.TOP_LEFT, 20, 20);
                            doc.add(reel);
                            String id = reel.getId();
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(1, () -> PeakClient.to(player).updateElement(docId, id, PeakPatch.reel().spinTo(2)))
                                    .atSeconds(2, () -> {
                                        player.sendSystemMessage(Component.literal("§e[reel] §fstop()"));
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.reel().stop());
                                    })
                                    .atSeconds(5, () -> {
                                        player.sendSystemMessage(Component.literal("§e[reel] §fjumpTo(4) shows Purple at once"));
                                        PeakClient.to(player).updateElement(docId, id, PeakPatch.reel().jumpTo(4));
                                    })
                                    .atSeconds(7, () -> PeakClient.to(player).updateElement(docId, id, PeakPatch.reel().spinTo(0, 60, 1)))
                                    .start();
                        })
        );
    }
}
