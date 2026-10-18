package net.gekidolukas.peakclient.ui;

public final class PeakPosition {
    private PeakAnchor anchor = PeakAnchor.TOP_LEFT;
    private int offsetX;
    private int offsetY;

    public static PeakPosition create() { return new PeakPosition(); }
    public static PeakPosition of(PeakAnchor anchor) { return new PeakPosition().anchor(anchor); }
    public static PeakPosition of(PeakAnchor anchor, int offsetX, int offsetY) {
        return new PeakPosition().anchor(anchor).offsetX(offsetX).offsetY(offsetY);
    }

    public PeakAnchor getAnchor() { return anchor; }
    public PeakPosition anchor(PeakAnchor anchor) { this.anchor = anchor; return this; }
    public int getOffsetX() { return offsetX; }
    public PeakPosition offsetX(int offsetX) { this.offsetX = offsetX; return this; }
    public int getOffsetY() { return offsetY; }
    public PeakPosition offsetY(int offsetY) { this.offsetY = offsetY; return this; }
}
