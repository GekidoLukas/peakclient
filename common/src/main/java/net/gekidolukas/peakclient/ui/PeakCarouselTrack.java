package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class PeakCarouselTrack<T> {
    private static final int MAX_QUEUE = 512;

    public record Placement<T>(T item, float start, int length) {}

    private final PeakCycleSequence<T> sequence;
    private final List<T> queue = new ArrayList<>();
    private float offset;

    public PeakCarouselTrack() {
        this(new PeakCycleSequence<>());
    }

    public PeakCarouselTrack(PeakCycleSequence<T> sequence) {
        this.sequence = sequence;
    }

    public void reset() {
        queue.clear();
        sequence.reset();
        offset = 0f;
    }

    public List<T> queued() {
        return List.copyOf(queue);
    }

    public List<Placement<T>> step(List<T> items, ToIntFunction<T> lengthOf, int gap, float viewportLength,
                                   PeakCycleOrder order, float advance) {
        if (items.isEmpty()) {
            queue.clear();
            offset = 0f;
            return List.of();
        }

        purgeMissing(items, lengthOf, gap);
        offset -= Math.max(0f, advance);

        while (!queue.isEmpty()) {
            int firstLength = Math.max(1, lengthOf.applyAsInt(queue.get(0)));
            if (offset + firstLength > 0f) break;
            offset += firstLength + gap;
            queue.remove(0);
        }

        if (queue.isEmpty()) {
            offset = 0f;
            queue.add(sequence.next(order, items, null));
        }

        float end = offset;
        for (T item : queue) end += Math.max(1, lengthOf.applyAsInt(item)) + gap;
        while (end - gap < viewportLength && queue.size() < MAX_QUEUE) {
            T last = queue.get(queue.size() - 1);
            T next = sequence.next(order, items, last);
            queue.add(next);
            end += Math.max(1, lengthOf.applyAsInt(next)) + gap;
        }

        List<Placement<T>> placements = new ArrayList<>(queue.size());
        float cursor = offset;
        for (T item : queue) {
            int length = Math.max(1, lengthOf.applyAsInt(item));
            placements.add(new Placement<>(item, cursor, length));
            cursor += length + gap;
        }
        return placements;
    }

    private void purgeMissing(List<T> items, ToIntFunction<T> lengthOf, int gap) {
        for (int i = 0; i < queue.size(); ) {
            T item = queue.get(i);
            if (containsIdentity(items, item)) {
                i++;
                continue;
            }
            if (i == 0) offset += Math.max(1, lengthOf.applyAsInt(item)) + gap;
            queue.remove(i);
        }
    }

    private static <E> boolean containsIdentity(List<E> items, E target) {
        for (E item : items) {
            if (item == target) return true;
        }
        return false;
    }
}
