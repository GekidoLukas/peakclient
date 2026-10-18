package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.ArrayList;
import java.util.List;

public final class PeakBarChart extends PeakElement<PeakBarChart> {
    private final List<PeakChartEntry> entries = new ArrayList<>();
    private double maxValue = 0;
    private boolean showValueLabels = true;
    private boolean showCategoryLabels = true;
    private PeakValueLabelPosition valueLabelPosition = PeakValueLabelPosition.OUTSIDE;
    private int barSpacing = 4;
    private int topPadding = 10;
    @PeakColorOrNone private PeakPaint backgroundColor = PeakPaint.NONE;
    @PeakOpaqueColor private PeakPaint categoryLabelColor = PeakPaint.rgb(170, 170, 170);
    @PeakOpaqueColor private PeakPaint valueLabelColor = PeakPaint.rgb(255, 255, 255);

    public PeakBarChart() { this(PeakId.random()); }
    public PeakBarChart(String id) {
        super(id);
        size(PeakSize.of(PeakSize.PeakDimension.pixels(160), PeakSize.PeakDimension.pixels(80)));
    }

    public static PeakBarChart create() { return new PeakBarChart(); }

    public List<PeakChartEntry> getEntries() { return entries; }
    public PeakBarChart addEntry(String label, double value, int color) {
        entries.add(PeakChartEntry.of(label, value, color));
        return this;
    }
    public PeakBarChart addEntry(String label, double value, PeakPaint color) {
        entries.add(PeakChartEntry.of(label, value, color));
        return this;
    }
    public PeakBarChart clearEntries() { entries.clear(); return this; }

    public double getMaxValue() { return maxValue; }
    public PeakBarChart maxValue(double maxValue) { this.maxValue = maxValue; return this; }
    public boolean isShowValueLabels() { return showValueLabels; }
    public PeakBarChart showValueLabels(boolean show) { this.showValueLabels = show; return this; }
    public boolean isShowCategoryLabels() { return showCategoryLabels; }
    public PeakBarChart showCategoryLabels(boolean show) { this.showCategoryLabels = show; return this; }
    public PeakValueLabelPosition getValueLabelPosition() { return valueLabelPosition; }
    public PeakBarChart valueLabelPosition(PeakValueLabelPosition position) { this.valueLabelPosition = position; return this; }
    public int getBarSpacing() { return barSpacing; }
    public PeakBarChart barSpacing(int spacing) { this.barSpacing = Math.max(0, spacing); return this; }
    public int getTopPadding() { return topPadding; }
    public PeakBarChart topPadding(int padding) { this.topPadding = Math.max(0, padding); return this; }
    public PeakPaint getBackgroundColor() { return backgroundColor; }
    public PeakBarChart backgroundColor(PeakPaint paint) { this.backgroundColor = paint.orNone(); return this; }
    public PeakBarChart backgroundColor(int argb) { return backgroundColor(PeakPaint.solid(argb)); }
    public PeakPaint getCategoryLabelColor() { return categoryLabelColor; }
    public PeakBarChart categoryLabelColor(PeakPaint paint) { this.categoryLabelColor = paint.opaque(); return this; }
    public PeakBarChart categoryLabelColor(int argb) { return categoryLabelColor(PeakPaint.solid(argb)); }
    public PeakPaint getValueLabelColor() { return valueLabelColor; }
    public PeakBarChart valueLabelColor(PeakPaint paint) { this.valueLabelColor = paint.opaque(); return this; }
    public PeakBarChart valueLabelColor(int argb) { return valueLabelColor(PeakPaint.solid(argb)); }

    @Override
    public String elementType() { return "bar_chart"; }
}
