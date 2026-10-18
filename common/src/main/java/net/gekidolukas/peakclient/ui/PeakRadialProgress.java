package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

public final class PeakRadialProgress extends PeakAbstractProgress<PeakRadialProgress> {
    private float startAngle = 0f;
    private boolean clockwise = true;
    private int thickness = 6;
    private float arcDegrees = 360f;

    public PeakRadialProgress() { this(PeakId.random()); }

    public PeakRadialProgress(String id) {
        super(id);
        size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40)));
    }

    public static PeakRadialProgress create() { return new PeakRadialProgress(); }
    public static PeakRadialProgress create(String id) { return new PeakRadialProgress(id); }

    public float getStartAngle() { return startAngle; }
    public PeakRadialProgress startAngle(float angle) { this.startAngle = angle; return this; }
    public boolean isClockwise() { return clockwise; }
    public PeakRadialProgress clockwise(boolean clockwise) { this.clockwise = clockwise; return this; }
    public int getThickness() { return thickness; }
    public PeakRadialProgress thickness(int thickness) { this.thickness = thickness; return this; }
    public float getArcDegrees() { return arcDegrees; }
    public PeakRadialProgress arcDegrees(float degrees) { this.arcDegrees = Math.max(0, Math.min(360, degrees)); return this; }

    @Override
    public String elementType() { return "radial_progress"; }
}
