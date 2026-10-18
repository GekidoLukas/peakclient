package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakTooltipBackgroundStyle;

public final class PeakTooltipLabelPatch extends PeakBasePatch<PeakTooltipLabelPatch> {
    public PeakTooltipLabelPatch text(String text) { data.addProperty("text", text); return this; }
    public PeakTooltipLabelPatch translationKey(String key) { data.addProperty("translationKey", key); return this; }
    public PeakTooltipLabelPatch color(int argb) { data.addProperty("color", argb); return this; }
    public PeakTooltipLabelPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakTooltipLabelPatch borderColor(int argb) { data.addProperty("borderColor", argb); return this; }
    public PeakTooltipLabelPatch borderColor(PeakPaint paint) { data.add("borderColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakTooltipLabelPatch backgroundStyle(PeakTooltipBackgroundStyle style) { data.addProperty("backgroundStyle", style.name()); return this; }
    public PeakTooltipLabelPatch backgroundTexture(String texture) { data.addProperty("backgroundTexture", texture); return this; }
    public PeakTooltipLabelPatch tooltipPadding(int h, int v) {
        data.addProperty("tooltipPaddingHorizontal", h);
        data.addProperty("tooltipPaddingVertical", v);
        return this;
    }
    @Override protected PeakTooltipLabelPatch self() { return this; }
}
