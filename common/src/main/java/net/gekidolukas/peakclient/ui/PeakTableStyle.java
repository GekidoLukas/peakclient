package net.gekidolukas.peakclient.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class PeakTableStyle {
    public enum LineMode { NONE, ROWS, COLUMNS, BOTH }

    private int headerRows = 0;
    @PeakColorOrNone private PeakPaint headerBackground = PeakPaint.NONE;
    @PeakColorOrNone private PeakPaint stripeFirst = PeakPaint.NONE;
    @PeakColorOrNone private PeakPaint stripeSecond = PeakPaint.NONE;
    private int lineWidth = 0;
    @PeakColorOrNone private PeakPaint lineColor = PeakPaint.NONE;
    private LineMode lineMode = LineMode.BOTH;
    private boolean linesAcrossSpans = false;
    private Map<Integer, PeakTrackStyle> rows = new LinkedHashMap<>();
    private Map<Integer, PeakTrackStyle> columns = new LinkedHashMap<>();

    public static PeakTableStyle create() { return new PeakTableStyle(); }

    public int getHeaderRows() { return Math.max(0, headerRows); }
    public PeakTableStyle headerRows(int rows) { this.headerRows = Math.max(0, rows); return this; }

    public PeakPaint getHeaderBackground() { return headerBackground; }
    public PeakTableStyle headerBackground(PeakPaint paint) { this.headerBackground = paint.orNone(); return this; }
    public PeakTableStyle headerBackground(int argb) { return headerBackground(PeakPaint.solid(argb)); }

    public PeakPaint getStripeFirst() { return stripeFirst; }
    public PeakPaint getStripeSecond() { return stripeSecond; }
    public PeakTableStyle stripes(PeakPaint first, PeakPaint second) {
        this.stripeFirst = first.orNone();
        this.stripeSecond = second.orNone();
        return this;
    }
    public PeakTableStyle stripes(int first, int second) { return stripes(PeakPaint.solid(first), PeakPaint.solid(second)); }

    public int getLineWidth() { return Math.max(0, lineWidth); }
    public PeakPaint getLineColor() { return lineColor; }
    public LineMode getLineMode() { return lineMode == null ? LineMode.BOTH : lineMode; }
    public PeakTableStyle lines(int width, PeakPaint paint) {
        this.lineWidth = Math.max(0, width);
        this.lineColor = paint.orNone();
        return this;
    }
    public PeakTableStyle lines(int width, int argb) { return lines(width, PeakPaint.solid(argb)); }
    public PeakTableStyle lines(int width, PeakPaint paint, LineMode mode) {
        lines(width, paint);
        this.lineMode = mode == null ? LineMode.BOTH : mode;
        return this;
    }
    public PeakTableStyle lines(int width, int argb, LineMode mode) { return lines(width, PeakPaint.solid(argb), mode); }
    public PeakTableStyle lineMode(LineMode mode) { this.lineMode = mode == null ? LineMode.BOTH : mode; return this; }

    public boolean isLinesAcrossSpans() { return linesAcrossSpans; }
    public PeakTableStyle linesAcrossSpans(boolean across) { this.linesAcrossSpans = across; return this; }

    public PeakTableStyle row(int index, Consumer<PeakTrackStyle> customizer) {
        PeakTrackStyle style = new PeakTrackStyle();
        customizer.accept(style);
        mutableRows().put(Math.max(0, index), style);
        return this;
    }

    public PeakTableStyle column(int index, Consumer<PeakTrackStyle> customizer) {
        PeakTrackStyle style = new PeakTrackStyle();
        customizer.accept(style);
        mutableColumns().put(Math.max(0, index), style);
        return this;
    }

    public PeakTrackStyle getRowStyle(int index) { return rows == null ? null : rows.get(index); }
    public PeakTrackStyle getColumnStyle(int index) { return columns == null ? null : columns.get(index); }
    public Map<Integer, PeakTrackStyle> getRowStyles() { return rows == null ? Map.of() : rows; }
    public Map<Integer, PeakTrackStyle> getColumnStyles() { return columns == null ? Map.of() : columns; }

    private Map<Integer, PeakTrackStyle> mutableRows() {
        if (rows == null) rows = new LinkedHashMap<>();
        return rows;
    }

    private Map<Integer, PeakTrackStyle> mutableColumns() {
        if (columns == null) columns = new LinkedHashMap<>();
        return columns;
    }

    public boolean hasTrackStyles() {
        return !getRowStyles().isEmpty() || !getColumnStyles().isEmpty();
    }

    public boolean hasHeader() {
        return getHeaderRows() > 0 && !headerBackground.isNone();
    }

    public boolean hasStripes() {
        return !stripeFirst.isNone() || !stripeSecond.isNone();
    }

    public boolean hasLines() {
        return getLineWidth() > 0 && !lineColor.isNone() && getLineMode() != LineMode.NONE;
    }

    public boolean isActive() {
        return hasHeader() || hasStripes() || hasLines() || hasTrackStyles();
    }
}
