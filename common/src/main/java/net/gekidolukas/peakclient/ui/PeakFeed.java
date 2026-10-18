package net.gekidolukas.peakclient.ui;

public final class PeakFeed extends PeakContainer<PeakFeed> {
    private int entryLifetimeTicks = 100;
    private int maxEntries = 6;
    private PeakPopupAnimation enterAnimation = PeakPopupAnimation.FADE;
    private PeakPopupAnimation exitAnimation = PeakPopupAnimation.FADE;
    private int enterDurationTicks = 8;
    private int exitDurationTicks = 10;
    private int slideDistance = 30;
    private int moveTicks = 6;
    private boolean newestFirst = false;
    private boolean autoRemove = true;

    private transient PeakFeedModel<PeakElement<?>> model;
    private transient PeakMotionTracker<PeakElement<?>> motion;

    public PeakFeed() { this(PeakId.random()); }
    public PeakFeed(String id) { super(id); }
    public static PeakFeed create() { return new PeakFeed(); }
    public static PeakFeed create(String id) { return new PeakFeed(id); }

    public int getEntryLifetimeTicks() { return Math.max(0, entryLifetimeTicks); }
    public PeakFeed entryLifetimeTicks(int ticks) { this.entryLifetimeTicks = Math.max(0, ticks); return this; }
    public PeakFeed entryLifetimeSeconds(double seconds) { return entryLifetimeTicks((int) Math.round(seconds * 20.0)); }
    public int getMaxEntries() { return Math.max(0, maxEntries); }
    public PeakFeed maxEntries(int count) { this.maxEntries = Math.max(0, count); return this; }
    public PeakPopupAnimation getEnterAnimation() { return enterAnimation == null ? PeakPopupAnimation.FADE : enterAnimation; }
    public PeakFeed enterAnimation(PeakPopupAnimation animation) { this.enterAnimation = animation; return this; }
    public PeakPopupAnimation getExitAnimation() { return exitAnimation == null ? PeakPopupAnimation.FADE : exitAnimation; }
    public PeakFeed exitAnimation(PeakPopupAnimation animation) { this.exitAnimation = animation; return this; }
    public PeakFeed animations(PeakPopupAnimation enter, PeakPopupAnimation exit) {
        return enterAnimation(enter).exitAnimation(exit);
    }
    public int getEnterDurationTicks() { return Math.max(0, enterDurationTicks); }
    public PeakFeed enterDurationTicks(int ticks) { this.enterDurationTicks = Math.max(0, ticks); return this; }
    public int getExitDurationTicks() { return Math.max(0, exitDurationTicks); }
    public PeakFeed exitDurationTicks(int ticks) { this.exitDurationTicks = Math.max(0, ticks); return this; }
    public PeakFeed durations(int enterTicks, int exitTicks) { return enterDurationTicks(enterTicks).exitDurationTicks(exitTicks); }
    public int getSlideDistance() { return slideDistance; }
    public PeakFeed slideDistance(int distance) { this.slideDistance = distance; return this; }
    public int getMoveTicks() { return Math.max(0, moveTicks); }
    public PeakFeed moveTicks(int ticks) { this.moveTicks = Math.max(0, ticks); return this; }
    public boolean isNewestFirst() { return newestFirst; }
    public PeakFeed newestFirst(boolean newestFirst) { this.newestFirst = newestFirst; return this; }
    public boolean isAutoRemove() { return autoRemove; }
    public PeakFeed autoRemove(boolean autoRemove) { this.autoRemove = autoRemove; return this; }

    public PeakFeedModel<PeakElement<?>> model() {
        if (model == null) model = new PeakFeedModel<>();
        return model;
    }

    public PeakMotionTracker<PeakElement<?>> motion() {
        if (motion == null) motion = new PeakMotionTracker<>();
        return motion;
    }

    public PeakFeedModel.Config modelConfig() {
        double enter = getEnterAnimation() == PeakPopupAnimation.NONE ? 0.0 : getEnterDurationTicks();
        double exit = getExitAnimation() == PeakPopupAnimation.NONE ? 0.0 : getExitDurationTicks();
        return new PeakFeedModel.Config(getEntryLifetimeTicks(), enter, exit, getMaxEntries());
    }

    public void resetAnchor() {
        if (model != null) model.reset();
        if (motion != null) motion.reset();
    }

    @Override
    public String elementType() { return "feed"; }
}
