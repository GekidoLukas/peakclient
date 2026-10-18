package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class PeakReel extends PeakContainer<PeakReel> {
    public record SpinRequest(int target, int ticks, int loops, String targetId) {}

    private PeakCarouselDirection movement = PeakCarouselDirection.UP;
    private int spinTicks = 100;
    private int minLoops = 2;
    private PeakReelEasing easing = PeakReelEasing.DECELERATE;
    private int startIndex = 0;
    private int fadeStart = 0;
    private int fadeEnd = 0;
    private int autoSpinSlot = 0;
    private int autoSpinDelayTicks = 0;
    private String autoSpinTargetId = "";

    private transient PeakReelMotion motion;
    private transient SpinRequest spinRequest;
    private transient int jumpSlot;
    private transient boolean stopRequested;
    private transient int autoSpinState;
    private transient double autoSpinDue;
    private transient List<String> knownIds;

    public PeakReel() { this(PeakId.random()); }
    public PeakReel(String id) { super(id); }
    public static PeakReel create() { return new PeakReel(); }
    public static PeakReel create(String id) { return new PeakReel(id); }

    public PeakCarouselDirection getMovement() { return movement == null ? PeakCarouselDirection.UP : movement; }
    public PeakReel movement(PeakCarouselDirection movement) { this.movement = movement; return this; }
    public int getSpinTicks() { return Math.max(1, spinTicks); }
    public PeakReel spinTicks(int ticks) { this.spinTicks = Math.max(1, ticks); return this; }
    public PeakReel spinSeconds(double seconds) { return spinTicks((int) Math.round(seconds * 20.0)); }
    public int getMinLoops() { return Math.max(0, minLoops); }
    public PeakReel minLoops(int loops) { this.minLoops = Math.max(0, loops); return this; }
    public PeakReelEasing getEasing() { return easing == null ? PeakReelEasing.DECELERATE : easing; }
    public PeakReel easing(PeakReelEasing easing) { this.easing = easing; return this; }
    public int getStartIndex() { return Math.max(0, startIndex); }
    public PeakReel startIndex(int index) { this.startIndex = Math.max(0, index); return this; }
    public int getFadeStart() { return Math.max(0, fadeStart); }
    public PeakReel fadeStart(int pixels) { this.fadeStart = Math.max(0, pixels); return this; }
    public int getFadeEnd() { return Math.max(0, fadeEnd); }
    public PeakReel fadeEnd(int pixels) { this.fadeEnd = Math.max(0, pixels); return this; }
    public PeakReel fade(int pixels) { return fadeStart(pixels).fadeEnd(pixels); }
    public int getAutoSpinTarget() { return Math.max(0, autoSpinSlot) - 1; }
    public int getAutoSpinDelayTicks() { return Math.max(0, autoSpinDelayTicks); }
    public PeakReel spinOnStart(int target) { return spinOnStart(target, 0); }
    public PeakReel spinOnStart(int target, int delayTicks) {
        this.autoSpinSlot = Math.max(0, target) + 1;
        this.autoSpinDelayTicks = Math.max(0, delayTicks);
        return this;
    }
    public String getAutoSpinTargetId() { return autoSpinTargetId == null ? "" : autoSpinTargetId; }
    public PeakReel spinOnStart(String childId) { return spinOnStart(childId, 0); }
    public PeakReel spinOnStart(String childId, int delayTicks) {
        this.autoSpinTargetId = childId == null ? "" : childId;
        this.autoSpinSlot = 0;
        this.autoSpinDelayTicks = Math.max(0, delayTicks);
        return this;
    }
    public PeakReel spinOnStart(PeakElement<?> element) { return spinOnStart(element, 0); }
    public PeakReel spinOnStart(PeakElement<?> element, int delayTicks) {
        boolean present = false;
        for (PeakElement<?> child : getChildren()) {
            if (child.getId().equals(element.getId())) {
                present = true;
                break;
            }
        }
        if (!present) getChildren().add(ThreadLocalRandom.current().nextInt(getChildren().size() + 1), element);
        return spinOnStart(element.getId(), delayTicks);
    }
    public PeakReel noSpinOnStart() {
        this.autoSpinSlot = 0;
        this.autoSpinTargetId = "";
        return this;
    }

    public List<String> knownIds() {
        return knownIds;
    }

    public void rememberIds(List<String> ids) {
        this.knownIds = List.copyOf(ids);
    }

    public PeakReelMotion motion() {
        if (motion == null) motion = new PeakReelMotion();
        return motion;
    }

    public SpinRequest takeSpinRequest() {
        SpinRequest request = spinRequest;
        spinRequest = null;
        return request;
    }

    public int takeJumpRequest() {
        int index = jumpSlot - 1;
        jumpSlot = 0;
        return index;
    }

    public boolean hasAutoSpin() {
        return getAutoSpinTarget() >= 0 || !getAutoSpinTargetId().isEmpty();
    }

    public boolean isAutoSpinPending(double now) {
        if (!hasAutoSpin() || autoSpinState >= 2) return false;
        if (autoSpinState == 0) {
            autoSpinDue = now + getAutoSpinDelayTicks();
            autoSpinState = 1;
        }
        return now >= autoSpinDue;
    }

    public void finishAutoSpin() {
        autoSpinState = 2;
    }

    public boolean takeStopRequest() {
        boolean requested = stopRequested;
        stopRequested = false;
        return requested;
    }

    public void resetAnchor() {
        if (motion != null) motion.reset();
        spinRequest = null;
        jumpSlot = 0;
        stopRequested = false;
        autoSpinState = 0;
        autoSpinDue = 0;
        knownIds = null;
    }

    @Override
    public String elementType() { return "reel"; }

    @Override
    protected Set<String> customPatchKeys() {
        Set<String> keys = new HashSet<>(super.customPatchKeys());
        keys.add("spin");
        keys.add("jump");
        keys.add("stop");
        return keys;
    }

    @Override
    protected void applyCustomPatch(String key, JsonElement value) {
        switch (key) {
            case "spin" -> {
                if (!value.isJsonObject()) return;
                JsonObject object = value.getAsJsonObject();
                int target = object.has("target") ? object.get("target").getAsInt() : 0;
                int ticks = object.has("ticks") ? object.get("ticks").getAsInt() : -1;
                int loops = object.has("loops") ? object.get("loops").getAsInt() : -1;
                String targetId = object.has("targetId") ? object.get("targetId").getAsString() : null;
                spinRequest = new SpinRequest(Math.max(0, target), ticks, loops, targetId);
            }
            case "jump" -> jumpSlot = Math.max(0, value.getAsInt()) + 1;
            case "stop" -> {
                if (value.getAsBoolean()) stopRequested = true;
            }
            default -> super.applyCustomPatch(key, value);
        }
    }
}
