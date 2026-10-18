package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakReaction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class PeakEventDispatch {
    static final int DATA_MIN_COOLDOWN_TICKS = 2;

    private static final Map<PeakReaction, FireState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private PeakEventDispatch() {}

    private static final class FireState {
        long lastFire = Long.MIN_VALUE;
        boolean fired;
    }

    public static void fire(PeakActionScope scope, PeakElement<?> element, PeakEvent event, Map<String, String> extra) {
        for (PeakReaction reaction : List.copyOf(element.getEvents())) {
            if (reaction.getOn() == event) fireReaction(scope, element, reaction, extra);
        }
    }

    static boolean fireReaction(PeakActionScope scope, PeakElement<?> element, PeakReaction reaction, Map<String, String> extra) {
        if (reaction.getActions().isEmpty() || !permit(reaction)) return false;
        PeakActionExecutor.run(scope, element.getId(), reaction.getActions(), extra);
        return true;
    }

    private static boolean permit(PeakReaction reaction) {
        long now = PeakClientTickCounter.current();
        FireState state = STATES.computeIfAbsent(reaction, key -> new FireState());
        if (reaction.isOnce() && state.fired) return false;
        int cooldown = Math.max(reaction.getCooldownTicks(), reaction.getOn().isData() ? DATA_MIN_COOLDOWN_TICKS : 0);
        if (cooldown > 0 && state.lastFire != Long.MIN_VALUE && now - state.lastFire < cooldown) return false;
        state.fired = true;
        state.lastFire = now;
        return true;
    }

    static void clear() {
        STATES.clear();
    }
}
