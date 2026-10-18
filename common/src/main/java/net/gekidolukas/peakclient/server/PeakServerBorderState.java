package net.gekidolukas.peakclient.server;

public final class PeakServerBorderState {
    public record Bounds(double minX, double minZ, double maxX, double maxZ) {
        Bounds towards(Bounds other, float progress) {
            return new Bounds(
                    minX + (other.minX - minX) * progress,
                    minZ + (other.minZ - minZ) * progress,
                    maxX + (other.maxX - maxX) * progress,
                    maxZ + (other.maxZ - maxZ) * progress);
        }
    }

    private static final Bounds EMPTY = new Bounds(0, 0, 0, 0);

    private boolean active = false;
    private Bounds from = EMPTY;
    private Bounds target = EMPTY;
    private long startTick;
    private int transitionTicks;

    public void setCurrent(double minX, double minZ, double maxX, double maxZ, long nowTick) {
        from = new Bounds(minX, minZ, maxX, maxZ);
        target = from;
        transitionTicks = 0;
        startTick = nowTick;
        active = true;
    }

    public void setTarget(double minX, double minZ, double maxX, double maxZ, int ticks, long nowTick) {
        if (active) {
            from = from.towards(target, progress(nowTick));
        }
        target = new Bounds(minX, minZ, maxX, maxZ);
        transitionTicks = Math.max(0, ticks);
        startTick = nowTick;
        active = true;
    }

    public void clear() {
        active = false;
    }

    public boolean isActive() { return active; }

    public Bounds currentBounds(long nowTick) {
        return from.towards(target, progress(nowTick));
    }

    public Bounds targetBounds() { return target; }

    public int remainingTicks(long nowTick) {
        if (transitionTicks <= 0) return 0;
        return (int) Math.max(0, transitionTicks - (nowTick - startTick));
    }

    private float progress(long nowTick) {
        if (transitionTicks <= 0) return 1f;
        return Math.min(1f, (nowTick - startTick) / (float) transitionTicks);
    }
}
