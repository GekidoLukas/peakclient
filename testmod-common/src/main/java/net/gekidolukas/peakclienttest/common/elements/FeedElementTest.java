package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class FeedElementTest implements PeakElementTest {
    private static final String[] KILLERS = {"Steve", "Alex", "Technoblade", "Herobrine", "Creeper", "Zombie"};
    private static final int[] COLORS = {0xFFAA3333, 0xFFCC7722, 0xFF33AA55, 0xFF3366CC, 0xFF8844BB, 0xFF228888};

    @Override public String name() { return "feed"; }

    @Override
    public String description() {
        return "PeakFeed: entries added at runtime, lifetime, maximum, order, anchoring, animations and moving neighbors";
    }

    private static PeakPanel entry(int index) {
        int slot = index % KILLERS.length;
        return PeakPanel.create().row().gap(6).padding(4).background(0xC0101820).border(1, COLORS[slot])
                .alignChildren(PeakAlign.CENTER)
                .add(PeakLabel.create("#" + (index + 1)).color(COLORS[slot]))
                .add(PeakLabel.create(KILLERS[slot] + " eliminated " + KILLERS[(slot + 2) % KILLERS.length]));
    }

    private static PeakFeed feed(int x, int y) {
        return PeakFeed.create().size(240, 150).gap(3).absolute(PeakAnchor.TOP_LEFT, x, y);
    }

    private static PeakElementTestStep stream(String label, int ticks, int count, double everySeconds, PeakFeed feed) {
        return PeakElementTestStep.of(label, ticks, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(feed);
            PeakClient.to(player).sendUi(doc);
            PeakTestTimeline timeline = PeakTestTimeline.create(player);
            for (int i = 0; i < count; i++) {
                int index = i;
                timeline.atSeconds(0.5 + i * everySeconds,
                        () -> PeakClient.to(player).addElement(docId, feed.getId(), entry(index)));
            }
            timeline.start();
        });
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                stream("kill feed: a new entry every second, lifetime 4 s, at most 5, newest below, slide from the right", 400, 14, 1.0,
                        feed(20, 20).entryLifetimeSeconds(4).maxEntries(5)
                                .animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.FADE).slideDistance(60)),
                stream("newestFirst: the newest entry appears on top and pushes the others down", 400, 12, 1.0,
                        feed(20, 20).entryLifetimeSeconds(4).maxEntries(5).newestFirst(true)
                                .animations(PeakPopupAnimation.SLIDE_FROM_LEFT, PeakPopupAnimation.SCALE)),
                stream("anchored at the bottom (mainAxisAlign END): older entries move up like a chat", 400, 12, 1.0,
                        feed(20, 20).entryLifetimeSeconds(5).maxEntries(6).mainAxisAlign(PeakMainAxisAlign.END)
                                .background(0x30000000).animations(PeakPopupAnimation.SLIDE_FROM_BOTTOM, PeakPopupAnimation.FADE)),
                stream("lifetime 0 with at most 3: entries only leave when pushed out, never by time", 300, 9, 0.8,
                        feed(20, 20).entryLifetimeTicks(0).maxEntries(3).moveTicks(12)),
                stream("moveTicks 0 and no animations: entries cut in and out without gliding", 300, 9, 0.8,
                        feed(20, 20).entryLifetimeSeconds(3).maxEntries(4).moveTicks(0)
                                .animations(PeakPopupAnimation.NONE, PeakPopupAnimation.NONE)),
                PeakElementTestStep.of("mixed entries: items and players as entries fade and scale as a whole", 300, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakFeed feed = feed(20, 20).entryLifetimeSeconds(4).maxEntries(4)
                            .animations(PeakPopupAnimation.SCALE, PeakPopupAnimation.FADE);
                    doc.add(feed);
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(0.5, () -> add(player, docId, feed, PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(32, 32)))
                            .atSeconds(1.5, () -> add(player, docId, feed, PeakChibiPlayer.clientPlayer().size(36, 52)))
                            .atSeconds(2.5, () -> add(player, docId, feed, entry(2)))
                            .atSeconds(3.5, () -> add(player, docId, feed, PeakItemElement.fixed(new ItemStack(Items.GOLDEN_APPLE)).size(32, 32)))
                            .start();
                })
        );
    }

    private static void add(ServerPlayer player, String docId, PeakFeed feed, PeakElement<?> element) {
        PeakClient.to(player).addElement(docId, feed.getId(), element);
    }
}
