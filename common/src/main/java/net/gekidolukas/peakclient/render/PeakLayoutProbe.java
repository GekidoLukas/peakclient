package net.gekidolukas.peakclient.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public final class PeakLayoutProbe {
    private final List<PeakLayoutEntry> entries = new ArrayList<>();
    private int[] lastIndexAtDepth = new int[16];

    public PeakLayoutProbe() {
        Arrays.fill(lastIndexAtDepth, -1);
    }

    public void record(PeakLayoutEntry entry) {
        int depth = Math.max(0, entry.depth());
        if (depth >= lastIndexAtDepth.length) {
            int previousLength = lastIndexAtDepth.length;
            lastIndexAtDepth = Arrays.copyOf(lastIndexAtDepth, Math.max(depth + 1, previousLength * 2));
            Arrays.fill(lastIndexAtDepth, previousLength, lastIndexAtDepth.length, -1);
        }
        int parent = depth > 1 ? lastIndexAtDepth[depth - 1] : -1;
        entries.add(entry.withParent(parent));
        lastIndexAtDepth[depth] = entries.size() - 1;
    }

    public List<PeakLayoutEntry> entries() {
        return entries;
    }

    public PeakLayoutEntry parentOf(PeakLayoutEntry entry) {
        int index = entry.parentIndex();
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    public List<PeakLayoutEntry> chainOf(PeakLayoutEntry entry) {
        List<PeakLayoutEntry> chain = new ArrayList<>();
        for (PeakLayoutEntry current = entry; current != null; current = parentOf(current)) chain.add(0, current);
        return chain;
    }

    public PeakLayoutEntry findAt(int mouseX, int mouseY) {
        return findAt(mouseX, mouseY, entry -> true);
    }

    public PeakLayoutEntry findAt(int mouseX, int mouseY, Predicate<PeakLayoutEntry> filter) {
        PeakLayoutEntry best = null;
        for (PeakLayoutEntry entry : entries) {
            if (!entry.containsVisible(mouseX, mouseY) || !filter.test(entry)) continue;
            best = entry;
        }
        return best;
    }
}
