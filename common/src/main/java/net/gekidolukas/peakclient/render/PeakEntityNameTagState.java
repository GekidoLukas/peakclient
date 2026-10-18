package net.gekidolukas.peakclient.render;

public final class PeakEntityNameTagState {
    private static boolean suppress = false;
    private static boolean force = false;

    private PeakEntityNameTagState() {}

    public static boolean isSuppressed() { return suppress; }
    public static boolean isForced() { return force; }

    public static void set(boolean showNameTag) {
        suppress = !showNameTag;
        force = showNameTag;
    }

    public static void reset() {
        suppress = false;
        force = false;
    }
}
