package net.gekidolukas.peakclient.ui;

public final class PeakPanel extends PeakContainer<PeakPanel> {

    public static final int DEFAULT_BACKGROUND = PeakArgb.rgba(0, 0, 0, 140);

    public PeakPanel() { this(PeakId.random()); }

    public PeakPanel(String id) {
        super(id);
        getStyle().background(DEFAULT_BACKGROUND);
    }

    public static PeakPanel create() { return new PeakPanel(); }
    public static PeakPanel create(String id) { return new PeakPanel(id); }

    @Override
    public String elementType() { return "panel"; }
}
