package net.gekidolukas.peakclient.ui;

import java.util.function.IntUnaryOperator;

public final class PeakReelMotion {
    private boolean initialized;
    private double position;
    private boolean spinning;
    private double from;
    private double to;
    private double start;
    private double duration;
    private PeakReelEasing easing = PeakReelEasing.DECELERATE;

    public void reset() {
        initialized = false;
        spinning = false;
    }

    public boolean isSpinning() {
        return spinning;
    }

    public void ensureInitialized(int index) {
        if (initialized) return;
        initialized = true;
        position = Math.max(0, index);
        spinning = false;
    }

    public double position(double now) {
        if (!spinning) return position;
        double progress = duration <= 0.0 ? 1.0 : (now - start) / duration;
        if (progress >= 1.0) {
            spinning = false;
            position = to;
            return position;
        }
        return from + (to - from) * easing.apply(progress);
    }

    public void remap(int oldCount, int newCount, IntUnaryOperator mapper, double now) {
        if (!initialized || oldCount <= 0 || newCount <= 0) return;
        double current = position(now);
        double base = Math.floor(current);
        double fraction = current - base;
        int oldSlot = (int) mod(base, oldCount);
        int mappedSlot = mapSlot(mapper, oldSlot, newCount);
        if (!spinning) {
            position = mappedSlot + fraction;
            return;
        }
        int targetSlot = (int) Math.round(mod(to, oldCount)) % oldCount;
        double toTarget = mod(targetSlot - current, oldCount);
        long loopsLeft = Math.max(0L, Math.round((to - current - toTarget) / oldCount));
        int mappedTarget = mapSlot(mapper, targetSlot, newCount);
        double newFrom = mappedSlot + fraction;
        double remaining = Math.max(0.0, start + duration - now);
        this.from = newFrom;
        this.to = newFrom + mod(mappedTarget - newFrom, newCount) + loopsLeft * (double) newCount;
        this.start = now;
        this.duration = remaining;
        this.position = newFrom;
    }

    private static int mapSlot(IntUnaryOperator mapper, int slot, int newCount) {
        int mapped = mapper.applyAsInt(slot);
        return mapped >= 0 && mapped < newCount ? mapped : Math.min(slot, newCount - 1);
    }

    public void normalize(int count) {
        if (spinning || count <= 0) return;
        position = mod(position, count);
    }

    public void jump(int index) {
        spinning = false;
        position = Math.max(0, index);
        initialized = true;
    }

    public void spin(int count, int target, double now, double durationTicks, int minLoops, PeakReelEasing easing) {
        if (count <= 0) return;
        double current = position(now);
        int slot = Math.max(0, Math.min(count - 1, target));
        double wanted = current + Math.max(0, minLoops) * (double) count;
        long laps = (long) Math.ceil((wanted - slot) / count - 1e-9);
        double end = slot + laps * (double) count;
        while (end < current - 1e-9) end += count;

        this.from = current;
        this.to = end;
        this.start = now;
        this.duration = Math.max(0.0, durationTicks);
        this.easing = easing == null ? PeakReelEasing.DECELERATE : easing;
        this.spinning = true;
        this.initialized = true;
        this.position = current;
    }

    public int settledIndex(int count) {
        if (count <= 0) return 0;
        double target = spinning ? to : position;
        return (int) Math.round(mod(target, count)) % count;
    }

    public static double mod(double value, int count) {
        double result = value % count;
        return result < 0 ? result + count : result;
    }
}
