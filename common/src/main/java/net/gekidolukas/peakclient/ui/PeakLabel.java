package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PeakLabel extends PeakElement<PeakLabel> {
    private String text;
    private String translationKey;
    private List<String> translationArgs = new ArrayList<>();
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    private boolean shadow = true;
    private int wrapWidth = 0;
    private int lineSpacing = 0;
    private int countUpTicks = 0;
    private float typewriterCharsPerSecond = 0f;

    private transient PeakLabelAnimation animation;

    public PeakLabel(String text) { this(PeakId.random(), text); }

    public PeakLabel(String id, String text) {
        super(id);
        this.text = text;
    }

    public static PeakLabel create(String text) { return new PeakLabel(text); }
    public static PeakLabel create(String id,String text) { return new PeakLabel(id,text); }

    public static PeakLabel translatable(String key, String... args) {
        PeakLabel label = new PeakLabel(PeakId.random(), "");
        return label.translationKey(key, args);
    }

    public static PeakLabel translatable(String id, String key, String... args) {
        PeakLabel label = new PeakLabel(id, "");
        return label.translationKey(key, args);
    }

    public String getText() { return text; }
    public PeakLabel text(String text) { this.text = text; this.translationKey = null; return this; }

    public String getTranslationKey() { return translationKey; }
    public List<String> getTranslationArgs() { return translationArgs; }
    public PeakLabel translationKey(String key, String... args) {
        this.translationKey = key;
        this.translationArgs = List.of(args);
        return this;
    }

    public PeakPaint getColor() { return color; }
    public PeakLabel color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakLabel color(int argb) { return color(PeakPaint.solid(argb)); }
    public boolean hasShadow() { return shadow; }
    public PeakLabel shadow(boolean shadow) { this.shadow = shadow; return this; }
    public int getWrapWidth() { return wrapWidth; }
    public PeakLabel wrapWidth(int width) { this.wrapWidth = Math.max(0, width); return this; }
    public int getLineSpacing() { return lineSpacing; }
    public PeakLabel lineSpacing(int spacing) { this.lineSpacing = spacing; return this; }
    public int getCountUpTicks() { return Math.max(0, countUpTicks); }
    public PeakLabel countUp(int ticks) { this.countUpTicks = Math.max(0, ticks); return this; }
    public PeakLabel countUpSeconds(double seconds) { return countUp((int) Math.round(seconds * 20.0)); }
    public float getTypewriterCharsPerSecond() { return Math.max(0f, typewriterCharsPerSecond); }
    public PeakLabel typewriter(float charsPerSecond) { this.typewriterCharsPerSecond = Math.max(0f, charsPerSecond); return this; }

    public PeakLabelAnimation animation() {
        if (animation == null) animation = new PeakLabelAnimation();
        return animation;
    }

    public void resetAnchor() {
        if (animation != null) animation.reset();
    }

    @Override
    public String elementType() { return "label"; }

    @Override
    protected Set<String> customPatchKeys() {
        Set<String> keys = new HashSet<>(super.customPatchKeys());
        keys.add("replay");
        return keys;
    }

    @Override
    protected void applyCustomPatch(String key, JsonElement value) {
        if (key.equals("replay")) {
            if (value.getAsBoolean()) animation().restart();
            return;
        }
        super.applyCustomPatch(key, value);
    }

    @Override
    protected void afterPatch(JsonObject patch) {
        if (patch.has("text") && !patch.has("translationKey")) this.translationKey = null;
        if (patch.has("translationKey") && !patch.has("translationArgs")) this.translationArgs = new ArrayList<>();
    }
}
