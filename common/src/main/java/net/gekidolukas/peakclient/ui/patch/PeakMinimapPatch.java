package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakBorderShape;
import net.gekidolukas.peakclient.ui.PeakMinimapPlayerIconMode;
import net.gekidolukas.peakclient.ui.PeakWorldBorderSource;

public final class PeakMinimapPatch extends PeakBasePatch<PeakMinimapPatch> {
    public PeakMinimapPatch texture(String texture) { data.addProperty("texture", texture); return this; }
    public PeakMinimapPatch viewRadiusBlocks(double blocks) { data.addProperty("viewRadiusBlocks", blocks); return this; }
    public PeakMinimapPatch worldBorderSource(PeakWorldBorderSource source) { data.addProperty("worldBorderSource", source.name()); return this; }

    public PeakMinimapPatch playerIconRotationOffset(float offset) { data.addProperty("playerIconRotationOffset", offset); return this; }
    public PeakMinimapPatch edgeIndicatorMaxDistance(double distance) { data.addProperty("edgeIndicatorMaxDistance", distance); return this; }
    public PeakMinimapPatch edgeIconScaleRange(float min, float max) {
        data.addProperty("edgeIconMinScale", min);
        data.addProperty("edgeIconMaxScale", max);
        return this;
    }
    public PeakMinimapPatch edgeIconScaleDistanceRange(double min, double max) {
        data.addProperty("edgeIconScaleMinDistance", min);
        data.addProperty("edgeIconScaleMaxDistance", max);
        return this;
    }

    public PeakMinimapPatch waypointLabelScale(float scale) { data.addProperty("waypointLabelScale", scale); return this; }
    public PeakMinimapPatch imageSize(int width, int height) {
        data.addProperty("imageWidth", width);
        data.addProperty("imageHeight", height);
        return this;
    }
    public PeakMinimapPatch worldBounds(double minX, double minZ, double maxX, double maxZ) {
        data.addProperty("worldMinX", minX);
        data.addProperty("worldMinZ", minZ);
        data.addProperty("worldMaxX", maxX);
        data.addProperty("worldMaxZ", maxZ);
        return this;
    }
    public PeakMinimapPatch borderColor(int argb) { data.addProperty("borderColor", argb); return this; }
    public PeakMinimapPatch borderColor(PeakPaint paint) { data.add("borderColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakMinimapPatch borderTexture(String texture, int texWidth, int texHeight, int corner) {
        data.addProperty("borderTexture", texture);
        data.addProperty("borderTextureWidth", texWidth);
        data.addProperty("borderTextureHeight", texHeight);
        data.addProperty("borderTextureCorner", corner);
        return this;
    }
    public PeakMinimapPatch playerIconMode(PeakMinimapPlayerIconMode mode) { data.addProperty("playerIconMode", mode.name()); return this; }
    public PeakMinimapPatch playerIconTexture(String texture) { data.addProperty("playerIconTexture", texture); return this; }
    public PeakMinimapPatch playerIconSize(int value) { data.addProperty("playerIconSize", value); return this; }
    public PeakMinimapPatch worldBorderColor(int argb) { data.addProperty("worldBorderColor", argb); return this; }
    public PeakMinimapPatch worldBorderColor(PeakPaint paint) { data.add("worldBorderColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakMinimapPatch worldBorderThickness(int value) { data.addProperty("worldBorderThickness", value); return this; }
    public PeakMinimapPatch worldBorderShape(PeakBorderShape shape) { data.addProperty("worldBorderShape", shape.name()); return this; }
    public PeakMinimapPatch outOfBorderColor(int argb) { data.addProperty("outOfBorderColor", argb); return this; }
    public PeakMinimapPatch outOfBorderColor(PeakPaint paint) { data.add("outOfBorderColor", PeakPaintAdapter.toTree(paint)); return this; }
    public PeakMinimapPatch outOfImageColor(int argb) { data.addProperty("outOfImageColor", argb); return this; }
    public PeakMinimapPatch outOfImageColor(PeakPaint paint) { data.add("outOfImageColor", PeakPaintAdapter.toTree(paint)); return this; }

    @Override protected PeakMinimapPatch self() { return this; }
}
