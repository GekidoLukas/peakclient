package net.gekidolukas.peakclient.ui;

public enum PeakChibiRotation {
    THREE_QUARTER(-157.5f),
    THREE_QUARTER_MIRRORED(157.5f),
    FRONT(180f),
    BACK(0f),
    LEFT(90f),
    RIGHT(-90f);

    private final float degrees;

    PeakChibiRotation(float degrees) { this.degrees = degrees; }

    public float degrees() { return degrees; }
}
