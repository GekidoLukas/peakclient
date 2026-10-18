package net.gekidolukas.peakclient.ui;

public final class PeakShape extends PeakContainer<PeakShape> {
    private PeakShapeKind shape = PeakShapeKind.ROUNDED;
    private int cornerRadius = 8;
    private float popOutTop = 0f;
    private float popOutBottom = 0f;
    private float popOutLeft = 0f;
    private float popOutRight = 0f;

    public PeakShape() { this(PeakId.random()); }
    public PeakShape(String id) { super(id); }
    public static PeakShape create() { return new PeakShape(); }
    public static PeakShape create(String id) { return new PeakShape(id); }
    public static PeakShape circle() { return new PeakShape().shape(PeakShapeKind.CIRCLE); }
    public static PeakShape rounded(int radius) { return new PeakShape().cornerRadius(radius); }
    public static PeakShape triangle() { return sharp(PeakShapeKind.TRIANGLE); }
    public static PeakShape diamond() { return sharp(PeakShapeKind.DIAMOND); }
    public static PeakShape pentagon() { return sharp(PeakShapeKind.PENTAGON); }
    public static PeakShape hexagon() { return sharp(PeakShapeKind.HEXAGON); }
    public static PeakShape hexagonFlat() { return sharp(PeakShapeKind.HEXAGON_FLAT); }
    public static PeakShape octagon() { return sharp(PeakShapeKind.OCTAGON); }
    public static PeakShape star() { return sharp(PeakShapeKind.STAR); }

    private static PeakShape sharp(PeakShapeKind kind) {
        return new PeakShape().shape(kind).cornerRadius(0);
    }

    public PeakShapeKind getShape() { return shape == null ? PeakShapeKind.ROUNDED : shape; }
    public PeakShape shape(PeakShapeKind shape) { this.shape = shape; return this; }
    public int getCornerRadius() { return Math.max(0, cornerRadius); }
    public PeakShape cornerRadius(int radius) { this.cornerRadius = Math.max(0, radius); return this; }

    public float getPopOutTop() { return clamp(popOutTop); }
    public PeakShape popOutTop(float fraction) { this.popOutTop = clamp(fraction); return this; }
    public float getPopOutBottom() { return clamp(popOutBottom); }
    public PeakShape popOutBottom(float fraction) { this.popOutBottom = clamp(fraction); return this; }
    public float getPopOutLeft() { return clamp(popOutLeft); }
    public PeakShape popOutLeft(float fraction) { this.popOutLeft = clamp(fraction); return this; }
    public float getPopOutRight() { return clamp(popOutRight); }
    public PeakShape popOutRight(float fraction) { this.popOutRight = clamp(fraction); return this; }

    public boolean hasPopOut() {
        return getPopOutTop() > 0f || getPopOutBottom() > 0f || getPopOutLeft() > 0f || getPopOutRight() > 0f;
    }

    private static float clamp(float fraction) {
        return Float.isNaN(fraction) ? 0f : Math.max(0f, Math.min(1f, fraction));
    }

    public int effectiveRadius(int width, int height) {
        int limit = Math.max(0, Math.min(width, height) / 2);
        return getShape() == PeakShapeKind.CIRCLE ? limit : Math.min(getCornerRadius(), limit);
    }

    @Override
    public String elementType() { return "shape"; }
}
