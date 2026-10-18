package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.HashSet;
import java.util.Set;

public abstract class PeakAbstractProgress<SELF extends PeakAbstractProgress<SELF>> extends PeakElement<SELF> {
    protected PeakProgressSource source = PeakProgressSource.MANUAL;
    protected double maxValue = 100;
    protected float autoIncrementPerTick = 0f;
    protected int transitionTicks = 0;

    @PeakOpaqueColor protected PeakPaint backgroundColor = PeakPaint.rgba(0, 0, 0, 140);
    @PeakOpaqueColor protected PeakPaint fillColor = PeakPaint.rgb(80, 220, 80);
    protected String backgroundTexture;
    protected String fillTexture;
    protected boolean hideIfEmpty = false;
    protected boolean hideIfFull = false;

    protected double baseValue = 0;
    protected transient long baseValueTick = PeakClientTickCounter.current();
    protected transient boolean transitioning = false;
    protected transient double transitionFrom;
    protected transient double transitionTarget;
    protected transient long transitionStartTick;
    protected transient int transitionDuration;

    protected PeakAbstractProgress(String id) { super(id); }

    public PeakProgressSource getSource() { return source; }
    public SELF source(PeakProgressSource source) { this.source = source; return self(); }
    public double getMaxValue() { return maxValue; }
    public SELF maxValue(double maxValue) { this.maxValue = maxValue; return self(); }
    public float getAutoIncrementPerTick() { return autoIncrementPerTick; }
    public SELF autoIncrementPerTick(float perTick) { this.autoIncrementPerTick = perTick; return self(); }
    public int getTransitionTicks() { return transitionTicks; }
    public SELF transitionTicks(int ticks) { this.transitionTicks = ticks; return self(); }

    public PeakPaint getBackgroundColor() { return backgroundColor; }
    public SELF backgroundColor(PeakPaint paint) { this.backgroundColor = paint.opaque(); return self(); }
    public SELF backgroundColor(int argb) { return backgroundColor(PeakPaint.solid(argb)); }
    public PeakPaint getFillColor() { return fillColor; }
    public SELF fillColor(PeakPaint paint) { this.fillColor = paint.opaque(); return self(); }
    public SELF fillColor(int argb) { return fillColor(PeakPaint.solid(argb)); }
    public String getBackgroundTexture() { return backgroundTexture; }
    public SELF backgroundTexture(String texture) { this.backgroundTexture = texture; return self(); }
    public String getFillTexture() { return fillTexture; }
    public SELF fillTexture(String texture) { this.fillTexture = texture; return self(); }
    public boolean isHideIfEmpty() { return hideIfEmpty; }
    public SELF hideIfEmpty(boolean hide) { this.hideIfEmpty = hide; return self(); }
    public boolean isHideIfFull() { return hideIfFull; }
    public SELF hideIfFull(boolean hide) { this.hideIfFull = hide; return self(); }

    public void resetAnchor() {
        this.baseValueTick = PeakClientTickCounter.current();
        this.transitioning = false;
    }

    @Override
    protected Set<String> customPatchKeys() {
        Set<String> keys = new HashSet<>(super.customPatchKeys());
        keys.add("value");
        return keys;
    }

    @Override
    protected void applyCustomPatch(String key, JsonElement value) {
        if (key.equals("value")) {
            setValue(value.getAsDouble());
            return;
        }
        super.applyCustomPatch(key, value);
    }

    public void setValue(double newValue) {
        double frozen = manualCurrentValue();
        transitionFrom = frozen;
        transitionTarget = clamp(newValue);
        transitionStartTick = PeakClientTickCounter.current();
        transitionDuration = transitionTicks;
        transitioning = transitionDuration > 0;
        if (!transitioning) {
            baseValue = transitionTarget;
            baseValueTick = transitionStartTick;
        }
    }

    public double manualCurrentValue() {
        long now = PeakClientTickCounter.current();
        if (transitioning) {
            long elapsed = now - transitionStartTick;
            if (elapsed >= transitionDuration) {
                transitioning = false;
                baseValue = transitionTarget;
                baseValueTick = transitionStartTick + transitionDuration;
            } else {
                float t = transitionDuration <= 0 ? 1f : (float) elapsed / transitionDuration;
                return clamp(transitionFrom + (transitionTarget - transitionFrom) * t);
            }
        }
        return clamp(baseValue + autoIncrementPerTick * (now - baseValueTick));
    }

    private double clamp(double v) { return Math.max(0, Math.min(maxValue, v)); }
}
