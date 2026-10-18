package net.gekidolukas.peakclient.ui;

public final class PeakSize {
    private PeakDimension width = PeakDimension.auto();
    private PeakDimension height = PeakDimension.auto();

    public static PeakSize create() { return new PeakSize(); }
    public static PeakSize auto() { return new PeakSize(); }
    public static PeakSize pixels(int width, int height) {
        return new PeakSize().width(PeakDimension.pixels(width)).height(PeakDimension.pixels(height));
    }
    public static PeakSize percent(float width, float height) {
        return new PeakSize().width(PeakDimension.percent(width)).height(PeakDimension.percent(height));
    }
    public static PeakSize of(PeakDimension width, PeakDimension height) {
        return new PeakSize().width(width).height(height);
    }

    public PeakDimension getWidth() { return width; }
    public PeakSize width(PeakDimension width) { this.width = width; return this; }
    public PeakDimension getHeight() { return height; }
    public PeakSize height(PeakDimension height) { this.height = height; return this; }

    public record PeakDimension(Mode mode, float value) {
        public enum Mode { AUTO, PIXELS, PERCENT }

        public static PeakDimension auto() { return new PeakDimension(Mode.AUTO, 0); }
        public static PeakDimension pixels(int pixels) { return new PeakDimension(Mode.PIXELS, pixels); }
        public static PeakDimension percent(float percentage) { return new PeakDimension(Mode.PERCENT, percentage); }
    }
}
