package net.gekidolukas.peakclient.ui;

public final class PeakSpacer extends PeakElement<PeakSpacer> {
    private boolean lineBreak = false;

    public PeakSpacer(int width, int height) { this(PeakId.random(), width, height); }

    public PeakSpacer(String id, int width, int height) {
        super(id);
        size(PeakSize.of(PeakSize.PeakDimension.pixels(width), PeakSize.PeakDimension.pixels(height)));
    }

    public static PeakSpacer create(int width, int height) { return new PeakSpacer(width, height); }

    public static PeakSpacer create(String id, int width, int height) { return new PeakSpacer(id, width, height); }

    public static PeakSpacer lineBreak() {
        PeakSpacer spacer = new PeakSpacer(0, 0);
        spacer.lineBreak = true;
        return spacer;
    }

    public boolean isLineBreak() { return lineBreak; }

    @Override
    public String elementType() { return "spacer"; }
}
