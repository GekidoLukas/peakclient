package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakMotionTracker<T> {
    public record Frame(float position, float appear, boolean departed) {}

    public record Departed<T>(T item, float position) {}

    private static final class State {
        float from;
        float to;
        double moveStart;
        boolean visible;
        double appearStart;
        float lastPosition;
        boolean drawn;
    }

    private static final double LONG_AGO = -1.0e9;

    private final Map<T, State> states = new IdentityHashMap<>();
    private boolean primed;
    private boolean initialFrame;

    public void reset() {
        states.clear();
        primed = false;
        initialFrame = false;
    }

    public int trackedCount() {
        return states.size();
    }

    public List<Departed<T>> beginFrame(Collection<T> items) {
        Set<T> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        keep.addAll(items);
        List<Departed<T>> departed = new ArrayList<>();
        Iterator<Map.Entry<T, State>> iterator = states.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<T, State> entry = iterator.next();
            if (keep.contains(entry.getKey())) continue;
            if (entry.getValue().drawn) departed.add(new Departed<>(entry.getKey(), entry.getValue().lastPosition));
            iterator.remove();
        }
        initialFrame = !primed && !items.isEmpty();
        if (!items.isEmpty()) primed = true;
        return departed;
    }

    public Frame track(T item, float target, boolean visible, double now, double moveTicks, double appearTicks) {
        State state = states.get(item);
        if (state == null) {
            state = new State();
            state.from = target;
            state.to = target;
            state.moveStart = now;
            states.put(item, state);
        }

        float current = positionOf(state, now, moveTicks);
        if (target != state.to) {
            state.from = current;
            state.to = target;
            state.moveStart = now;
        }

        if (visible && !state.visible) state.appearStart = initialFrame ? LONG_AGO : now;
        boolean hiddenNow = !visible && state.visible && state.drawn;
        state.visible = visible;

        float appear = 0f;
        if (visible) {
            appear = appearTicks <= 0.0 ? 1f : (float) Math.min(1.0, Math.max(0.0, (now - state.appearStart) / appearTicks));
        }
        state.lastPosition = current;
        state.drawn = visible && appear > 0.001f;
        return new Frame(current, appear, hiddenNow);
    }

    private static float positionOf(State state, double now, double moveTicks) {
        if (moveTicks <= 0.0) return state.to;
        double t = Math.min(1.0, Math.max(0.0, (now - state.moveStart) / moveTicks));
        double eased = t * t * (3.0 - 2.0 * t);
        return (float) (state.from + (state.to - state.from) * eased);
    }
}
