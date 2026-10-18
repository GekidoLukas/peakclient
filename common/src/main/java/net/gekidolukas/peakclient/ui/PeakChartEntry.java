package net.gekidolukas.peakclient.ui;

public final class PeakChartEntry {
    private String id = PeakId.random();
    private String label;
    private double value;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.rgb(85, 170, 255);

    private transient PeakPaint opaqueColor;

    private PeakChartEntry() {}

    public static PeakChartEntry of(String label, double value, PeakPaint color) {
        PeakChartEntry e = new PeakChartEntry();
        e.label = label; e.value = value; e.color = color.opaque();
        return e;
    }

    public static PeakChartEntry of(String label, double value, int argb) {
        return of(label, value, PeakPaint.solid(argb));
    }

    public String id() { return id; }
    public String label() { return label; }
    public double value() { return value; }
    public PeakPaint color() {
        if (opaqueColor == null) opaqueColor = color.opaque();
        return opaqueColor;
    }
}