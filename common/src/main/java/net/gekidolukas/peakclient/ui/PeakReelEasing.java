package net.gekidolukas.peakclient.ui;

public enum PeakReelEasing {
    DECELERATE, SETTLE;

    public double apply(double progress) {
        double p = Math.max(0.0, Math.min(1.0, progress));
        return switch (this) {
            case DECELERATE -> 1.0 - Math.pow(1.0 - p, 4.0);
            case SETTLE -> {
                double c1 = 1.0;
                double c3 = c1 + 1.0;
                double shifted = p - 1.0;
                yield 1.0 + c3 * shifted * shifted * shifted + c1 * shifted * shifted;
            }
        };
    }
}
