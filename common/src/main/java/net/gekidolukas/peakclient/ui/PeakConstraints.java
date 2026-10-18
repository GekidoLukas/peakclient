package net.gekidolukas.peakclient.ui;

public final class PeakConstraints {
    private PeakSize.PeakDimension minWidth = PeakSize.PeakDimension.auto();
    private PeakSize.PeakDimension minHeight = PeakSize.PeakDimension.auto();
    private PeakSize.PeakDimension maxWidth = PeakSize.PeakDimension.auto();
    private PeakSize.PeakDimension maxHeight = PeakSize.PeakDimension.auto();

    public static PeakConstraints create() { return new PeakConstraints(); }

    public PeakSize.PeakDimension getMinWidth() { return minWidth; }
    public PeakConstraints minWidth(int pixels) { this.minWidth = PeakSize.PeakDimension.pixels(pixels); return this; }
    public PeakSize.PeakDimension getMinHeight() { return minHeight; }
    public PeakConstraints minHeight(int pixels) { this.minHeight = PeakSize.PeakDimension.pixels(pixels); return this; }
    public PeakSize.PeakDimension getMaxWidth() { return maxWidth; }
    public PeakConstraints maxWidth(int pixels) { this.maxWidth = PeakSize.PeakDimension.pixels(pixels); return this; }
    public PeakSize.PeakDimension getMaxHeight() { return maxHeight; }
    public PeakConstraints maxHeight(int pixels) { this.maxHeight = PeakSize.PeakDimension.pixels(pixels); return this; }

    public PeakConstraints minSize(int width, int height) { return minWidth(width).minHeight(height); }
    public PeakConstraints maxSize(int width, int height) { return maxWidth(width).maxHeight(height); }
}
