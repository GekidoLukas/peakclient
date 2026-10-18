package net.gekidolukas.peakclient.render;

import java.util.HashMap;
import java.util.Map;

public final class PeakChibiMetrics {
    private static final int MAX_ENTRIES = 64;
    private static final Map<String, Sample> LAST = new HashMap<>();

    private PeakChibiMetrics() {}

    public record Sample(float unit, float physicalPixelsPerUnit, float absScale, float scissorUnitFactor, double guiScale,
                         float headScale, float roundedHeadScale, float headPixels, int roundedHeadPixels,
                         double neckScreenY, double headTopScreenY, double armTopScreenY, double feetScreenY) {}

    static void record(String elementId, Sample sample) {
        if (LAST.size() > MAX_ENTRIES) LAST.clear();
        LAST.put(elementId, sample);
    }

    public static Sample last(String elementId) {
        return LAST.get(elementId);
    }
}
