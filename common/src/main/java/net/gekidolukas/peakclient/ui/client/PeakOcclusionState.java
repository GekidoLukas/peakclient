package net.gekidolukas.peakclient.ui.client;

public final class PeakOcclusionState {
    private boolean known;
    private boolean occluded;
    private long nextCheck = Long.MIN_VALUE;
    private float factor = 1f;
    private double lastTime = Double.NaN;
    private boolean snapNext = true;

    public boolean isKnown() { return known; }
    public boolean isOccluded() { return occluded; }
    public float factor() { return factor; }

    public boolean due(long tick) {
        return !known || tick >= nextCheck;
    }

    public void record(boolean occluded, long tick, int intervalTicks) {
        this.occluded = occluded;
        this.known = true;
        this.nextCheck = tick + Math.max(1, intervalTicks);
    }

    public void disable() {
        known = true;
        occluded = false;
        nextCheck = Long.MIN_VALUE;
        snapNext = true;
        factor = 1f;
        lastTime = Double.NaN;
    }

    public float advance(double now, float target, int fadeTicks) {
        if (snapNext || Double.isNaN(lastTime)) {
            factor = target;
            snapNext = false;
        } else {
            double elapsed = Math.max(0.0, now - lastTime);
            float step = (float) (elapsed / Math.max(1, fadeTicks));
            if (factor < target) factor = Math.min(target, factor + step);
            else if (factor > target) factor = Math.max(target, factor - step);
        }
        lastTime = now;
        return factor;
    }

    public static final class Budget {
        public static final int MAX_PER_TICK = 16;

        private long tick = Long.MIN_VALUE;
        private int used;

        public boolean tryAcquire(long currentTick) {
            if (currentTick != tick) {
                tick = currentTick;
                used = 0;
            }
            if (used >= MAX_PER_TICK) return false;
            used++;
            return true;
        }
    }
}
