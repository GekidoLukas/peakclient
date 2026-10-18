package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakCycleOrder;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;

public final class PeakStaggerPatch extends PeakContainerPatch<PeakStaggerPatch> {
    public PeakStaggerPatch replay() { data.addProperty("replay", true); return this; }
    public PeakStaggerPatch hide() { data.addProperty("hide", true); return this; }
    public PeakStaggerPatch enterAnimation(PeakPopupAnimation animation) { data.addProperty("enterAnimation", animation.name()); return this; }
    public PeakStaggerPatch exitAnimation(PeakPopupAnimation animation) { data.addProperty("exitAnimation", animation.name()); return this; }
    public PeakStaggerPatch delayTicks(int ticks) { data.addProperty("delayTicks", Math.max(0, ticks)); return this; }
    public PeakStaggerPatch enterDurationTicks(int ticks) { data.addProperty("enterDurationTicks", Math.max(0, ticks)); return this; }
    public PeakStaggerPatch exitDurationTicks(int ticks) { data.addProperty("exitDurationTicks", Math.max(0, ticks)); return this; }
    public PeakStaggerPatch startDelayTicks(int ticks) { data.addProperty("startDelayTicks", Math.max(0, ticks)); return this; }
    public PeakStaggerPatch slideDistance(int distance) { data.addProperty("slideDistance", distance); return this; }
    public PeakStaggerPatch order(PeakCycleOrder order) { data.addProperty("order", order.name()); return this; }
    public PeakStaggerPatch reverse(boolean reverse) { data.addProperty("reverse", reverse); return this; }
    @Override protected PeakStaggerPatch self() { return this; }
}
