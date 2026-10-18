package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakProgressSource;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

public abstract class PeakAbstractProgressPatch<SELF extends PeakAbstractProgressPatch<SELF>> extends PeakBasePatch<SELF> {
    public SELF value(double value) { data.addProperty("value", value); return self(); }
    public SELF maxValue(double max) { data.addProperty("maxValue", max); return self(); }
    public SELF source(PeakProgressSource source) { data.addProperty("source", source.name()); return self(); }
    public SELF autoIncrementPerTick(float perTick) { data.addProperty("autoIncrementPerTick", perTick); return self(); }
    public SELF transitionTicks(int value) { data.addProperty("transitionTicks", value); return self(); }
    public SELF backgroundColor(int argb) { data.addProperty("backgroundColor", argb); return self(); }
    public SELF backgroundColor(PeakPaint paint) { data.add("backgroundColor", PeakPaintAdapter.toTree(paint)); return self(); }
    public SELF fillColor(int argb) { data.addProperty("fillColor", argb); return self(); }
    public SELF fillColor(PeakPaint paint) { data.add("fillColor", PeakPaintAdapter.toTree(paint)); return self(); }
    public SELF backgroundTexture(String texture) { data.addProperty("backgroundTexture", texture); return self(); }
    public SELF fillTexture(String texture) { data.addProperty("fillTexture", texture); return self(); }
    public SELF hideIfFull(boolean hide) { data.addProperty("hideIfFull", hide); return self(); }
    public SELF hideIfEmpty(boolean hide) { data.addProperty("hideIfEmpty", hide); return self(); }
}
