package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;

public final class PeakCyclerTimeline<T> {
    private static final int MAX_STEPS = 64;

    public enum Phase { ENTER, HOLD, EXIT }

    public record Visible<T>(T item, Phase phase, float progress) {}

    public record Config(double intervalTicks, double enterTicks, double exitTicks, PeakCycleOrder order,
                         boolean overlap, boolean paused) {}

    private final PeakCycleSequence<T> sequence;
    private T current;
    private double currentStart;
    private T previous;
    private double previousExitStart;
    private double lastNow = Double.NaN;
    private T pendingTarget;
    private boolean forceNext;

    public PeakCyclerTimeline() {
        this(new PeakCycleSequence<>());
    }

    public PeakCyclerTimeline(PeakCycleSequence<T> sequence) {
        this.sequence = sequence;
    }

    public void reset() {
        current = null;
        previous = null;
        pendingTarget = null;
        forceNext = false;
        lastNow = Double.NaN;
        sequence.reset();
    }

    public T getCurrentItem() {
        return current;
    }

    public void requestNext() {
        forceNext = true;
    }

    public void requestShow(T item) {
        pendingTarget = item;
        forceNext = true;
    }

    public List<Visible<T>> update(double now, List<T> items, Config config) {
        double delta = Double.isNaN(lastNow) ? 0.0 : Math.max(0.0, now - lastNow);
        lastNow = now;

        if (items.isEmpty()) {
            current = null;
            previous = null;
            pendingTarget = null;
            forceNext = false;
            return List.of();
        }

        if (config.paused() && current != null) {
            currentStart += delta;
            if (previous != null) previousExitStart += delta;
        }

        if (current == null || !contains(items, current)) {
            current = pick(items, config);
            currentStart = now;
            previous = null;
            forceNext = false;
        }
        if (previous != null && !contains(items, previous)) previous = null;

        if (!config.paused()) {
            if (forceNext) jump(now, config);
            advance(now, items, config);
        }
        return visible(now, items, config);
    }

    private void jump(double now, Config config) {
        forceNext = false;
        if (pendingTarget == current) {
            pendingTarget = null;
            return;
        }
        double exitStart = config.enterTicks() + config.intervalTicks();
        if (now - currentStart < exitStart) currentStart = now - exitStart;
    }

    private void advance(double now, List<T> items, Config config) {
        for (int step = 0; step < MAX_STEPS; step++) {
            if (items.size() == 1 && previous == null && pendingTarget == null) break;
            double elapsed = now - currentStart;
            if (config.overlap()) {
                double switchAt = config.enterTicks() + config.intervalTicks();
                if (elapsed < switchAt) break;
                previous = current;
                previousExitStart = currentStart + switchAt;
                current = pick(items, config);
                currentStart += switchAt;
            } else {
                double total = config.enterTicks() + config.intervalTicks() + config.exitTicks();
                if (elapsed < total) break;
                current = pick(items, config);
                currentStart += total;
            }
        }
        if (previous != null && now - previousExitStart >= config.exitTicks()) previous = null;
    }

    private T pick(List<T> items, Config config) {
        if (pendingTarget != null) {
            T target = pendingTarget;
            pendingTarget = null;
            if (contains(items, target)) return target;
        }
        T next = sequence.next(config.order(), items, current);
        return next == null ? items.get(0) : next;
    }

    private List<Visible<T>> visible(double now, List<T> items, Config config) {
        List<Visible<T>> result = new ArrayList<>(2);
        if (previous != null && config.exitTicks() > 0.0) {
            float progress = (float) Math.min(1.0, Math.max(0.0, (now - previousExitStart) / config.exitTicks()));
            result.add(new Visible<>(previous, Phase.EXIT, progress));
        }
        double elapsed = Math.max(0.0, now - currentStart);
        double enter = config.enterTicks();
        double holdEnd = enter + config.intervalTicks();
        if (elapsed < enter) {
            result.add(new Visible<>(current, Phase.ENTER, (float) (elapsed / enter)));
        } else if (!config.overlap() && elapsed >= holdEnd && config.exitTicks() > 0.0 && items.size() > 1) {
            float progress = (float) Math.min(1.0, (elapsed - holdEnd) / config.exitTicks());
            result.add(new Visible<>(current, Phase.EXIT, progress));
        } else {
            result.add(new Visible<>(current, Phase.HOLD, 1f));
        }
        return result;
    }

    private static <E> boolean contains(List<E> items, E target) {
        for (E item : items) {
            if (item == target) return true;
        }
        return false;
    }
}
