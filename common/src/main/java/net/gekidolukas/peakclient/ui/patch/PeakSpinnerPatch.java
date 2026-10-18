package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

public final class PeakSpinnerPatch extends PeakBasePatch<PeakSpinnerPatch> {
    @Override protected PeakSpinnerPatch self() { return this; }

    public PeakSpinnerPatch color(int argb) { data.addProperty("color", argb); return self(); }
    public PeakSpinnerPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return self(); }
    public PeakSpinnerPatch dots(int value) { data.addProperty("dots", value); return self(); }
    public PeakSpinnerPatch periodTicks(int value) { data.addProperty("periodTicks", value); return self(); }
}
