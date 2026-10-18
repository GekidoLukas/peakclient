package net.gekidolukas.peakclient.ui;

public final class PeakDiv extends PeakContainer<PeakDiv> {
    public PeakDiv() { this(PeakId.random()); }

    public PeakDiv(String id) {
        super(id);
    }

    public static PeakDiv create() { return new PeakDiv(); }
    public static PeakDiv create(String id) { return new PeakDiv(id); }

    @Override
    public String elementType() { return "div"; }
}
