package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class PeakPieChart extends PeakElement<PeakPieChart> {
    private final List<PeakChartEntry> entries = new ArrayList<>();
    private float startAngle = 0f;
    private boolean showLabels = true;
    private int donutThickness = 0;
    @PeakColorOrNone private PeakPaint backgroundColor = PeakPaint.NONE;
    private PeakLegendPosition legendPosition = PeakLegendPosition.BOTTOM;

    public PeakPieChart() { this(PeakId.random()); }
    public PeakPieChart(String id) {
        super(id);
        size(PeakSize.of(PeakSize.PeakDimension.pixels(60), PeakSize.PeakDimension.pixels(60)));
    }

    public static PeakPieChart create() { return new PeakPieChart(); }

    public List<PeakChartEntry> getEntries() { return entries; }
    public PeakPieChart addEntry(String label, double value, int color) {
        entries.add(PeakChartEntry.of(label, value, color));
        return this;
    }
    public PeakPieChart addEntry(String label, double value, PeakPaint color) {
        entries.add(PeakChartEntry.of(label, value, color));
        return this;
    }

    public PeakPieChart addEntry(Component label, double value, int color) {
        entries.add(PeakChartEntry.of(label, value, color));
        return this;
    }

    public PeakPieChart addEntry(Component label, double value, PeakPaint color) {
        entries.add(PeakChartEntry.of(label, value, color));
        return this;
    }
    public PeakPieChart clearEntries() { entries.clear(); return this; }

    public float getStartAngle() { return startAngle; }
    public PeakPieChart startAngle(float angle) { this.startAngle = angle; return this; }
    public boolean isShowLabels() { return showLabels; }
    public PeakPieChart showLabels(boolean show) { this.showLabels = show; return this; }
    public int getDonutThickness() { return donutThickness; }
    public PeakPieChart donutThickness(int thickness) { this.donutThickness = Math.max(0, thickness); return this; }
    public PeakPaint getBackgroundColor() { return backgroundColor; }
    public PeakPieChart backgroundColor(PeakPaint paint) { this.backgroundColor = paint.orNone(); return this; }
    public PeakPieChart backgroundColor(int argb) { return backgroundColor(PeakPaint.solid(argb)); }
    public PeakLegendPosition getLegendPosition() { return legendPosition; }
    public PeakPieChart legendPosition(PeakLegendPosition position) { this.legendPosition = position; return this; }

    @Override
    public String elementType() { return "pie_chart"; }
}
