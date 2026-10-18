package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakFeedModel<T> {
    public enum Phase { ENTER, HOLD, EXIT }

    public record Config(double lifetimeTicks, double enterTicks, double exitTicks, int maxEntries) {}

    public record Entry<T>(T item, Phase phase, float progress, boolean expired) {}

    private static final class State {
        final double spawn;
        double exitStart = Double.NaN;

        State(double spawn) {
            this.spawn = spawn;
        }
    }

    private final Map<T, State> states = new IdentityHashMap<>();

    public void reset() {
        states.clear();
    }

    public List<Entry<T>> update(double now, List<T> items, Config config) {
        Set<T> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        keep.addAll(items);
        states.keySet().removeIf(item -> !keep.contains(item));
        for (T item : items) states.computeIfAbsent(item, key -> new State(now));

        if (config.lifetimeTicks() > 0.0) {
            for (T item : items) {
                State state = states.get(item);
                if (Double.isNaN(state.exitStart) && now >= state.spawn + config.enterTicks() + config.lifetimeTicks()) {
                    state.exitStart = now;
                }
            }
        }

        if (config.maxEntries() > 0) {
            int active = 0;
            for (T item : items) {
                if (Double.isNaN(states.get(item).exitStart)) active++;
            }
            int excess = active - config.maxEntries();
            for (T item : items) {
                if (excess <= 0) break;
                State state = states.get(item);
                if (Double.isNaN(state.exitStart)) {
                    state.exitStart = now;
                    excess--;
                }
            }
        }

        List<Entry<T>> entries = new ArrayList<>(items.size());
        for (T item : items) {
            State state = states.get(item);
            if (!Double.isNaN(state.exitStart)) {
                double elapsed = now - state.exitStart;
                if (config.exitTicks() <= 0.0 || elapsed >= config.exitTicks()) {
                    entries.add(new Entry<>(item, Phase.EXIT, 1f, true));
                } else {
                    entries.add(new Entry<>(item, Phase.EXIT, (float) (elapsed / config.exitTicks()), false));
                }
            } else if (config.enterTicks() > 0.0 && now - state.spawn < config.enterTicks()) {
                entries.add(new Entry<>(item, Phase.ENTER, (float) ((now - state.spawn) / config.enterTicks()), false));
            } else {
                entries.add(new Entry<>(item, Phase.HOLD, 1f, false));
            }
        }
        return entries;
    }
}
