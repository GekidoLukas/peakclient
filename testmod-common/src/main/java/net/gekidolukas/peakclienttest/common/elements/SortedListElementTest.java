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

public final class SortedListElementTest implements PeakElementTest {
    private static final String[] NAMES = {"Steve", "Alex", "Technoblade", "Herobrine", "Creeper", "Zombie"};
    private static final int[] COLORS = {0xFFAA3333, 0xFFCC7722, 0xFF33AA55, 0xFF3366CC, 0xFF8844BB, 0xFF228888};
    private static final int[] START_SCORES = {40, 35, 30, 25, 20, 15};

    @Override public String name() { return "sortedlist"; }

    @Override
    public String description() {
        return "PeakSortedList: sortKey ordering, animated rank changes, ascending order, maxVisible and bound sort keys";
    }

    private static PeakPanel row(int index, String idPrefix, int score) {
        return PeakPanel.create(idPrefix + "/row-" + index).size(200, 20).row().gap(8).padding(4)
                .background(0xFF182028).border(1, COLORS[index]).sortKey(score)
                .add(PeakLabel.create(idPrefix + "/name-" + index, NAMES[index]).color(PeakPaint.WHITE))
                .add(PeakLabel.create(idPrefix + "/score-" + index, Integer.toString(score)).color(PeakPaint.YELLOW));
    }

    private static PeakSortedList board(String idPrefix, int rows) {
        PeakSortedList list = PeakSortedList.create(idPrefix + "/board").gap(4).absolute(PeakAnchor.TOP_LEFT, 20, 20);
        for (int i = 0; i < rows; i++) list.add(row(i, idPrefix, START_SCORES[i]));
        return list;
    }

    private static void setScore(ServerPlayer player, String docId, String prefix, int index, int score) {
        PeakClient.to(player).updateElement(docId, prefix + "/row-" + index, PeakPatch.panel().sortKey(score));
        PeakClient.to(player).updateElement(docId, prefix + "/score-" + index, PeakPatch.label().text(Integer.toString(score)));
    }

    private static PeakElementTestStep scoring(String label, int ticks, PeakSortedList board, String prefix, int[][] changes) {
        return PeakElementTestStep.of(label, ticks, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(board);
            PeakClient.to(player).sendUi(doc);
            PeakTestTimeline timeline = PeakTestTimeline.create(player);
            for (int i = 0; i < changes.length; i++) {
                int[] change = changes[i];
                timeline.atSeconds(1 + i * 1.5, () -> setScore(player, docId, prefix, change[0], change[1]));
            }
            timeline.start();
        });
    }

    @Override
    public List<PeakElementTestStep> steps() {
        int[][] overtakes = {{5, 50}, {3, 45}, {0, 10}, {2, 60}, {4, 5}, {1, 70}};
        return List.of(
                scoring("descending leaderboard: rows glide to their new rank when a score changes (Zombie, Herobrine, Steve, Technoblade, Creeper, Alex)",
                        400, board("peaktest:sorted/a", 6), "peaktest:sorted/a", overtakes),
                scoring("ascending order: the lowest score is on top", 400,
                        board("peaktest:sorted/b", 6).ascending(), "peaktest:sorted/b", overtakes),
                scoring("maxVisible 3: only the top three are shown, rows fade in and out of the top", 400,
                        board("peaktest:sorted/c", 6).maxVisible(3), "peaktest:sorted/c", overtakes),
                scoring("moveTicks 0: rows jump to the new rank without gliding", 300,
                        board("peaktest:sorted/d", 6).moveTicks(0), "peaktest:sorted/d", overtakes),
                scoring("moveTicks 30 with the board anchored at the bottom of a 160 px box", 400,
                        board("peaktest:sorted/e", 6).moveTicks(30).size(220, 160).mainAxisAlign(PeakMainAxisAlign.END)
                                .background(0x30000000), "peaktest:sorted/e", overtakes),
                PeakElementTestStep.of("bound sort keys: the scores come from custom values (peaktest:s0..s3), changed every 1.5 s", 400,
                        (player, docId) -> {
                            PeakDocument doc = PeakDocument.create(docId);
                            PeakSortedList board = PeakSortedList.create("peaktest:sorted/f/board").gap(4)
                                    .absolute(PeakAnchor.TOP_LEFT, 20, 20);
                            for (int i = 0; i < 4; i++) {
                                PeakPanel row = PeakPanel.create("peaktest:sorted/f/row-" + i).size(200, 20).row().gap(8).padding(4)
                                        .background(0xFF182028).border(1, COLORS[i])
                                        .bind("sortKey", "{peaktest:s" + i + "}")
                                        .add(PeakLabel.create(NAMES[i]).color(PeakPaint.WHITE))
                                        .add(PeakLabel.create("{peaktest:s" + i + "}").color(PeakPaint.YELLOW));
                                board.add(row);
                                PeakClient.to(player).setValue("peaktest:s" + i, START_SCORES[i]);
                            }
                            doc.add(board);
                            PeakClient.to(player).sendUi(doc);
                            player.sendSystemMessage(Component.literal("§e[sortedlist] §fbound sortKey: scores change by themselves"));
                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            int[][] changes = {{3, 99}, {0, 10}, {2, 80}, {1, 5}};
                            for (int i = 0; i < changes.length; i++) {
                                int[] change = changes[i];
                                timeline.atSeconds(1 + i * 1.5,
                                        () -> PeakClient.to(player).setValue("peaktest:s" + change[0], change[1]));
                            }
                            timeline.start();
                        }),
                PeakElementTestStep.of("exit animation FADE: rows are deleted one by one, each fades out where it stood while the others close the gap", 400,
                        (player, docId) -> {
                            PeakSortedList board = board("peaktest:sorted/g", 6).exitAnimation(PeakPopupAnimation.FADE).exitDurationTicks(14);
                            PeakDocument doc = PeakDocument.create(docId);
                            doc.add(board);
                            PeakClient.to(player).sendUi(doc);
                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            int[] order = {2, 0, 4, 1};
                            for (int i = 0; i < order.length; i++) {
                                int index = order[i];
                                timeline.atSeconds(1 + i * 1.5,
                                        () -> PeakClient.to(player).deleteElement(docId, "peaktest:sorted/g/row-" + index));
                            }
                            timeline.start();
                        }),
                scoring("exit animation SCALE with maxVisible 3: a row pushed out of the top three shrinks away instead of vanishing", 400,
                        board("peaktest:sorted/h", 6).maxVisible(3).exitAnimation(PeakPopupAnimation.SCALE).exitDurationTicks(12),
                        "peaktest:sorted/h", overtakes),
                scoring("no exit animation (default NONE): the same changes make the row disappear at once", 400,
                        board("peaktest:sorted/i", 6).maxVisible(3), "peaktest:sorted/i", overtakes)
        );
    }
}
