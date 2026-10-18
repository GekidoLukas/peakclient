package net.gekidolukas.peakclient.ui;

public final class PeakColorPickerLayout {
    public static final int BAR_WIDTH = 10;
    public static final int GAP = 4;
    public static final int FOOTER_HEIGHT = 16;
    public static final int PREVIEW_WIDTH = 26;
    public static final int MIN_FIELD = 8;

    public enum Part { NONE, FIELD, HUE, ALPHA }

    private final PeakRect field;
    private final PeakRect hue;
    private final PeakRect alpha;
    private final PeakRect preview;
    private final boolean hasAlpha;
    private final int hexX;
    private final int footerY;

    private PeakColorPickerLayout(PeakRect field, PeakRect hue, PeakRect alpha, PeakRect preview, boolean hasAlpha, int hexX, int footerY) {
        this.field = field;
        this.hue = hue;
        this.alpha = alpha;
        this.preview = preview;
        this.hasAlpha = hasAlpha;
        this.hexX = hexX;
        this.footerY = footerY;
    }

    public static PeakColorPickerLayout of(int width, int height, boolean withAlpha) {
        int minWidth = MIN_FIELD + GAP + BAR_WIDTH + (withAlpha ? GAP + BAR_WIDTH : 0);
        int w = Math.max(width, minWidth);
        int areaHeight = Math.max(MIN_FIELD, height - FOOTER_HEIGHT - GAP);

        int alphaX = w - BAR_WIDTH;
        int hueX = withAlpha ? alphaX - GAP - BAR_WIDTH : w - BAR_WIDTH;
        int fieldWidth = Math.max(MIN_FIELD, hueX - GAP);

        PeakRect field = new PeakRect(0, 0, fieldWidth, areaHeight);
        PeakRect hue = new PeakRect(hueX, 0, BAR_WIDTH, areaHeight);
        PeakRect alpha = withAlpha ? new PeakRect(alphaX, 0, BAR_WIDTH, areaHeight) : null;
        int footerY = areaHeight + GAP;
        PeakRect preview = new PeakRect(0, footerY, PREVIEW_WIDTH, FOOTER_HEIGHT);
        return new PeakColorPickerLayout(field, hue, alpha, preview, withAlpha, PREVIEW_WIDTH + GAP, footerY);
    }

    public PeakRect field() { return field; }
    public PeakRect hue() { return hue; }
    public PeakRect alpha() { return alpha; }
    public PeakRect preview() { return preview; }
    public boolean hasAlpha() { return hasAlpha; }
    public int hexX() { return hexX; }
    public int footerY() { return footerY; }

    public Part partAt(int x, int y) {
        if (field.contains(x, y)) return Part.FIELD;
        if (hue.contains(x, y)) return Part.HUE;
        if (alpha != null && alpha.contains(x, y)) return Part.ALPHA;
        return Part.NONE;
    }

    public float saturationAt(int x) {
        return fraction(x - field.x(), field.width());
    }

    public float valueAt(int y) {
        return 1f - fraction(y - field.y(), field.height());
    }

    public float hueAt(int y) {
        return fraction(y - hue.y(), hue.height()) * 360f;
    }

    public float alphaAt(int y) {
        if (alpha == null) return 1f;
        return 1f - fraction(y - alpha.y(), alpha.height());
    }

    public int fieldX(float saturation) {
        return field.x() + Math.round(clamp01(saturation) * (field.width() - 1));
    }

    public int fieldY(float value) {
        return field.y() + Math.round((1f - clamp01(value)) * (field.height() - 1));
    }

    public int hueY(float hueDegrees) {
        return hue.y() + Math.round(clamp01(hueDegrees / 360f) * (hue.height() - 1));
    }

    public int alphaY(float alphaFraction) {
        if (alpha == null) return 0;
        return alpha.y() + Math.round((1f - clamp01(alphaFraction)) * (alpha.height() - 1));
    }

    private static float fraction(int offset, int length) {
        if (length <= 1) return 0f;
        return clamp01(offset / (float) (length - 1));
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
