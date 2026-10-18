package net.gekidolukas.peakclient.ui;

public record PeakPixelSize(int width, int height) {
    public static final PeakPixelSize ZERO = new PeakPixelSize(0, 0);

    public PeakPixelSize scaledBy(float factor) {
        return new PeakPixelSize(Math.round(width * factor), Math.round(height * factor));
    }
}
