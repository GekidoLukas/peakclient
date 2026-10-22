package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakClientApiHolder;
import net.gekidolukas.peakclient.api.PeakLeaderboardRanking;
import net.gekidolukas.peakclient.api.PeakLeaderboardWindow;
import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakLeaderboardWindowTest {
    private final Queue<Runnable> queue = new ArrayDeque<>();
    private boolean onServerThread;
    private RecordingPeakApi api;
    private PeakClientApi previous;
    private PeakAudience audience;

    private void drain() {
        boolean before = onServerThread;
        onServerThread = true;
        try {
            while (!queue.isEmpty()) queue.poll().run();
        } finally {
            onServerThread = before;
        }
    }

    @BeforeEach
    void install() {
        previous = PeakClientApiHolder.INSTANCE;
        api = new RecordingPeakApi();
        PeakClientApiHolder.INSTANCE = api;
        onServerThread = false;
        PeakServerThread.install(() -> onServerThread, queue::add);
        audience = PeakClient.audienceOfIds(List.of(UUID.randomUUID()));
        drain();
    }

    @AfterEach
    void restore() {
        audience.close();
        drain();
        PeakServerThread.reset();
        PeakClientApiHolder.INSTANCE = previous;
    }

    private PeakView open(PeakLeaderboardWindow<String> board) {
        PeakView view = audience.open(PeakDocument.create("test:hud").add(board.element()));
        drain();
        board.bind(view);
        drain();
        return view;
    }

    private void flush(PeakLeaderboardWindow<String> board, PeakView view) {
        board.flush();
        drain();
        view.flush();
        drain();
    }

    private List<PeakUiOperation> lastOperations() {
        for (int i = api.calls.size() - 1; i >= 0; i--) {
            if (api.calls.get(i).name().equals("update:test:hud")) {
                @SuppressWarnings("unchecked")
                List<PeakUiOperation> operations = (List<PeakUiOperation>) api.calls.get(i).payload();
                return operations;
            }
        }
        return List.of();
    }

    private int updatePackets() {
        return (int) api.calls.stream().filter(call -> call.name().equals("update:test:hud")).count();
    }

    private PeakSortedList list(PeakView view, PeakLeaderboardWindow<String> board) {
        return assertInstanceOf(PeakSortedList.class, view.document().findById(board.listId()));
    }

    private Map<String, PeakElement<?>> rowsByName(PeakSortedList list) {
        Map<String, PeakElement<?>> rows = new HashMap<>();
        for (PeakElement<?> row : list.getChildren()) {
            PeakLabel name = childLabel(row, "/name");
            rows.put(name.getText().getString(), row);
        }
        return rows;
    }

    private PeakLabel childLabel(PeakElement<?> row, String suffix) {
        return assertInstanceOf(PeakLabel.class, ((PeakContainer<?>) row).getChildren().stream()
                .filter(child -> child.getId().endsWith(suffix)).findFirst().orElseThrow());
    }

    @Test
    void theRankingOrdersByScoreAndKeepsTheFirstReacherAheadOnTies() {
        PeakLeaderboardRanking<String> ranking = new PeakLeaderboardRanking<>();
        ranking.set("a", "A", 10);
        ranking.set("b", "B", 20);
        ranking.set("c", "C", 20);
        ranking.set("d", "D", 5);

        List<PeakLeaderboardRanking.Placed<String>> window = ranking.window(3);

        assertEquals(List.of("b", "c", "a"), window.stream().map(placed -> placed.entry().key()).toList());
        assertEquals(List.of(0, 1, 2), window.stream().map(PeakLeaderboardRanking.Placed::rank).toList());
        assertEquals(3, ranking.rankOf("d"));
        assertEquals(-1, ranking.rankOf("missing"));

        ranking.add("c", 1);
        assertEquals("c", ranking.window(1).get(0).entry().key());
    }

    @Test
    void anAscendingRankingPutsTheLowestScoreFirst() {
        PeakLeaderboardRanking<String> ranking = new PeakLeaderboardRanking<String>().ascending(true);
        ranking.set("slow", "Slow", 90.5);
        ranking.set("fast", "Fast", 61.2);
        ranking.set("mid", "Mid", 75.0);

        assertEquals(List.of("fast", "mid", "slow"), ranking.window(5).stream().map(placed -> placed.entry().key()).toList());
    }

    @Test
    void theRankingCountsVersionsOnlyForRealChanges() {
        PeakLeaderboardRanking<String> ranking = new PeakLeaderboardRanking<>();
        ranking.set("a", "A", 1);
        long version = ranking.version();

        ranking.set("a", "A", 1);
        assertEquals(version, ranking.version());

        ranking.set("a", "A", 2);
        assertTrue(ranking.version() > version);

        long before = ranking.version();
        assertFalse(ranking.remove("missing"));
        assertEquals(before, ranking.version());
    }

    @Test
    void theDiffListsRemovedAddedAndChangedEntries() {
        PeakLeaderboardRanking<String> ranking = new PeakLeaderboardRanking<>();
        ranking.set("a", "A", 30);
        ranking.set("b", "B", 20);
        ranking.set("c", "C", 10);
        Map<String, PeakLeaderboardRanking.Placed<String>> shown = new HashMap<>();
        for (PeakLeaderboardRanking.Placed<String> placed : ranking.window(2)) shown.put(placed.entry().key(), placed);

        ranking.set("c", "C", 35);
        PeakLeaderboardRanking.Diff<String> diff = PeakLeaderboardRanking.diff(shown, ranking.window(2));

        assertEquals(List.of("b"), diff.removed());
        assertEquals(List.of("c"), diff.added().stream().map(placed -> placed.entry().key()).toList());
        assertEquals(List.of("a"), diff.changed().stream().map(placed -> placed.entry().key()).toList());
        assertFalse(PeakLeaderboardRanking.diff(shown, ranking.window(2)).isEmpty());
        assertTrue(PeakLeaderboardRanking.diff(Map.of(), List.of()).isEmpty());
    }

    @Test
    void theWindowSendsOnlyTheTopRowsPlusTheSpareRow() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.create("test:board", 3);
        PeakView view = open(board);
        for (int i = 0; i < 20; i++) board.set("p" + i, "P" + i, i);

        flush(board, view);

        PeakSortedList list = list(view, board);
        assertEquals(3, list.getMaxVisible());
        assertEquals(4, list.getChildren().size());
        assertTrue(lastOperations().stream().allMatch(operation -> operation instanceof PeakUiOperation.Add));
        Map<String, PeakElement<?>> rows = rowsByName(list);
        assertEquals(4.0, rows.get("P19").getSortKey());
        assertEquals(3.0, rows.get("P18").getSortKey());
        assertEquals(1.0, rows.get("P16").getSortKey());
        assertFalse(rows.containsKey("P15"));
    }

    @Test
    void unchangedWindowsSendNothingAndSmallChangesSendSmallPatches() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("test:board", 3).spare(0);
        PeakView view = open(board);
        board.set("a", "A", 30);
        board.set("b", "B", 20);
        board.set("c", "C", 10);
        flush(board, view);
        int packets = updatePackets();

        flush(board, view);
        assertEquals(packets, updatePackets());

        board.set("a", "A", 35);
        flush(board, view);

        assertEquals(packets + 1, updatePackets());
        List<PeakUiOperation> operations = lastOperations();
        assertEquals(1, operations.size());
        PeakUiOperation.Update update = assertInstanceOf(PeakUiOperation.Update.class, operations.get(0));
        assertTrue(update.elementId().endsWith("/score"));
        assertEquals("35", update.patch().get("text").getAsString());
    }

    @Test
    void anEntryThatOvertakesAnotherSwapsTheirSortKeysAndLabels() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("test:board", 2).spare(0);
        PeakView view = open(board);
        board.set("a", "A", 30);
        board.set("b", "B", 20);
        flush(board, view);

        board.set("b", "B", 40);
        flush(board, view);

        Map<String, PeakElement<?>> rows = rowsByName(list(view, board));
        assertEquals(2.0, rows.get("B").getSortKey());
        assertEquals(1.0, rows.get("A").getSortKey());
        assertEquals("#1", childLabel(rows.get("B"), "/rank").getText().getString());
        assertEquals("#2", childLabel(rows.get("A"), "/rank").getText().getString());
    }

    @Test
    void entriesLeavingTheWindowAreDeletedAndNewOnesGetFreshIds() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("test:board", 2).spare(0);
        PeakView view = open(board);
        board.set("a", "A", 30);
        board.set("b", "B", 20);
        flush(board, view);
        String idOfB = rowsByName(list(view, board)).get("B").getId();

        board.set("c", "C", 25);
        flush(board, view);

        PeakSortedList list = list(view, board);
        Map<String, PeakElement<?>> rows = rowsByName(list);
        assertEquals(2, list.getChildren().size());
        assertFalse(rows.containsKey("B"));
        assertNotNull(rows.get("C"));
        assertTrue(lastOperations().stream().anyMatch(operation ->
                operation instanceof PeakUiOperation.Delete delete && delete.elementId().equals(idOfB)));
        assertTrue(rows.get("C").getId().matches("test:board/row-\\d+"));
    }

    @Test
    void removingAnEntryPullsTheNextOneIntoTheWindow() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("test:board", 2).spare(0);
        PeakView view = open(board);
        board.set("a", "A", 30);
        board.set("b", "B", 20);
        board.set("c", "C", 10);
        flush(board, view);

        board.remove("a");
        flush(board, view);

        Map<String, PeakElement<?>> rows = rowsByName(list(view, board));
        assertEquals(2, rows.size());
        assertTrue(rows.containsKey("B"));
        assertTrue(rows.containsKey("C"));
        assertEquals(1, board.rankOf("c"));
    }

    @Test
    void callsFromAnotherThreadWaitForTheServerThread() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.create("test:board", 2);
        PeakView view = open(board);

        board.set("a", "A", 5);

        assertEquals(0, board.count());
        drain();
        assertEquals(1, board.count());
        flush(board, view);
        assertEquals(1, list(view, board).getChildren().size());
    }

    @Test
    void theOwnRowIsHiddenInsideTheWindowAndFilledPerPlayer() {
        UUID viewer = UUID.randomUUID();
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("test:board", 2)
                .ownRow(uuid -> uuid.equals(viewer) ? "me" : null);
        PeakDocument document = PeakDocument.create("test:own").add(board.element());

        PeakElement<?> self = document.findById(board.selfId());

        assertNotNull(self);
        assertEquals(1, self.getBindings().size());
        assertTrue(self.getBindings().containsKey("hidden"));
        assertTrue(self.getBindings().get("hidden").contains("test:board/self-hidden"));
        assertEquals("{test:board/self-rank#-}", childLabel(self, "/rank").getText().getString());
    }

    @Test
    void aroundReturnsTheNeighbourRanksAndClampsAtTheEnds() {
        PeakLeaderboardRanking<String> ranking = new PeakLeaderboardRanking<>();
        for (int i = 0; i < 10; i++) ranking.set("p" + i, "P" + i, 100 - i);

        assertEquals(List.of(3, 4, 5), ranking.around("p4", 1, 1).stream().map(PeakLeaderboardRanking.Placed::rank).toList());
        assertEquals(List.of(0, 1, 2), ranking.around("p0", 2, 2).stream().map(PeakLeaderboardRanking.Placed::rank).toList());
        assertEquals(List.of(8, 9), ranking.around("p9", 1, 3).stream().map(PeakLeaderboardRanking.Placed::rank).toList());
        assertTrue(ranking.around("missing", 1, 1).isEmpty());
    }

    @Test
    void theOwnBlockWithNeighboursHasOneRowPerSlotAndMarksTheOwnSlot() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("test:board", 3)
                .ownRow(uuid -> "me").neighbours(2).ownRowStyle(panel -> panel.background(0xFF123456));
        PeakDocument document = PeakDocument.create("test:own").add(board.element());

        PeakContainer<?> block = assertInstanceOf(PeakContainer.class, document.findById(board.selfId()));

        assertEquals(5, block.getChildren().size());
        assertTrue(block.getBindings().get("hidden").contains("test:board/self-hidden"));
        assertEquals("test:board/self/row-0", block.getChildren().get(0).getId());
        assertTrue(block.getChildren().get(0).getBindings().get("hidden").contains("test:board/self-gone-0"));
        assertTrue(block.getChildren().get(2).getBindings().isEmpty());
        assertEquals("{test:board/self-rank-2#-}", childLabel(block.getChildren().get(2), "/rank").getText().getString());
    }

    @Test
    void theOwnLinesListTheViewersRowAndItsNeighbours() {
        UUID viewer = UUID.randomUUID();
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.<String>create("test:board", 2)
                .ownRow(uuid -> uuid.equals(viewer) ? "me" : null).neighbours(1);
        for (int i = 0; i < 6; i++) board.ranking().set("p" + i, "P" + i, 100 - i * 10);
        board.ranking().set("me", "Me", 55);

        List<PeakLeaderboardRanking.Placed<String>> lines = board.ownLines(viewer);

        assertEquals(List.of(4, 5, 6), lines.stream().map(PeakLeaderboardRanking.Placed::rank).toList());
        assertEquals("me", lines.get(1).entry().key());
        assertTrue(board.ownLines(UUID.randomUUID()).isEmpty());

        board.ranking().set("me", "Me", 500);
        assertEquals(List.of(0, 1), board.ownLines(viewer).stream().map(PeakLeaderboardRanking.Placed::rank).toList());
    }

    @Test
    void theListFadesLeavingRowsByDefaultAndTheWindowCanChangeIt() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.create("test:board", 3);
        PeakDocument document = PeakDocument.create("test:exit").add(board.element());
        PeakSortedList list = assertInstanceOf(PeakSortedList.class, document.findById(board.listId()));
        assertEquals(PeakPopupAnimation.FADE, list.getExitAnimation());
        assertEquals(8, list.getExitDurationTicks());

        PeakLeaderboardWindow<String> custom = PeakLeaderboardWindow.<String>create("test:custom", 3)
                .exitAnimation(PeakPopupAnimation.NONE).exitDurationTicks(3);
        PeakSortedList customList = assertInstanceOf(PeakSortedList.class,
                PeakDocument.create("test:exit2").add(custom.element()).findById(custom.listId()));
        assertEquals(PeakPopupAnimation.NONE, customList.getExitAnimation());
        assertEquals(3, customList.getExitDurationTicks());
    }

    @Test
    void rowIdsAndKeysFollowTheIdConvention() {
        PeakLeaderboardWindow<String> board = PeakLeaderboardWindow.create("test:board", 2);

        assertEquals("test:board/list", board.listId());
        assertEquals("test:board/self", board.selfId());
    }
}
