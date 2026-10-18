package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakTableStyle;
import net.gekidolukas.peakclient.ui.PeakTrack;
import net.gekidolukas.peakclient.ui.PeakTrackStyle;

import java.util.Arrays;
import java.util.Map;

public final class PeakTablePatch extends PeakContainerPatch<PeakTablePatch> {
    public PeakTablePatch columns(PeakTrack... tracks) { return field("columns", Arrays.asList(tracks)); }
    public PeakTablePatch columns(String specification) { return field("columns", PeakTrack.parse(specification)); }
    public PeakTablePatch columns(int count) {
        PeakTrack[] tracks = new PeakTrack[Math.max(1, count)];
        for (int i = 0; i < tracks.length; i++) tracks[i] = PeakTrack.auto();
        return columns(tracks);
    }
    public PeakTablePatch rows(PeakTrack... tracks) { return field("rows", Arrays.asList(tracks)); }
    public PeakTablePatch rows(String specification) { return field("rows", PeakTrack.parse(specification)); }
    public PeakTablePatch rowHeight(PeakTrack track) { return field("rowHeight", track); }
    public PeakTablePatch spacing(int spacing) { return columnSpacing(spacing).rowSpacing(spacing); }
    public PeakTablePatch columnSpacing(int spacing) { data.addProperty("columnSpacing", Math.max(0, spacing)); return this; }
    public PeakTablePatch rowSpacing(int spacing) { data.addProperty("rowSpacing", Math.max(0, spacing)); return this; }
    public PeakTablePatch minColumnWidth(int width) { data.addProperty("minColumnWidth", Math.max(0, width)); return this; }
    public PeakTablePatch cellPadding(int all) { return field("cellPadding", PeakInsets.all(Math.max(0, all))); }
    public PeakTablePatch cellPadding(int vertical, int horizontal) {
        return field("cellPadding", PeakInsets.of(vertical, horizontal, vertical, horizontal));
    }
    public PeakTablePatch decor(PeakTableStyle style) { return field("decor", style); }
    public PeakTablePatch headerRows(int rows) { data.addProperty("decor.headerRows", Math.max(0, rows)); return this; }
    public PeakTablePatch headerBackground(int color) { data.addProperty("decor.headerBackground", color); return this; }
    public PeakTablePatch headerBackground(PeakPaint paint) {
        data.add("decor.headerBackground", PeakPaintAdapter.toTree(paint));
        return this;
    }
    public PeakTablePatch stripes(int first, int second) {
        data.addProperty("decor.stripeFirst", first);
        data.addProperty("decor.stripeSecond", second);
        return this;
    }
    public PeakTablePatch stripes(PeakPaint first, PeakPaint second) {
        data.add("decor.stripeFirst", PeakPaintAdapter.toTree(first));
        data.add("decor.stripeSecond", PeakPaintAdapter.toTree(second));
        return this;
    }
    public PeakTablePatch lines(int width, int color) {
        data.addProperty("decor.lineWidth", Math.max(0, width));
        data.addProperty("decor.lineColor", color);
        return this;
    }
    public PeakTablePatch lines(int width, PeakPaint paint) {
        data.addProperty("decor.lineWidth", Math.max(0, width));
        data.add("decor.lineColor", PeakPaintAdapter.toTree(paint));
        return this;
    }
    public PeakTablePatch linesAcrossSpans(boolean across) { data.addProperty("decor.linesAcrossSpans", across); return this; }
    public PeakTablePatch rowStyles(Map<Integer, PeakTrackStyle> styles) { return field("decor.rows", styles); }
    public PeakTablePatch columnStyles(Map<Integer, PeakTrackStyle> styles) { return field("decor.columns", styles); }
    public PeakTablePatch rowBackground(int row, int color) {
        return rowStyles(Map.of(Math.max(0, row), PeakTrackStyle.create().background(color)));
    }
    public PeakTablePatch rowBackground(int row, PeakPaint paint) {
        return rowStyles(Map.of(Math.max(0, row), PeakTrackStyle.create().background(paint)));
    }
    public PeakTablePatch columnBackground(int column, int color) {
        return columnStyles(Map.of(Math.max(0, column), PeakTrackStyle.create().background(color)));
    }
    public PeakTablePatch columnBackground(int column, PeakPaint paint) {
        return columnStyles(Map.of(Math.max(0, column), PeakTrackStyle.create().background(paint)));
    }
    public PeakTablePatch cellAlign(PeakAlign horizontal, PeakAlign vertical) {
        data.addProperty("cellAlign", horizontal.name());
        data.addProperty("cellVerticalAlign", vertical.name());
        return this;
    }

    @Override protected PeakTablePatch self() { return this; }
}
