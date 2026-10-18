package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakProgressSource;

public final class PeakIconBarPatch extends PeakBasePatch<PeakIconBarPatch> {
    public PeakIconBarPatch source(PeakProgressSource source) { data.addProperty("source", source.name()); return this; }
    public PeakIconBarPatch value(double value) { data.addProperty("value", value); return this; }
    public PeakIconBarPatch maxValue(double max) { data.addProperty("maxValue", max); return this; }
    public PeakIconBarPatch maxIcons(int value) { data.addProperty("maxIcons", value); return this; }
    public PeakIconBarPatch useHalfIcons(boolean use) { data.addProperty("useHalfIcons", use); return this; }
    public PeakIconBarPatch iconSize(int value) { data.addProperty("iconSize", value); return this; }
    public PeakIconBarPatch iconSpacing(int value) { data.addProperty("iconSpacing", value); return this; }
    public PeakIconBarPatch fullIconTexture(String texture) { data.addProperty("fullIconTexture", texture); return this; }
    public PeakIconBarPatch halfIconTexture(String texture) { data.addProperty("halfIconTexture", texture); return this; }
    public PeakIconBarPatch emptyIconTexture(String texture) { data.addProperty("emptyIconTexture", texture); return this; }
    public PeakIconBarPatch emptyBehindFullAndHalf(boolean behind) { data.addProperty("emptyBehindFullAndHalf", behind); return this; }
    public PeakIconBarPatch hideIfEmpty(boolean hide) { data.addProperty("hideIfEmpty", hide); return this; }
    public PeakIconBarPatch hideIfFull(boolean hide) { data.addProperty("hideIfFull", hide); return this; }
    public PeakIconBarPatch fallbackFullColor(int argb) { data.addProperty("fallbackFullColor", argb); return this; }
    public PeakIconBarPatch fallbackFullColor(PeakPaint paint) { data.add("fallbackFullColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakIconBarPatch fallbackHalfColor(int argb) { data.addProperty("fallbackHalfColor", argb); return this; }
    public PeakIconBarPatch fallbackHalfColor(PeakPaint paint) { data.add("fallbackHalfColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakIconBarPatch fallbackEmptyColor(int argb) { data.addProperty("fallbackEmptyColor", argb); return this; }
    public PeakIconBarPatch fallbackEmptyColor(PeakPaint paint) { data.add("fallbackEmptyColor", PeakPaintAdapter.toTree(paint)); return this; }
    @Override protected PeakIconBarPatch self() { return this; }
}
