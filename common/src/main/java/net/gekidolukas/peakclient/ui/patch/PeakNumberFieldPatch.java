package net.gekidolukas.peakclient.ui.patch;

public final class PeakNumberFieldPatch extends PeakBasePatch<PeakNumberFieldPatch> {
    @Override protected PeakNumberFieldPatch self() { return this; }

    public PeakNumberFieldPatch value(double value) { data.addProperty("value", value); return self(); }
    public PeakNumberFieldPatch min(double min) { data.addProperty("min", min); return self(); }
    public PeakNumberFieldPatch max(double max) { data.addProperty("max", max); return self(); }
    public PeakNumberFieldPatch step(double step) { data.addProperty("step", step); return self(); }
    public PeakNumberFieldPatch decimals(int decimals) { data.addProperty("decimals", decimals); return self(); }
    public PeakNumberFieldPatch showButtons(boolean show) { data.addProperty("showButtons", show); return self(); }
}
