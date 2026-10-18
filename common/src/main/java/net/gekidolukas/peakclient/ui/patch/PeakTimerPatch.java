package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakTimerFormat;
import net.gekidolukas.peakclient.ui.PeakTimerMode;

public final class PeakTimerPatch extends PeakBasePatch<PeakTimerPatch> {
    public PeakTimerPatch value(long value) { data.addProperty("value", value); return this; }
    public PeakTimerPatch running(boolean running) { data.addProperty("running", running); return this; }
    public PeakTimerPatch mode(PeakTimerMode mode) { data.addProperty("mode", mode.name()); return this; }
    public PeakTimerPatch target(long target) { data.addProperty("target", target); return this; }
    public PeakTimerPatch format(PeakTimerFormat format) { data.addProperty("format", format.name()); return this; }
    public PeakTimerPatch prefix(String prefix) { data.addProperty("prefix", prefix); return this; }
    public PeakTimerPatch suffix(String suffix) { data.addProperty("suffix", suffix); return this; }
    public PeakTimerPatch color(int argb) { data.addProperty("color", argb); return this; }
    public PeakTimerPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakTimerPatch shadow(boolean shadow) { data.addProperty("shadow", shadow); return this; }
    @Override protected PeakTimerPatch self() { return this; }
}
