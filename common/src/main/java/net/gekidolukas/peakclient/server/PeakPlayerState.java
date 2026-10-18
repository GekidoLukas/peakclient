package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class PeakPlayerState {
    public record Position(double x, double y, double z) {}

    public record WaypointEntry(String json, Position position) {}

    private final Map<String, Map<String, String>> customValues = new LinkedHashMap<>();
    private final Map<String, WaypointEntry> waypoints = new LinkedHashMap<>();
    private final Map<PeakVanillaElement, PeakVanillaHudEntry> vanillaHud = new EnumMap<>(PeakVanillaElement.class);
    private final PeakServerBorderState border = new PeakServerBorderState();
    private long offlineSinceTick = -1;

    public void setCustomValue(String namespace, String key, String value) {
        customValues.computeIfAbsent(namespace.toLowerCase(Locale.ROOT), ignored -> new LinkedHashMap<>()).put(key, value);
    }

    public void clearCustomValue(String namespace, String key) {
        String normalizedNamespace = namespace.toLowerCase(Locale.ROOT);
        Map<String, String> values = customValues.get(normalizedNamespace);
        if (values == null) return;
        values.remove(key);
        if (values.isEmpty()) customValues.remove(normalizedNamespace);
    }

    public void clearCustomNamespace(String namespace) {
        customValues.remove(namespace.toLowerCase(Locale.ROOT));
    }

    public Map<String, Map<String, String>> customValues() { return customValues; }

    public void upsertWaypoint(String id, String json) {
        WaypointEntry previous = waypoints.get(id);
        waypoints.put(id, new WaypointEntry(json, previous == null ? null : previous.position()));
    }

    public void removeWaypoint(String id) { waypoints.remove(id); }

    public void clearWaypoints() { waypoints.clear(); }

    public void syncWaypointPosition(String id, double x, double y, double z) {
        WaypointEntry entry = waypoints.get(id);
        if (entry != null) waypoints.put(id, new WaypointEntry(entry.json(), new Position(x, y, z)));
    }

    public Map<String, WaypointEntry> waypoints() { return waypoints; }

    public void mergeVanillaHud(Map<PeakVanillaElement, PeakVanillaHudEntry> entries) {
        for (Map.Entry<PeakVanillaElement, PeakVanillaHudEntry> entry : entries.entrySet()) {
            PeakVanillaHudEntry merged = PeakVanillaHudEntry.merge(vanillaHud.get(entry.getKey()), entry.getValue());
            if (merged.isDefault()) vanillaHud.remove(entry.getKey());
            else vanillaHud.put(entry.getKey(), merged);
        }
    }

    public Map<PeakVanillaElement, PeakVanillaHudEntry> vanillaHud() { return vanillaHud; }

    public PeakServerBorderState border() { return border; }

    public long offlineSinceTick() { return offlineSinceTick; }

    public void markOffline(long tick) { offlineSinceTick = tick; }

    public void markOnline() { offlineSinceTick = -1; }

    public boolean isEmpty() {
        return customValues.isEmpty() && waypoints.isEmpty() && vanillaHud.isEmpty() && !border.isActive();
    }
}
