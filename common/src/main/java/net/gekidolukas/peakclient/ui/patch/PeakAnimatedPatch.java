package net.gekidolukas.peakclient.ui.patch;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakAnimationEffect;
import net.gekidolukas.peakclient.ui.PeakPaint;

public final class PeakAnimatedPatch extends PeakContainerPatch<PeakAnimatedPatch> {
    public PeakAnimatedPatch effect(PeakAnimationEffect effect) { data.addProperty("effect", effect.name()); return this; }
    public PeakAnimatedPatch periodTicks(int ticks) { data.addProperty("periodTicks", Math.max(1, ticks)); return this; }
    public PeakAnimatedPatch periodSeconds(double seconds) { return periodTicks((int) Math.round(seconds * 20.0)); }
    public PeakAnimatedPatch amplitude(float amplitude) { data.addProperty("amplitude", Math.max(0f, amplitude)); return this; }
    public PeakAnimatedPatch delayTicks(int ticks) { data.addProperty("delayTicks", Math.max(0, ticks)); return this; }
    public PeakAnimatedPatch playing(boolean playing) { data.addProperty("playing", playing); return this; }
    public PeakAnimatedPatch color(int argb) { data.addProperty("color", PeakPaint.solid(argb).colorAt(0f)); return this; }

    public PeakAnimatedPatch trigger(PeakAnimationEffect effect, int ticks, float intensity, int color) {
        JsonObject object = new JsonObject();
        object.addProperty("effect", effect.name());
        object.addProperty("ticks", Math.max(1, ticks));
        object.addProperty("intensity", intensity);
        if (color != 0) object.addProperty("color", color);
        data.add("trigger", object);
        return this;
    }

    public PeakAnimatedPatch trigger(PeakAnimationEffect effect, int ticks, float intensity) { return trigger(effect, ticks, intensity, 0); }
    public PeakAnimatedPatch trigger(PeakAnimationEffect effect) { return trigger(effect, 12, 1f, 0); }
    public PeakAnimatedPatch trigger(PeakAnimationEffect effect, int color) { return trigger(effect, 14, 1f, color); }
    public PeakAnimatedPatch shake() { return trigger(PeakAnimationEffect.SHAKE, 14, 1f); }
    public PeakAnimatedPatch pop() { return trigger(PeakAnimationEffect.POP, 12, 1f); }
    public PeakAnimatedPatch flash() { return trigger(PeakAnimationEffect.FLASH, 14, 1f); }
    public PeakAnimatedPatch flash(int color) { return trigger(PeakAnimationEffect.FLASH, 14, 1f, color); }
    public PeakAnimatedPatch glow(int color) { return trigger(PeakAnimationEffect.GLOW, 24, 1f, color); }
    public PeakAnimatedPatch shine() { return trigger(PeakAnimationEffect.SHINE, 20, 1f); }
    public PeakAnimatedPatch outline(int color) { return trigger(PeakAnimationEffect.OUTLINE, 20, 1f, color); }

    @Override protected PeakAnimatedPatch self() { return this; }
}
