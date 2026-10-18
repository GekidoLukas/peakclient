package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakChartEntry;
import net.gekidolukas.peakclient.ui.PeakValueLabelPosition;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.List;

public final class PeakBarChartPatch extends PeakBasePatch<PeakBarChartPatch> {
    public PeakBarChartPatch entries(List<PeakChartEntry> entries) { data.add("entries", PeakUiSerializer.GSON.toJsonTree(entries)); return this; }
    public PeakBarChartPatch maxValue(double max) { data.addProperty("maxValue", max); return this; }
    public PeakBarChartPatch showValueLabels(boolean show) { data.addProperty("showValueLabels", show); return this; }
    public PeakBarChartPatch showCategoryLabels(boolean show) { data.addProperty("showCategoryLabels", show); return this; }
    public PeakBarChartPatch valueLabelPosition(PeakValueLabelPosition position) { data.addProperty("valueLabelPosition", position.name()); return this; }
    public PeakBarChartPatch barSpacing(int value) { data.addProperty("barSpacing", value); return this; }
    public PeakBarChartPatch topPadding(int value) { data.addProperty("topPadding", value); return this; }
    public PeakBarChartPatch backgroundColor(int argb) { data.addProperty("backgroundColor", argb); return this; }
    public PeakBarChartPatch backgroundColor(PeakPaint paint) { data.add("backgroundColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakBarChartPatch categoryLabelColor(int argb) { data.addProperty("categoryLabelColor", argb); return this; }
    public PeakBarChartPatch categoryLabelColor(PeakPaint paint) { data.add("categoryLabelColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakBarChartPatch valueLabelColor(int argb) { data.addProperty("valueLabelColor", argb); return this; }
    public PeakBarChartPatch valueLabelColor(PeakPaint paint) { data.add("valueLabelColor", PeakPaintAdapter.toTree(paint)); return this; }
    @Override protected PeakBarChartPatch self() { return this; }
}