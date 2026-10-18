package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;

import java.util.HashSet;
import java.util.Set;

public final class PeakCycler extends PeakContainer<PeakCycler> {
    private int intervalTicks = 100;
    private PeakCycleOrder order = PeakCycleOrder.SEQUENTIAL;
    private PeakCyclerTransition transition = PeakCyclerTransition.SEQUENTIAL;
    private PeakPopupAnimation enterAnimation = PeakPopupAnimation.FADE;
    private PeakPopupAnimation exitAnimation = PeakPopupAnimation.FADE;
    private int enterDurationTicks = 10;
    private int exitDurationTicks = 10;
    private int slideDistance = 40;
    private boolean paused = false;

    private transient PeakCyclerTimeline<PeakElement<?>> timeline;
    private transient boolean nextRequested;
    private transient int requestedSlot;

    public PeakCycler() { this(PeakId.random()); }
    public PeakCycler(String id) { super(id); }
    public static PeakCycler create() { return new PeakCycler(); }
    public static PeakCycler create(String id) { return new PeakCycler(id); }

    public int getIntervalTicks() { return Math.max(1, intervalTicks); }
    public PeakCycler intervalTicks(int ticks) { this.intervalTicks = Math.max(1, ticks); return this; }
    public PeakCycler intervalSeconds(double seconds) { return intervalTicks((int) Math.round(seconds * 20.0)); }
    public PeakCycleOrder getOrder() { return order == null ? PeakCycleOrder.SEQUENTIAL : order; }
    public PeakCycler order(PeakCycleOrder order) { this.order = order; return this; }
    public PeakCyclerTransition getTransition() { return transition == null ? PeakCyclerTransition.SEQUENTIAL : transition; }
    public PeakCycler transition(PeakCyclerTransition transition) { this.transition = transition; return this; }
    public PeakPopupAnimation getEnterAnimation() { return enterAnimation == null ? PeakPopupAnimation.FADE : enterAnimation; }
    public PeakCycler enterAnimation(PeakPopupAnimation animation) { this.enterAnimation = animation; return this; }
    public PeakPopupAnimation getExitAnimation() { return exitAnimation == null ? PeakPopupAnimation.FADE : exitAnimation; }
    public PeakCycler exitAnimation(PeakPopupAnimation animation) { this.exitAnimation = animation; return this; }
    public PeakCycler animations(PeakPopupAnimation enter, PeakPopupAnimation exit) {
        return enterAnimation(enter).exitAnimation(exit);
    }
    public int getEnterDurationTicks() { return Math.max(0, enterDurationTicks); }
    public PeakCycler enterDurationTicks(int ticks) { this.enterDurationTicks = Math.max(0, ticks); return this; }
    public int getExitDurationTicks() { return Math.max(0, exitDurationTicks); }
    public PeakCycler exitDurationTicks(int ticks) { this.exitDurationTicks = Math.max(0, ticks); return this; }
    public PeakCycler durations(int enterTicks, int exitTicks) { return enterDurationTicks(enterTicks).exitDurationTicks(exitTicks); }
    public int getSlideDistance() { return slideDistance; }
    public PeakCycler slideDistance(int distance) { this.slideDistance = distance; return this; }
    public boolean isPaused() { return paused; }
    public PeakCycler paused(boolean paused) { this.paused = paused; return this; }

    public PeakCyclerTimeline<PeakElement<?>> timeline() {
        if (timeline == null) timeline = new PeakCyclerTimeline<>();
        return timeline;
    }

    public PeakCyclerTimeline.Config timelineConfig() {
        double enter = getEnterAnimation() == PeakPopupAnimation.NONE ? 0.0 : getEnterDurationTicks();
        double exit = getExitAnimation() == PeakPopupAnimation.NONE ? 0.0 : getExitDurationTicks();
        return new PeakCyclerTimeline.Config(getIntervalTicks(), enter, exit, getOrder(),
                getTransition() == PeakCyclerTransition.OVERLAP, paused);
    }

    public boolean takeNextRequest() {
        boolean requested = nextRequested;
        nextRequested = false;
        return requested;
    }

    public int takeIndexRequest() {
        int index = requestedSlot - 1;
        requestedSlot = 0;
        return index;
    }

    public void resetAnchor() {
        if (timeline != null) timeline.reset();
        nextRequested = false;
        requestedSlot = 0;
    }

    @Override
    public String elementType() { return "cycler"; }

    @Override
    protected Set<String> customPatchKeys() {
        Set<String> keys = new HashSet<>(super.customPatchKeys());
        keys.add("next");
        keys.add("index");
        return keys;
    }

    @Override
    protected void applyCustomPatch(String key, JsonElement value) {
        if (key.equals("next")) {
            if (value.getAsBoolean()) nextRequested = true;
            return;
        }
        if (key.equals("index")) {
            requestedSlot = Math.max(0, value.getAsInt()) + 1;
            return;
        }
        super.applyCustomPatch(key, value);
    }
}
