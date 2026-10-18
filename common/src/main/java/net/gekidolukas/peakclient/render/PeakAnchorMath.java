package net.gekidolukas.peakclient.render;

public final class PeakAnchorMath {
    private PeakAnchorMath() {}

    public static float distanceAlpha(double distance, float minDistance, float maxDistance, float fadeDistance) {
        if (minDistance > 0f && distance < minDistance - fadeDistance) return 0f;
        if (maxDistance > 0f && distance > maxDistance) return 0f;
        float alpha = 1f;
        if (fadeDistance > 0f) {
            if (minDistance > 0f && distance < minDistance) {
                alpha = Math.min(alpha, (float) ((distance - (minDistance - fadeDistance)) / fadeDistance));
            }
            if (maxDistance > 0f && distance > maxDistance - fadeDistance) {
                alpha = Math.min(alpha, (float) ((maxDistance - distance) / fadeDistance));
            }
        } else if (minDistance > 0f && distance < minDistance) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, alpha));
    }

    public static float perspectiveScale(double distance, float referenceDistance, float minScale, float maxScale) {
        if (distance <= 1e-3) return maxScale;
        float scale = (float) (referenceDistance / distance);
        return Math.max(minScale, Math.min(maxScale, scale));
    }
}
