package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakOption;

import java.util.Arrays;

public final class PeakRadioGroupPatch extends PeakBasePatch<PeakRadioGroupPatch> {
    @Override protected PeakRadioGroupPatch self() { return this; }

    public PeakRadioGroupPatch selected(String value) { data.addProperty("selected", value); return self(); }
    public PeakRadioGroupPatch options(PeakOption... options) { return field("options", Arrays.asList(options)); }
    public PeakRadioGroupPatch gap(int value) { data.addProperty("gap", value); return self(); }
    public PeakRadioGroupPatch color(int argb) { data.addProperty("color", argb); return self(); }
    public PeakRadioGroupPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return self(); }
}
