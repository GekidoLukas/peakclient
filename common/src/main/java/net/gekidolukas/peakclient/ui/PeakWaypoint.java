package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PeakWaypoint {
    public static final int AUTO_OFFSET = Integer.MIN_VALUE;

    private String id = PeakId.random();
    private double x, y, z;
    private Component label;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    private String icon;
    private float iconScale = 1.0f;
    private int labelYOffset = AUTO_OFFSET;
    private boolean labelOnMap = false;
    private int expireAfterTicks = 0;

    private String trackedEntityUuid;
    private PeakEntityTrackingMode entityTrackingMode = PeakEntityTrackingMode.NONE;

    private final Set<String> visibleInElementIds = new LinkedHashSet<>();

    private PeakWaypoint() {}

    public static PeakWaypoint of(double x, double y, double z, String label) {
        return of(x, y, z, label == null ? null : Component.literal(label));
    }

    public static PeakWaypoint of(double x, double y, double z, Component label) {
        PeakWaypoint w = new PeakWaypoint();
        w.x = x; w.y = y; w.z = z; w.label = label;
        return w;
    }

    public static PeakWaypoint trackingEntity(String entityUuid, PeakEntityTrackingMode mode,
                                              double initialX, double initialY, double initialZ) {
        PeakWaypoint w = new PeakWaypoint();
        w.trackedEntityUuid = entityUuid;
        w.entityTrackingMode = mode;
        w.x = initialX; w.y = initialY; w.z = initialZ;
        return w;
    }

    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public Component getLabel() { return label; }
    public PeakWaypoint label(String label) { this.label = label == null ? null : Component.literal(label); return this; }
    public PeakWaypoint label(Component label) { this.label = label; return this; }
    public PeakPaint getColor() { return color; }
    public PeakWaypoint color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakWaypoint color(int argb) { return color(PeakPaint.solid(argb)); }
    public String getIcon() { return icon; }
    public PeakWaypoint icon(String texture) { this.icon = texture; return this; }
    public float getIconScale() { return iconScale; }
    public PeakWaypoint iconScale(float scale) { this.iconScale = scale; return this; }
    public int getLabelYOffset() { return labelYOffset; }
    public PeakWaypoint labelYOffset(int offset) { this.labelYOffset = offset; return this; }

    public int getExpireAfterTicks() { return expireAfterTicks; }
    public PeakWaypoint expireAfterTicks(int ticks) { this.expireAfterTicks = Math.max(0, ticks); return this; }

    public boolean isLabelOnMap() { return labelOnMap; }
    public PeakWaypoint labelOnMap(boolean labelOnMap) { this.labelOnMap = labelOnMap; return this; }

    public String getTrackedEntityUuid() { return trackedEntityUuid; }
    public PeakEntityTrackingMode getEntityTrackingMode() { return entityTrackingMode; }

    public void updateLiveTrackedPosition(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }
    public Set<String> getVisibleInElementIds() { return visibleInElementIds; }
    public PeakWaypoint visibleIn(String... elementIds) {
        visibleInElementIds.addAll(List.of(elementIds));
        return this;
    }
    public boolean isVisibleIn(String elementId) {
        return visibleInElementIds.isEmpty() || visibleInElementIds.contains(elementId);
    }
}