package net.gekidolukas.peakclient.ui;

import java.util.Locale;

public final class PeakArgb {
    public static final int NONE = 0x00000000;

    private PeakArgb() {}

    public static int rgba(int red, int green, int blue, int alpha) {
        if ((alpha & 0xFF) == 0) return NONE;
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int rgb(int red, int green, int blue) {
        return rgba(red, green, blue, 255);
    }

    public static int withAlpha(int argb, int alpha) {
        int clamped = Math.max(0, Math.min(255, alpha));
        if (clamped == 0) return NONE;
        return clamped << 24 | (argb & 0xFFFFFF);
    }

    public static int withAlpha(int argb, float alpha) {
        return withAlpha(argb, Math.round(alpha * 255f));
    }

    public static int lerp(int from, int to, float t) {
        int alpha = channel(from, to, 24, t);
        int red = channel(from, to, 16, t);
        int green = channel(from, to, 8, t);
        int blue = channel(from, to, 0, t);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int ensureOpaque(int argb) {
        return (argb >>> 24) == 0 ? (argb | 0xFF000000) : argb;
    }

    public static int normalize(int argb) {
        return argb == 0 ? NONE : ensureOpaque(argb);
    }

    public static Integer parse(String text) {
        if (text == null) return null;
        String value = text.trim();
        try {
            if (value.startsWith("#")) {
                String digits = value.substring(1);
                if (digits.length() == 6) return 0xFF000000 | Integer.parseUnsignedInt(digits, 16);
                if (digits.length() == 8) return (int) Long.parseLong(digits, 16);
                return null;
            }
            if (value.startsWith("0x") || value.startsWith("0X")) {
                return (int) Long.parseLong(value.substring(2), 16);
            }
            return (int) Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String format(int argb) {
        return String.format(Locale.ROOT, "#%08X", argb);
    }

    private static int channel(int from, int to, int shift, float t) {
        int start = (from >>> shift) & 0xFF;
        int end = (to >>> shift) & 0xFF;
        return Math.round(start + (end - start) * t) & 0xFF;
    }
}
