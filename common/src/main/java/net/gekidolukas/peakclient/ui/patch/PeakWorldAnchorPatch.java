package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakAnchorEdge;
import net.gekidolukas.peakclient.ui.PeakAnchorTarget;
import net.gekidolukas.peakclient.ui.PeakOcclusion;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.UUID;

public final class PeakWorldAnchorPatch extends PeakContainerPatch<PeakWorldAnchorPatch> {
    public PeakWorldAnchorPatch target(PeakAnchorTarget target) {
        data.add("target", PeakUiSerializer.GSON.toJsonTree(target));
        return this;
    }

    public PeakWorldAnchorPatch moveTo(double x, double y, double z) {
        return target(PeakAnchorTarget.at(x, y, z));
    }

    public PeakWorldAnchorPatch followEntity(UUID uuid) {
        return target(PeakAnchorTarget.entity(uuid));
    }

    public PeakWorldAnchorPatch followWaypoint(String waypointId) {
        return target(PeakAnchorTarget.waypoint(waypointId));
    }

    public PeakWorldAnchorPatch screenOffsetX(int x) { data.addProperty("screenOffsetX", x); return this; }
    public PeakWorldAnchorPatch screenOffsetY(int y) { data.addProperty("screenOffsetY", y); return this; }
    public PeakWorldAnchorPatch pivot(PeakAnchor pivot) { data.addProperty("pivot", pivot.name()); return this; }
    public PeakWorldAnchorPatch minDistance(float distance) { data.addProperty("minDistance", Math.max(0f, distance)); return this; }
    public PeakWorldAnchorPatch maxDistance(float distance) { data.addProperty("maxDistance", Math.max(0f, distance)); return this; }
    public PeakWorldAnchorPatch fadeDistance(float distance) { data.addProperty("fadeDistance", Math.max(0f, distance)); return this; }
    public PeakWorldAnchorPatch edge(PeakAnchorEdge edge) { data.addProperty("edge", edge.name()); return this; }
    public PeakWorldAnchorPatch edgeMargin(int margin) { data.addProperty("edgeMargin", Math.max(0, margin)); return this; }
    public PeakWorldAnchorPatch dimension(String dimension) { data.addProperty("dimension", dimension); return this; }

    public PeakWorldAnchorPatch occlusion(PeakOcclusion occlusion) {
        data.add("occlusion", PeakUiSerializer.GSON.toJsonTree(occlusion));
        return this;
    }

    public PeakWorldAnchorPatch occlusionMode(PeakOcclusion.Mode mode) {
        data.addProperty("occlusion.mode", mode.name());
        return this;
    }

    public PeakWorldAnchorPatch noOcclusion() { return occlusionMode(PeakOcclusion.Mode.NONE); }

    @Override protected PeakWorldAnchorPatch self() { return this; }
}
