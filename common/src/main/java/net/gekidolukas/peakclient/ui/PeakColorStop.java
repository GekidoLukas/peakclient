package net.gekidolukas.peakclient.ui;

public record PeakColorStop(float position, int color) {
    public static PeakColorStop of(float position, int color) { return new PeakColorStop(position, color); }
}
