package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakMenuCommand;
import net.gekidolukas.peakclient.server.PeakClientReadyEvent;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import net.gekidolukas.peakclient.ui.PeakWaypoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class RecordingPeakApi implements PeakClientApi {
    public record Call(String name, Object payload, List<UUID> ids) {}

    public final List<Call> calls = new ArrayList<>();

    private void record(String name, Object payload, Collection<UUID> ids) {
        calls.add(new Call(name, payload, List.copyOf(ids)));
    }

    @Override public void sendUiToClient(PeakDocument document, Collection<UUID> playerIds) { record("sendUi", document, playerIds); }
    @Override public void sendUiUpdateToClient(String documentId, List<PeakUiOperation> operations, Collection<UUID> playerIds) { record("update:" + documentId, operations, playerIds); }
    @Override public void setVanillaHudElements(Map<PeakVanillaElement, PeakVanillaHudEntry> entries, Collection<UUID> playerIds) { record("hud", entries, playerIds); }
    @Override public void sendMenuToClient(PeakDocument menu, Collection<UUID> playerIds) { record("sendMenu", menu, playerIds); }
    @Override public void sendMenuUpdateToClient(String menuId, List<PeakUiOperation> operations, Collection<UUID> playerIds) { record("menuUpdate:" + menuId, operations, playerIds); }
    @Override public void menuCommand(PeakMenuCommand command, String menuId, int flags, Collection<UUID> playerIds) { record("menu:" + command + ":" + menuId, flags, playerIds); }
    @Override public void setCustomValue(String namespace, String key, String value, Collection<UUID> playerIds) { record("value:" + namespace + ":" + key, value, playerIds); }
    @Override public void clearCustomValue(String namespace, String key, Collection<UUID> playerIds) { record("clearValue:" + namespace + ":" + key, null, playerIds); }
    @Override public void clearCustomNamespace(String namespace, Collection<UUID> playerIds) { record("clearNamespace:" + namespace, null, playerIds); }
    @Override public void upsertWaypoint(PeakWaypoint waypoint, Collection<UUID> playerIds) { record("upsertWaypoint", waypoint, playerIds); }
    @Override public void removeWaypoint(String waypointId, Collection<UUID> playerIds) { record("removeWaypoint", waypointId, playerIds); }
    @Override public void clearWaypoints(Collection<UUID> playerIds) { record("clearWaypoints", null, playerIds); }
    @Override public void syncWaypointPosition(String waypointId, double x, double y, double z, Collection<UUID> playerIds) { record("syncWaypoint", waypointId, playerIds); }
    @Override public void setCustomWorldBorder(double minX, double minZ, double maxX, double maxZ, Collection<UUID> playerIds) { record("border", null, playerIds); }
    @Override public void setNextCustomWorldBorder(double minX, double minZ, double maxX, double maxZ, int transitionTicks, Collection<UUID> playerIds) { record("nextBorder", transitionTicks, playerIds); }
    @Override public void clearCustomWorldBorder(Collection<UUID> playerIds) { record("clearBorder", null, playerIds); }
    @Override public boolean hasClient(UUID playerId) { return true; }
    @Override public void onClientReady(Consumer<PeakClientReadyEvent> listener) {}
    @Override public void forgetPlayer(UUID playerId) {}
}
