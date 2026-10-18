package net.gekidolukas.peakclient.ui;

public final class PeakProgressBar extends PeakAbstractProgress<PeakProgressBar> {
    public PeakProgressBar() { this(PeakId.random()); }

    public PeakProgressBar(String id) {
        super(id);
        size(PeakSize.of(PeakSize.PeakDimension.pixels(100), PeakSize.PeakDimension.pixels(10)));
    }

    public static PeakProgressBar create() { return new PeakProgressBar(); }
    public static PeakProgressBar create(String id) { return new PeakProgressBar(id); }

    @Override
    public String elementType() { return "progress_bar"; }
}
