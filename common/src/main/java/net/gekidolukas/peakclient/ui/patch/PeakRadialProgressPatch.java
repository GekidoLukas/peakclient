package net.gekidolukas.peakclient.ui.patch;

public final class PeakRadialProgressPatch extends PeakAbstractProgressPatch<PeakRadialProgressPatch> {
    public PeakRadialProgressPatch startAngle(float angle) { data.addProperty("startAngle", angle); return this; }
    public PeakRadialProgressPatch clockwise(boolean clockwise) { data.addProperty("clockwise", clockwise); return this; }
    public PeakRadialProgressPatch thickness(int thickness) { data.addProperty("thickness", thickness); return this; }
    public PeakRadialProgressPatch arcDegrees(float degrees) { data.addProperty("arcDegrees", degrees); return this; }
    @Override protected PeakRadialProgressPatch self() { return this; }
}
