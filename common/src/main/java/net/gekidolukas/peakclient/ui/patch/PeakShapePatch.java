package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakShapeKind;

public final class PeakShapePatch extends PeakContainerPatch<PeakShapePatch> {
    public PeakShapePatch shape(PeakShapeKind shape) { data.addProperty("shape", shape.name()); return this; }
    public PeakShapePatch cornerRadius(int radius) { data.addProperty("cornerRadius", Math.max(0, radius)); return this; }
    public PeakShapePatch popOutTop(float fraction) { data.addProperty("popOutTop", clamp(fraction)); return this; }
    public PeakShapePatch popOutBottom(float fraction) { data.addProperty("popOutBottom", clamp(fraction)); return this; }
    public PeakShapePatch popOutLeft(float fraction) { data.addProperty("popOutLeft", clamp(fraction)); return this; }
    public PeakShapePatch popOutRight(float fraction) { data.addProperty("popOutRight", clamp(fraction)); return this; }
    public PeakShapePatch noPopOut() {
        return popOutTop(0f).popOutBottom(0f).popOutLeft(0f).popOutRight(0f);
    }

    private static float clamp(float fraction) {
        return Math.max(0f, Math.min(1f, fraction));
    }

    @Override protected PeakShapePatch self() { return this; }
}
