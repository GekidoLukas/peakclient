package net.gekidolukas.peakclient.ui;

public final class PeakScrollbarStyle {
    public static final int DEFAULT_THICKNESS = 3;
    public static final int DEFAULT_MARGIN = 2;
    public static final int DEFAULT_MIN_THUMB_SIZE = 8;
    public static final int DEFAULT_TRACK_COLOR = 0x50000000;
    public static final int DEFAULT_THUMB_COLOR = 0xC0DDDDDD;

    private Boolean visible;
    private Integer thickness;
    private Integer margin;
    private Integer radius;
    private Integer minThumbSize;
    @PeakColorOrNone private PeakPaint trackColor;
    @PeakColorOrNone private PeakPaint thumbColor;
    @PeakColorOrNone private PeakPaint thumbHoverColor;
    @PeakColorOrNone private PeakPaint thumbDragColor;

    public static PeakScrollbarStyle create() { return new PeakScrollbarStyle(); }

    public static PeakScrollbarStyle resolve(PeakScrollbarStyle own, PeakScrollbarStyle theme) {
        PeakScrollbarStyle ownStyle = own == null ? new PeakScrollbarStyle() : own;
        PeakScrollbarStyle themeStyle = theme == null ? new PeakScrollbarStyle() : theme;
        PeakScrollbarStyle result = new PeakScrollbarStyle();
        result.visible = pick(ownStyle.visible, themeStyle.visible, Boolean.TRUE);
        result.thickness = pick(ownStyle.thickness, themeStyle.thickness, DEFAULT_THICKNESS);
        result.margin = pick(ownStyle.margin, themeStyle.margin, DEFAULT_MARGIN);
        result.radius = pick(ownStyle.radius, themeStyle.radius, 0);
        result.minThumbSize = pick(ownStyle.minThumbSize, themeStyle.minThumbSize, DEFAULT_MIN_THUMB_SIZE);
        result.trackColor = pick(ownStyle.trackColor, themeStyle.trackColor, PeakPaint.solid(DEFAULT_TRACK_COLOR));
        result.thumbColor = pick(ownStyle.thumbColor, themeStyle.thumbColor, PeakPaint.solid(DEFAULT_THUMB_COLOR));
        result.thumbHoverColor = pick(ownStyle.thumbHoverColor, themeStyle.thumbHoverColor, PeakPaint.NONE);
        result.thumbDragColor = pick(ownStyle.thumbDragColor, themeStyle.thumbDragColor, PeakPaint.NONE);
        return result;
    }

    private static <T> T pick(T own, T theme, T fallback) {
        if (own != null) return own;
        if (theme != null) return theme;
        return fallback;
    }

    public boolean isVisible() { return visible == null || visible; }
    public PeakScrollbarStyle visible(boolean visible) { this.visible = visible; return this; }
    public PeakScrollbarStyle hidden() { return visible(false); }

    public int getThickness() { return Math.max(1, thickness == null ? DEFAULT_THICKNESS : thickness); }
    public PeakScrollbarStyle thickness(int thickness) { this.thickness = Math.max(1, thickness); return this; }

    public int getMargin() { return Math.max(0, margin == null ? DEFAULT_MARGIN : margin); }
    public PeakScrollbarStyle margin(int margin) { this.margin = Math.max(0, margin); return this; }

    public int getRadius() { return Math.max(0, Math.min(radius == null ? 0 : radius, getThickness() / 2)); }
    public PeakScrollbarStyle radius(int radius) { this.radius = Math.max(0, radius); return this; }

    public int getMinThumbSize() { return Math.max(1, minThumbSize == null ? DEFAULT_MIN_THUMB_SIZE : minThumbSize); }
    public PeakScrollbarStyle minThumbSize(int size) { this.minThumbSize = Math.max(1, size); return this; }

    public PeakPaint getTrackColor() { return trackColor == null ? PeakPaint.solid(DEFAULT_TRACK_COLOR) : trackColor; }
    public PeakScrollbarStyle trackColor(PeakPaint paint) { this.trackColor = paint.orNone(); return this; }
    public PeakScrollbarStyle trackColor(int argb) { return trackColor(PeakPaint.solid(argb)); }
    public PeakScrollbarStyle noTrack() { this.trackColor = PeakPaint.NONE; return this; }

    public PeakPaint getThumbColor() { return thumbColor == null ? PeakPaint.solid(DEFAULT_THUMB_COLOR) : thumbColor; }
    public PeakScrollbarStyle thumbColor(PeakPaint paint) { this.thumbColor = paint.orNone(); return this; }
    public PeakScrollbarStyle thumbColor(int argb) { return thumbColor(PeakPaint.solid(argb)); }

    public PeakPaint getThumbHoverColor() {
        return thumbHoverColor != null && !thumbHoverColor.isNone() ? thumbHoverColor : getThumbColor();
    }
    public PeakScrollbarStyle thumbHoverColor(PeakPaint paint) { this.thumbHoverColor = paint.orNone(); return this; }
    public PeakScrollbarStyle thumbHoverColor(int argb) { return thumbHoverColor(PeakPaint.solid(argb)); }

    public PeakPaint getThumbDragColor() {
        return thumbDragColor != null && !thumbDragColor.isNone() ? thumbDragColor : getThumbHoverColor();
    }
    public PeakScrollbarStyle thumbDragColor(PeakPaint paint) { this.thumbDragColor = paint.orNone(); return this; }
    public PeakScrollbarStyle thumbDragColor(int argb) { return thumbDragColor(PeakPaint.solid(argb)); }
}
