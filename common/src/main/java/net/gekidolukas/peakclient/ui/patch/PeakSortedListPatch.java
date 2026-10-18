package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakSortOrder;

public final class PeakSortedListPatch extends PeakContainerPatch<PeakSortedListPatch> {
    public PeakSortedListPatch sortOrder(PeakSortOrder order) { data.addProperty("sortOrder", order.name()); return this; }
    public PeakSortedListPatch maxVisible(int count) { data.addProperty("maxVisible", Math.max(0, count)); return this; }
    public PeakSortedListPatch moveTicks(int ticks) { data.addProperty("moveTicks", Math.max(0, ticks)); return this; }
    public PeakSortedListPatch enterAnimation(PeakPopupAnimation animation) { data.addProperty("enterAnimation", animation.name()); return this; }
    public PeakSortedListPatch enterDurationTicks(int ticks) { data.addProperty("enterDurationTicks", Math.max(0, ticks)); return this; }
    public PeakSortedListPatch exitAnimation(PeakPopupAnimation animation) { data.addProperty("exitAnimation", animation.name()); return this; }
    public PeakSortedListPatch exitDurationTicks(int ticks) { data.addProperty("exitDurationTicks", Math.max(0, ticks)); return this; }
    public PeakSortedListPatch slideDistance(int distance) { data.addProperty("slideDistance", distance); return this; }
    @Override protected PeakSortedListPatch self() { return this; }
}
