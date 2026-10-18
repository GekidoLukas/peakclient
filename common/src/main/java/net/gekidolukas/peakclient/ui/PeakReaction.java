package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PeakReaction {
    private PeakEvent on = PeakEvent.CLICK;
    private List<PeakAction> actions = new ArrayList<>();
    private String watch;
    private Double threshold;
    private Integer ticks;
    private Integer cooldownTicks;
    private Boolean once;
    private Boolean fireOnStart;

    public static PeakReaction of(PeakEvent event) {
        PeakReaction reaction = new PeakReaction();
        reaction.on = event;
        return reaction;
    }

    public PeakReaction then(PeakAction... actions) {
        if (this.actions == null) this.actions = new ArrayList<>();
        this.actions.addAll(Arrays.asList(actions));
        return this;
    }

    public PeakReaction watch(String expression) {
        this.watch = expression == null || expression.isEmpty() ? null : expression;
        return this;
    }

    public PeakReaction threshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public PeakReaction ticks(int ticks) {
        this.ticks = Math.max(0, ticks);
        return this;
    }

    public PeakReaction cooldown(int ticks) {
        this.cooldownTicks = ticks > 0 ? ticks : null;
        return this;
    }

    public PeakReaction once() {
        this.once = Boolean.TRUE;
        return this;
    }

    public PeakReaction fireOnStart() {
        this.fireOnStart = Boolean.TRUE;
        return this;
    }

    public PeakEvent getOn() { return on == null ? PeakEvent.CLICK : on; }
    public List<PeakAction> getActions() { return actions == null ? List.of() : actions; }
    public String getWatch() { return watch == null ? "" : watch; }
    public double getThreshold() { return threshold == null ? 0.0 : threshold; }
    public int getTicks() { return ticks == null ? 0 : Math.max(0, ticks); }
    public int getCooldownTicks() { return cooldownTicks == null ? 0 : Math.max(0, cooldownTicks); }
    public boolean isOnce() { return Boolean.TRUE.equals(once); }
    public boolean isFireOnStart() { return Boolean.TRUE.equals(fireOnStart); }
}
