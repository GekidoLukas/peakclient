package net.gekidolukas.peakclient.ui;

public enum PeakAnimationEffect {
    NONE(0f, false),
    PULSE(0.1f, false),
    BOB(3f, false),
    SWAY(3f, false),
    WOBBLE(5f, false),
    SPIN(1f, false),
    BLINK(0.7f, false),
    SHAKE(2f, false),
    POP(0.15f, false),
    FLASH(0.6f, true),
    GLOW(0.8f, true),
    SHINE(0.7f, true),
    OUTLINE(1f, true);

    private final float defaultAmplitude;
    private final boolean overlay;

    PeakAnimationEffect(float defaultAmplitude, boolean overlay) {
        this.defaultAmplitude = defaultAmplitude;
        this.overlay = overlay;
    }

    public float getDefaultAmplitude() {
        return defaultAmplitude;
    }

    public boolean isOverlay() {
        return overlay;
    }
}
