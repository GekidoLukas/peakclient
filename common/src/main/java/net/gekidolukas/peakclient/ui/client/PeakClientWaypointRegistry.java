package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.render.PeakElementLifecycleListener;
import net.gekidolukas.peakclient.render.PeakElementLifecycleRegistry;
import net.gekidolukas.peakclient.ui.PeakWaypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakClientWaypointRegistry {
    private static final Map<String, PeakWaypoint> WAYPOINTS = new ConcurrentHashMap<>();
    private static final Map<String, Long> ARRIVAL_TICKS = new ConcurrentHashMap<>();

    private static final PeakElementLifecycleListener LIFECYCLE_LISTENER = PeakClientWaypointRegistry::onElementRemoved;

    static {
        PeakElementLifecycleRegistry.register(LIFECYCLE_LISTENER);
    }

    private PeakClientWaypointRegistry() {}

    public static void upsert(PeakWaypoint waypoint) {
        WAYPOINTS.put(waypoint.getId(), waypoint);
        ARRIVAL_TICKS.put(waypoint.getId(), PeakClientTickCounter.current());
    }

    public static PeakWaypoint find(String waypointId) {
        return WAYPOINTS.get(waypointId);
    }

    public static void remove(String waypointId) {
        WAYPOINTS.remove(waypointId);
        ARRIVAL_TICKS.remove(waypointId);
    }

    public static void clearAll() {
        WAYPOINTS.clear();
        ARRIVAL_TICKS.clear();
    }

    public static void syncPosition(String waypointId, double x, double y, double z) {
        PeakWaypoint waypoint = WAYPOINTS.get(waypointId);
        if (waypoint != null) waypoint.updateLiveTrackedPosition(x, y, z);
    }

    public static List<PeakWaypoint> getVisibleIn(String elementId) {
        List<PeakWaypoint> result = new ArrayList<>();
        for (PeakWaypoint waypoint : WAYPOINTS.values()) {
            if (waypoint.isVisibleIn(elementId)) result.add(waypoint);
        }
        return result;
    }

    public static void tickExpiry() {
        long now = PeakClientTickCounter.current();
        for (PeakWaypoint waypoint : WAYPOINTS.values()) {
            if (waypoint.getExpireAfterTicks() <= 0) continue;
            Long arrival = ARRIVAL_TICKS.get(waypoint.getId());
            if (arrival != null && now - arrival >= waypoint.getExpireAfterTicks()) remove(waypoint.getId());
        }
    }

    private static void onElementRemoved(String elementId) {
        for (PeakWaypoint waypoint : WAYPOINTS.values()) {
            if (!waypoint.getVisibleInElementIds().remove(elementId)) continue;
            if (waypoint.getVisibleInElementIds().isEmpty()) remove(waypoint.getId());
        }
    }
}
