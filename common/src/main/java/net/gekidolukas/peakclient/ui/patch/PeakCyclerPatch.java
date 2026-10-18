package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakCycleOrder;
import net.gekidolukas.peakclient.ui.PeakCyclerTransition;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;

public final class PeakCyclerPatch extends PeakContainerPatch<PeakCyclerPatch> {
    public PeakCyclerPatch next() { data.addProperty("next", true); return this; }
    public PeakCyclerPatch showIndex(int index) { data.addProperty("index", Math.max(0, index)); return this; }
    public PeakCyclerPatch paused(boolean paused) { data.addProperty("paused", paused); return this; }
    public PeakCyclerPatch intervalTicks(int ticks) { data.addProperty("intervalTicks", Math.max(1, ticks)); return this; }
    public PeakCyclerPatch intervalSeconds(double seconds) { return intervalTicks((int) Math.round(seconds * 20.0)); }
    public PeakCyclerPatch order(PeakCycleOrder order) { data.addProperty("order", order.name()); return this; }
    public PeakCyclerPatch transition(PeakCyclerTransition transition) { data.addProperty("transition", transition.name()); return this; }
    public PeakCyclerPatch enterAnimation(PeakPopupAnimation animation) { data.addProperty("enterAnimation", animation.name()); return this; }
    public PeakCyclerPatch exitAnimation(PeakPopupAnimation animation) { data.addProperty("exitAnimation", animation.name()); return this; }
    public PeakCyclerPatch enterDurationTicks(int ticks) { data.addProperty("enterDurationTicks", Math.max(0, ticks)); return this; }
    public PeakCyclerPatch exitDurationTicks(int ticks) { data.addProperty("exitDurationTicks", Math.max(0, ticks)); return this; }
    public PeakCyclerPatch slideDistance(int distance) { data.addProperty("slideDistance", distance); return this; }
    @Override protected PeakCyclerPatch self() { return this; }
}
