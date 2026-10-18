package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class CarouselElementTest implements PeakElementTest {
    private static final int[] COLORS = {0xFFAA3333, 0xFFCC7722, 0xFF33AA55, 0xFF3366CC, 0xFF8844BB};

    @Override public String name() { return "carousel"; }

    @Override
    public String description() {
        return "PeakCarousel: movement direction, speed, order, edge fade, gap, alignment, mixed sizes, items and players as children, live patches";
    }

    private static PeakPanel card(String text, int color, int width, int height) {
        return PeakPanel.create().size(width, height).background(color).padding(4)
                .add(PeakLabel.create(text).color(PeakPaint.WHITE));
    }

    private static PeakCarousel cards(PeakCarousel carousel, int count, int width, int height) {
        for (int i = 0; i < count; i++) {
            carousel.add(card("Card " + (i + 1), COLORS[i % COLORS.length], width, height));
        }
        return carousel;
    }

    private static PeakCarousel strip(int width, int height) {
        return PeakCarousel.create().size(width, height).gap(6).background(0x60000000);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("LEFT at 40 px/s with a 24 px fade on both sides: cards slide in from the right and dissolve at the edges", 240, doc ->
                        doc.add(cards(strip(220, 40).movement(PeakCarouselDirection.LEFT).speed(40f).fade(24), 5, 50, 36)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("RIGHT at 80 px/s without a fade: the first card leads and the cards are cut hard at the border", 240, doc ->
                        doc.add(cards(strip(220, 40).movement(PeakCarouselDirection.RIGHT).speed(80f), 5, 50, 36)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("UP (left) and DOWN (right) with a 16 px fade at the top and bottom", 240, doc -> {
                    doc.add(cards(strip(70, 110).movement(PeakCarouselDirection.UP).speed(30f).fade(16), 5, 60, 30)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(cards(strip(70, 110).movement(PeakCarouselDirection.DOWN).speed(30f).fade(16), 5, 60, 30)
                            .absolute(PeakAnchor.TOP_LEFT, 110, 20));
                }),
                PeakElementTestStep.document("order RANDOM (top) vs. SEQUENTIAL (bottom): the top never shows the same card twice in a row", 300, doc -> {
                    doc.add(cards(strip(220, 40).speed(60f).order(PeakCycleOrder.RANDOM).fade(16), 5, 50, 36)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(cards(strip(220, 40).speed(60f).order(PeakCycleOrder.SEQUENTIAL).fade(16), 5, 50, 36)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 80));
                }),
                PeakElementTestStep.document("different sizes with alignChildren CENTER (top) and END (bottom) and gap 12", 240, doc -> {
                    doc.add(strip(240, 64).gap(12).alignChildren(PeakAlign.CENTER).speed(40f).fade(20)
                            .add(card("Small", COLORS[0], 40, 20))
                            .add(card("Medium", COLORS[1], 70, 36))
                            .add(card("Tall", COLORS[2], 50, 56))
                            .add(card("Wide", COLORS[3], 100, 24))
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(strip(240, 64).gap(12).alignChildren(PeakAlign.END).speed(40f).fade(20)
                            .add(card("Small", COLORS[0], 40, 20))
                            .add(card("Medium", COLORS[1], 70, 36))
                            .add(card("Tall", COLORS[2], 50, 56))
                            .add(card("Wide", COLORS[3], 100, 24))
                            .absolute(PeakAnchor.TOP_LEFT, 20, 100));
                }),
                PeakElementTestStep.document("asymmetric fade: none at the start, 60 px at the end, with padding 8 and a border", 240, doc ->
                        doc.add(cards(strip(240, 60).padding(8).border(2, PeakPaint.WHITE).borderRadius(6).fade(0, 60).speed(40f), 5, 50, 40)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("items and a player as children inside a fade (the item stack must fade, not pop)", 240, doc ->
                        doc.add(strip(240, 56).gap(14).alignChildren(PeakAlign.CENTER).speed(40f).fade(32)
                                .add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(32, 32))
                                .add(PeakItemElement.fixed(new ItemStack(Items.APPLE, 12)).size(32, 32))
                                .add(PeakChibiPlayer.clientPlayer().size(36, 52))
                                .add(PeakLabel.create("Label"))
                                .add(PeakItemElement.fixed(new ItemStack(Items.GOLDEN_APPLE)).size(32, 32))
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.of("live patches: speed 20 now, 120 after 3 s, movement RIGHT after 6 s, fade off after 9 s", 300, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(cards(strip(220, 40).speed(20f).fade(24), 5, 50, 36).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    String id = doc.getChildren().get(0).getId();
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(3, () -> PeakClient.to(player).updateElement(docId, id, PeakPatch.carousel().speed(120f)))
                            .atSeconds(6, () -> PeakClient.to(player).updateElement(docId, id,
                                    PeakPatch.carousel().movement(PeakCarouselDirection.RIGHT)))
                            .atSeconds(9, () -> PeakClient.to(player).updateElement(docId, id, PeakPatch.carousel().fade(0)))
                            .start();
                }),
                PeakElementTestStep.document("PINGPONG horizontal: the band runs to the end, waits, runs back and waits; the fade only shows on the side where more content follows", 400, doc ->
                        doc.add(cards(strip(220, 40).pingPong().speed(30f).pauseTicks(30).fade(24), 6, 50, 36)
                                .absolute(PeakAnchor.TOP_LEFT, 20, 20))),
                PeakElementTestStep.document("PINGPONG vertical (left) and RIGHT mirrored (right) with a short pause", 400, doc -> {
                    doc.add(cards(strip(70, 110).movement(PeakCarouselDirection.UP).pingPong().speed(30f).pauseTicks(20).fade(16), 6, 60, 30)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(cards(strip(220, 40).movement(PeakCarouselDirection.RIGHT).pingPong().speed(40f).pauseTicks(20).fade(24), 6, 50, 36)
                            .absolute(PeakAnchor.TOP_LEFT, 110, 20));
                }),
                PeakElementTestStep.document("onlyWhenOverflowing: the top band (3 cards) fits and stands still without a fade, the middle band (6 cards) scrolls, the bottom band (3 cards, PINGPONG) stands still", 300, doc -> {
                    doc.add(cards(strip(220, 40).speed(40f).fade(24).onlyWhenOverflowing(true), 3, 50, 36)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(cards(strip(220, 40).speed(40f).fade(24).onlyWhenOverflowing(true), 6, 50, 36)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 80));
                    doc.add(cards(strip(220, 40).pingPong().speed(40f).fade(24).onlyWhenOverflowing(true), 3, 50, 36)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 140));
                }),
                PeakElementTestStep.of("live: the band holds 3 cards and stands still; after 2 s three more cards are added and it starts to scroll, after 8 s it switches to PINGPONG", 300, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCarousel band = cards(strip(220, 40).speed(40f).fade(24).onlyWhenOverflowing(true), 3, 50, 36)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20);
                    doc.add(band);
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline timeline = PeakTestTimeline.create(player);
                    for (int i = 0; i < 3; i++) {
                        int index = i;
                        timeline.atSeconds(2 + i * 0.3, () -> PeakClient.to(player).addElement(docId, band.getId(),
                                card("New " + (index + 1), COLORS[(index + 3) % COLORS.length], 50, 36)));
                    }
                    timeline.atSeconds(8, () -> PeakClient.to(player).updateElement(docId, band.getId(), PeakPatch.carousel().pingPong()));
                    timeline.start();
                })
        );
    }
}
