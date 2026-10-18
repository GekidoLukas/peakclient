package net.gekidolukas.peakclient.ui;

public record PeakRevealState(float scaleX, float scaleY, float clipLeft, float clipTop, float clipRight, float clipBottom) {
    public static final PeakRevealState IDENTITY = new PeakRevealState(1f, 1f, 0f, 0f, 1f, 1f);
    private static final float EPSILON = 0.0001f;

    public static PeakRevealState scaled(float scaleX, float scaleY) {
        return new PeakRevealState(scaleX, scaleY, 0f, 0f, 1f, 1f);
    }

    public static PeakRevealState clipped(float left, float top, float right, float bottom) {
        return new PeakRevealState(1f, 1f, left, top, right, bottom);
    }

    public boolean isScaled() {
        return Math.abs(scaleX - 1f) >= EPSILON || Math.abs(scaleY - 1f) >= EPSILON;
    }

    public boolean isClipped() {
        return clipLeft > EPSILON || clipTop > EPSILON || clipRight < 1f - EPSILON || clipBottom < 1f - EPSILON;
    }

    public boolean isIdentity() {
        return !isScaled() && !isClipped();
    }

    public boolean isEmpty() {
        return scaleX < EPSILON || scaleY < EPSILON || clipRight - clipLeft < EPSILON || clipBottom - clipTop < EPSILON;
    }
}
