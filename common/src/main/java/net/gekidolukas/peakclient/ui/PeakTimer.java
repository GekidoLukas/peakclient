package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class PeakTimer extends PeakElement<PeakTimer> {
    private PeakTimerMode mode = PeakTimerMode.COUNT_DOWN;
    private long value;
    private long target = 0;
    private boolean running = true;
    private PeakTimerFormat format = PeakTimerFormat.MM_SS;
    private Component prefix = Component.empty();
    private Component suffix = Component.empty();
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    private boolean shadow = true;

    private transient long anchorTick = PeakClientTickCounter.current();

    public PeakTimer(long initialValue) { this(PeakId.random(), initialValue); }

    public PeakTimer(String id, long initialValue) {
        super(id);
        this.value = initialValue;
    }

    public static PeakTimer create(long initialValue) { return new PeakTimer(initialValue); }
    public static PeakTimer create(String id, long initialValue) { return new PeakTimer(id,initialValue); }

    public static PeakTimer countDownFrom(long ticks) {
        return countDownFrom(PeakId.random(), ticks);
    }

    public static PeakTimer countDownFrom(String id, long ticks) {
        return new PeakTimer(id, ticks).mode(PeakTimerMode.COUNT_DOWN).target(0);
    }

    public static PeakTimer countUpFrom(long startTicks) {
        return countUpFrom(PeakId.random(), startTicks);
    }

    public static PeakTimer countUpFrom(String id, long startTicks) {
        return new PeakTimer(id, startTicks).mode(PeakTimerMode.COUNT_UP).target(Long.MAX_VALUE);
    }

    public PeakTimer running(boolean running) {
        this.running = running;
        resetAnchor();
        return this;
    }

    public PeakTimerMode getMode() { return mode; }
    public PeakTimer mode(PeakTimerMode mode) { this.mode = mode; return this; }
    public long getValue() { return value; }
    public long getTarget() { return target; }
    public PeakTimer target(long target) { this.target = target; return this; }
    public boolean isRunning() { return running; }
    public PeakTimerFormat getFormat() { return format; }
    public PeakTimer format(PeakTimerFormat format) { this.format = format; return this; }
    public Component getPrefix() { return prefix == null ? Component.empty() : prefix; }
    public PeakTimer prefix(String prefix) { return prefix(Component.literal(prefix == null ? "" : prefix)); }
    public PeakTimer prefix(Component prefix) { this.prefix = prefix == null ? Component.empty() : prefix; return this; }
    public Component getSuffix() { return suffix == null ? Component.empty() : suffix; }
    public PeakTimer suffix(String suffix) { return suffix(Component.literal(suffix == null ? "" : suffix)); }
    public PeakTimer suffix(Component suffix) { this.suffix = suffix == null ? Component.empty() : suffix; return this; }
    public PeakPaint getColor() { return color; }
    public PeakTimer color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakTimer color(int argb) { return color(PeakPaint.solid(argb)); }
    public boolean hasShadow() { return shadow; }
    public PeakTimer shadow(boolean shadow) { this.shadow = shadow; return this; }

    public void resetAnchor() { this.anchorTick = PeakClientTickCounter.current(); }

    public boolean isFinished() {
        long current = currentValue();
        if (mode == PeakTimerMode.COUNT_DOWN) return current <= target;
        return target != Long.MAX_VALUE && current >= target;
    }

    public long currentValue() {
        if (!running) return value;
        long elapsed = PeakClientTickCounter.current() - anchorTick;
        long delta = mode == PeakTimerMode.COUNT_UP ? elapsed : -elapsed;
        long computed = value + delta;
        if (mode == PeakTimerMode.COUNT_DOWN) {
            return Math.max(computed, target);
        }
        return target == Long.MAX_VALUE ? computed : Math.min(computed, target);
    }

    @Override
    public String elementType() { return "timer"; }

    private transient boolean patchAnchorRelevant;
    private transient long patchFrozenValue;

    @Override
    protected void beforePatch(JsonObject patch) {
        patchAnchorRelevant = patch.has("value") || patch.has("running") || patch.has("mode") || patch.has("target");
        if (patchAnchorRelevant) patchFrozenValue = currentValue();
    }

    @Override
    protected void afterPatch(JsonObject patch) {
        if (!patchAnchorRelevant) return;
        if (!patch.has("value")) this.value = patchFrozenValue;
        resetAnchor();
        patchAnchorRelevant = false;
    }
}
