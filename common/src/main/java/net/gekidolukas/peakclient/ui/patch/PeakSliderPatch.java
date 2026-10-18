package net.gekidolukas.peakclient.ui.patch;

public final class PeakSliderPatch extends PeakBasePatch<PeakSliderPatch> {
    @Override protected PeakSliderPatch self() { return this; }

    public PeakSliderPatch value(double value) { data.addProperty("value", value); return self(); }
    public PeakSliderPatch min(double min) { data.addProperty("min", min); return self(); }
    public PeakSliderPatch max(double max) { data.addProperty("max", max); return self(); }
    public PeakSliderPatch step(double step) { data.addProperty("step", step); return self(); }
    public PeakSliderPatch showValue(boolean showValue) { data.addProperty("showValue", showValue); return self(); }
}
