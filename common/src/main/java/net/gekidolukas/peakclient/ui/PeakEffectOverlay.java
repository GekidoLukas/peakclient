package net.gekidolukas.peakclient.ui;

public record PeakEffectOverlay(Kind kind, int color, float strength, float progress) {
    public enum Kind { FLASH, GLOW, SHINE, OUTLINE }

    public boolean isVisible() {
        return strength > 0.002f;
    }
}
