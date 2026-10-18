package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PeakCompass extends PeakElement<PeakCompass> {
    private float fieldOfViewDegrees = 90f;
    private double maxDistance = 0;
    private boolean showCardinalLabels = true;
    private boolean showIntercardinalLabels = false;
    private boolean showTickMarks = true;
    private PeakCompassTickPosition tickPosition = PeakCompassTickPosition.BOTTOM;
    private float tickIntervalDegrees = 15f;
    @PeakOpaqueColor private PeakPaint backgroundColor = PeakPaint.rgba(0, 0, 0, 140);
    @PeakOpaqueColor private PeakPaint tickColor = PeakPaint.rgb(255, 255, 255);
    private int waypointIconSize = 4;
    private int waypointLabelYOffset = PeakWaypoint.AUTO_OFFSET;

    private PeakCompassOverlapMode overlapMode = PeakCompassOverlapMode.STACK;
    private PeakCompassStackDirection stackDirection = PeakCompassStackDirection.UP;
    private int stackSpacing = 2;
    private int maxStackedWaypoints = 5;
    private boolean groupSameNameWaypoints = true;

    private float minIconScale = 0.6f;
    private float maxIconScale = 1.4f;
    private double iconScaleMinDistance = 10;
    private double iconScaleMaxDistance = 200;

    private int sameNameMergeDistance = 24;

    public PeakCompass() { this(PeakId.random()); }

    public PeakCompass(String id) {
        super(id);
        size(PeakSize.of(PeakSize.PeakDimension.pixels(140), PeakSize.PeakDimension.pixels(16)));
    }

    public static PeakCompass create() { return new PeakCompass(); }
    public static PeakCompass create(String id) { return new PeakCompass(id); }

    public float getFieldOfViewDegrees() { return fieldOfViewDegrees; }
    public PeakCompass fieldOfViewDegrees(float degrees) { this.fieldOfViewDegrees = degrees; return this; }
    public double getMaxDistance() { return maxDistance; }
    public PeakCompass maxDistance(double distance) { this.maxDistance = distance; return this; }
    public boolean isShowCardinalLabels() { return showCardinalLabels; }
    public PeakCompass showCardinalLabels(boolean show) { this.showCardinalLabels = show; return this; }
    public boolean isShowIntercardinalLabels() { return showIntercardinalLabels; }
    public PeakCompass showIntercardinalLabels(boolean show) { this.showIntercardinalLabels = show; return this; }
    public boolean isShowTickMarks() { return showTickMarks; }
    public PeakCompass showTickMarks(boolean show) { this.showTickMarks = show; return this; }
    public PeakCompassTickPosition getTickPosition() { return tickPosition; }
    public PeakCompass tickPosition(PeakCompassTickPosition position) { this.tickPosition = position; return this; }
    public float getTickIntervalDegrees() { return tickIntervalDegrees; }
    public PeakCompass tickIntervalDegrees(float degrees) { this.tickIntervalDegrees = Math.max(1f, degrees); return this; }
    public PeakPaint getBackgroundColor() { return backgroundColor; }
    public PeakCompass backgroundColor(PeakPaint paint) { this.backgroundColor = paint.opaque(); return this; }
    public PeakCompass backgroundColor(int argb) { return backgroundColor(PeakPaint.solid(argb)); }
    public PeakPaint getTickColor() { return tickColor; }
    public PeakCompass tickColor(PeakPaint paint) { this.tickColor = paint.opaque(); return this; }
    public PeakCompass tickColor(int argb) { return tickColor(PeakPaint.solid(argb)); }
    public int getWaypointIconSize() { return waypointIconSize; }
    public PeakCompass waypointIconSize(int size) { this.waypointIconSize = size; return this; }
    public int getWaypointLabelYOffset() { return waypointLabelYOffset; }
    public PeakCompass waypointLabelYOffset(int offset) { this.waypointLabelYOffset = offset; return this; }

    public PeakCompassOverlapMode getOverlapMode() { return overlapMode; }
    public PeakCompass overlapMode(PeakCompassOverlapMode mode) { this.overlapMode = mode; return this; }
    public PeakCompassStackDirection getStackDirection() { return stackDirection; }
    public PeakCompass stackDirection(PeakCompassStackDirection direction) { this.stackDirection = direction; return this; }
    public int getStackSpacing() { return stackSpacing; }
    public PeakCompass stackSpacing(int spacing) { this.stackSpacing = spacing; return this; }
    public int getMaxStackedWaypoints() { return maxStackedWaypoints; }
    public PeakCompass maxStackedWaypoints(int max) { this.maxStackedWaypoints = Math.max(1, max); return this; }
    public boolean isGroupSameNameWaypoints() { return groupSameNameWaypoints; }
    public PeakCompass groupSameNameWaypoints(boolean group) { this.groupSameNameWaypoints = group; return this; }

    public float getMinIconScale() { return minIconScale; }
    public float getMaxIconScale() { return maxIconScale; }
    public PeakCompass iconScaleRange(float min, float max) { this.minIconScale = min; this.maxIconScale = max; return this; }
    public double getIconScaleMinDistance() { return iconScaleMinDistance; }
    public double getIconScaleMaxDistance() { return iconScaleMaxDistance; }
    public PeakCompass iconScaleDistanceRange(double minDistance, double maxDistance) {
        this.iconScaleMinDistance = minDistance;
        this.iconScaleMaxDistance = maxDistance;
        return this;
    }

    public int getSameNameMergeDistance() { return sameNameMergeDistance; }
    public PeakCompass sameNameMergeDistance(int pixels) { this.sameNameMergeDistance = Math.max(0, pixels); return this; }
    @Override
    public String elementType() { return "compass"; }
}
