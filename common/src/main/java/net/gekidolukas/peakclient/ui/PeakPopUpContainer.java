package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakClientTickCounter;

import java.util.HashSet;
import java.util.Set;

public final class PeakPopUpContainer extends PeakContainer<PeakPopUpContainer> {
    private boolean animateEnter = true;
    private boolean animateExit = true;
    private PeakPopupAnimation enterAnimation = PeakPopupAnimation.FADE;
    private PeakPopupAnimation exitAnimation = PeakPopupAnimation.FADE;
    private int enterDurationTicks = 6;
    private int exitDurationTicks = 6;
    private int timeToLiveTicks = 0;
    private boolean autoRemoveOnExpire = true;
    private int slideDistance = 40;

    private transient long spawnTick = PeakClientTickCounter.current();
    private transient long closeStartTick = -1;
    private transient boolean removalRequested = false;

    public PeakPopUpContainer() { this(PeakId.random()); }
    public PeakPopUpContainer(String id) { super(id); }
    public static PeakPopUpContainer create() { return new PeakPopUpContainer(); }
    public static PeakPopUpContainer create(String id) { return new PeakPopUpContainer(id); }

    public boolean isAnimateEnter() { return animateEnter; }
    public PeakPopUpContainer animateEnter(boolean animate) { this.animateEnter = animate; return this; }
    public boolean isAnimateExit() { return animateExit; }
    public PeakPopUpContainer animateExit(boolean animate) { this.animateExit = animate; return this; }
    public PeakPopupAnimation getEnterAnimation() { return enterAnimation; }
    public PeakPopUpContainer enterAnimation(PeakPopupAnimation anim) { this.enterAnimation = anim; return this; }
    public PeakPopupAnimation getExitAnimation() { return exitAnimation; }
    public PeakPopUpContainer exitAnimation(PeakPopupAnimation anim) { this.exitAnimation = anim; return this; }
    public int getEnterDurationTicks() { return enterDurationTicks; }
    public PeakPopUpContainer enterDurationTicks(int ticks) { this.enterDurationTicks = ticks; return this; }
    public int getExitDurationTicks() { return exitDurationTicks; }
    public PeakPopUpContainer exitDurationTicks(int ticks) { this.exitDurationTicks = ticks; return this; }
    public int getTimeToLiveTicks() { return timeToLiveTicks; }
    public PeakPopUpContainer timeToLiveTicks(int ticks) { this.timeToLiveTicks = ticks; return this; }
    public boolean isAutoRemoveOnExpire() { return autoRemoveOnExpire; }
    public PeakPopUpContainer autoRemoveOnExpire(boolean autoRemove) { this.autoRemoveOnExpire = autoRemove; return this; }
    public int getSlideDistance() { return slideDistance; }
    public PeakPopUpContainer slideDistance(int distance) { this.slideDistance = distance; return this; }

    public void resetAnchor() {
        spawnTick = PeakClientTickCounter.current();
        closeStartTick = -1;
        removalRequested = false;
    }

    public void requestClose() {
        if (closeStartTick < 0) closeStartTick = PeakClientTickCounter.current();
    }

    public boolean isRemovalRequested() { return removalRequested; }

    public PeakAnimState computeAnimState() {
        long now = PeakClientTickCounter.current();

        if (timeToLiveTicks > 0 && closeStartTick < 0 && now - spawnTick >= timeToLiveTicks) {
            closeStartTick = now;
        }

        if (closeStartTick >= 0) {
            long elapsed = now - closeStartTick;
            if (!animateExit || exitAnimation == PeakPopupAnimation.NONE) {
                if (autoRemoveOnExpire) removalRequested = true;
                return PeakAnimState.HIDDEN;
            }
            float progress = exitDurationTicks <= 0 ? 1f : Math.min(1f, elapsed / (float) exitDurationTicks);
            if (progress >= 1f) {
                if (autoRemoveOnExpire) removalRequested = true;
                return PeakAnimState.HIDDEN;
            }
            return PeakTransitions.sample(exitAnimation, 1f - progress, slideDistance);
        }

        long elapsedIn = now - spawnTick;
        if (!animateEnter || enterAnimation == PeakPopupAnimation.NONE) return PeakAnimState.VISIBLE;
        float progress = enterDurationTicks <= 0 ? 1f : Math.min(1f, elapsedIn / (float) enterDurationTicks);
        return PeakTransitions.sample(enterAnimation, progress, slideDistance);
    }

    @Override
    public String elementType() { return "popup"; }

    @Override
    protected Set<String> customPatchKeys() {
        Set<String> keys = new HashSet<>(super.customPatchKeys());
        keys.add("close");
        return keys;
    }

    @Override
    protected void applyCustomPatch(String key, JsonElement value) {
        if (key.equals("close")) {
            if (value.getAsBoolean()) requestClose();
            return;
        }
        super.applyCustomPatch(key, value);
    }
}
