package net.gekidolukas.peakclient.ui;

import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

public final class PeakLinearGradient implements PeakPaint {
    public static final int DEFAULT_CYCLE_TICKS = 40;

    private final List<PeakColorStop> stops;
    private final PeakGradientDirection direction;
    private final Float angle;
    private final int cycleTicks;

    private PeakLinearGradient(List<PeakColorStop> stops, PeakGradientDirection direction, Float angle, int cycleTicks) {
        this.stops = PeakColorStops.sorted(stops);
        this.cycleTicks = Math.max(0, cycleTicks);
        Float canonical = angle == null ? null : normalizeAngle(angle);
        if (canonical != null && canonical == 0f) {
            this.direction = PeakGradientDirection.LEFT_TO_RIGHT;
            this.angle = null;
        } else if (canonical != null && canonical == 90f) {
            this.direction = PeakGradientDirection.TOP_TO_BOTTOM;
            this.angle = null;
        } else {
            this.direction = direction == null ? PeakGradientDirection.LEFT_TO_RIGHT : direction;
            this.angle = canonical;
        }
    }

    private static float normalizeAngle(float degrees) {
        float wrapped = degrees % 360f;
        if (wrapped < 0f) wrapped += 360f;
        return Math.abs(wrapped - Math.round(wrapped)) < 1e-4f ? Math.round(wrapped) % 360 : wrapped;
    }

    public static PeakLinearGradient of(int... colors) {
        return new PeakLinearGradient(PeakColorStops.evenly(colors), PeakGradientDirection.LEFT_TO_RIGHT, null, 0);
    }

    public static PeakLinearGradient of(PeakSolidColor... colors) {
        int[] argb = new int[colors.length];
        for (int i = 0; i < colors.length; i++) argb[i] = colors[i].argb();
        return of(argb);
    }

    public static PeakLinearGradient of(List<PeakColorStop> stops, PeakGradientDirection direction, int cycleTicks) {
        return new PeakLinearGradient(stops, direction, null, cycleTicks);
    }

    public static PeakLinearGradient of(List<PeakColorStop> stops, PeakGradientDirection direction, Float angle, int cycleTicks) {
        return new PeakLinearGradient(stops, direction, angle, cycleTicks);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<PeakColorStop> getStops() { return stops; }

    public PeakGradientDirection getDirection() { return direction; }

    public float getAngle() {
        if (angle != null) return angle;
        return direction == PeakGradientDirection.TOP_TO_BOTTOM ? 90f : 0f;
    }

    public boolean hasCustomAngle() { return angle != null; }

    public boolean isAxisAligned() { return angle == null; }

    public int getCycleTicks() { return cycleTicks; }

    public PeakLinearGradient direction(PeakGradientDirection direction) {
        return new PeakLinearGradient(stops, direction, null, cycleTicks);
    }

    public PeakLinearGradient angle(float degrees) {
        return new PeakLinearGradient(stops, direction, degrees, cycleTicks);
    }

    public PeakLinearGradient horizontal() {
        return direction(PeakGradientDirection.LEFT_TO_RIGHT);
    }

    public PeakLinearGradient vertical() {
        return direction(PeakGradientDirection.TOP_TO_BOTTOM);
    }

    public PeakLinearGradient animated() {
        return animated(DEFAULT_CYCLE_TICKS);
    }

    public PeakLinearGradient animated(int cycleTicks) {
        return new PeakLinearGradient(stops, direction, angle, Math.max(1, cycleTicks));
    }

    public PeakLinearGradient still() {
        return new PeakLinearGradient(stops, direction, angle, 0);
    }

    @Override
    public int colorAt(float position) {
        return PeakColorStops.sample(stops, cycleTicks, position);
    }

    @Override
    public int colorAt(float u, float v, float aspect) {
        if (angle == null) return colorAt(direction == PeakGradientDirection.TOP_TO_BOTTOM ? v : u);
        double radians = Math.toRadians(angle);
        double cos = Math.cos(radians), sin = Math.sin(radians);
        double length = Math.abs(aspect * cos) + Math.abs(sin);
        if (length < 1e-6) return colorAt(0f);
        double along = ((u - 0.5) * aspect * cos + (v - 0.5) * sin) / length + 0.5;
        return colorAt((float) along);
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
    public PeakLinearGradient withAlpha(float alpha) {
        return mapColors(color -> PeakArgb.withAlpha(color, alpha));
    }

    @Override
    public PeakLinearGradient opaque() {
        return mapColors(PeakArgb::ensureOpaque);
    }

    @Override
    public PeakLinearGradient orNone() {
        return mapColors(PeakArgb::normalize);
    }

    @Override
    public String toExpression() {
        return PeakPaintAdapter.toExpression(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PeakLinearGradient gradient)) return false;
        return cycleTicks == gradient.cycleTicks && direction == gradient.direction
                && Objects.equals(angle, gradient.angle) && stops.equals(gradient.stops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stops, direction, angle, cycleTicks);
    }

    @Override
    public String toString() {
        return toExpression();
    }

    private PeakLinearGradient mapColors(IntUnaryOperator operator) {
        return new PeakLinearGradient(PeakColorStops.map(stops, operator), direction, angle, cycleTicks);
    }

    public static final class Builder {
        private final List<PeakColorStop> stops = new ArrayList<>();
        private PeakGradientDirection direction = PeakGradientDirection.LEFT_TO_RIGHT;
        private Float angle;
        private int cycleTicks;

        private Builder() {}

        public Builder stop(float position, int argb) {
            stops.add(new PeakColorStop(position, PeakArgb.normalize(argb)));
            return this;
        }

        public Builder stop(float position, PeakSolidColor color) {
            return stop(position, color.argb());
        }

        public Builder direction(PeakGradientDirection direction) {
            this.direction = direction;
            this.angle = null;
            return this;
        }

        public Builder angle(float degrees) {
            this.angle = degrees;
            return this;
        }

        public Builder vertical() {
            return direction(PeakGradientDirection.TOP_TO_BOTTOM);
        }

        public Builder horizontal() {
            return direction(PeakGradientDirection.LEFT_TO_RIGHT);
        }

        public Builder animated(int cycleTicks) {
            this.cycleTicks = Math.max(1, cycleTicks);
            return this;
        }

        public Builder animated() {
            return animated(DEFAULT_CYCLE_TICKS);
        }

        public PeakLinearGradient build() {
            return new PeakLinearGradient(stops, direction, angle, cycleTicks);
        }
    }
}
