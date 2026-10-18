package net.gekidolukas.peakclient.ui;

public final class PeakOption {
    private String value = "";
    private String text = "";
    private boolean enabled = true;

    public static PeakOption of(String value, String text) {
        PeakOption option = new PeakOption();
        option.value = value == null ? "" : value;
        option.text = text == null ? option.value : text;
        return option;
    }

    public static PeakOption of(String value, String text, boolean enabled) {
        return of(value, text).enabled(enabled);
    }

    public String getValue() { return value == null ? "" : value; }
    public String getText() { return text == null ? getValue() : text; }
    public boolean isEnabled() { return enabled; }
    public PeakOption enabled(boolean enabled) { this.enabled = enabled; return this; }
}
