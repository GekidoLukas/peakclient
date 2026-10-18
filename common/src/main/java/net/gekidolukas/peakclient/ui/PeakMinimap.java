package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

public final class PeakMinimap extends PeakElement<PeakMinimap> {
    private String texture;
    private int imageWidth = 512;
    private int imageHeight = 512;
    private double worldMinX, worldMinZ, worldMaxX, worldMaxZ;
    private double viewRadiusBlocks = 128;

    @PeakColorOrNone private PeakPaint borderColor = PeakPaint.NONE;
    private String borderTexture;
    private int borderTextureWidth = 32, borderTextureHeight = 32, borderTextureCorner = 8;

    private PeakMinimapPlayerIconMode playerIconMode = PeakMinimapPlayerIconMode.TEXTURE;
    private String playerIconTexture;
    private int playerIconSize = 8;
    private float playerIconRotationOffset = 180f;

    private PeakWorldBorderSource worldBorderSource = PeakWorldBorderSource.NONE;
    @PeakOpaqueColor private PeakPaint worldBorderColor = PeakPaint.rgb(255, 60, 60);
    private int worldBorderThickness = 2;
    @PeakColorOrNone private PeakPaint outOfBorderColor = PeakPaint.rgba(0, 0, 0, 0);
    @PeakColorOrNone private PeakPaint outOfImageColor = PeakMapColorPresets.WATER;
    private PeakBorderShape worldBorderShape = PeakBorderShape.SQUARE;

    private double edgeIndicatorMaxDistance = 0; // 0 = deactivated
    private float edgeIconMinScale = 0.6f;
    private float edgeIconMaxScale = 1.4f;
    private double edgeIconScaleMinDistance = 10;
    private double edgeIconScaleMaxDistance = 200;

    private float waypointLabelScale = 0.7f;

    public PeakMinimap(String texture) { this(PeakId.random(), texture); }

    public PeakMinimap(String id, String texture) {
        super(id);
        this.texture = texture;
        size(PeakSize.of(PeakSize.PeakDimension.pixels(128), PeakSize.PeakDimension.pixels(128)));
    }

    public static PeakMinimap create(String texture) { return new PeakMinimap(texture); }

    public String getTexture() { return texture; }
    public PeakMinimap texture(String texture) { this.texture = texture; return this; }
    public int getImageWidth() { return imageWidth; }
    public int getImageHeight() { return imageHeight; }
    public PeakMinimap imageSize(int width, int height) { this.imageWidth = width; this.imageHeight = height; return this; }

    public double getWorldMinX() { return worldMinX; }
    public double getWorldMinZ() { return worldMinZ; }
    public double getWorldMaxX() { return worldMaxX; }
    public double getWorldMaxZ() { return worldMaxZ; }

    public PeakMinimap worldBounds(double minX, double minZ, double maxX, double maxZ) {
        this.worldMinX = minX; this.worldMinZ = minZ; this.worldMaxX = maxX; this.worldMaxZ = maxZ;
        return this;
    }

    public double getViewRadiusBlocks() { return viewRadiusBlocks; }
    public PeakMinimap viewRadiusBlocks(double blocks) { this.viewRadiusBlocks = blocks; return this; }

    public PeakPaint getBorderColor() { return borderColor; }
    public PeakMinimap borderColor(PeakPaint paint) { this.borderColor = paint.orNone(); return this; }
    public PeakMinimap borderColor(int argb) { return borderColor(PeakPaint.solid(argb)); }
    public String getBorderTexture() { return borderTexture; }
    public int getBorderTextureWidth() { return borderTextureWidth; }
    public int getBorderTextureHeight() { return borderTextureHeight; }
    public int getBorderTextureCorner() { return borderTextureCorner; }
    public PeakMinimap borderTexture(String texture, int texWidth, int texHeight, int corner) {
        this.borderTexture = texture; this.borderTextureWidth = texWidth; this.borderTextureHeight = texHeight; this.borderTextureCorner = corner;
        return this;
    }
    public PeakMinimap borderTexture(String texture) { return borderTexture(texture, 32, 32, 8); }

    public PeakMinimapPlayerIconMode getPlayerIconMode() { return playerIconMode; }
    public PeakMinimap playerIconMode(PeakMinimapPlayerIconMode mode) { this.playerIconMode = mode; return this; }
    public String getPlayerIconTexture() { return playerIconTexture; }
    public PeakMinimap playerIconTexture(String texture) {
        this.playerIconTexture = texture;
        this.playerIconMode = PeakMinimapPlayerIconMode.TEXTURE;
        return this;
    }
    public int getPlayerIconSize() { return playerIconSize; }
    public PeakMinimap playerIconSize(int size) { this.playerIconSize = size; return this; }
    public float getPlayerIconRotationOffset() { return playerIconRotationOffset; }
    public PeakMinimap playerIconRotationOffset(float offset) { this.playerIconRotationOffset = offset; return this; }

    public PeakWorldBorderSource getWorldBorderSource() { return worldBorderSource; }
    public PeakMinimap worldBorderSource(PeakWorldBorderSource source) { this.worldBorderSource = source; return this; }
    public PeakPaint getWorldBorderColor() { return worldBorderColor; }
    public PeakMinimap worldBorderColor(PeakPaint paint) { this.worldBorderColor = paint.opaque(); return this; }
    public PeakMinimap worldBorderColor(int argb) { return worldBorderColor(PeakPaint.solid(argb)); }
    public int getWorldBorderThickness() { return worldBorderThickness; }
    public PeakMinimap worldBorderThickness(int thickness) { this.worldBorderThickness = thickness; return this; }
    public PeakPaint getOutOfBorderColor() { return outOfBorderColor; }
    public PeakMinimap outOfBorderColor(PeakPaint paint) { this.outOfBorderColor = paint.orNone(); return this; }
    public PeakMinimap outOfBorderColor(int argb) { return outOfBorderColor(PeakPaint.solid(argb)); }
    public PeakPaint getOutOfImageColor() { return outOfImageColor; }
    public PeakMinimap outOfImageColor(PeakPaint paint) { this.outOfImageColor = paint.orNone(); return this; }
    public PeakMinimap outOfImageColor(int argb) { return outOfImageColor(PeakPaint.solid(argb)); }

    public PeakBorderShape getWorldBorderShape() { return worldBorderShape; }
    public PeakMinimap worldBorderShape(PeakBorderShape shape) { this.worldBorderShape = shape; return this; }

    public double getEdgeIndicatorMaxDistance() { return edgeIndicatorMaxDistance; }
    public PeakMinimap edgeIndicatorMaxDistance(double distance) { this.edgeIndicatorMaxDistance = distance; return this; }
    public float getEdgeIconMinScale() { return edgeIconMinScale; }
    public float getEdgeIconMaxScale() { return edgeIconMaxScale; }
    public PeakMinimap edgeIconScaleRange(float min, float max) { this.edgeIconMinScale = min; this.edgeIconMaxScale = max; return this; }
    public double getEdgeIconScaleMinDistance() { return edgeIconScaleMinDistance; }
    public double getEdgeIconScaleMaxDistance() { return edgeIconScaleMaxDistance; }
    public PeakMinimap edgeIconScaleDistanceRange(double min, double max) {
        this.edgeIconScaleMinDistance = min;
        this.edgeIconScaleMaxDistance = max;
        return this;
    }

    public float getWaypointLabelScale() { return waypointLabelScale; }
    public PeakMinimap waypointLabelScale(float scale) { this.waypointLabelScale = Math.max(0.1f, scale); return this; }
    @Override
    public String elementType() { return "minimap"; }
}
