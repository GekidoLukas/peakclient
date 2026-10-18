package net.gekidolukas.peakclient.ui;

import java.util.Locale;

public final class PeakColorMath {
    public record Hsv(float hue, float saturation, float value) {}

    private PeakColorMath() {}

    public static int hsvToRgb(float hue, float saturation, float value) {
        float h = hue % 360f;
        if (h < 0f) h += 360f;
        float s = clamp01(saturation);
        float v = clamp01(value);

        float c = v * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = v - c;
        float r;
        float g;
        float b;
        if (h < 60f) {
            r = c; g = x; b = 0f;
        } else if (h < 120f) {
            r = x; g = c; b = 0f;
        } else if (h < 180f) {
            r = 0f; g = c; b = x;
        } else if (h < 240f) {
            r = 0f; g = x; b = c;
        } else if (h < 300f) {
            r = x; g = 0f; b = c;
        } else {
            r = c; g = 0f; b = x;
        }
        return channel(r + m) << 16 | channel(g + m) << 8 | channel(b + m);
    }

    public static Hsv rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue;
        if (delta == 0f) hue = 0f;
        else if (max == r) hue = 60f * (((g - b) / delta) % 6f);
        else if (max == g) hue = 60f * ((b - r) / delta + 2f);
        else hue = 60f * ((r - g) / delta + 4f);
        if (hue < 0f) hue += 360f;

        float saturation = max == 0f ? 0f : delta / max;
        return new Hsv(hue, saturation, max);
    }

    public static Integer tryParseHex(String text, boolean allowAlpha) {
        if (text == null) return null;
        String digits = text.trim();
        if (digits.startsWith("#")) digits = digits.substring(1);
        else if (digits.startsWith("0x") || digits.startsWith("0X")) digits = digits.substring(2);
        if (digits.length() != 6 && !(allowAlpha && digits.length() == 8)) return null;
        for (int i = 0; i < digits.length(); i++) {
            if (Character.digit(digits.charAt(i), 16) < 0) return null;
        }
        long value = Long.parseLong(digits, 16);
        if (digits.length() == 6) return 0xFF000000 | (int) value;
        int argb = (int) value;
        return (argb >>> 24) == 0 ? (argb | 0xFF000000) : argb;
    }

    public static String formatHex(int argb, boolean withAlpha) {
        if (withAlpha) return String.format(Locale.ROOT, "#%08X", argb);
        return String.format(Locale.ROOT, "#%06X", argb & 0xFFFFFF);
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255f)));
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
