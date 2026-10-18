package net.gekidolukas.peakclient.ui;

public final class PeakVanillaHudEntry {
    private Boolean enabled;
    private Integer offsetX;
    private Integer offsetY;

    public static PeakVanillaHudEntry create() {
        PeakVanillaHudEntry entry = new PeakVanillaHudEntry();
        entry.enabled = true;
        entry.offsetX = 0;
        entry.offsetY = 0;
        return entry;
    }

    public static PeakVanillaHudEntry disabled() {
        PeakVanillaHudEntry entry = new PeakVanillaHudEntry();
        entry.enabled = false;
        return entry;
    }

    public static PeakVanillaHudEntry shown() {
        PeakVanillaHudEntry entry = new PeakVanillaHudEntry();
        entry.enabled = true;
        return entry;
    }

    public static PeakVanillaHudEntry moved(int x, int y) {
        PeakVanillaHudEntry entry = new PeakVanillaHudEntry();
        entry.offsetX = x;
        entry.offsetY = y;
        return entry;
    }

    public static PeakVanillaHudEntry merge(PeakVanillaHudEntry base, PeakVanillaHudEntry patch) {
        PeakVanillaHudEntry result = new PeakVanillaHudEntry();
        result.enabled = patch.enabled != null ? patch.enabled : (base == null ? Boolean.TRUE : base.isEnabled());
        result.offsetX = patch.offsetX != null ? patch.offsetX : (base == null ? Integer.valueOf(0) : base.getOffsetX());
        result.offsetY = patch.offsetY != null ? patch.offsetY : (base == null ? Integer.valueOf(0) : base.getOffsetY());
        return result;
    }

    public boolean isEnabled() { return enabled == null || enabled; }
    public PeakVanillaHudEntry enabled(boolean enabled) { this.enabled = enabled; return this; }
    public int getOffsetX() { return offsetX == null ? 0 : offsetX; }
    public PeakVanillaHudEntry offsetX(int offsetX) { this.offsetX = offsetX; return this; }
    public int getOffsetY() { return offsetY == null ? 0 : offsetY; }
    public PeakVanillaHudEntry offsetY(int offsetY) { this.offsetY = offsetY; return this; }
    public PeakVanillaHudEntry offset(int x, int y) { this.offsetX = x; this.offsetY = y; return this; }

    public boolean isDefault() { return isEnabled() && getOffsetX() == 0 && getOffsetY() == 0; }
}
