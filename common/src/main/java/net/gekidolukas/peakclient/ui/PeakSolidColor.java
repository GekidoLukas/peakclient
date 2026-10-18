package net.gekidolukas.peakclient.ui;

public record PeakSolidColor(int argb) implements PeakPaint {
    @Override
    public int colorAt(float position) {
        return argb;
    }

    @Override
    public int firstColor() {
        return argb;
    }

    @Override
    public boolean isNone() {
        return (argb >>> 24) == 0;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public PeakSolidColor withAlpha(float alpha) {
        return new PeakSolidColor(PeakArgb.withAlpha(argb, alpha));
    }

    @Override
    public PeakSolidColor opaque() {
        return new PeakSolidColor(PeakArgb.ensureOpaque(argb));
    }

    @Override
    public PeakSolidColor orNone() {
        return new PeakSolidColor(PeakArgb.normalize(argb));
    }

    @Override
    public String toExpression() {
        return PeakArgb.format(argb);
    }
}
