package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

public final class PeakOption {
    private String value = "";
    private Component text = Component.empty();
    private boolean enabled = true;

    public static PeakOption of(String value, String text) {
        String safeValue = value == null ? "" : value;
        return of(safeValue, Component.literal(text == null ? safeValue : text));
    }

    public static PeakOption of(String value, Component text) {
        PeakOption option = new PeakOption();
        option.value = value == null ? "" : value;
        option.text = text == null ? Component.literal(option.value) : text;
        return option;
    }

    public static PeakOption of(String value, String text, boolean enabled) {
        return of(value, text).enabled(enabled);
    }

    public static PeakOption of(String value, Component text, boolean enabled) {
        return of(value, text).enabled(enabled);
    }

    public String getValue() { return value == null ? "" : value; }
    public Component getText() { return text == null ? Component.literal(getValue()) : text; }
    public boolean isEnabled() { return enabled; }
    public PeakOption enabled(boolean enabled) { this.enabled = enabled; return this; }
}
