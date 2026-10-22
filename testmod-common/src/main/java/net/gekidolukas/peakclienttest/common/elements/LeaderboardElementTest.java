package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakLeaderboardWindow;
import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LeaderboardElementTest implements PeakElementTest {
    private static final String[] NAMES = {"Steve", "Alex", "Technoblade", "Herobrine", "Creeper", "Zombie", "Skeleton", "Enderman"};
    private static final int LONG_TICKS = 24000;
    private static final Map<UUID, Runnable> OPEN = new ConcurrentHashMap<>();

    @Override public String name() { return "leaderboard"; }

    @Override
    public String description() {
        return "PeakLeaderboardWindow: top-N window of a large ranking, gliding ranks, ascending races, own row, spare rows, stress and rejoin";
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§e[leaderboard] §f" + text));
    }

    private static String botName(int index) {
        return NAMES[index % NAMES.length] + (index / NAMES.length + 1);
    }

    private static PeakLeaderboardWindow<String> board(String id, int size) {
        return PeakLeaderboardWindow.<String>create(id, size).intervalTicks(5);
    }

    private static void fill(PeakLeaderboardWindow<String> board, int count, Random random, int maxScore) {
        for (int i = 0; i < count; i++) board.set("bot-" + i, botName(i), random.nextInt(maxScore));
    }

    private static void closePrevious(UUID playerId) {
        Runnable previous = OPEN.remove(playerId);
        if (previous != null) previous.run();
    }

    private static PeakTestTimeline open(ServerPlayer player, String docId, boolean persistent, List<PeakLeaderboardWindow<String>> boards) {
        closePrevious(player.getUUID());
        PeakAudience audience = PeakClient.audience(List.of(player));
        PeakDocument document = PeakDocument.create(docId);
        int x = 20;
        for (PeakLeaderboardWindow<String> board : boards) {
            board.element().absolute(PeakAnchor.TOP_LEFT, x, 20);
            document.add(board.element());
            x += 190;
        }
        PeakView view = audience.open(document).persistent(persistent);
        for (PeakLeaderboardWindow<String> board : boards) board.bind(view);

        Runnable close = () -> {
            for (PeakLeaderboardWindow<String> board : boards) board.close();
            view.close();
            audience.close();
        };
        OPEN.put(player.getUUID(), close);
        PeakTestTimeline timeline = PeakTestTimeline.create(player);
        timeline.onCancel(() -> {
            if (OPEN.remove(player.getUUID(), close)) close.run();
        });
        timeline.at(LONG_TICKS, () -> {});
        return timeline;
    }

    private static void randomWalk(PeakTestTimeline timeline, PeakLeaderboardWindow<String> board, Random random, int bots,
                                   int changesPerSecond, int seconds, int maxDelta) {
        for (int second = 1; second <= seconds; second++) {
            timeline.at(second * 20, () -> {
                for (int i = 0; i < changesPerSecond; i++) {
                    board.add("bot-" + random.nextInt(bots), 1 + random.nextInt(maxDelta));
                }
            });
        }
    }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();

        steps.add(PeakElementTestStep.of("top 8 of 40 entries: every second some scores grow; ranks glide, rows entering the top 8 fade in, rows below disappear", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(7);
                    PeakLeaderboardWindow<String> board = board("peaktest:leaderboard/main", 8);
                    fill(board, 40, random, 100);
                    PeakTestTimeline timeline = open(player, docId, false, List.of(board));
                    randomWalk(timeline, board, random, 40, 5, 120, 25);
                    timeline.start();
                    say(player, "watch the order: only the top 8 rows exist on your client, the other 32 entries stay on the server");
                }));

        steps.add(PeakElementTestStep.of("time race: ascending order with seconds, ties keep the first one that reached the time", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(11);
                    PeakLeaderboardWindow<String> board = board("peaktest:leaderboard/race", 6).ascending()
                            .scoreFormat(score -> String.format(Locale.ROOT, "%.2fs", score));
                    for (int i = 0; i < 20; i++) board.set("bot-" + i, botName(i), 60 + (i % 4) * 2.5);
                    PeakTestTimeline timeline = open(player, docId, false, List.of(board));
                    for (int second = 1; second <= 120; second++) {
                        timeline.at(second * 20, () -> board.add("bot-" + random.nextInt(20), -(0.5 + random.nextInt(4) * 0.5)));
                    }
                    timeline.start();
                    say(player, "lower is better; equal times keep their order until someone improves");
                }));

        steps.add(PeakElementTestStep.of("component names: gold bold, red, translated and gradient-free styled names; your own row below shows your styled name too", LONG_TICKS,
                (player, docId) -> {
                    PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("peaktest:leaderboard/names", 5)
                            .intervalTicks(5).ownRow(uuid -> uuid.equals(player.getUUID()) ? "me" : null);
                    board.set("gold", Component.literal("Gold Bold").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), 90);
                    board.set("red", Component.literal("Red Italic").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), 80);
                    board.set("seed", Component.translatable("commands.seed.success", Component.literal("42").withStyle(ChatFormatting.YELLOW)), 70);
                    board.set("alt", Component.literal("Alt font").withStyle(Style.EMPTY.withFont(ResourceLocation.parse("minecraft:alt"))), 60);
                    board.set("plain", "Plain legacy §aGreen", 50);
                    board.set("me", Component.literal(player.getGameProfile().getName()).withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE), 10);
                    PeakTestTimeline timeline = open(player, docId, false, List.of(board));
                    timeline.atSeconds(4, () -> board.add("me", 85));
                    timeline.start();
                    say(player, "all names keep their styles; after 4 s you climb into the list and your own row hides");
                }));

        steps.add(PeakElementTestStep.of("own row (outside the window): you start at the bottom; the row below the list shows your rank until you climb into the top 6", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(3);
                    PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("peaktest:leaderboard/own", 6)
                            .intervalTicks(5).ownRow(uuid -> uuid.equals(player.getUUID()) ? "me" : null);
                    fill(board, 30, random, 200);
                    board.set("me", player.getName().getString(), 0);
                    PeakTestTimeline timeline = open(player, docId, false, List.of(board));
                    for (int second = 1; second <= 90; second++) {
                        timeline.at(second * 20, () -> {
                            board.add("me", 8 + random.nextInt(10));
                            board.add("bot-" + random.nextInt(30), random.nextInt(6));
                        });
                    }
                    timeline.start();
                    say(player, "the own row disappears once you are inside the top 6");
                }));

        steps.add(PeakElementTestStep.of("neighbours: the block below the list shows the rank above you, your own highlighted row and the rank below you; at the very top or bottom the missing row hides", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(13);
                    PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("peaktest:leaderboard/neighbours", 5)
                            .intervalTicks(5).ownRow(uuid -> uuid.equals(player.getUUID()) ? "me" : null).neighbours(1);
                    fill(board, 30, random, 200);
                    board.set("me", player.getName().getString(), 0);
                    PeakTestTimeline timeline = open(player, docId, false, List.of(board));
                    for (int second = 1; second <= 100; second++) {
                        timeline.at(second * 20, () -> {
                            board.add("me", 6 + random.nextInt(10));
                            board.add("bot-" + random.nextInt(30), random.nextInt(8));
                        });
                    }
                    timeline.start();
                    say(player, "you start last: the row below you is hidden; once you reach the top 5 the whole block hides");
                }));

        steps.add(PeakElementTestStep.of("own row (always): the row stays visible below the list even while you are in the window; a player without an entry shows no row", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(5);
                    PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("peaktest:leaderboard/always", 5)
                            .intervalTicks(5).ownRow(uuid -> uuid.equals(player.getUUID()) ? "me" : null,
                                    PeakLeaderboardWindow.OwnRow.ALWAYS);
                    fill(board, 12, random, 100);
                    PeakTestTimeline timeline = open(player, docId, false, List.of(board));
                    timeline.at(80, () -> board.set("me", player.getName().getString(), 120));
                    timeline.at(240, () -> board.remove("me"));
                    timeline.start();
                    say(player, "at 4 s you join the ranking at the top, at 12 s you are removed and the own row hides");
                }));

        steps.add(PeakElementTestStep.of("spare rows: left board spare 0, right board spare 1; the same score changes drive both, compare how a row dropping out of the top 5 leaves", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(9);
                    PeakLeaderboardWindow<String> noSpare = board("peaktest:leaderboard/spare0", 5).spare(0);
                    PeakLeaderboardWindow<String> spare = board("peaktest:leaderboard/spare1", 5).spare(1);
                    for (int i = 0; i < 12; i++) {
                        noSpare.set("bot-" + i, botName(i), 50 + i);
                        spare.set("bot-" + i, botName(i), 50 + i);
                    }
                    PeakTestTimeline timeline = open(player, docId, false, List.of(noSpare, spare));
                    for (int second = 1; second <= 120; second++) {
                        timeline.at(second * 20, () -> {
                            String key = "bot-" + random.nextInt(12);
                            double delta = random.nextInt(21) - 8;
                            noSpare.add(key, delta);
                            spare.add(key, delta);
                        });
                    }
                    timeline.start();
                    say(player, "with spare 1 the row ranked 6 waits hidden and glides into the list when it moves up");
                }));

        steps.add(PeakElementTestStep.of("stress: 2000 entries, 50 score changes per second, only the top 10 reach the client (F3+0 shows the UI cost)", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(21);
                    PeakLeaderboardWindow<String> board = board("peaktest:leaderboard/stress", 10);
                    fill(board, 2000, random, 1000);
                    PeakTestTimeline timeline = open(player, docId, false, List.of(board));
                    randomWalk(timeline, board, random, 2000, 50, 120, 40);
                    timeline.at(100, () -> say(player, "entries on the server: " + board.count() + ", rows on the client: 11"));
                    timeline.start();
                }));

        steps.add(PeakElementTestStep.of("rejoin: leave the server and join again; the list returns complete because the view is persistent", LONG_TICKS,
                (player, docId) -> {
                    Random random = new Random(33);
                    PeakLeaderboardWindow<String> board = board("peaktest:leaderboard/rejoin", 6);
                    fill(board, 25, random, 100);
                    PeakTestTimeline timeline = open(player, docId, true, List.of(board));
                    randomWalk(timeline, board, random, 25, 3, 120, 20);
                    timeline.start();
                    say(player, "disconnect and reconnect now; after the join the current top 6 must be visible without waiting for changes");
                }));

        return steps;
    }
}
