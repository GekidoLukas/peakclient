package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class EventsElementTest implements PeakElementTest {
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final String SCORE = "peaktest:events/score";
    private static final String HEALTH = "peaktest:events/health";

    @Override public String name() { return "events"; }

    @Override
    public String description() {
        return "Element events: value changes (increase, decrease, thresholds), appear, show/hide, timer end, delay, interval, once and highlights";
    }

    private static PeakAnimated box(String id, String text) {
        return PeakAnimated.create(id).borderRadius(8).add(PeakPanel.create().padding(8).borderRadius(8).background(0xFF202830)
                .add(PeakLabel.create(text).color(PeakPaint.WHITE)));
    }

    private static PeakPanel row(int x, int y) {
        return PeakPanel.create().absolute(PeakAnchor.TOP_LEFT, x, y).row().gap(14).padding(10).background(0x40000000);
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§e[events] §f" + text));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("score box: green flash on increase, red flash on decrease, white shine on any change (values 10, 14, 9, 9, 20 at 1, 3, 5, 7 and 9 s)", 320,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            String boxId = "peaktest:events/score-box";
                            PeakAnimated scoreBox = box(boxId, "Score: {" + SCORE + "#0}")
                                    .on(PeakReaction.of(PeakEvent.VALUE_INCREASE).watch("{" + SCORE + "}")
                                            .then(PeakAction.trigger(boxId, PeakAnimationEffect.FLASH, GREEN)))
                                    .on(PeakReaction.of(PeakEvent.VALUE_DECREASE).watch("{" + SCORE + "}")
                                            .then(PeakAction.trigger(boxId, PeakAnimationEffect.FLASH, RED)))
                                    .on(PeakReaction.of(PeakEvent.VALUE_CHANGE).watch("{" + SCORE + "}")
                                            .then(PeakAction.trigger(boxId, PeakAnimationEffect.SHINE)));
                            doc.add(row(30, 30).add(scoreBox));
                            PeakClient.to(player).setValue(SCORE, 10);
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(1, () -> { say(player, "score 10 -> 14 (increase)"); PeakClient.to(player).setValue(SCORE, 14); })
                                    .atSeconds(3, () -> { say(player, "score 14 -> 9 (decrease)"); PeakClient.to(player).setValue(SCORE, 9); })
                                    .atSeconds(5, () -> { say(player, "score set to 9 again: nothing must happen"); PeakClient.to(player).setValue(SCORE, 9); })
                                    .atSeconds(7, () -> { say(player, "score 9 -> 20"); PeakClient.to(player).setValue(SCORE, 20); })
                                    .start();
                        }),
                PeakElementTestStep.of("thresholds: below 8 gives a red outline and a sound, above 15 a gold glow (health 20, 12, 6, 10, 18 at 1, 3, 5, 7 and 9 s)", 320,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            String boxId = "peaktest:events/health-box";
                            PeakAnimated healthBox = box(boxId, "Health: {" + HEALTH + "#0}")
                                    .onBelow("{" + HEALTH + "}", 8,
                                            PeakAction.trigger(boxId, PeakAnimationEffect.OUTLINE, RED),
                                            PeakAction.sound("minecraft:entity.experience_orb.pickup", 1f, 0.6f))
                                    .onAbove("{" + HEALTH + "}", 15,
                                            PeakAction.trigger(boxId, PeakAnimationEffect.GLOW, 0xFFFFD700),
                                            PeakAction.sound("minecraft:entity.experience_orb.pickup", 1f, 1.6f));
                            doc.add(row(30, 30).add(healthBox));
                            PeakClient.to(player).setValue(HEALTH, 20);
                            PeakClient.to(player).sendUi(doc);
                            int[] values = {12, 6, 10, 18};
                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            for (int i = 0; i < values.length; i++) {
                                int value = values[i];
                                timeline.atSeconds(1 + i * 2, () -> {
                                    say(player, "health " + value);
                                    PeakClient.to(player).setValue(HEALTH, value);
                                });
                            }
                            timeline.start();
                        }),
                PeakElementTestStep.document("lifecycle: appear pops the first box, delay (3 s) flashes the second once, interval (2 s) shines the third", 280, doc -> {
                    String popId = "peaktest:events/pop-box";
                    String delayId = "peaktest:events/delay-box";
                    String intervalId = "peaktest:events/interval-box";
                    doc.add(row(30, 30)
                            .add(box(popId, "Appear").onAppear(PeakAction.trigger(popId, PeakAnimationEffect.POP, 18, 2f, 0)))
                            .add(box(delayId, "Delay 3 s").after(60, PeakAction.trigger(delayId, PeakAnimationEffect.FLASH, GREEN)))
                            .add(box(intervalId, "Every 2 s").every(40, PeakAction.trigger(intervalId, PeakAnimationEffect.SHINE))));
                }),
                PeakElementTestStep.of("show and hide: the panel is hidden at 2 s and shown at 4 s; the watcher box outlines on each (and plays a sound)", 240,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            String panelId = "peaktest:events/toggle-panel";
                            String watcherId = "peaktest:events/watcher-box";
                            PeakPanel panel = PeakPanel.create(panelId).padding(8).background(0xFF336699)
                                    .add(PeakLabel.create("Watched panel").color(PeakPaint.WHITE))
                                    .onHide(PeakAction.trigger(watcherId, PeakAnimationEffect.OUTLINE, RED), PeakAction.sound("minecraft:block.note_block.bass"))
                                    .onShow(PeakAction.trigger(watcherId, PeakAnimationEffect.OUTLINE, GREEN), PeakAction.sound("minecraft:block.note_block.pling"));
                            doc.add(row(30, 30).add(panel).add(box(watcherId, "Watcher")));
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(2, () -> {
                                        say(player, "hide the panel");
                                        PeakClient.to(player).updateElement(docId, panelId, PeakPatch.panel().hidden(true));
                                    })
                                    .atSeconds(4, () -> {
                                        say(player, "show the panel");
                                        PeakClient.to(player).updateElement(docId, panelId, PeakPatch.panel().hidden(false));
                                    })
                                    .start();
                        }),
                PeakElementTestStep.document("timer end: a 5 s countdown flashes its box and plays a sound when it reaches zero", 240, doc -> {
                    String boxId = "peaktest:events/timer-box";
                    PeakTimer timer = PeakTimer.countDownFrom("peaktest:events/timer", 100).format(PeakTimerFormat.MM_SS)
                            .color(PeakPaint.WHITE)
                            .onTimerEnd(PeakAction.trigger(boxId, PeakAnimationEffect.FLASH, GREEN),
                                    PeakAction.sound("minecraft:entity.player.levelup"));
                    doc.add(row(30, 30).add(PeakAnimated.create(boxId).borderRadius(8)
                            .add(PeakPanel.create().padding(8).borderRadius(8).background(0xFF202830)
                                    .add(PeakLabel.create("Ends in ").color(PeakPaint.WHITE), timer).row())));
                }),
                PeakElementTestStep.of("once and cooldown: the left box reacts only to the first change, the right box at most once per 2 s (a value change every 500 ms for 5 s)", 300,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            String onceId = "peaktest:events/once-box";
                            String coolId = "peaktest:events/cool-box";
                            String key = "peaktest:events/tick";
                            doc.add(row(30, 30)
                                    .add(box(onceId, "Once: {" + key + "#0}").on(PeakReaction.of(PeakEvent.VALUE_CHANGE).watch("{" + key + "}").once()
                                            .then(PeakAction.trigger(onceId, PeakAnimationEffect.FLASH, GREEN))))
                                    .add(box(coolId, "Cooldown: {" + key + "#0}").on(PeakReaction.of(PeakEvent.VALUE_CHANGE).watch("{" + key + "}").cooldown(40)
                                            .then(PeakAction.trigger(coolId, PeakAnimationEffect.FLASH, GREEN)))));
                            PeakClient.to(player).setValue(key, 0);
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            for (int i = 1; i <= 10; i++) {
                                int value = i;
                                timeline.atSeconds(0.5 * i, () -> PeakClient.to(player).setValue(key, value));
                            }
                            timeline.start();
                        }),
                PeakElementTestStep.of("highlight sugar: highlightOnTrend gives a green flash on increase and a red one on decrease, highlightOnChange a shine on every text change (score 5, 8, 6; name Alex, Steve)", 300,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            String trendKey = "peaktest:events/trend";
                            String nameKey = "peaktest:events/name";
                            doc.add(row(30, 30)
                                    .add(box("peaktest:events/trend-box", "Trend: {" + trendKey + "#0}").highlightOnTrend("{" + trendKey + "}"))
                                    .add(box("peaktest:events/name-box", "Name: {" + nameKey + "#-}").highlightOnChange("{" + nameKey + "}")));
                            PeakClient.to(player).setValue(trendKey, 5);
                            PeakClient.to(player).setValue(nameKey, "Alex");
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(1, () -> { say(player, "trend 5 -> 8"); PeakClient.to(player).setValue(trendKey, 8); })
                                    .atSeconds(3, () -> { say(player, "trend 8 -> 6"); PeakClient.to(player).setValue(trendKey, 6); })
                                    .atSeconds(5, () -> { say(player, "name Alex -> Steve"); PeakClient.to(player).setValue(nameKey, "Steve"); })
                                    .start();
                        }),
                PeakElementTestStep.of("server feedback: SEND from a HUD reports the change (old, new, delta) and the timer end in the chat; nothing else is sent (value 1, 4 at 1 and 3 s, timer ends at 5 s)", 300,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            String key = "peaktest:events/server-value";
                            PeakTimer timer = PeakTimer.countDownFrom("peaktest:events/server-timer", 100).format(PeakTimerFormat.MM_SS)
                                    .color(PeakPaint.WHITE)
                                    .onTimerEnd(PeakAction.send("events-timer-end"));
                            PeakPanel valueBox = PeakPanel.create("peaktest:events/server-box").padding(8).background(0xFF202830)
                                    .add(PeakLabel.create("Value: {" + key + "#0}").color(PeakPaint.WHITE))
                                    .onValueChange("{" + key + "}", PeakAction.send("events-value-changed").arg("source", "hud"));
                            doc.add(row(30, 30).add(valueBox).add(PeakPanel.create().padding(8).background(0xFF202830)
                                    .add(timer)));
                            PeakClient.to(player).setValue(key, 1);
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline.create(player)
                                    .atSeconds(1, () -> { say(player, "value 1 -> 4"); PeakClient.to(player).setValue(key, 4); })
                                    .atSeconds(3, () -> { say(player, "value 4 -> 4: nothing is sent"); PeakClient.to(player).setValue(key, 4); })
                                    .start();
                        }),
                PeakElementTestStep.document("a label as its own watcher: the text shows the clock of the world; the box shines whenever the minute changes", 280, doc -> {
                    String boxId = "peaktest:events/clock-box";
                    doc.add(row(30, 30).add(box(boxId, "{mc:time}")
                            .onValueChange("{mc:time}", PeakAction.trigger(boxId, PeakAnimationEffect.SHINE))));
                })
        );
    }
}
