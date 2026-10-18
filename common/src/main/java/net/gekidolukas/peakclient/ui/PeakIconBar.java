package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

public final class PeakIconBar extends PeakElement<PeakIconBar> {
    private PeakProgressSource source = PeakProgressSource.MANUAL;
    private double value = 20;
    private double maxValue = 20;
    private int maxIcons = 10;
    private boolean useHalfIcons = true;
    private int iconSize = 9;
    private int iconSpacing = 1;
    private String fullIconTexture;
    private String halfIconTexture;
    private String emptyIconTexture;
    @PeakOpaqueColor private PeakPaint fallbackFullColor = PeakPaint.rgb(220, 60, 60);
    @PeakOpaqueColor private PeakPaint fallbackHalfColor = PeakPaint.rgb(160, 40, 40);
    @PeakOpaqueColor private PeakPaint fallbackEmptyColor = PeakPaint.rgba(60, 60, 60, 160);
    private boolean emptyBehindFullAndHalf = false;
    private boolean hideIfEmpty = false;
    private boolean hideIfFull = false;

    public PeakIconBar() { this(PeakId.random()); }
    public PeakIconBar(String id) { super(id); }

    public static PeakIconBar create() { return new PeakIconBar(); }
    public static PeakIconBar create(String id) { return new PeakIconBar(id); }

    public PeakProgressSource getSource() { return source; }
    public PeakIconBar source(PeakProgressSource source) { this.source = source; return this; }
    public double getValue() { return value; }
    public PeakIconBar value(double value) { this.value = value; return this; }
    public double getMaxValue() { return maxValue; }
    public PeakIconBar maxValue(double maxValue) { this.maxValue = maxValue; return this; }
    public int getMaxIcons() { return maxIcons; }
    public PeakIconBar maxIcons(int maxIcons) { this.maxIcons = maxIcons; return this; }
    public boolean isUseHalfIcons() { return useHalfIcons; }
    public PeakIconBar useHalfIcons(boolean use) { this.useHalfIcons = use; return this; }
    public int getIconSize() { return iconSize; }
    public PeakIconBar iconSize(int size) { this.iconSize = size; return this; }
    public int getIconSpacing() { return iconSpacing; }
    public PeakIconBar iconSpacing(int spacing) { this.iconSpacing = spacing; return this; }
    public String getFullIconTexture() { return fullIconTexture; }
    public PeakIconBar fullIconTexture(String texture) { this.fullIconTexture = texture; return this; }
    public String getHalfIconTexture() { return halfIconTexture; }
    public PeakIconBar halfIconTexture(String texture) { this.halfIconTexture = texture; return this; }
    public String getEmptyIconTexture() { return emptyIconTexture; }
    public PeakIconBar emptyIconTexture(String texture) { this.emptyIconTexture = texture; return this; }
    public PeakPaint getFallbackFullColor() { return fallbackFullColor; }
    public PeakIconBar fallbackFullColor(PeakPaint paint) { this.fallbackFullColor = paint.opaque(); return this; }
    public PeakIconBar fallbackFullColor(int argb) { return fallbackFullColor(PeakPaint.solid(argb)); }
    public PeakPaint getFallbackHalfColor() { return fallbackHalfColor; }
    public PeakIconBar fallbackHalfColor(PeakPaint paint) { this.fallbackHalfColor = paint.opaque(); return this; }
    public PeakIconBar fallbackHalfColor(int argb) { return fallbackHalfColor(PeakPaint.solid(argb)); }
    public PeakPaint getFallbackEmptyColor() { return fallbackEmptyColor; }
    public PeakIconBar fallbackEmptyColor(PeakPaint paint) { this.fallbackEmptyColor = paint.opaque(); return this; }
    public PeakIconBar fallbackEmptyColor(int argb) { return fallbackEmptyColor(PeakPaint.solid(argb)); }
    public boolean isEmptyBehindFullAndHalf() { return emptyBehindFullAndHalf; }
    public PeakIconBar emptyBehindFullAndHalf(boolean behind) { this.emptyBehindFullAndHalf = behind; return this; }
    public boolean isHideIfEmpty() { return hideIfEmpty; }
    public PeakIconBar hideIfEmpty(boolean hide) { this.hideIfEmpty = hide; return this; }
    public boolean isHideIfFull() { return hideIfFull; }
    public PeakIconBar hideIfFull(boolean hide) { this.hideIfFull = hide; return this; }

    @Override
    public String elementType() { return "icon_bar"; }
}
