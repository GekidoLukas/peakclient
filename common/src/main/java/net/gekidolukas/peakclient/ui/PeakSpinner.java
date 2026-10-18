package net.gekidolukas.peakclient.ui;

public final class PeakSpinner extends PeakElement<PeakSpinner> {
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    private int dots = 8;
    private int periodTicks = 16;

    public PeakSpinner() { this(PeakId.random()); }

    public PeakSpinner(String id) {
        super(id);
        size = PeakSize.pixels(16, 16);
    }

    public static PeakSpinner create() { return new PeakSpinner(); }
    public static PeakSpinner create(String id) { return new PeakSpinner(id); }

    public PeakPaint getColor() { return color; }
    public PeakSpinner color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakSpinner color(int argb) { return color(PeakPaint.solid(argb)); }

    public int getDots() { return Math.max(3, Math.min(dots, 16)); }
    public PeakSpinner dots(int dots) { this.dots = Math.max(3, Math.min(dots, 16)); return this; }

    public int getPeriodTicks() { return Math.max(4, periodTicks); }
    public PeakSpinner periodTicks(int ticks) { this.periodTicks = Math.max(4, ticks); return this; }

    @Override
    public String elementType() { return "spinner"; }
}
