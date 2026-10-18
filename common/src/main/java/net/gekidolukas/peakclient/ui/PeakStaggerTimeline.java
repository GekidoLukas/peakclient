package net.gekidolukas.peakclient.ui;

public final class PeakStaggerTimeline {
    public enum Mode { UNSTARTED, IN, OUT, GONE }

    public record Config(double delayTicks, double enterTicks, double exitTicks, double startDelayTicks) {}

    private Mode mode = Mode.UNSTARTED;
    private double start;

    public Mode getMode() {
        return mode;
    }

    public void reset() {
        mode = Mode.UNSTARTED;
    }

    public void ensureStarted(double now, boolean autoPlay) {
        if (mode != Mode.UNSTARTED) return;
        mode = autoPlay ? Mode.IN : Mode.GONE;
        start = now;
    }

    public void play(double now) {
        mode = Mode.IN;
        start = now;
    }

    public void hide(double now) {
        if (mode == Mode.GONE) return;
        mode = Mode.OUT;
        start = now;
    }

    public void finish(double now, int count, Config config) {
        if (mode != Mode.OUT) return;
        double total = Math.max(0, count - 1) * config.delayTicks() + config.exitTicks();
        if (now - start >= total) mode = Mode.GONE;
    }

    public boolean isExiting() {
        return mode == Mode.OUT;
    }

    public float appearance(double now, int rank, int count, Config config) {
        return switch (mode) {
            case IN -> progress(now - start - config.startDelayTicks() - rank * config.delayTicks(), config.enterTicks());
            case OUT -> 1f - progress(now - start - rank * config.delayTicks(), config.exitTicks());
            case GONE, UNSTARTED -> 0f;
        };
    }

    private static float progress(double elapsed, double duration) {
        if (elapsed < 0.0) return 0f;
        if (duration <= 0.0) return 1f;
        return (float) Math.min(1.0, elapsed / duration);
    }
}
