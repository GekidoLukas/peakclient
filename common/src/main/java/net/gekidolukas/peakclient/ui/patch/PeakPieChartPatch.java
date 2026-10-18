package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakChartEntry;
import net.gekidolukas.peakclient.ui.PeakLegendPosition;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.List;

public final class PeakPieChartPatch extends PeakBasePatch<PeakPieChartPatch> {
    public PeakPieChartPatch entries(List<PeakChartEntry> entries) { data.add("entries", PeakUiSerializer.GSON.toJsonTree(entries)); return this; }
    public PeakPieChartPatch legendPosition(PeakLegendPosition position) { data.addProperty("legendPosition", position.name()); return this; }
    public PeakPieChartPatch showLabels(boolean show) { data.addProperty("showLabels", show); return this; }
    @Override protected PeakPieChartPatch self() { return this; }
}
