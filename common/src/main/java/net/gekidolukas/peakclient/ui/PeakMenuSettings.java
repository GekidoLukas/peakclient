package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PeakMenuSettings {
    public static final String VANILLA_PANORAMA = "minecraft:textures/gui/title/background/panorama";

    private String title = "";
    private PeakMenuEscape escape = PeakMenuEscape.CLOSE_ALL;
    @PeakColorOrNone private PeakPaint backdrop = PeakPaint.solid(0x90000000);
    private boolean pauseGame = false;
    private String backdropTexture = "";
    private int backdropTextureWidth = 0;
    private int backdropTextureHeight = 0;
    private PeakObjectFit backdropFit = PeakObjectFit.COVER;
    private boolean backdropBlur = false;
    private String backdropPanorama = "";
    private float backdropPanoramaSpeed = 1f;
    private float backdropPanoramaPitch = 10f;
    private boolean backdropPanoramaOverlay = true;
    private PeakScrollbarStyle scrollbar = new PeakScrollbarStyle();
    private PeakFocusStyle focus = new PeakFocusStyle();
    private PeakTooltipStyle tooltip = new PeakTooltipStyle();
    private PeakSoundSpec clickSound = new PeakSoundSpec();
    private PeakSoundSpec hoverSound;
    @PeakColorOrNone private PeakPaint invalidColor = PeakPaint.solid(0xFFFF5555);
    private List<PeakShortcut> shortcuts = new ArrayList<>();

    public static PeakMenuSettings create() { return new PeakMenuSettings(); }

    public String getTitle() { return title; }
    public PeakMenuSettings title(String title) { this.title = title == null ? "" : title; return this; }

    public PeakMenuEscape getEscape() { return escape; }
    public PeakMenuSettings escape(PeakMenuEscape escape) { this.escape = escape; return this; }

    public boolean isPauseGame() { return pauseGame; }
    public PeakMenuSettings pauseGame(boolean pause) { this.pauseGame = pause; return this; }

    public PeakPaint getBackdrop() { return backdrop; }
    public PeakMenuSettings backdrop(PeakPaint paint) { this.backdrop = paint.orNone(); return this; }
    public PeakMenuSettings backdrop(int argb) { return backdrop(PeakPaint.solid(argb)); }
    public PeakMenuSettings noBackdrop() { this.backdrop = PeakPaint.NONE; return this; }

    public String getBackdropTexture() { return backdropTexture == null ? "" : backdropTexture; }
    public int getBackdropTextureWidth() { return Math.max(0, backdropTextureWidth); }
    public int getBackdropTextureHeight() { return Math.max(0, backdropTextureHeight); }
    public PeakMenuSettings backdropTexture(String texture, int width, int height) {
        this.backdropTexture = texture == null ? "" : texture;
        this.backdropTextureWidth = Math.max(0, width);
        this.backdropTextureHeight = Math.max(0, height);
        return this;
    }

    public PeakObjectFit getBackdropFit() { return backdropFit == null ? PeakObjectFit.COVER : backdropFit; }
    public PeakMenuSettings backdropFit(PeakObjectFit fit) { this.backdropFit = fit; return this; }

    public boolean isBackdropBlur() { return backdropBlur; }
    public PeakMenuSettings backdropBlur(boolean blur) { this.backdropBlur = blur; return this; }

    public String getBackdropPanorama() { return backdropPanorama == null ? "" : backdropPanorama; }
    public PeakMenuSettings backdropPanorama(String basePath) {
        this.backdropPanorama = basePath == null ? "" : basePath;
        return this;
    }
    public PeakMenuSettings backdropPanorama(String basePath, float speed) {
        backdropPanorama(basePath);
        this.backdropPanoramaSpeed = speed;
        return this;
    }
    public PeakMenuSettings vanillaPanorama() { return backdropPanorama(VANILLA_PANORAMA); }
    public PeakMenuSettings noPanorama() { return backdropPanorama(""); }

    public float getBackdropPanoramaSpeed() { return backdropPanoramaSpeed; }
    public PeakMenuSettings backdropPanoramaSpeed(float speed) { this.backdropPanoramaSpeed = speed; return this; }

    public float getBackdropPanoramaPitch() { return backdropPanoramaPitch; }
    public PeakMenuSettings backdropPanoramaPitch(float degrees) { this.backdropPanoramaPitch = degrees; return this; }

    public boolean isBackdropPanoramaOverlay() { return backdropPanoramaOverlay; }
    public PeakMenuSettings backdropPanoramaOverlay(boolean overlay) { this.backdropPanoramaOverlay = overlay; return this; }

    public PeakScrollbarStyle getScrollbar() {
        if (scrollbar == null) scrollbar = new PeakScrollbarStyle();
        return scrollbar;
    }
    public PeakMenuSettings scrollbar(Consumer<PeakScrollbarStyle> customizer) {
        customizer.accept(getScrollbar());
        return this;
    }
    public PeakMenuSettings hideScrollbars() {
        getScrollbar().visible(false);
        return this;
    }

    public PeakFocusStyle getFocus() {
        if (focus == null) focus = new PeakFocusStyle();
        return focus;
    }
    public PeakMenuSettings focus(Consumer<PeakFocusStyle> customizer) {
        customizer.accept(getFocus());
        return this;
    }
    public PeakMenuSettings focusColor(PeakPaint paint) {
        getFocus().color(paint);
        return this;
    }
    public PeakMenuSettings focusColor(int color) {
        getFocus().color(color);
        return this;
    }

    public PeakTooltipStyle getTooltip() {
        if (tooltip == null) tooltip = new PeakTooltipStyle();
        return tooltip;
    }
    public PeakMenuSettings tooltip(Consumer<PeakTooltipStyle> customizer) {
        customizer.accept(getTooltip());
        return this;
    }

    public PeakSoundSpec getClickSound() {
        if (clickSound == null) clickSound = new PeakSoundSpec();
        return clickSound;
    }
    public PeakMenuSettings clickSound(String sound) {
        this.clickSound = PeakSoundSpec.of(sound);
        return this;
    }
    public PeakMenuSettings clickSound(String sound, float volume, float pitch) {
        this.clickSound = PeakSoundSpec.of(sound, volume, pitch);
        return this;
    }
    public PeakMenuSettings noClickSound() {
        this.clickSound = PeakSoundSpec.none();
        return this;
    }

    public PeakSoundSpec getHoverSound() { return hoverSound; }
    public PeakMenuSettings hoverSound(String sound) {
        this.hoverSound = PeakSoundSpec.of(sound);
        return this;
    }
    public PeakMenuSettings hoverSound(String sound, float volume, float pitch) {
        this.hoverSound = PeakSoundSpec.of(sound, volume, pitch);
        return this;
    }

    public PeakPaint getInvalidColor() { return invalidColor; }
    public PeakMenuSettings invalidColor(PeakPaint paint) { this.invalidColor = paint.orNone(); return this; }
    public PeakMenuSettings invalidColor(int argb) { return invalidColor(PeakPaint.solid(argb)); }

    public List<PeakShortcut> getShortcuts() { return shortcuts == null ? List.of() : shortcuts; }
    public PeakMenuSettings shortcut(String spec, PeakAction... actions) {
        if (shortcuts == null) shortcuts = new ArrayList<>();
        shortcuts.add(PeakShortcut.of(spec, actions));
        return this;
    }
}
