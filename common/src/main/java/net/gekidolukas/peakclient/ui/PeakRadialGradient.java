package net.gekidolukas.peakclient.ui;

import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

public final class PeakRadialGradient implements PeakPaint {
    public static final float DEFAULT_CENTER = 0.5f;

    private final List<PeakColorStop> stops;
    private final float centerX;
    private final float centerY;
    private final float radius;
    private final int cycleTicks;

    private PeakRadialGradient(List<PeakColorStop> stops, float centerX, float centerY, float radius, int cycleTicks) {
        this.stops = PeakColorStops.sorted(stops);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = Math.max(0f, radius);
        this.cycleTicks = Math.max(0, cycleTicks);
    }

    public static PeakRadialGradient of(int... colors) {
        return new PeakRadialGradient(PeakColorStops.evenly(colors), DEFAULT_CENTER, DEFAULT_CENTER, 0f, 0);
    }

    public static PeakRadialGradient of(PeakSolidColor... colors) {
        int[] argb = new int[colors.length];
        for (int i = 0; i < colors.length; i++) argb[i] = colors[i].argb();
        return of(argb);
    }

    public static PeakRadialGradient of(List<PeakColorStop> stops, float centerX, float centerY, float radius, int cycleTicks) {
        return new PeakRadialGradient(stops, centerX, centerY, radius, cycleTicks);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<PeakColorStop> getStops() { return stops; }

    public float getCenterX() { return centerX; }

    public float getCenterY() { return centerY; }

    public float getRadius() { return radius; }

    public int getCycleTicks() { return cycleTicks; }

    public PeakRadialGradient center(float x, float y) {
        return new PeakRadialGradient(stops, x, y, radius, cycleTicks);
    }

    public PeakRadialGradient radius(float shorterSideFraction) {
        return new PeakRadialGradient(stops, centerX, centerY, shorterSideFraction, cycleTicks);
    }

    public PeakRadialGradient animated() {
        return animated(PeakLinearGradient.DEFAULT_CYCLE_TICKS);
    }

    public PeakRadialGradient animated(int cycleTicks) {
        return new PeakRadialGradient(stops, centerX, centerY, radius, Math.max(1, cycleTicks));
    }

    public PeakRadialGradient still() {
        return new PeakRadialGradient(stops, centerX, centerY, radius, 0);
    }

    public float reach(float aspect) {
        if (radius > 0f) return radius * Math.min(aspect, 1f);
        float farX = Math.max(centerX, 1f - centerX) * aspect;
        float farY = Math.max(centerY, 1f - centerY);
        return (float) Math.hypot(farX, farY);
    }

    @Override
    public int colorAt(float position) {
        return PeakColorStops.sample(stops, cycleTicks, position);
    }

    @Override
    public int colorAt(float u, float v, float aspect) {
        float reach = reach(aspect);
        if (reach < 1e-6f) return colorAt(0f);
        float distance = (float) Math.hypot((u - centerX) * aspect, v - centerY);
        return colorAt(distance / reach);
    }

    @Override
    public int firstColor() {
        return stops.get(0).color();
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isAnimated() {
        return cycleTicks > 0;
    }

    @Override
    public PeakRadialGradient withAlpha(float alpha) {
        return mapColors(color -> PeakArgb.withAlpha(color, alpha));
    }

    @Override
    public PeakRadialGradient opaque() {
        return mapColors(PeakArgb::ensureOpaque);
    }

    @Override
    public PeakRadialGradient orNone() {
        return mapColors(PeakArgb::normalize);
    }

    @Override
    public String toExpression() {
        return PeakPaintAdapter.toExpression(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PeakRadialGradient gradient)) return false;
        return cycleTicks == gradient.cycleTicks && centerX == gradient.centerX && centerY == gradient.centerY
                && radius == gradient.radius && stops.equals(gradient.stops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stops, centerX, centerY, radius, cycleTicks);
    }

    @Override
    public String toString() {
        return toExpression();
    }

    private PeakRadialGradient mapColors(IntUnaryOperator operator) {
        return new PeakRadialGradient(PeakColorStops.map(stops, operator), centerX, centerY, radius, cycleTicks);
    }

    public static final class Builder {
        private final List<PeakColorStop> stops = new ArrayList<>();
        private float centerX = DEFAULT_CENTER;
        private float centerY = DEFAULT_CENTER;
        private float radius;
        private int cycleTicks;

        private Builder() {}

        public Builder stop(float position, int argb) {
            stops.add(new PeakColorStop(position, PeakArgb.normalize(argb)));
            return this;
        }

        public Builder stop(float position, PeakSolidColor color) {
            return stop(position, color.argb());
        }

        public Builder center(float x, float y) {
            this.centerX = x;
            this.centerY = y;
            return this;
        }

        public Builder radius(float shorterSideFraction) {
            this.radius = shorterSideFraction;
            return this;
        }

        public Builder animated(int cycleTicks) {
            this.cycleTicks = Math.max(1, cycleTicks);
            return this;
        }

        public Builder animated() {
            return animated(PeakLinearGradient.DEFAULT_CYCLE_TICKS);
        }

        public PeakRadialGradient build() {
            return new PeakRadialGradient(stops, centerX, centerY, radius, cycleTicks);
        }
    }
}
