package net.gekidolukas.peakclient.ui;

import java.util.UUID;

public final class PeakAnchorTarget {
    public enum Kind { POSITION, ENTITY, WAYPOINT }

    public enum EntityPoint { ABOVE_HEAD, CENTER, FEET }

    private Kind kind = Kind.POSITION;
    private double x;
    private double y;
    private double z;
    private String entityUuid = "";
    private EntityPoint point = EntityPoint.ABOVE_HEAD;
    private String waypointId = "";
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    private transient UUID parsedUuid;
    private transient String parsedFrom;

    public static PeakAnchorTarget at(double x, double y, double z) {
        PeakAnchorTarget target = new PeakAnchorTarget();
        target.x = x;
        target.y = y;
        target.z = z;
        return target;
    }

    public static PeakAnchorTarget entity(UUID uuid) {
        return entity(uuid.toString());
    }

    public static PeakAnchorTarget entity(String uuid) {
        PeakAnchorTarget target = new PeakAnchorTarget();
        target.kind = Kind.ENTITY;
        target.entityUuid = uuid == null ? "" : uuid;
        return target;
    }

    public static PeakAnchorTarget waypoint(String waypointId) {
        PeakAnchorTarget target = new PeakAnchorTarget();
        target.kind = Kind.WAYPOINT;
        target.waypointId = waypointId == null ? "" : waypointId;
        return target;
    }

    public Kind getKind() { return kind == null ? Kind.POSITION : kind; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public PeakAnchorTarget position(double x, double y, double z) {
        this.kind = Kind.POSITION;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public String getEntityUuid() { return entityUuid == null ? "" : entityUuid; }
    public EntityPoint getPoint() { return point == null ? EntityPoint.ABOVE_HEAD : point; }
    public PeakAnchorTarget point(EntityPoint point) { this.point = point; return this; }

    public String getWaypointId() { return waypointId == null ? "" : waypointId; }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public PeakAnchorTarget offset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public PeakAnchorTarget above(double blocks) {
        this.offsetY = blocks;
        return this;
    }

    public UUID parsedEntityUuid() {
        String text = getEntityUuid();
        if (text.isEmpty()) return null;
        if (!text.equals(parsedFrom)) {
            parsedFrom = text;
            try {
                parsedUuid = UUID.fromString(text);
            } catch (IllegalArgumentException e) {
                parsedUuid = null;
            }
        }
        return parsedUuid;
    }
}
