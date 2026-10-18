package net.gekidolukas.peakclient.ui;

public final class PeakSoundSpec {
    public static final String DEFAULT_CLICK = "minecraft:ui.button.click";

    private String sound = DEFAULT_CLICK;
    private float volume = 1f;
    private float pitch = 1f;

    public static PeakSoundSpec create() { return new PeakSoundSpec(); }

    public static PeakSoundSpec of(String sound) {
        PeakSoundSpec spec = new PeakSoundSpec();
        spec.sound = sound == null ? "" : sound;
        return spec;
    }

    public static PeakSoundSpec of(String sound, float volume, float pitch) {
        return of(sound).volume(volume).pitch(pitch);
    }

    public static PeakSoundSpec none() { return of(""); }

    public String getSound() { return sound == null ? "" : sound; }
    public float getVolume() { return Math.max(0f, volume); }
    public float getPitch() { return Math.max(0.1f, pitch); }

    public PeakSoundSpec volume(float volume) { this.volume = Math.max(0f, volume); return this; }
    public PeakSoundSpec pitch(float pitch) { this.pitch = Math.max(0.1f, pitch); return this; }

    public boolean isNone() { return getSound().isEmpty(); }
}
