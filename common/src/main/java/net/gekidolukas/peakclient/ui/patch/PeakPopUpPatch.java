package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPopupAnimation;

public final class PeakPopUpPatch extends PeakContainerPatch<PeakPopUpPatch> {
    public PeakPopUpPatch close() { data.addProperty("close", true); return this; }
    public PeakPopUpPatch animateEnter(boolean animate) { data.addProperty("animateEnter", animate); return this; }
    public PeakPopUpPatch animateExit(boolean animate) { data.addProperty("animateExit", animate); return this; }
    public PeakPopUpPatch enterAnimation(PeakPopupAnimation anim) { data.addProperty("enterAnimation", anim.name()); return this; }
    public PeakPopUpPatch exitAnimation(PeakPopupAnimation anim) { data.addProperty("exitAnimation", anim.name()); return this; }
    public PeakPopUpPatch enterDurationTicks(int ticks) { data.addProperty("enterDurationTicks", ticks); return this; }
    public PeakPopUpPatch exitDurationTicks(int ticks) { data.addProperty("exitDurationTicks", ticks); return this; }
    public PeakPopUpPatch timeToLiveTicks(int ticks) { data.addProperty("timeToLiveTicks", ticks); return this; }
    public PeakPopUpPatch autoRemoveOnExpire(boolean autoRemove) { data.addProperty("autoRemoveOnExpire", autoRemove); return this; }
    public PeakPopUpPatch slideDistance(int distance) { data.addProperty("slideDistance", distance); return this; }
    @Override protected PeakPopUpPatch self() { return this; }
}
