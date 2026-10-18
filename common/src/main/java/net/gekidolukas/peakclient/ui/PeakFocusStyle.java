package net.gekidolukas.peakclient.ui;

public final class PeakFocusStyle {
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_WIDTH = 1;

    @PeakColorOrNone private PeakPaint color = PeakPaint.solid(DEFAULT_COLOR);
    private int width = DEFAULT_WIDTH;

    public static PeakFocusStyle create() { return new PeakFocusStyle(); }

    public PeakPaint getColor() { return color; }
    public PeakFocusStyle color(PeakPaint paint) { this.color = paint.orNone(); return this; }
    public PeakFocusStyle color(int argb) { return color(PeakPaint.solid(argb)); }

    public int getWidth() { return Math.max(0, width); }
    public PeakFocusStyle width(int width) { this.width = Math.max(0, width); return this; }

    public PeakFocusStyle none() {
        this.color = PeakPaint.NONE;
        return this;
    }

    public boolean isVisible() {
        return !color.isNone() && getWidth() > 0;
    }
}
