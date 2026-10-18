package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;

public final class PeakClientCustomWorldBorderState {
    private static volatile boolean active = false;
    private static double fromMinX, fromMinZ, fromMaxX, fromMaxZ;
    private static double targetMinX, targetMinZ, targetMaxX, targetMaxZ;
    private static long startTick;
    private static int transitionTicks;

    private PeakClientCustomWorldBorderState() {}

    public static void setCurrent(double minX, double minZ, double maxX, double maxZ) {
        fromMinX = minX; fromMinZ = minZ; fromMaxX = maxX; fromMaxZ = maxZ;
        targetMinX = minX; targetMinZ = minZ; targetMaxX = maxX; targetMaxZ = maxZ;
        transitionTicks = 0;
        startTick = PeakClientTickCounter.current();
        active = true;
    }

    public static void setTarget(double minX, double minZ, double maxX, double maxZ, int transitionTicks) {
        if (active) {
            float fraction = progress();
            fromMinX = lerp(fromMinX, targetMinX, fraction);
            fromMinZ = lerp(fromMinZ, targetMinZ, fraction);
            fromMaxX = lerp(fromMaxX, targetMaxX, fraction);
            fromMaxZ = lerp(fromMaxZ, targetMaxZ, fraction);
        }
        targetMinX = minX; targetMinZ = minZ; targetMaxX = maxX; targetMaxZ = maxZ;
        PeakClientCustomWorldBorderState.transitionTicks = Math.max(0, transitionTicks);
        startTick = PeakClientTickCounter.current();
        active = true;
    }

    public static void clear() { active = false; }
    public static boolean isActive() { return active; }

    private static double lerp(double from, double to, float t) { return from + (to - from) * t; }

    private static float progress() {
        if (transitionTicks <= 0) return 1f;
        long elapsed = PeakClientTickCounter.current() - startTick;
        return Math.min(1f, elapsed / (float) transitionTicks);
    }

    public static double currentMinX() { return lerp(fromMinX, targetMinX, progress()); }
    public static double currentMinZ() { return lerp(fromMinZ, targetMinZ, progress()); }
    public static double currentMaxX() { return lerp(fromMaxX, targetMaxX, progress()); }
    public static double currentMaxZ() { return lerp(fromMaxZ, targetMaxZ, progress()); }
}