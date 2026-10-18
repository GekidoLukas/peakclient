package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class PeakStagger extends PeakContainer<PeakStagger> {
    private PeakPopupAnimation enterAnimation = PeakPopupAnimation.FADE;
    private PeakPopupAnimation exitAnimation = PeakPopupAnimation.FADE;
    private int delayTicks = 4;
    private int enterDurationTicks = 10;
    private int exitDurationTicks = 10;
    private int startDelayTicks = 0;
    private int slideDistance = 24;
    private PeakCycleOrder order = PeakCycleOrder.SEQUENTIAL;
    private boolean reverse = false;
    private boolean autoPlay = true;

    private transient PeakStaggerTimeline timeline;
    private transient int[] ranks;
    private transient boolean playRequested;
    private transient boolean hideRequested;

    public PeakStagger() { this(PeakId.random()); }
    public PeakStagger(String id) { super(id); }
    public static PeakStagger create() { return new PeakStagger(); }
    public static PeakStagger create(String id) { return new PeakStagger(id); }

    public PeakPopupAnimation getEnterAnimation() { return enterAnimation == null ? PeakPopupAnimation.FADE : enterAnimation; }
    public PeakStagger enterAnimation(PeakPopupAnimation animation) { this.enterAnimation = animation; return this; }
    public PeakPopupAnimation getExitAnimation() { return exitAnimation == null ? PeakPopupAnimation.FADE : exitAnimation; }
    public PeakStagger exitAnimation(PeakPopupAnimation animation) { this.exitAnimation = animation; return this; }
    public PeakStagger animations(PeakPopupAnimation enter, PeakPopupAnimation exit) {
        return enterAnimation(enter).exitAnimation(exit);
    }
    public int getDelayTicks() { return Math.max(0, delayTicks); }
    public PeakStagger delayTicks(int ticks) { this.delayTicks = Math.max(0, ticks); return this; }
    public int getEnterDurationTicks() { return Math.max(0, enterDurationTicks); }
    public PeakStagger enterDurationTicks(int ticks) { this.enterDurationTicks = Math.max(0, ticks); return this; }
    public int getExitDurationTicks() { return Math.max(0, exitDurationTicks); }
    public PeakStagger exitDurationTicks(int ticks) { this.exitDurationTicks = Math.max(0, ticks); return this; }
    public PeakStagger durations(int enterTicks, int exitTicks) { return enterDurationTicks(enterTicks).exitDurationTicks(exitTicks); }
    public int getStartDelayTicks() { return Math.max(0, startDelayTicks); }
    public PeakStagger startDelayTicks(int ticks) { this.startDelayTicks = Math.max(0, ticks); return this; }
    public int getSlideDistance() { return slideDistance; }
    public PeakStagger slideDistance(int distance) { this.slideDistance = distance; return this; }
    public PeakCycleOrder getOrder() { return order == null ? PeakCycleOrder.SEQUENTIAL : order; }
    public PeakStagger order(PeakCycleOrder order) { this.order = order; return this; }
    public boolean isReverse() { return reverse; }
    public PeakStagger reverse(boolean reverse) { this.reverse = reverse; return this; }
    public boolean isAutoPlay() { return autoPlay; }
    public PeakStagger autoPlay(boolean autoPlay) { this.autoPlay = autoPlay; return this; }

    public PeakStaggerTimeline timeline() {
        if (timeline == null) timeline = new PeakStaggerTimeline();
        return timeline;
    }

    public PeakStaggerTimeline.Config timelineConfig() {
        double enter = getEnterAnimation() == PeakPopupAnimation.NONE ? 0.0 : getEnterDurationTicks();
        double exit = getExitAnimation() == PeakPopupAnimation.NONE ? 0.0 : getExitDurationTicks();
        return new PeakStaggerTimeline.Config(getDelayTicks(), enter, exit, getStartDelayTicks());
    }

    public int[] ranksFor(int count) {
        if (ranks == null || ranks.length != count) ranks = buildRanks(count);
        return ranks;
    }

    private int[] buildRanks(int count) {
        List<Integer> order = new ArrayList<>(count);
        for (int i = 0; i < count; i++) order.add(i);
        if (getOrder() == PeakCycleOrder.RANDOM) Collections.shuffle(order, new Random());
        else if (reverse) Collections.reverse(order);
        int[] result = new int[count];
        for (int i = 0; i < count; i++) result[i] = order.get(i);
        return result;
    }

    public boolean takePlayRequest() {
        boolean requested = playRequested;
        playRequested = false;
        if (requested) ranks = null;
        return requested;
    }

    public boolean takeHideRequest() {
        boolean requested = hideRequested;
        hideRequested = false;
        return requested;
    }

    public void resetAnchor() {
        if (timeline != null) timeline.reset();
        playRequested = false;
        hideRequested = false;
        ranks = null;
    }

    @Override
    public String elementType() { return "stagger"; }

    @Override
    protected Set<String> customPatchKeys() {
        Set<String> keys = new HashSet<>(super.customPatchKeys());
        keys.add("replay");
        keys.add("hide");
        return keys;
    }

    @Override
    protected void applyCustomPatch(String key, JsonElement value) {
        if (key.equals("replay")) {
            if (value.getAsBoolean()) playRequested = true;
            return;
        }
        if (key.equals("hide")) {
            if (value.getAsBoolean()) hideRequested = true;
            return;
        }
        super.applyCustomPatch(key, value);
    }
}
