package net.gekidolukas.peakclient.ui;

public final class PeakWatchable {
    private PeakWatchable() {}

    public static boolean hasDefaultValue(PeakElement<?> element) {
        return element instanceof PeakLabel || element instanceof PeakTimer
                || element instanceof PeakAbstractProgress<?> || element instanceof PeakInput;
    }
}
