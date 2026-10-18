package net.gekidolukas.peakclient.ui;

public final class PeakTooltipStyle {
    public static final int DEFAULT_BACKGROUND = 0xF0100010;
    public static final int DEFAULT_BORDER_TOP = 0x505000FF;
    public static final int DEFAULT_BORDER_BOTTOM = 0x5028007F;

    @PeakColorOrNone private PeakPaint background = PeakPaint.solid(DEFAULT_BACKGROUND);
    @PeakColorOrNone private PeakPaint border = defaultBorder();
    private int borderWidth = 1;
    private int padding = 3;
    @PeakOpaqueColor private PeakPaint textColor = PeakPaint.WHITE;
    private boolean shadow = true;
    private int maxWidth = 0;

    public static PeakTooltipStyle create() { return new PeakTooltipStyle(); }

    public static PeakPaint defaultBorder() {
        return PeakPaint.gradient(DEFAULT_BORDER_TOP, DEFAULT_BORDER_BOTTOM).vertical();
    }

    public PeakPaint getBackground() { return background; }
    public PeakTooltipStyle background(PeakPaint paint) { this.background = paint.orNone(); return this; }
    public PeakTooltipStyle background(int argb) { return background(PeakPaint.solid(argb)); }

    public PeakPaint getBorder() { return border; }
    public PeakTooltipStyle border(PeakPaint paint) { this.border = paint.orNone(); return this; }
    public PeakTooltipStyle border(int argb) { return border(PeakPaint.solid(argb)); }
    public PeakTooltipStyle border(int top, int bottom) { return border(PeakPaint.gradient(top, bottom).vertical()); }

    public int getBorderWidth() { return Math.max(0, borderWidth); }
    public PeakTooltipStyle borderWidth(int width) { this.borderWidth = Math.max(0, width); return this; }

    public int getPadding() { return Math.max(0, padding); }
    public PeakTooltipStyle padding(int padding) { this.padding = Math.max(0, padding); return this; }

    public PeakPaint getTextColor() { return textColor; }
    public PeakTooltipStyle textColor(PeakPaint paint) { this.textColor = paint.opaque(); return this; }
    public PeakTooltipStyle textColor(int argb) { return textColor(PeakPaint.solid(argb)); }

    public boolean hasShadow() { return shadow; }
    public PeakTooltipStyle shadow(boolean shadow) { this.shadow = shadow; return this; }

    public int getMaxWidth() { return Math.max(0, maxWidth); }
    public PeakTooltipStyle maxWidth(int width) { this.maxWidth = Math.max(0, width); return this; }
}
