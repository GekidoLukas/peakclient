package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakClientReadyEvent;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import net.gekidolukas.peakclient.ui.PeakWaypoint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface PeakClientApi {

    static PeakClientApi get() {
        PeakClientApi api = PeakClientApiHolder.INSTANCE;
        if (api == null) {
            throw new IllegalStateException("PeakClient is not initialized yet. "
                    + "Declare PeakClient as a required dependency of your mod and use it after the server has started.");
        }
        return api;
    }

    void sendUiToClient(PeakDocument document, Collection<UUID> playerIds);
    void sendUiUpdateToClient(String documentId, List<PeakUiOperation> operations, Collection<UUID> playerIds);
    void setVanillaHudElements(Map<PeakVanillaElement, PeakVanillaHudEntry> entries, Collection<UUID> playerIds);

    void sendMenuToClient(PeakDocument menu, Collection<UUID> playerIds);
    void sendMenuUpdateToClient(String menuId, List<PeakUiOperation> operations, Collection<UUID> playerIds);
    void menuCommand(PeakMenuCommand command, String menuId, int flags, Collection<UUID> playerIds);

    void setCustomValue(String namespace, String key, String value, Collection<UUID> playerIds);
    void clearCustomValue(String namespace, String key, Collection<UUID> playerIds);
    void clearCustomNamespace(String namespace, Collection<UUID> playerIds);

    void upsertWaypoint(PeakWaypoint waypoint, Collection<UUID> playerIds);
    void removeWaypoint(String waypointId, Collection<UUID> playerIds);
    void clearWaypoints(Collection<UUID> playerIds);
    void syncWaypointPosition(String waypointId, double x, double y, double z, Collection<UUID> playerIds);

    void setCustomWorldBorder(double minX, double minZ, double maxX, double maxZ, Collection<UUID> playerIds);
    void setNextCustomWorldBorder(double minX, double minZ, double maxX, double maxZ, int transitionTicks, Collection<UUID> playerIds);
    void clearCustomWorldBorder(Collection<UUID> playerIds);

    boolean hasClient(UUID playerId);
    void onClientReady(Consumer<PeakClientReadyEvent> listener);
    void forgetPlayer(UUID playerId);
}
