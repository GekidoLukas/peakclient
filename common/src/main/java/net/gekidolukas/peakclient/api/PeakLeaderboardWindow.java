package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.PeakServerThread;
import net.gekidolukas.peakclient.helper.PeakTask;
import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.server.PeakScheduler;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.PeakDiv;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class PeakLeaderboardWindow<K> {
    public enum OwnRow { OUTSIDE_WINDOW, ALWAYS }

    private static final String NO_VALUE = "-";

    private final String id;
    private final int size;
    private final PeakLeaderboardRanking<K> ranking = new PeakLeaderboardRanking<>();
    private final PeakStandardLeaderboardRow<K> standardRow = PeakStandardLeaderboardRow.create();
    private final Map<K, PeakLeaderboardRanking.Placed<K>> shown = new HashMap<>();
    private final Map<K, String> rowIds = new HashMap<>();
    private final List<PeakTask> tasks = new ArrayList<>();

    private PeakLeaderboardRow<K> row = standardRow;
    private int spare = 1;
    private int intervalTicks = 10;
    private int moveTicks = 10;
    private int gap = 2;
    private PeakPopupAnimation enterAnimation = PeakPopupAnimation.FADE;
    private PeakPopupAnimation exitAnimation = PeakPopupAnimation.FADE;
    private int exitDurationTicks = 8;
    private Function<UUID, K> ownKeyOf;
    private OwnRow ownRow = OwnRow.OUTSIDE_WINDOW;
    private int neighbours;
    private Consumer<PeakPanel> ownStyle = panel -> panel.background(0xB02F5F9A);
    private final Map<UUID, Snapshot<K>> snapshots = new ConcurrentHashMap<>();

    private PeakDiv root;
    private PeakView view;
    private long rowCounter;
    private long flushedVersion = -1;
    private boolean closed;

    private PeakLeaderboardWindow(String id, int size) {
        this.id = PeakId.require(id, "Leaderboard");
        this.size = Math.max(1, size);
    }

    public static <K> PeakLeaderboardWindow<K> create(String id, int size) {
        return new PeakLeaderboardWindow<>(id, size);
    }

    public String getId() { return id; }

    public int getSize() { return size; }

    public String listId() { return id + "/list"; }

    public String selfId() { return id + "/self"; }

    public PeakLeaderboardWindow<K> ascending() {
        ranking.ascending(true);
        return this;
    }

    public PeakLeaderboardWindow<K> descending() {
        ranking.ascending(false);
        return this;
    }

    public PeakLeaderboardWindow<K> spare(int rows) {
        this.spare = Math.max(0, rows);
        return this;
    }

    public PeakLeaderboardWindow<K> intervalTicks(int ticks) {
        this.intervalTicks = Math.max(1, ticks);
        return this;
    }

    public PeakLeaderboardWindow<K> moveTicks(int ticks) {
        this.moveTicks = Math.max(0, ticks);
        return this;
    }

    public PeakLeaderboardWindow<K> gap(int gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public PeakLeaderboardWindow<K> enterAnimation(PeakPopupAnimation animation) {
        this.enterAnimation = animation;
        return this;
    }

    public PeakLeaderboardWindow<K> exitAnimation(PeakPopupAnimation animation) {
        this.exitAnimation = animation;
        return this;
    }

    public PeakLeaderboardWindow<K> exitDurationTicks(int ticks) {
        this.exitDurationTicks = Math.max(0, ticks);
        return this;
    }

    public PeakLeaderboardWindow<K> row(PeakLeaderboardRow<K> row) {
        this.row = row;
        return this;
    }

    public PeakLeaderboardWindow<K> rowStyle(Consumer<PeakPanel> style) {
        standardRow.style(style);
        return this;
    }

    public PeakLeaderboardWindow<K> columns(int rankWidth, int nameWidth, int scoreWidth) {
        standardRow.columns(rankWidth, nameWidth, scoreWidth);
        return this;
    }

    public PeakLeaderboardWindow<K> rankFormat(IntFunction<String> format) {
        standardRow.rankFormat(format);
        return this;
    }

    public PeakLeaderboardWindow<K> scoreFormat(DoubleFunction<String> format) {
        standardRow.scoreFormat(format);
        return this;
    }

    public PeakLeaderboardWindow<K> ownRow(Function<UUID, K> keyOf) {
        return ownRow(keyOf, OwnRow.OUTSIDE_WINDOW);
    }

    public PeakLeaderboardWindow<K> ownRow(Function<UUID, K> keyOf, OwnRow mode) {
        this.ownKeyOf = keyOf;
        this.ownRow = mode;
        return this;
    }

    public PeakLeaderboardWindow<K> neighbours(int count) {
        this.neighbours = Math.max(0, Math.min(5, count));
        return this;
    }

    public PeakLeaderboardWindow<K> ownRowStyle(Consumer<PeakPanel> style) {
        this.ownStyle = style;
        return this;
    }

    public synchronized PeakElement<?> element() {
        if (root != null) return root;
        PeakSortedList list = PeakSortedList.create(listId()).column().gap(gap).descending().maxVisible(size)
                .moveTicks(moveTicks).enterAnimation(enterAnimation).exitAnimation(exitAnimation)
                .exitDurationTicks(exitDurationTicks);
        root = PeakDiv.create(id).column().gap(gap);
        root.add(list);
        if (ownKeyOf != null) root.add(buildOwnRow());
        return root;
    }

    public PeakLeaderboardWindow<K> bind(PeakView boundView) {
        element();
        PeakServerThread.run(() -> {
            if (closed || view != null) return;
            view = boundView;
            PeakAudience audience = boundView.audience();
            tasks.add(audience.every(intervalTicks, this::flushNow));
            if (ownKeyOf != null) bindOwnRowValues(audience);
            flushNow();
        });
        return this;
    }

    public void set(K key, String name, double score) {
        PeakServerThread.run(() -> ranking.set(key, name, score));
    }

    public void set(K key, double score) {
        PeakServerThread.run(() -> ranking.set(key, score));
    }

    public void add(K key, double delta) {
        PeakServerThread.run(() -> ranking.add(key, delta));
    }

    public void add(K key, String name, double delta) {
        PeakServerThread.run(() -> ranking.add(key, name, delta));
    }

    public void remove(K key) {
        PeakServerThread.run(() -> ranking.remove(key));
    }

    public void clear() {
        PeakServerThread.run(ranking::clear);
    }

    public int rankOf(K key) { return ranking.rankOf(key); }

    public int count() { return ranking.size(); }

    public PeakLeaderboardRanking<K> ranking() { return ranking; }

    public void flush() {
        PeakServerThread.run(this::flushNow);
    }

    public void close() {
        PeakServerThread.run(() -> {
            closed = true;
            snapshots.clear();
            for (PeakTask task : tasks) task.stop();
            tasks.clear();
        });
    }

    private void flushNow() {
        if (view == null || view.isClosed() || closed) return;
        if (ranking.version() == flushedVersion) return;
        flushedVersion = ranking.version();

        int windowSize = size + spare;
        List<PeakLeaderboardRanking.Placed<K>> window = ranking.window(windowSize);
        PeakLeaderboardRanking.Diff<K> diff = PeakLeaderboardRanking.diff(shown, window);
        if (diff.isEmpty()) return;

        for (K key : diff.removed()) {
            String rowId = rowIds.remove(key);
            shown.remove(key);
            if (rowId != null) view.delete(rowId);
        }
        for (PeakLeaderboardRanking.Placed<K> placed : diff.added()) {
            String rowId = id + "/row-" + (rowCounter++);
            rowIds.put(placed.entry().key(), rowId);
            PeakElement<?> element = row.create(rowId, placed);
            element.sortKey(sortKey(placed.rank(), windowSize));
            view.add(listId(), element);
            shown.put(placed.entry().key(), placed);
        }
        for (PeakLeaderboardRanking.Placed<K> placed : diff.changed()) {
            K key = placed.entry().key();
            String rowId = rowIds.get(key);
            PeakLeaderboardRanking.Placed<K> previous = shown.get(key);
            if (previous.rank() != placed.rank()) {
                view.update(rowId, PeakPatch.panel().sortKey(sortKey(placed.rank(), windowSize)));
            }
            row.update(view, rowId, previous, placed);
            shown.put(key, placed);
        }
    }

    private static double sortKey(int rank, int windowSize) {
        return windowSize - rank;
    }

    private PeakValueKey<String> selfKey(String name) {
        return PeakValueKey.ofString(id + "/self-" + name);
    }

    private PeakValueKey<String> slotKey(String name, int slot) {
        return neighbours == 0 ? selfKey(name) : selfKey(name + "-" + slot);
    }

    private String slotId(int slot) {
        return neighbours == 0 ? selfId() : selfId() + "/row-" + slot;
    }

    private PeakElement<?> buildOwnRow() {
        if (neighbours == 0) {
            PeakPanel panel = slotPanel(0);
            panel.bind("hidden", selfKey("hidden").placeholder("true"));
            return panel;
        }
        PeakDiv block = PeakDiv.create(selfId()).column().gap(gap);
        block.bind("hidden", selfKey("hidden").placeholder("true"));
        for (int slot = 0; slot <= neighbours * 2; slot++) {
            PeakPanel panel = slotPanel(slot);
            if (slot != neighbours) panel.bind("hidden", slotKey("gone", slot).placeholder("true"));
            block.add(panel);
        }
        return block;
    }

    private PeakPanel slotPanel(int slot) {
        PeakPanel panel = standardRow.panel(slotId(slot), slotKey("rank", slot).placeholder(NO_VALUE),
                slotKey("name", slot).placeholder(NO_VALUE), slotKey("score", slot).placeholder(NO_VALUE));
        if (slot == neighbours) ownStyle.accept(panel);
        return panel;
    }

    private void bindOwnRowValues(PeakAudience audience) {
        for (int slot = 0; slot <= neighbours * 2; slot++) {
            int current = slot;
            tasks.add(audience.values().bindPerPlayer(slotKey("rank", slot),
                    uuid -> slotText(uuid, current, placed -> standardRow.getRankFormat().apply(placed.rank())), intervalTicks));
            tasks.add(audience.values().bindPerPlayer(slotKey("name", slot),
                    uuid -> slotText(uuid, current, placed -> placed.entry().name()), intervalTicks));
            tasks.add(audience.values().bindPerPlayer(slotKey("score", slot),
                    uuid -> slotText(uuid, current, placed -> standardRow.getScoreFormat().apply(placed.entry().score())), intervalTicks));
            if (neighbours > 0 && slot != neighbours) {
                tasks.add(audience.values().bindPerPlayer(slotKey("gone", slot),
                        uuid -> String.valueOf(slotPlaced(uuid, current) == null), intervalTicks));
            }
        }
        tasks.add(audience.values().bindPerPlayer(selfKey("hidden"), this::ownHidden, intervalTicks));
    }

    private record Snapshot<K>(long version, K key, int rank, List<PeakLeaderboardRanking.Placed<K>> lines) {}

    private Snapshot<K> snapshot(UUID uuid) {
        K key = ownKeyOf == null ? null : ownKeyOf.apply(uuid);
        long version = ranking.version();
        Snapshot<K> cached = snapshots.get(uuid);
        if (cached != null && cached.version() == version && Objects.equals(cached.key(), key)) return cached;
        Snapshot<K> fresh = compute(key, version);
        snapshots.put(uuid, fresh);
        return fresh;
    }

    private Snapshot<K> compute(K key, long version) {
        if (key == null || !ranking.contains(key)) return new Snapshot<>(version, key, -1, List.of());
        int rank = ranking.rankOf(key);
        List<PeakLeaderboardRanking.Placed<K>> lines = neighbours == 0
                ? List.of(new PeakLeaderboardRanking.Placed<>(ranking.get(key), rank))
                : ranking.around(key, neighbours, neighbours);
        return new Snapshot<>(version, key, rank, lines);
    }

    private PeakLeaderboardRanking.Placed<K> slotPlaced(UUID uuid, int slot) {
        Snapshot<K> snapshot = snapshot(uuid);
        if (snapshot.rank() < 0) return null;
        int wanted = snapshot.rank() + slot - neighbours;
        for (PeakLeaderboardRanking.Placed<K> placed : snapshot.lines()) {
            if (placed.rank() == wanted) return placed;
        }
        return null;
    }

    private String slotText(UUID uuid, int slot, Function<PeakLeaderboardRanking.Placed<K>, String> formatter) {
        PeakLeaderboardRanking.Placed<K> placed = slotPlaced(uuid, slot);
        return placed == null ? NO_VALUE : formatter.apply(placed);
    }

    public List<PeakLeaderboardRanking.Placed<K>> ownLines(UUID uuid) {
        return snapshot(uuid).lines();
    }

    private String ownHidden(UUID uuid) {
        Snapshot<K> snapshot = snapshot(uuid);
        if (snapshot.rank() < 0) return "true";
        if (ownRow == OwnRow.ALWAYS) return "false";
        return String.valueOf(snapshot.rank() < size);
    }
}
