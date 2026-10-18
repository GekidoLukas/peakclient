package net.gekidolukas.peakclient.ui;

public final class PeakTrackStyle {
    @PeakColorOrNone private PeakPaint background = PeakPaint.NONE;
    private int lineWidth = -1;
    @PeakColorOrNone private PeakPaint lineColor = PeakPaint.NONE;

    public static PeakTrackStyle create() { return new PeakTrackStyle(); }

    public PeakPaint getBackground() { return background; }
    public PeakTrackStyle background(PeakPaint paint) { this.background = paint.orNone(); return this; }
    public PeakTrackStyle background(int argb) { return background(PeakPaint.solid(argb)); }

    public int getLineWidth() { return lineWidth; }
    public PeakPaint getLineColor() { return lineColor; }
    public PeakTrackStyle line(int width, PeakPaint paint) {
        this.lineWidth = Math.max(0, width);
        this.lineColor = paint.orNone();
        return this;
    }
    public PeakTrackStyle line(int width, int argb) { return line(width, PeakPaint.solid(argb)); }
    public PeakTrackStyle noLine() {
        this.lineWidth = 0;
        return this;
    }

    public boolean hasBackground() { return !background.isNone(); }
    public boolean overridesLine() { return lineWidth >= 0; }
}
