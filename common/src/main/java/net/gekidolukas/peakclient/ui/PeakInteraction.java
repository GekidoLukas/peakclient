package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

public final class PeakInteraction {
    private boolean enabled = true;
    private boolean silent = false;
    private boolean keepFocus = false;
    private Component tooltip = Component.empty();
    private PeakElement<?> tooltipElement;
    private boolean elementTooltip = false;
    private PeakStyle hoverStyle;
    private PeakStyle pressedStyle;
    private PeakStyle disabledStyle;
    private PeakStyle activeStyle;
    @PeakColorOrNone private PeakPaint focusColor = PeakPaint.NONE;
    private int focusWidth = 0;
    private PeakTooltipStyle tooltipStyle;
    private PeakSoundSpec clickSound;
    private PeakSoundSpec hoverSound;
    private boolean invalid = false;
    private boolean required = false;
    @PeakColorOrNone private PeakPaint invalidColor = PeakPaint.NONE;
    private String errorElement = "";

    public static PeakInteraction create() { return new PeakInteraction(); }

    public boolean isEnabled() { return enabled; }
    public PeakInteraction enabled(boolean enabled) { this.enabled = enabled; return this; }

    public boolean isSilent() { return silent; }
    public PeakInteraction silent(boolean silent) { this.silent = silent; return this; }

    public boolean isKeepFocus() { return keepFocus; }
    public PeakInteraction keepFocus(boolean keep) { this.keepFocus = keep; return this; }

    public Component getTooltip() { return tooltip == null ? Component.empty() : tooltip; }
    public PeakInteraction tooltip(String tooltip) { return tooltip(Component.literal(tooltip == null ? "" : tooltip)); }
    public PeakInteraction tooltip(Component tooltip) {
        this.tooltip = tooltip == null ? Component.empty() : tooltip;
        this.tooltipElement = null;
        this.elementTooltip = false;
        return this;
    }

    public PeakElement<?> getTooltipElement() { return elementTooltip ? tooltipElement : null; }
    public PeakInteraction tooltip(PeakElement<?> element) {
        this.tooltipElement = element;
        this.elementTooltip = element != null;
        this.tooltip = Component.empty();
        return this;
    }

    public boolean hasTooltip() {
        return getTooltipElement() != null || !getTooltip().getString().isEmpty();
    }

    public PeakStyle getHoverStyle() { return hoverStyle; }
    public PeakInteraction hoverStyle(PeakStyle style) { this.hoverStyle = style; return this; }

    public PeakStyle getPressedStyle() { return pressedStyle; }
    public PeakInteraction pressedStyle(PeakStyle style) { this.pressedStyle = style; return this; }

    public PeakStyle getDisabledStyle() { return disabledStyle; }
    public PeakInteraction disabledStyle(PeakStyle style) { this.disabledStyle = style; return this; }

    public PeakStyle getActiveStyle() { return activeStyle; }
    public PeakInteraction activeStyle(PeakStyle style) { this.activeStyle = style; return this; }

    public PeakPaint getFocusColor() { return focusColor; }
    public PeakInteraction focusColor(PeakPaint paint) { this.focusColor = paint.orNone(); return this; }
    public PeakInteraction focusColor(int argb) { return focusColor(PeakPaint.solid(argb)); }

    public int getFocusWidth() { return Math.max(0, focusWidth); }
    public PeakInteraction focusWidth(int width) { this.focusWidth = Math.max(0, width); return this; }

    public PeakTooltipStyle getTooltipStyle() { return tooltipStyle; }
    public PeakInteraction tooltipStyle(PeakTooltipStyle style) { this.tooltipStyle = style; return this; }

    public PeakSoundSpec getClickSound() { return clickSound; }
    public PeakInteraction clickSound(PeakSoundSpec sound) { this.clickSound = sound; return this; }

    public PeakSoundSpec getHoverSound() { return hoverSound; }
    public PeakInteraction hoverSound(PeakSoundSpec sound) { this.hoverSound = sound; return this; }

    public boolean isInvalid() { return invalid; }
    public PeakInteraction invalid(boolean invalid) { this.invalid = invalid; return this; }

    public boolean isRequired() { return required; }
    public PeakInteraction required(boolean required) { this.required = required; return this; }

    public String getErrorElement() { return errorElement == null ? "" : errorElement; }
    public PeakInteraction errorElement(String elementId) { this.errorElement = elementId == null ? "" : elementId; return this; }

    public PeakPaint getInvalidColor() { return invalidColor; }
    public PeakInteraction invalidColor(PeakPaint paint) { this.invalidColor = paint.orNone(); return this; }
    public PeakInteraction invalidColor(int argb) { return invalidColor(PeakPaint.solid(argb)); }
}
