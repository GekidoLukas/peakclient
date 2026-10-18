package net.gekidolukas.peakclient.ui;

public final class PeakStyle {
    @PeakColorOrNone private PeakPaint background = PeakPaint.NONE;
    private PeakInsets padding = PeakInsets.create();
    private PeakInsets margin = PeakInsets.create();
    @PeakColorOrNone private PeakPaint borderColor = PeakPaint.NONE;
    private int borderWidth = 0;
    private int borderRadius = 0;
    @PeakColorOrNone private PeakPaint shadowColor = PeakPaint.NONE;
    private int shadowOffsetX = 2;
    private int shadowOffsetY = 2;
    private int shadowBlur = 0;

    public static PeakStyle create() { return new PeakStyle(); }

    public PeakPaint getBackground() { return background; }
    public PeakStyle background(PeakPaint paint) { this.background = paint.orNone(); return this; }
    public PeakStyle background(int argb) { return background(PeakPaint.solid(argb)); }

    public PeakInsets getPadding() { return padding; }

    public PeakStyle padding(int all) { this.padding.set(all); return this; }
    public PeakStyle padding(int top, int right, int bottom, int left) {
        this.padding = PeakInsets.of(top, right, bottom, left);
        return this;
    }
    public PeakStyle padding(PeakInsets padding) { this.padding = padding; return this; }

    public PeakInsets getMargin() { return margin; }
    public PeakStyle margin(int all) { this.margin.set(all); return this; }
    public PeakStyle margin(int top, int right, int bottom, int left) {
        this.margin = PeakInsets.of(top, right, bottom, left);
        return this;
    }
    public PeakStyle margin(PeakInsets margin) { this.margin = margin; return this; }

    public PeakPaint getBorderColor() { return borderColor; }
    public PeakStyle borderColor(PeakPaint paint) { this.borderColor = paint.orNone(); return this; }
    public PeakStyle borderColor(int argb) { return borderColor(PeakPaint.solid(argb)); }
    public int getBorderWidth() { return borderWidth; }
    public PeakStyle borderWidth(int width) { this.borderWidth = width; return this; }

    public int getBorderRadius() { return borderRadius; }
    public PeakStyle borderRadius(int radius) { this.borderRadius = Math.max(0, radius); return this; }

    public PeakPaint getShadowColor() { return shadowColor; }
    public PeakStyle shadowColor(PeakPaint paint) { this.shadowColor = paint.orNone(); return this; }
    public PeakStyle shadowColor(int argb) { return shadowColor(PeakPaint.solid(argb)); }
    public int getShadowOffsetX() { return shadowOffsetX; }
    public int getShadowOffsetY() { return shadowOffsetY; }
    public PeakStyle shadowOffset(int x, int y) { this.shadowOffsetX = x; this.shadowOffsetY = y; return this; }
    public int getShadowBlur() { return shadowBlur; }
    public PeakStyle shadowBlur(int blur) { this.shadowBlur = Math.max(0, blur); return this; }
    public PeakStyle boxShadow(PeakPaint paint) { return shadowColor(paint); }
    public PeakStyle boxShadow(int argb) { return shadowColor(argb); }
    public PeakStyle boxShadow(PeakPaint paint, int offsetX, int offsetY, int blur) {
        shadowColor(paint);
        this.shadowOffsetX = offsetX;
        this.shadowOffsetY = offsetY;
        this.shadowBlur = blur;
        return this;
    }
    public PeakStyle boxShadow(int argb, int offsetX, int offsetY, int blur) {
        return boxShadow(PeakPaint.solid(argb), offsetX, offsetY, blur);
    }

    public PeakStyle withBorderRadius(int radius) {
        PeakStyle copy = new PeakStyle();
        copy.background = background;
        copy.padding = PeakInsets.of(padding.getTop(), padding.getRight(), padding.getBottom(), padding.getLeft());
        copy.margin = PeakInsets.of(margin.getTop(), margin.getRight(), margin.getBottom(), margin.getLeft());
        copy.borderColor = borderColor;
        copy.borderWidth = borderWidth;
        copy.borderRadius = Math.max(0, radius);
        copy.shadowColor = shadowColor;
        copy.shadowOffsetX = shadowOffsetX;
        copy.shadowOffsetY = shadowOffsetY;
        copy.shadowBlur = shadowBlur;
        return copy;
    }
}
