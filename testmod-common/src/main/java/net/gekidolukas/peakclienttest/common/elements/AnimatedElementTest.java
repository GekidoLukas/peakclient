package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakAnimatedPatch;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class AnimatedElementTest implements PeakElementTest {
    @Override public String name() { return "animated"; }

    @Override
    public String description() {
        return "PeakAnimated: looping effects, period, amplitude, delay, playing, one-shot triggers and whole-group effects";
    }

    private static PeakPanel card(String text, int color) {
        return PeakPanel.create().size(54, 30).background(color).padding(5)
                .add(PeakLabel.create(text).color(PeakPaint.WHITE));
    }

    private static PeakAnimated effect(PeakAnimationEffect effect, String text, int color) {
        return PeakAnimated.of(effect).add(card(text, color));
    }

    private static PeakAnimated rounded(PeakAnimated animated, String text, int color) {
        return animated.borderRadius(8).add(PeakPanel.create().size(60, 32).borderRadius(8).background(color).padding(6)
                .add(PeakLabel.create(text).color(PeakPaint.WHITE)));
    }

    private static PeakPanel row(int x, int y) {
        return PeakPanel.create().absolute(PeakAnchor.TOP_LEFT, x, y).row().gap(16).padding(10)
                .background(0x40000000);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("looping effects: PULSE, BOB, SWAY, WOBBLE, SPIN, BLINK, SHAKE", 240, doc ->
                        doc.add(row(20, 30)
                                .add(effect(PeakAnimationEffect.PULSE, "Pulse", 0xFFAA3333))
                                .add(effect(PeakAnimationEffect.BOB, "Bob", 0xFFCC7722))
                                .add(effect(PeakAnimationEffect.SWAY, "Sway", 0xFF33AA55))
                                .add(effect(PeakAnimationEffect.WOBBLE, "Wobble", 0xFF3366CC))
                                .add(effect(PeakAnimationEffect.SPIN, "Spin", 0xFF8844BB))
                                .add(effect(PeakAnimationEffect.BLINK, "Blink", 0xFF228888))
                                .add(effect(PeakAnimationEffect.SHAKE, "Shake", 0xFF886622)))),
                PeakElementTestStep.document("period and amplitude: slow (80 ticks) vs. fast (12 ticks) PULSE, big BOB (8 px), big WOBBLE (20 deg)", 240, doc ->
                        doc.add(row(20, 30)
                                .add(PeakAnimated.of(PeakAnimationEffect.PULSE).periodTicks(80).amplitude(0.3f).add(card("Slow", 0xFFAA3333)))
                                .add(PeakAnimated.of(PeakAnimationEffect.PULSE).periodTicks(12).amplitude(0.15f).add(card("Fast", 0xFFCC7722)))
                                .add(PeakAnimated.of(PeakAnimationEffect.BOB).periodTicks(30).amplitude(8f).add(card("Bob 8", 0xFF33AA55)))
                                .add(PeakAnimated.of(PeakAnimationEffect.WOBBLE).periodTicks(40).amplitude(20f).add(card("Wobble", 0xFF3366CC))))),
                PeakElementTestStep.of("delay and playing: the right card starts 3 s later, all pause at 5 s and resume at 8 s", 300, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakAnimated first = PeakAnimated.of(PeakAnimationEffect.BOB).amplitude(6f).add(card("Now", 0xFF33AA55));
                    PeakAnimated second = PeakAnimated.of(PeakAnimationEffect.BOB).amplitude(6f).delayTicks(60).add(card("Later", 0xFF3366CC));
                    doc.add(row(20, 30).add(first).add(second));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(5, () -> {
                                PeakClient.to(player).updateElement(docId, first.getId(), PeakPatch.animated().playing(false));
                                PeakClient.to(player).updateElement(docId, second.getId(), PeakPatch.animated().playing(false));
                            })
                            .atSeconds(8, () -> {
                                PeakClient.to(player).updateElement(docId, first.getId(), PeakPatch.animated().playing(true));
                                PeakClient.to(player).updateElement(docId, second.getId(), PeakPatch.animated().playing(true));
                            })
                            .start();
                }),
                PeakElementTestStep.of("one-shot triggers: shake at 1 s, pop at 3 s, flash at 5 s, wobble at 7 s, spin at 9 s (chat names them)", 300,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakAnimated target = PeakAnimated.create().add(card("Target", 0xFFAA3333));
                            doc.add(row(60, 40).add(target));
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(1, () -> fire(player, docId, target, "shake", PeakPatch.animated().shake()))
                                    .atSeconds(3, () -> fire(player, docId, target, "pop", PeakPatch.animated().pop()))
                                    .atSeconds(5, () -> fire(player, docId, target, "flash", PeakPatch.animated().flash()))
                                    .atSeconds(7, () -> fire(player, docId, target, "wobble",
                                            PeakPatch.animated().trigger(PeakAnimationEffect.WOBBLE, 24, 2f)))
                                    .atSeconds(9, () -> fire(player, docId, target, "spin",
                                            PeakPatch.animated().trigger(PeakAnimationEffect.SPIN, 20, 1f)))
                                    .start();
                        }),
                PeakElementTestStep.document("a whole group: background, border, label, item and player pulse and blink together as one", 240, doc -> {
                    PeakPanel group = PeakPanel.create().row().gap(10).padding(8).background(0xFF202830)
                            .border(2, PeakPaint.WHITE).borderRadius(6).alignChildren(PeakAlign.CENTER)
                            .add(PeakLabel.create("Group"))
                            .add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(28, 28))
                            .add(PeakChibiPlayer.clientPlayer().size(28, 40));
                    doc.add(PeakAnimated.of(PeakAnimationEffect.PULSE).amplitude(0.12f).absolute(PeakAnchor.TOP_LEFT, 30, 30)
                            .add(group));
                    PeakPanel group2 = PeakPanel.create().row().gap(10).padding(8).background(0xFF202830)
                            .border(2, PeakPaint.WHITE).alignChildren(PeakAlign.CENTER)
                            .add(PeakLabel.create("Blink"))
                            .add(PeakItemElement.fixed(new ItemStack(Items.GOLDEN_APPLE)).size(28, 28))
                            .add(PeakChibiPlayer.clientPlayer().size(28, 40));
                    doc.add(PeakAnimated.of(PeakAnimationEffect.BLINK).periodTicks(50).absolute(PeakAnchor.TOP_LEFT, 30, 110)
                            .add(group2));
                }),
                PeakElementTestStep.document("highlight loops: POP, FLASH, GLOW (gold), SHINE, OUTLINE (white) repeat on their own; the cards have rounded corners", 300, doc ->
                        doc.add(row(20, 30)
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.POP).periodTicks(30), "Pop", 0xFFAA3333))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.FLASH).periodTicks(40), "Flash", 0xFFCC7722))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.GLOW).periodTicks(50), "Glow", 0xFF33AA55))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.SHINE).periodTicks(50), "Shine", 0xFF3366CC))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.OUTLINE).periodTicks(40), "Outline", 0xFF8844BB)))),
                PeakElementTestStep.document("colors and amplitude: GLOW red, FLASH green, OUTLINE cyan, SHINE with amplitude 1, FLASH weak (0.2)", 300, doc ->
                        doc.add(row(20, 30)
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.GLOW).color(0xFF3333).periodTicks(50), "Red", 0xFF202830))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.FLASH).color(0x33FF33).periodTicks(40), "Green", 0xFF202830))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.OUTLINE).color(0x33FFFF).periodTicks(40), "Cyan", 0xFF202830))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.SHINE).amplitude(1f).periodTicks(50), "Bright", 0xFF202830))
                                .add(rounded(PeakAnimated.of(PeakAnimationEffect.FLASH).amplitude(0.2f).periodTicks(40), "Weak", 0xFF202830)))),
                PeakElementTestStep.of("highlight triggers: pop at 1 s, flash green at 3 s, flash red at 5 s, glow at 7 s, shine at 9 s, outline at 11 s (chat names them)", 360,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakAnimated target = rounded(PeakAnimated.create(), "Score 120", 0xFF202830);
                            doc.add(row(30, 30).add(target));
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(1, () -> fire(player, docId, target, "pop", PeakPatch.animated().pop()))
                                    .atSeconds(3, () -> fire(player, docId, target, "flash green (increase)", PeakPatch.animated().flash(0xFF55FF55)))
                                    .atSeconds(5, () -> fire(player, docId, target, "flash red (decrease)", PeakPatch.animated().flash(0xFFFF5555)))
                                    .atSeconds(7, () -> fire(player, docId, target, "glow gold", PeakPatch.animated().glow(0xFFFFD700)))
                                    .atSeconds(9, () -> fire(player, docId, target, "shine", PeakPatch.animated().shine()))
                                    .atSeconds(11, () -> fire(player, docId, target, "outline white", PeakPatch.animated().outline(0xFFFFFFFF)))
                                    .start();
                        }),
                PeakElementTestStep.of("highlights on other content: item, player, label and a square panel (no radius); every effect fires in turn on all four", 360,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakAnimated item = PeakAnimated.create().add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(32, 32));
                            PeakAnimated head = PeakAnimated.create().add(PeakChibiPlayer.clientPlayer().size(32, 44));
                            PeakAnimated label = PeakAnimated.create().padding(6).background(0xFF202830)
                                    .add(PeakLabel.create("Technoblade").color(PeakPaint.WHITE));
                            PeakAnimated square = PeakAnimated.create().add(card("Square", 0xFF336699));
                            doc.add(row(30, 30).alignChildren(PeakAlign.CENTER).add(item).add(head).add(label).add(square));
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            PeakAnimationEffect[] effects = {PeakAnimationEffect.POP, PeakAnimationEffect.FLASH, PeakAnimationEffect.GLOW,
                                    PeakAnimationEffect.SHINE, PeakAnimationEffect.OUTLINE};
                            for (int i = 0; i < effects.length; i++) {
                                PeakAnimationEffect effect = effects[i];
                                timeline.atSeconds(1 + i * 2, () -> {
                                    player.sendSystemMessage(Component.literal("§e[animated] §f" + effect));
                                    for (PeakAnimated animated : List.of(item, head, label, square)) {
                                        PeakClient.to(player).updateElement(docId, animated.getId(), PeakPatch.animated().trigger(effect, 24, 1f, 0xFFFFD700));
                                    }
                                });
                            }
                            timeline.start();
                        }),
                PeakElementTestStep.document("highlights with opacity 0.5 and inside overflow HIDDEN: the glow outside the box is cut by the parent, the group stays translucent", 300, doc -> {
                    doc.add(row(20, 30)
                            .add(rounded(PeakAnimated.of(PeakAnimationEffect.GLOW).periodTicks(50).opacity(0.5f), "Opacity", 0xFF33AA55))
                            .add(PeakPanel.create().size(80, 50).padding(8).overflow(PeakOverflow.HIDDEN).background(0xFF101820)
                                    .add(rounded(PeakAnimated.of(PeakAnimationEffect.GLOW).periodTicks(50), "Clipped", 0xFF3366CC))));
                }),
                PeakElementTestStep.of("loop plus one-shot: a SWAY loop with a shake on top at 2 s, inside a BOB animated", 160, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakAnimated inner = PeakAnimated.of(PeakAnimationEffect.SWAY).add(card("Inner", 0xFF3366CC));
                    doc.add(row(30, 30).add(PeakAnimated.of(PeakAnimationEffect.BOB).add(inner)));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(2, () -> fire(player, docId, inner, "shake on top of the loop", PeakPatch.animated().shake()))
                            .start();
                })
        );
    }

    private static void fire(ServerPlayer player, String docId, PeakAnimated target, String label, PeakAnimatedPatch patch) {
        player.sendSystemMessage(Component.literal("§e[animated] §f" + label));
        PeakClient.to(player).updateElement(docId, target.getId(), patch);
    }
}
