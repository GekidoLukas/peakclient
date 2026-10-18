package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakColorMath;
import net.gekidolukas.peakclient.ui.PeakColorPicker;

public final class PeakColorPickerState {
    private float hue;
    private float saturation;
    private float brightness = 1f;
    private float opacity = 1f;
    private int lastValue;
    private int lastTargetColor = -1;
    private boolean initialized;

    public static PeakColorPickerState of(PeakColorPicker picker) {
        return new PeakColorPickerState().sync(picker);
    }

    public PeakColorPickerState sync(PeakColorPicker picker) {
        int current = picker.getValue();
        if (initialized && current == lastValue) return this;

        PeakColorMath.Hsv hsv = PeakColorMath.rgbToHsv(current & 0xFFFFFF);
        boolean keepHue = initialized && (hsv.saturation() == 0f || hsv.value() == 0f);
        if (!keepHue) hue = hsv.hue();
        saturation = hsv.saturation();
        brightness = hsv.value();
        opacity = ((current >>> 24) & 0xFF) / 255f;
        lastValue = current;
        initialized = true;
        return this;
    }

    public float hue() { return hue; }
    public float saturation() { return saturation; }
    public float brightness() { return brightness; }
    public float opacity() { return opacity; }

    public void setSaturationBrightness(float saturation, float brightness) {
        this.saturation = clamp01(saturation);
        this.brightness = clamp01(brightness);
    }

    public void setHue(float degrees) {
        this.hue = Math.max(0f, Math.min(359.999f, degrees));
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(1f / 255f, clamp01(opacity));
    }

    public int argb(boolean withAlpha) {
        int rgb = PeakColorMath.hsvToRgb(hue, saturation, brightness);
        int alpha = withAlpha ? Math.max(1, Math.round(opacity * 255f)) : 255;
        return alpha << 24 | rgb;
    }

    public int lastTargetColor() { return lastTargetColor; }

    public void seenTargetColor(int rgb) { this.lastTargetColor = rgb; }

    public void applied(int value) {
        this.lastValue = value;
        this.initialized = true;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
