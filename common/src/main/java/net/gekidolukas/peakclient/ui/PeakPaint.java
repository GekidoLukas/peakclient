package net.gekidolukas.peakclient.ui;

import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

public sealed interface PeakPaint permits PeakSolidColor, PeakLinearGradient, PeakRadialGradient {
    PeakSolidColor NONE = new PeakSolidColor(PeakArgb.NONE);
    PeakSolidColor WHITE = new PeakSolidColor(0xFFFFFFFF);
    PeakSolidColor BLACK = new PeakSolidColor(0xFF000000);
    PeakSolidColor RED = new PeakSolidColor(0xFFFF5555);
    PeakSolidColor GREEN = new PeakSolidColor(0xFF55FF55);
    PeakSolidColor BLUE = new PeakSolidColor(0xFF5555FF);
    PeakSolidColor YELLOW = new PeakSolidColor(0xFFFFFF55);
    PeakSolidColor ORANGE = new PeakSolidColor(0xFFFFAA00);
    PeakSolidColor GRAY = new PeakSolidColor(0xFFAAAAAA);

    static PeakSolidColor solid(int argb) {
        return new PeakSolidColor(PeakArgb.normalize(argb));
    }

    static PeakSolidColor rgb(int red, int green, int blue) {
        return new PeakSolidColor(PeakArgb.rgb(red, green, blue));
    }

    static PeakSolidColor rgba(int red, int green, int blue, int alpha) {
        return new PeakSolidColor(PeakArgb.rgba(red, green, blue, alpha));
    }

    static PeakSolidColor hex(String text) {
        String digits = text.trim();
        if (digits.startsWith("#")) digits = digits.substring(1);
        else if (digits.startsWith("0x") || digits.startsWith("0X")) digits = digits.substring(2);
        long value;
        try {
            value = Long.parseLong(digits, 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a hex color: '" + text + "'");
        }
        if (digits.length() == 6) return new PeakSolidColor(0xFF000000 | (int) value);
        if (digits.length() == 8) return solid((int) value);
        throw new IllegalArgumentException("Hex color must have 6 or 8 digits: '" + text + "'");
    }

    static PeakLinearGradient gradient(int... colors) {
        return PeakLinearGradient.of(colors);
    }

    static PeakLinearGradient gradient(PeakSolidColor... colors) {
        return PeakLinearGradient.of(colors);
    }

    static PeakRadialGradient radial(int... colors) {
        return PeakRadialGradient.of(colors);
    }

    static PeakRadialGradient radial(PeakSolidColor... colors) {
        return PeakRadialGradient.of(colors);
    }

    static PeakPaint parse(String expression) {
        return PeakPaintAdapter.parse(expression);
    }

    int colorAt(float position);

    default int colorAt(float u, float v, float aspect) {
        return colorAt(u);
    }

    int firstColor();

    boolean isNone();

    boolean isSolid();

    boolean isAnimated();

    PeakPaint withAlpha(float alpha);

    PeakPaint opaque();

    PeakPaint orNone();

    String toExpression();
}
