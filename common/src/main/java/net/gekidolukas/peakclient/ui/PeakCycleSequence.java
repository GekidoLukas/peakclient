package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PeakCycleSequence<T> {
    private final Random random;
    private final List<T> bag = new ArrayList<>();

    public PeakCycleSequence() {
        this(new Random());
    }

    public PeakCycleSequence(Random random) {
        this.random = random;
    }

    public T next(PeakCycleOrder order, List<T> items, T current) {
        if (items.isEmpty()) {
            bag.clear();
            return null;
        }
        if (items.size() == 1) return items.get(0);
        if (order == PeakCycleOrder.RANDOM) return nextRandom(items, current);
        return nextSequential(items, current);
    }

    public void reset() {
        bag.clear();
    }

    private T nextSequential(List<T> items, T current) {
        int index = indexOf(items, current);
        return items.get(index < 0 ? 0 : (index + 1) % items.size());
    }

    private T nextRandom(List<T> items, T current) {
        bag.removeIf(candidate -> indexOf(items, candidate) < 0);
        if (bag.isEmpty()) refill(items, current);
        return bag.remove(0);
    }

    private void refill(List<T> items, T current) {
        bag.addAll(items);
        for (int i = bag.size() - 1; i > 0; i--) {
            int swap = random.nextInt(i + 1);
            T held = bag.get(i);
            bag.set(i, bag.get(swap));
            bag.set(swap, held);
        }
        if (current != null && bag.get(0) == current) {
            int swap = 1 + random.nextInt(bag.size() - 1);
            T held = bag.get(0);
            bag.set(0, bag.get(swap));
            bag.set(swap, held);
        }
    }

    private static <E> int indexOf(List<E> items, E target) {
        if (target == null) return -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == target) return i;
        }
        return -1;
    }
}
