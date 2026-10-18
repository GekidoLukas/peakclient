package net.gekidolukas.peakclient.render;

public final class PeakEdgeClamp {
    public record Result(float x, float y, float directionX, float directionY) {}

    private PeakEdgeClamp() {}

    public static Result clamp(float viewX, float viewY, float guiWidth, float guiHeight, float insetX, float insetY) {
        float directionX = viewX;
        float directionY = -viewY;
        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (length < 1e-4f) {
            directionX = 0f;
            directionY = 1f;
        } else {
            directionX /= length;
            directionY /= length;
        }

        float centerX = guiWidth / 2f, centerY = guiHeight / 2f;
        float halfWidth = Math.max(0f, centerX - insetX);
        float halfHeight = Math.max(0f, centerY - insetY);
        float scaleX = Math.abs(directionX) < 1e-5f ? Float.MAX_VALUE : halfWidth / Math.abs(directionX);
        float scaleY = Math.abs(directionY) < 1e-5f ? Float.MAX_VALUE : halfHeight / Math.abs(directionY);
        float reach = Math.min(scaleX, scaleY);
        return new Result(centerX + directionX * reach, centerY + directionY * reach, directionX, directionY);
    }
}
