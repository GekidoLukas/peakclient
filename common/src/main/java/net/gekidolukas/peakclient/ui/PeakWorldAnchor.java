package net.gekidolukas.peakclient.ui;

import java.util.UUID;
import java.util.function.Consumer;

public final class PeakWorldAnchor extends PeakContainer<PeakWorldAnchor> {
    private PeakAnchorTarget target = PeakAnchorTarget.at(0, 0, 0);
    private int screenOffsetX = 0;
    private int screenOffsetY = 0;
    private PeakAnchor pivot = PeakAnchor.BOTTOM_CENTER;
    private float minDistance = 0f;
    private float maxDistance = 0f;
    private float fadeDistance = 0f;
    private PeakAnchorScale scaleMode = PeakAnchorScale.NONE;
    private float referenceDistance = 10f;
    private float minScale = 0.5f;
    private float maxScale = 1.5f;
    private PeakAnchorEdge edge = PeakAnchorEdge.HIDE;
    private int edgeMargin = 12;
    @PeakOpaqueColor private PeakPaint edgeArrowColor = PeakPaint.WHITE;
    private int edgeArrowSize = 7;
    private boolean edgeShowContent = true;
    private PeakOcclusion occlusion = new PeakOcclusion();
    private String dimension = "";

    public PeakWorldAnchor() { this(PeakId.random()); }

    public PeakWorldAnchor(String id) {
        super(id);
        this.positionMode = PeakPositionMode.ABSOLUTE;
    }

    public static PeakWorldAnchor create() { return new PeakWorldAnchor(); }
    public static PeakWorldAnchor create(String id) { return new PeakWorldAnchor(id); }
    public static PeakWorldAnchor at(double x, double y, double z) { return new PeakWorldAnchor().target(PeakAnchorTarget.at(x, y, z)); }
    public static PeakWorldAnchor at(String id, double x, double y, double z) { return new PeakWorldAnchor(id).target(PeakAnchorTarget.at(x, y, z)); }
    public static PeakWorldAnchor entity(UUID uuid) { return new PeakWorldAnchor().target(PeakAnchorTarget.entity(uuid)); }
    public static PeakWorldAnchor entity(String id, UUID uuid) { return new PeakWorldAnchor(id).target(PeakAnchorTarget.entity(uuid)); }
    public static PeakWorldAnchor waypoint(String waypointId) { return new PeakWorldAnchor().target(PeakAnchorTarget.waypoint(waypointId)); }

    @Override
    public PeakPositionMode getPositionMode() { return PeakPositionMode.ABSOLUTE; }

    public PeakAnchorTarget getTarget() {
        if (target == null) target = PeakAnchorTarget.at(0, 0, 0);
        return target;
    }
    public PeakWorldAnchor target(PeakAnchorTarget target) { this.target = target == null ? PeakAnchorTarget.at(0, 0, 0) : target; return this; }
    public PeakWorldAnchor target(Consumer<PeakAnchorTarget> customizer) { customizer.accept(getTarget()); return this; }
    public PeakWorldAnchor above(double blocks) { getTarget().above(blocks); return this; }
    public PeakWorldAnchor worldOffset(double x, double y, double z) { getTarget().offset(x, y, z); return this; }

    public int getScreenOffsetX() { return screenOffsetX; }
    public int getScreenOffsetY() { return screenOffsetY; }
    public PeakWorldAnchor screenOffset(int x, int y) { this.screenOffsetX = x; this.screenOffsetY = y; return this; }

    public PeakAnchor getPivot() { return pivot == null ? PeakAnchor.BOTTOM_CENTER : pivot; }
    public PeakWorldAnchor pivot(PeakAnchor pivot) { this.pivot = pivot; return this; }

    public float getMinDistance() { return Math.max(0f, minDistance); }
    public float getMaxDistance() { return Math.max(0f, maxDistance); }
    public float getFadeDistance() { return Math.max(0f, fadeDistance); }
    public PeakWorldAnchor distanceRange(float min, float max) {
        this.minDistance = Math.max(0f, min);
        this.maxDistance = Math.max(0f, max);
        return this;
    }
    public PeakWorldAnchor maxDistance(float max) { this.maxDistance = Math.max(0f, max); return this; }
    public PeakWorldAnchor minDistance(float min) { this.minDistance = Math.max(0f, min); return this; }
    public PeakWorldAnchor fadeDistance(float fade) { this.fadeDistance = Math.max(0f, fade); return this; }

    public PeakAnchorScale getScaleMode() { return scaleMode == null ? PeakAnchorScale.NONE : scaleMode; }
    public float getReferenceDistance() { return Math.max(0.1f, referenceDistance); }
    public float getMinScale() { return Math.max(0.05f, minScale); }
    public float getMaxScale() { return Math.max(getMinScale(), maxScale); }
    public PeakWorldAnchor scaleWithDistance(float referenceDistance, float minScale, float maxScale) {
        this.scaleMode = PeakAnchorScale.PERSPECTIVE;
        this.referenceDistance = Math.max(0.1f, referenceDistance);
        this.minScale = Math.max(0.05f, minScale);
        this.maxScale = Math.max(this.minScale, maxScale);
        return this;
    }
    public PeakWorldAnchor scaleWithDistance() { return scaleWithDistance(referenceDistance, minScale, maxScale); }
    public PeakWorldAnchor fixedScale() { this.scaleMode = PeakAnchorScale.NONE; return this; }

    public PeakAnchorEdge getEdge() { return edge == null ? PeakAnchorEdge.HIDE : edge; }
    public int getEdgeMargin() { return Math.max(0, edgeMargin); }
    public PeakPaint getEdgeArrowColor() { return edgeArrowColor == null ? PeakPaint.WHITE : edgeArrowColor; }
    public int getEdgeArrowSize() { return Math.max(0, edgeArrowSize); }
    public boolean isEdgeShowContent() { return edgeShowContent; }
    public PeakWorldAnchor edge(PeakAnchorEdge edge) { this.edge = edge; return this; }
    public PeakWorldAnchor clampToEdge() { return edge(PeakAnchorEdge.CLAMP); }
    public PeakWorldAnchor clampToEdge(int margin) { this.edgeMargin = Math.max(0, margin); return edge(PeakAnchorEdge.CLAMP); }
    public PeakWorldAnchor edgeArrow(PeakPaint color, int size) {
        this.edgeArrowColor = color.opaque();
        this.edgeArrowSize = Math.max(0, size);
        return this;
    }
    public PeakWorldAnchor edgeArrow(int argb, int size) { return edgeArrow(PeakPaint.solid(argb), size); }
    public PeakWorldAnchor edgeShowContent(boolean show) { this.edgeShowContent = show; return this; }

    public PeakOcclusion getOcclusion() {
        if (occlusion == null) occlusion = new PeakOcclusion();
        return occlusion;
    }
    public PeakWorldAnchor occlusion(PeakOcclusion occlusion) { this.occlusion = occlusion == null ? new PeakOcclusion() : occlusion; return this; }
    public PeakWorldAnchor occlusion(PeakOcclusion.Mode mode) { getOcclusion().mode(mode); return this; }
    public PeakWorldAnchor noOcclusion() { getOcclusion().mode(PeakOcclusion.Mode.NONE); return this; }

    public String getDimension() { return dimension == null ? "" : dimension; }
    public PeakWorldAnchor dimension(String dimension) { this.dimension = dimension == null ? "" : dimension; return this; }

    @Override
    public String elementType() { return "world_anchor"; }
}
