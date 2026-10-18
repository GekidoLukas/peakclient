package net.gekidolukas.peakclient.render;

public record PeakShapeMask(float x1, float y1, float x2, float y2, float radius, float[] outline,
                            float topLine, float bottomLine, float leftLine, float rightLine) {
    public static final float NONE = Float.NaN;

    public boolean hasPopOut() {
        return !Float.isNaN(topLine) || !Float.isNaN(bottomLine) || !Float.isNaN(leftLine) || !Float.isNaN(rightLine);
    }

    public static PeakShapeMask rounded(float x1, float y1, float x2, float y2, float radius) {
        return new PeakShapeMask(x1, y1, x2, y2, radius, null, NONE, NONE, NONE, NONE);
    }
}
