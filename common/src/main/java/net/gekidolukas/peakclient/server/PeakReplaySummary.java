package net.gekidolukas.peakclient.server;

public record PeakReplaySummary(int customValues, int waypoints, int vanillaHudOverrides, boolean worldBorder) {
    public static final PeakReplaySummary EMPTY = new PeakReplaySummary(0, 0, 0, false);
}
