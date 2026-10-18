package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakReaction;
import net.gekidolukas.peakclient.ui.PeakTimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class PeakEventRuntime {
    private static final Map<PeakElement<?>, ElementState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private PeakEventRuntime() {}

    private record Pending(PeakElement<?> element, PeakReaction reaction, Map<String, String> extra) {}

    private static final class ValueState {
        boolean has;
        String last;
    }

    private static final class ElementState {
        boolean started;
        long startTick;
        boolean appeared;
        Boolean hidden;
        Boolean timerFinished;
        final Map<PeakReaction, ValueState> values = new IdentityHashMap<>();
        final Map<PeakReaction, Long> nextInterval = new IdentityHashMap<>();
        final Map<PeakReaction, Boolean> delayed = new IdentityHashMap<>();
    }

    public static void evaluate(PeakContainer<?> root, PeakActionScope scope) {
        List<Pending> pending = new ArrayList<>();
        collect(root, PeakClientTickCounter.current(), pending);
        for (Pending item : pending) {
            PeakEventDispatch.fireReaction(scope, item.element(), item.reaction(), item.extra());
        }
    }

    public static void reset(PeakElement<?> element) {
        STATES.remove(element);
    }

    private static void collect(PeakElement<?> element, long now, List<Pending> pending) {
        if (!element.getEvents().isEmpty()) evaluateElement(element, now, pending);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : new ArrayList<>(container.getChildren())) collect(child, now, pending);
        }
    }

    private static void evaluateElement(PeakElement<?> element, long now, List<Pending> pending) {
        ElementState state = STATES.computeIfAbsent(element, key -> new ElementState());
        boolean firstEvaluation = !state.started;
        if (firstEvaluation) {
            state.started = true;
            state.startTick = now;
        }

        boolean hiddenNow = element.isHidden();
        boolean hiddenChanged = state.hidden != null && state.hidden != hiddenNow;
        state.hidden = hiddenNow;
        boolean appearNow = !state.appeared && !hiddenNow;
        if (appearNow) state.appeared = true;

        boolean timerFinished = element instanceof PeakTimer timer && timer.isFinished();
        boolean timerJustFinished = element instanceof PeakTimer && state.timerFinished != null
                && !state.timerFinished && timerFinished;
        state.timerFinished = element instanceof PeakTimer ? timerFinished : null;

        for (PeakReaction reaction : List.copyOf(element.getEvents())) {
            switch (reaction.getOn()) {
                case APPEAR -> {
                    if (appearNow) pending.add(new Pending(element, reaction, Map.of()));
                }
                case SHOW -> {
                    if (hiddenChanged && !hiddenNow) pending.add(new Pending(element, reaction, Map.of()));
                }
                case HIDE -> {
                    if (hiddenChanged && hiddenNow) pending.add(new Pending(element, reaction, Map.of()));
                }
                case TIMER_END -> {
                    if (timerJustFinished || (firstEvaluation && timerFinished && reaction.isFireOnStart())) {
                        pending.add(new Pending(element, reaction, Map.of()));
                    }
                }
                case DELAY -> {
                    if (!state.delayed.containsKey(reaction) && now - state.startTick >= reaction.getTicks()) {
                        state.delayed.put(reaction, Boolean.TRUE);
                        pending.add(new Pending(element, reaction, Map.of()));
                    }
                }
                case INTERVAL -> evaluateInterval(element, reaction, state, now, pending);
                case VALUE_CHANGE, VALUE_INCREASE, VALUE_DECREASE, VALUE_ABOVE, VALUE_BELOW ->
                        evaluateValue(element, reaction, state, pending);
                default -> { }
            }
        }
    }

    private static void evaluateInterval(PeakElement<?> element, PeakReaction reaction, ElementState state, long now,
                                         List<Pending> pending) {
        int ticks = reaction.getTicks();
        if (ticks <= 0) return;
        long next = state.nextInterval.computeIfAbsent(reaction, key -> state.startTick + ticks);
        if (now < next) return;
        pending.add(new Pending(element, reaction, Map.of()));
        state.nextInterval.put(reaction, next + ticks * (1 + (now - next) / ticks));
    }

    private static void evaluateValue(PeakElement<?> element, PeakReaction reaction, ElementState state, List<Pending> pending) {
        String value = PeakWatchValues.valueOf(element, reaction);
        if (value == null) return;
        ValueState values = state.values.computeIfAbsent(reaction, key -> new ValueState());
        if (!values.has) {
            values.has = true;
            values.last = value;
            if (reaction.isFireOnStart() && matchesAtStart(reaction, value)) {
                pending.add(new Pending(element, reaction, change("", value)));
            }
            return;
        }
        if (value.equals(values.last)) return;

        String old = values.last;
        values.last = value;
        Double before = PeakWatchValues.number(old);
        Double after = PeakWatchValues.number(value);
        boolean numeric = before != null && after != null;
        double threshold = reaction.getThreshold();
        boolean fire = switch (reaction.getOn()) {
            case VALUE_CHANGE -> true;
            case VALUE_INCREASE -> numeric && after > before;
            case VALUE_DECREASE -> numeric && after < before;
            case VALUE_ABOVE -> numeric && before <= threshold && after > threshold;
            case VALUE_BELOW -> numeric && before >= threshold && after < threshold;
            default -> false;
        };
        if (fire) pending.add(new Pending(element, reaction, change(old, value)));
    }

    private static boolean matchesAtStart(PeakReaction reaction, String value) {
        Double number = PeakWatchValues.number(value);
        return switch (reaction.getOn()) {
            case VALUE_CHANGE -> true;
            case VALUE_ABOVE -> number != null && number > reaction.getThreshold();
            case VALUE_BELOW -> number != null && number < reaction.getThreshold();
            default -> false;
        };
    }

    private static Map<String, String> change(String old, String value) {
        Map<String, String> extra = new LinkedHashMap<>();
        extra.put("old", old);
        extra.put("new", value);
        Double before = PeakWatchValues.number(old);
        Double after = PeakWatchValues.number(value);
        if (before != null && after != null) extra.put("delta", PeakWatchValues.format(after - before));
        return extra;
    }
}
