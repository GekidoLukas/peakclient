package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.api.PeakTargets;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class PeakPopupBuilder {
    private final PeakTargets targets;
    private final List<PeakElement<?>> children = new ArrayList<>();
    private PeakPopupAnimation enterAnimation = PeakPopupAnimation.FADE;
    private PeakPopupAnimation exitAnimation = PeakPopupAnimation.FADE;
    private PeakPaint backgroundColor = PeakPaint.rgba(0, 0, 0, 160);
    private int padding = 6;
    private PeakLayoutDirection direction = PeakLayoutDirection.COLUMN;
    private int timeToLiveTicks = 60;

    public PeakPopupBuilder(PeakTargets targets) {
        this.targets = targets;
    }

    public PeakPopupBuilder enterAnimation(PeakPopupAnimation animation) { this.enterAnimation = animation; return this; }
    public PeakPopupBuilder exitAnimation(PeakPopupAnimation animation) { this.exitAnimation = animation; return this; }
    public PeakPopupBuilder background(PeakPaint paint) { this.backgroundColor = paint; return this; }
    public PeakPopupBuilder background(int argb) { return background(PeakPaint.solid(argb)); }
    public PeakPopupBuilder padding(int pixels) { this.padding = pixels; return this; }
    public PeakPopupBuilder direction(PeakLayoutDirection direction) { this.direction = direction; return this; }
    public PeakPopupBuilder timeToLiveTicks(int ticks) { this.timeToLiveTicks = Math.max(1, ticks); return this; }

    public PeakPopupBuilder addChild(PeakElement<?> child) { children.add(child); return this; }
    public PeakPopupBuilder addLabel(String text) { children.add(PeakLabel.create(text)); return this; }
    public PeakPopupBuilder addLabel(String text, PeakPaint color) { children.add(PeakLabel.create(text).color(color)); return this; }
    public PeakPopupBuilder addLabel(String text, int color) { return addLabel(text, PeakPaint.solid(color)); }
    public PeakPopupBuilder addLabel(Component text) { children.add(PeakLabel.create(text)); return this; }
    public PeakPopupBuilder addLabel(Component text, PeakPaint color) { children.add(PeakLabel.create(text).color(color)); return this; }
    public PeakPopupBuilder addLabel(Component text, int color) { return addLabel(text, PeakPaint.solid(color)); }

    public void show() {
        PeakHud.showPopup(targets.ids(), children, enterAnimation, exitAnimation, backgroundColor, padding, direction, timeToLiveTicks);
    }
}
