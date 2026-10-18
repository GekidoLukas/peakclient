package net.gekidolukas.peakclient.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public final class PeakLeaderboardRanking<K> {
    public record Entry<K>(K key, String name, double score, long order) {}

    public record Placed<K>(Entry<K> entry, int rank) {}

    public record Diff<K>(List<K> removed, List<Placed<K>> added, List<Placed<K>> changed) {
        public boolean isEmpty() {
            return removed.isEmpty() && added.isEmpty() && changed.isEmpty();
        }
    }

    private final Map<K, Entry<K>> entries = new HashMap<>();
    private boolean ascending;
    private long nextOrder;
    private long version;

    public boolean isAscending() { return ascending; }

    public PeakLeaderboardRanking<K> ascending(boolean ascending) {
        if (this.ascending != ascending) version++;
        this.ascending = ascending;
        return this;
    }

    public long version() { return version; }

    public int size() { return entries.size(); }

    public boolean contains(K key) { return entries.containsKey(key); }

    public Entry<K> get(K key) { return entries.get(key); }

    public void set(K key, String name, double score) {
        Entry<K> existing = entries.get(key);
        if (existing != null && existing.score() == score && existing.name().equals(name)) return;
        long order = existing != null && existing.score() == score ? existing.order() : nextOrder++;
        entries.put(key, new Entry<>(key, name, score, order));
        version++;
    }

    public void set(K key, double score) {
        Entry<K> existing = entries.get(key);
        set(key, existing != null ? existing.name() : String.valueOf(key), score);
    }

    public void add(K key, double delta) {
        Entry<K> existing = entries.get(key);
        if (existing == null) set(key, String.valueOf(key), delta);
        else set(key, existing.name(), existing.score() + delta);
    }

    public void add(K key, String name, double delta) {
        Entry<K> existing = entries.get(key);
        set(key, name, existing == null ? delta : existing.score() + delta);
    }

    public boolean remove(K key) {
        if (entries.remove(key) == null) return false;
        version++;
        return true;
    }

    public void clear() {
        if (entries.isEmpty()) return;
        entries.clear();
        version++;
    }

    public int rankOf(K key) {
        Entry<K> entry = entries.get(key);
        if (entry == null) return -1;
        Comparator<Entry<K>> better = better();
        int rank = 0;
        for (Entry<K> other : entries.values()) {
            if (better.compare(other, entry) < 0) rank++;
        }
        return rank;
    }

    public List<Placed<K>> window(int count) {
        int limit = Math.max(0, Math.min(count, entries.size()));
        if (limit == 0) return List.of();
        Comparator<Entry<K>> better = better();
        PriorityQueue<Entry<K>> worstFirst = new PriorityQueue<>(limit + 1, better.reversed());
        for (Entry<K> entry : entries.values()) {
            if (worstFirst.size() < limit) {
                worstFirst.add(entry);
            } else if (better.compare(entry, worstFirst.peek()) < 0) {
                worstFirst.poll();
                worstFirst.add(entry);
            }
        }
        List<Entry<K>> ordered = new ArrayList<>(worstFirst);
        ordered.sort(better);
        List<Placed<K>> placed = new ArrayList<>(ordered.size());
        for (int rank = 0; rank < ordered.size(); rank++) {
            placed.add(new Placed<>(ordered.get(rank), rank));
        }
        return placed;
    }

    public List<Placed<K>> around(K key, int before, int after) {
        int rank = rankOf(key);
        if (rank < 0) return List.of();
        int from = Math.max(0, rank - Math.max(0, before));
        List<Placed<K>> upToLast = window(rank + Math.max(0, after) + 1);
        return List.copyOf(upToLast.subList(Math.min(from, upToLast.size()), upToLast.size()));
    }

    public static <K> Diff<K> diff(Map<K, Placed<K>> shown, List<Placed<K>> window) {
        Set<K> inWindow = new HashSet<>();
        List<Placed<K>> added = new ArrayList<>();
        List<Placed<K>> changed = new ArrayList<>();
        for (Placed<K> placed : window) {
            K key = placed.entry().key();
            inWindow.add(key);
            Placed<K> previous = shown.get(key);
            if (previous == null) {
                added.add(placed);
            } else if (previous.rank() != placed.rank()
                    || previous.entry().score() != placed.entry().score()
                    || !previous.entry().name().equals(placed.entry().name())) {
                changed.add(placed);
            }
        }
        List<K> removed = new ArrayList<>();
        for (K key : shown.keySet()) {
            if (!inWindow.contains(key)) removed.add(key);
        }
        return new Diff<>(removed, added, changed);
    }

    private Comparator<Entry<K>> better() {
        Comparator<Entry<K>> byScore = Comparator.comparingDouble(Entry::score);
        if (!ascending) byScore = byScore.reversed();
        return byScore.thenComparingLong(Entry::order);
    }
}
