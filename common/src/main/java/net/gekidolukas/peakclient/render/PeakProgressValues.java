package net.gekidolukas.peakclient.render;

public record PeakProgressValues(double current, double max) {
    public boolean isFull() {
        return max > 0 && current >= max;
    }

    public boolean isEmpty() {
        return current <= 0;
    }

    public double fraction() {
        return max <= 0 ? 0 : Math.max(0, Math.min(1, current / max));
    }
}
