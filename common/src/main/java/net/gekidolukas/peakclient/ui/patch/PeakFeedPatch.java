package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPopupAnimation;

public final class PeakFeedPatch extends PeakContainerPatch<PeakFeedPatch> {
    public PeakFeedPatch entryLifetimeTicks(int ticks) { data.addProperty("entryLifetimeTicks", Math.max(0, ticks)); return this; }
    public PeakFeedPatch entryLifetimeSeconds(double seconds) { return entryLifetimeTicks((int) Math.round(seconds * 20.0)); }
    public PeakFeedPatch maxEntries(int count) { data.addProperty("maxEntries", Math.max(0, count)); return this; }
    public PeakFeedPatch enterAnimation(PeakPopupAnimation animation) { data.addProperty("enterAnimation", animation.name()); return this; }
    public PeakFeedPatch exitAnimation(PeakPopupAnimation animation) { data.addProperty("exitAnimation", animation.name()); return this; }
    public PeakFeedPatch enterDurationTicks(int ticks) { data.addProperty("enterDurationTicks", Math.max(0, ticks)); return this; }
    public PeakFeedPatch exitDurationTicks(int ticks) { data.addProperty("exitDurationTicks", Math.max(0, ticks)); return this; }
    public PeakFeedPatch slideDistance(int distance) { data.addProperty("slideDistance", distance); return this; }
    public PeakFeedPatch moveTicks(int ticks) { data.addProperty("moveTicks", Math.max(0, ticks)); return this; }
    public PeakFeedPatch newestFirst(boolean newestFirst) { data.addProperty("newestFirst", newestFirst); return this; }
    public PeakFeedPatch autoRemove(boolean autoRemove) { data.addProperty("autoRemove", autoRemove); return this; }
    @Override protected PeakFeedPatch self() { return this; }
}
