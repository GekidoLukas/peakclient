package net.gekidolukas.peakclient.ui.patch;

import com.google.gson.JsonArray;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

public final class PeakLabelPatch extends PeakBasePatch<PeakLabelPatch> {
    public PeakLabelPatch text(String text) { data.addProperty("text", text); return this; }
    public PeakLabelPatch translationKey(String key, String... args) {
        data.addProperty("translationKey", key);
        JsonArray argsJson = new JsonArray();
        for (String arg : args) argsJson.add(arg);
        data.add("translationArgs", argsJson);
        return this;
    }
    public PeakLabelPatch color(int argb) { data.addProperty("color", argb); return this; }
    public PeakLabelPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakLabelPatch shadow(boolean shadow) { data.addProperty("shadow", shadow); return this; }

    public PeakLabelPatch wrapWidth(int value) { data.addProperty("wrapWidth", value); return this; }
    public PeakLabelPatch lineSpacing(int value) { data.addProperty("lineSpacing", value); return this; }
    public PeakLabelPatch countUp(int ticks) { data.addProperty("countUpTicks", Math.max(0, ticks)); return this; }
    public PeakLabelPatch typewriter(float charsPerSecond) { data.addProperty("typewriterCharsPerSecond", Math.max(0f, charsPerSecond)); return this; }
    public PeakLabelPatch replay() { data.addProperty("replay", true); return this; }
    @Override protected PeakLabelPatch self() { return this; }
}
