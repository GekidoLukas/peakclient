package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

public final class PeakChartEntry {
    private String id = PeakId.random();
    private Component label;
    private double value;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.rgb(85, 170, 255);

    private transient PeakPaint opaqueColor;

    private PeakChartEntry() {}

    public static PeakChartEntry of(Component label, double value, PeakPaint color) {
        PeakChartEntry e = new PeakChartEntry();
        e.label = label == null ? Component.empty() : label;
        e.value = value;
        e.color = color.opaque();
        return e;
    }

    public static PeakChartEntry of(Component label, double value, int argb) {
        return of(label, value, PeakPaint.solid(argb));
    }

    public static PeakChartEntry of(String label, double value, PeakPaint color) {
        return of(Component.literal(label == null ? "" : label), value, color);
    }

    public static PeakChartEntry of(String label, double value, int argb) {
        return of(label, value, PeakPaint.solid(argb));
    }

    public String id() { return id; }
    public Component label() { return label == null ? Component.empty() : label; }
    public double value() { return value; }
    public PeakPaint color() {
        if (opaqueColor == null) opaqueColor = color.opaque();
        return opaqueColor;
    }
}
