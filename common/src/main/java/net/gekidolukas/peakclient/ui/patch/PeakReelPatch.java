package net.gekidolukas.peakclient.ui.patch;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakCarouselDirection;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakReelEasing;

public final class PeakReelPatch extends PeakContainerPatch<PeakReelPatch> {
    public PeakReelPatch spinTo(int index) { return spin(index, -1, -1); }
    public PeakReelPatch spinTo(int index, int ticks) { return spin(index, ticks, -1); }
    public PeakReelPatch spinTo(int index, int ticks, int minLoops) { return spin(index, ticks, minLoops); }

    public PeakReelPatch spinTo(String childId) { return spinToChild(childId, -1, -1); }
    public PeakReelPatch spinTo(String childId, int ticks) { return spinToChild(childId, ticks, -1); }
    public PeakReelPatch spinTo(String childId, int ticks, int minLoops) { return spinToChild(childId, ticks, minLoops); }
    public PeakReelPatch spinTo(PeakElement<?> child) { return spinToChild(child.getId(), -1, -1); }
    public PeakReelPatch spinTo(PeakElement<?> child, int ticks) { return spinToChild(child.getId(), ticks, -1); }
    public PeakReelPatch spinTo(PeakElement<?> child, int ticks, int minLoops) { return spinToChild(child.getId(), ticks, minLoops); }

    private PeakReelPatch spinToChild(String childId, int ticks, int loops) {
        JsonObject object = new JsonObject();
        object.addProperty("targetId", childId);
        if (ticks > 0) object.addProperty("ticks", ticks);
        if (loops >= 0) object.addProperty("loops", loops);
        data.add("spin", object);
        return this;
    }

    private PeakReelPatch spin(int index, int ticks, int loops) {
        JsonObject object = new JsonObject();
        object.addProperty("target", Math.max(0, index));
        if (ticks > 0) object.addProperty("ticks", ticks);
        if (loops >= 0) object.addProperty("loops", loops);
        data.add("spin", object);
        return this;
    }

    public PeakReelPatch jumpTo(int index) { data.addProperty("jump", Math.max(0, index)); return this; }
    public PeakReelPatch stop() { data.addProperty("stop", true); return this; }
    public PeakReelPatch spinOnStart(int index) { data.addProperty("autoSpinSlot", Math.max(0, index) + 1); return this; }
    public PeakReelPatch spinOnStart(String childId) { data.addProperty("autoSpinTargetId", childId); data.addProperty("autoSpinSlot", 0); return this; }
    public PeakReelPatch movement(PeakCarouselDirection movement) { data.addProperty("movement", movement.name()); return this; }
    public PeakReelPatch spinTicks(int ticks) { data.addProperty("spinTicks", Math.max(1, ticks)); return this; }
    public PeakReelPatch minLoops(int loops) { data.addProperty("minLoops", Math.max(0, loops)); return this; }
    public PeakReelPatch easing(PeakReelEasing easing) { data.addProperty("easing", easing.name()); return this; }
    public PeakReelPatch fadeStart(int pixels) { data.addProperty("fadeStart", Math.max(0, pixels)); return this; }
    public PeakReelPatch fadeEnd(int pixels) { data.addProperty("fadeEnd", Math.max(0, pixels)); return this; }
    public PeakReelPatch fade(int pixels) { return fadeStart(pixels).fadeEnd(pixels); }
    @Override protected PeakReelPatch self() { return this; }
}
