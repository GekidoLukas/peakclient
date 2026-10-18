package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public final class PeakAnimated extends PeakContainer<PeakAnimated> {
    public static final int HIGHLIGHT_INCREASE = 0xFF55FF55;
    public static final int HIGHLIGHT_DECREASE = 0xFFFF5555;

    private PeakAnimationEffect effect = PeakAnimationEffect.NONE;
    private int periodTicks = 40;
    private float amplitude = 0f;
    private int delayTicks = 0;
    private boolean playing = true;
    @PeakColorOrNone private PeakPaint color = PeakPaint.NONE;

    private transient PeakAnimatedClock clock;

    public PeakAnimated() { this(PeakId.random()); }
    public PeakAnimated(String id) { super(id); }
    public static PeakAnimated create() { return new PeakAnimated(); }
    public static PeakAnimated create(String id) { return new PeakAnimated(id); }
    public static PeakAnimated of(PeakAnimationEffect effect) { return new PeakAnimated().effect(effect); }

    public PeakAnimationEffect getEffect() { return effect == null ? PeakAnimationEffect.NONE : effect; }
    public PeakAnimated effect(PeakAnimationEffect effect) { this.effect = effect; return this; }
    public int getPeriodTicks() { return Math.max(1, periodTicks); }
    public PeakAnimated periodTicks(int ticks) { this.periodTicks = Math.max(1, ticks); return this; }
    public PeakAnimated periodSeconds(double seconds) { return periodTicks((int) Math.round(seconds * 20.0)); }
    public float getAmplitude() { return Math.max(0f, amplitude); }
    public PeakAnimated amplitude(float amplitude) { this.amplitude = Math.max(0f, amplitude); return this; }
    public int getDelayTicks() { return Math.max(0, delayTicks); }
    public PeakAnimated delayTicks(int ticks) { this.delayTicks = Math.max(0, ticks); return this; }
    public boolean isPlaying() { return playing; }
    public PeakAnimated playing(boolean playing) { this.playing = playing; return this; }
    public PeakPaint getColor() { return color == null ? PeakPaint.NONE : color; }
    public PeakAnimated color(PeakPaint paint) { this.color = paint == null ? PeakPaint.NONE : paint.orNone(); return this; }
    public PeakAnimated color(int argb) { return color(PeakPaint.solid(argb)); }

    public PeakAnimated highlightOnChange(String watch) {
        return highlightOnChange(watch, PeakAnimationEffect.SHINE, 0);
    }

    public PeakAnimated highlightOnChange(String watch, PeakAnimationEffect effect, int color) {
        return highlight(PeakEvent.VALUE_CHANGE, watch, effect, color);
    }

    public PeakAnimated highlightOnIncrease(String watch) {
        return highlightOnIncrease(watch, PeakAnimationEffect.FLASH, HIGHLIGHT_INCREASE);
    }

    public PeakAnimated highlightOnIncrease(String watch, PeakAnimationEffect effect, int color) {
        return highlight(PeakEvent.VALUE_INCREASE, watch, effect, color);
    }

    public PeakAnimated highlightOnDecrease(String watch) {
        return highlightOnDecrease(watch, PeakAnimationEffect.FLASH, HIGHLIGHT_DECREASE);
    }

    public PeakAnimated highlightOnDecrease(String watch, PeakAnimationEffect effect, int color) {
        return highlight(PeakEvent.VALUE_DECREASE, watch, effect, color);
    }

    public PeakAnimated highlightOnTrend(String watch) {
        return highlightOnIncrease(watch).highlightOnDecrease(watch);
    }

    private PeakAnimated highlight(PeakEvent event, String watch, PeakAnimationEffect effect, int color) {
        return on(PeakReaction.of(event).watch(watch).then(PeakAction.trigger(getId(), effect, color)));
    }

    public PeakAnimatedClock clock() {
        if (clock == null) clock = new PeakAnimatedClock();
        return clock;
    }

    public PeakAnimatedClock.Config clockConfig() {
        PeakPaint paint = getColor();
        int argb = paint.isNone() ? 0 : paint.colorAt(0.5f);
        return new PeakAnimatedClock.Config(getEffect(), getPeriodTicks(), getAmplitude(), getDelayTicks(), playing, argb);
    }

    public void resetAnchor() {
        if (clock != null) clock.reset();
    }

    @Override
    public String elementType() { return "animated"; }

    @Override
    protected Set<String> customPatchKeys() {
        Set<String> keys = new HashSet<>(super.customPatchKeys());
        keys.add("trigger");
        return keys;
    }

    @Override
    protected void applyCustomPatch(String key, JsonElement value) {
        if (key.equals("trigger")) {
            if (value.isJsonObject()) {
                JsonObject object = value.getAsJsonObject();
                PeakAnimationEffect triggered = parseEffect(object.has("effect") ? object.get("effect").getAsString() : "");
                double ticks = object.has("ticks") ? object.get("ticks").getAsDouble() : 12.0;
                float intensity = object.has("intensity") ? object.get("intensity").getAsFloat() : 1f;
                int triggerColor = object.has("color") ? PeakArgb.normalize(object.get("color").getAsInt()) : 0;
                clock().trigger(triggered, ticks, intensity, triggerColor);
            }
            return;
        }
        super.applyCustomPatch(key, value);
    }

    private static PeakAnimationEffect parseEffect(String name) {
        try {
            return PeakAnimationEffect.valueOf(name);
        } catch (IllegalArgumentException e) {
            return PeakAnimationEffect.NONE;
        }
    }
}
