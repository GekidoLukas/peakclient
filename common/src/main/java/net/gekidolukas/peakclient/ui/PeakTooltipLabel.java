package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

public final class PeakTooltipLabel extends PeakElement<PeakTooltipLabel> {
    private Component text;
    private int tooltipPaddingHorizontal = 0;
    private int tooltipPaddingVertical = 0;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    @PeakOpaqueColor private PeakPaint borderColor = PeakPaint.rgba(80, 0, 255, 140);
    private PeakTooltipBackgroundStyle backgroundStyle = PeakTooltipBackgroundStyle.VANILLA;

    private String backgroundTexture;
    private int textureWidth = 16;
    private int textureHeight = 16;
    private int cornerSize = 3;

    public PeakTooltipLabel(String text) { this(PeakId.random(), text); }

    public PeakTooltipLabel(String id, String text) { this(id, Component.literal(text == null ? "" : text)); }

    public PeakTooltipLabel(String id, Component text) {
        super(id);
        this.text = text == null ? Component.empty() : text;
    }

    public static PeakTooltipLabel create(String text) { return new PeakTooltipLabel(text); }
    public static PeakTooltipLabel create(String id, String text) { return new PeakTooltipLabel(id, text); }
    public static PeakTooltipLabel create(Component text) { return new PeakTooltipLabel(PeakId.random(), text); }
    public static PeakTooltipLabel create(String id, Component text) { return new PeakTooltipLabel(id, text); }

    public static PeakTooltipLabel translatable(String key, Object... args) {
        return new PeakTooltipLabel(PeakId.random(), Component.translatable(key, args));
    }

    public Component getText() { return text; }
    public PeakTooltipLabel text(String text) { this.text = Component.literal(text == null ? "" : text); return this; }
    public PeakTooltipLabel text(Component text) { this.text = text == null ? Component.empty() : text; return this; }

    public int getTooltipPaddingHorizontal() { return tooltipPaddingHorizontal; }
    public int getTooltipPaddingVertical() { return tooltipPaddingVertical; }
    public PeakTooltipLabel tooltipPadding(int horizontal, int vertical) {
        this.tooltipPaddingHorizontal = horizontal;
        this.tooltipPaddingVertical = vertical;
        return this;
    }

    public PeakPaint getColor() { return color; }
    public PeakTooltipLabel color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakTooltipLabel color(int argb) { return color(PeakPaint.solid(argb)); }
    public PeakPaint getBorderColor() { return borderColor; }
    public PeakTooltipLabel borderColor(PeakPaint paint) { this.borderColor = paint.opaque(); return this; }
    public PeakTooltipLabel borderColor(int argb) { return borderColor(PeakPaint.solid(argb)); }

    public PeakTooltipBackgroundStyle getBackgroundStyle() { return backgroundStyle; }
    public PeakTooltipLabel backgroundStyle(PeakTooltipBackgroundStyle style) { this.backgroundStyle = style; return this; }

    public String getBackgroundTexture() { return backgroundTexture; }
    public int getTextureWidth() { return textureWidth; }
    public int getTextureHeight() { return textureHeight; }
    public int getCornerSize() { return cornerSize; }

    public PeakTooltipLabel backgroundTexture(String texture, int textureWidth, int textureHeight, int cornerSize) {
        this.backgroundTexture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.cornerSize = cornerSize;
        this.backgroundStyle = PeakTooltipBackgroundStyle.CUSTOM_TEXTURE;
        return this;
    }

    public PeakTooltipLabel backgroundTexture(String texture) {
        return backgroundTexture(texture, 16, 16, 3);
    }

    @Override
    public String elementType() { return "tooltip_label"; }
}
