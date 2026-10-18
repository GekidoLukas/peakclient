package net.gekidolukas.peakclient.server;

import com.google.common.reflect.TypeToken;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.PeakServerThread;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakMenuCommand;
import net.gekidolukas.peakclient.network.PeakCustomValueClearPayload;
import net.gekidolukas.peakclient.network.PeakCustomValueSetPayload;
import net.gekidolukas.peakclient.network.PeakCustomWorldBorderClearPayload;
import net.gekidolukas.peakclient.network.PeakCustomWorldBorderCurrentPayload;
import net.gekidolukas.peakclient.network.PeakCustomWorldBorderTargetPayload;
import net.gekidolukas.peakclient.network.PeakVanillaHudPayload;
import net.gekidolukas.peakclient.network.PeakWaypointClearPayload;
import net.gekidolukas.peakclient.network.PeakWaypointPositionSyncPayload;
import net.gekidolukas.peakclient.network.PeakWaypointRemovePayload;
import net.gekidolukas.peakclient.network.PeakWaypointUpsertPayload;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import net.gekidolukas.peakclient.ui.PeakWaypoint;
import net.gekidolukas.peakclient.ui.serialize.PeakGzip;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class PeakClientApiImpl implements PeakClientApi {
    private static final Set<String> RESERVED_NAMESPACES = Set.of("minecraft", "mc", "peakclient", "pc");
    private static final Type VANILLA_HUD_TYPE = new TypeToken<Map<PeakVanillaElement, PeakVanillaHudEntry>>() {}.getType();

    private static void onServer(Collection<UUID> playerIds, Consumer<List<UUID>> action) {
        List<UUID> ids = List.copyOf(playerIds);
        PeakServerThread.run(() -> action.accept(ids));
    }

    @Override
    public void sendUiToClient(PeakDocument document, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> PeakUiSender.sendDocument(document, ids));
    }

    @Override
    public void sendUiUpdateToClient(String documentId, List<PeakUiOperation> operations, Collection<UUID> playerIds) {
        List<PeakUiOperation> copy = List.copyOf(operations);
        onServer(playerIds, ids -> PeakUiSender.sendDocumentUpdate(documentId, copy, ids));
    }

    @Override
    public void sendMenuToClient(PeakDocument menu, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> PeakUiSender.sendMenu(menu, ids));
    }

    @Override
    public void sendMenuUpdateToClient(String menuId, List<PeakUiOperation> operations, Collection<UUID> playerIds) {
        List<PeakUiOperation> copy = List.copyOf(operations);
        onServer(playerIds, ids -> PeakUiSender.sendMenuUpdate(menuId, copy, ids));
    }

    @Override
    public void menuCommand(PeakMenuCommand command, String menuId, int flags, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> PeakUiSender.menuCommand(command, menuId, flags, ids));
    }

    @Override
    public void setVanillaHudElements(Map<PeakVanillaElement, PeakVanillaHudEntry> entries, Collection<UUID> playerIds) {
        Map<PeakVanillaElement, PeakVanillaHudEntry> copy = new EnumMap<>(PeakVanillaElement.class);
        copy.putAll(entries);
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).mergeVanillaHud(copy);
            String json = PeakUiSerializer.GSON.toJson(copy, VANILLA_HUD_TYPE);
            PeakRecipients.send(ids, new PeakVanillaHudPayload(PeakGzip.compress(json)));
        });
    }

    @Override
    public void setCustomValue(String namespace, String key, String value, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            String normalizedNamespace = namespace.toLowerCase(Locale.ROOT);
            if (RESERVED_NAMESPACES.contains(normalizedNamespace)) {
                PeakLog.warn("Refused to set custom value: namespace '{}' is reserved for built-in placeholders", namespace);
                return;
            }
            for (UUID id : ids) PeakPlayerStates.of(id).setCustomValue(normalizedNamespace, key, value);
            PeakRecipients.send(ids, new PeakCustomValueSetPayload(normalizedNamespace, key, value));
        });
    }

    @Override
    public void clearCustomValue(String namespace, String key, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).clearCustomValue(namespace, key);
            PeakRecipients.send(ids, new PeakCustomValueClearPayload(namespace.toLowerCase(Locale.ROOT), key));
        });
    }

    @Override
    public void clearCustomNamespace(String namespace, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).clearCustomNamespace(namespace);
            PeakRecipients.send(ids, new PeakCustomValueClearPayload(namespace.toLowerCase(Locale.ROOT), ""));
        });
    }

    @Override
    public void upsertWaypoint(PeakWaypoint waypoint, Collection<UUID> playerIds) {
        String json = PeakUiSerializer.GSON.toJson(waypoint, PeakWaypoint.class);
        String waypointId = waypoint.getId();
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).upsertWaypoint(waypointId, json);
            PeakRecipients.send(ids, new PeakWaypointUpsertPayload(PeakGzip.compress(json)));
        });
    }

    @Override
    public void removeWaypoint(String waypointId, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).removeWaypoint(waypointId);
            PeakRecipients.send(ids, new PeakWaypointRemovePayload(waypointId));
        });
    }

    @Override
    public void clearWaypoints(Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).clearWaypoints();
            PeakRecipients.send(ids, new PeakWaypointClearPayload());
        });
    }

    @Override
    public void syncWaypointPosition(String waypointId, double x, double y, double z, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).syncWaypointPosition(waypointId, x, y, z);
            PeakRecipients.send(ids, new PeakWaypointPositionSyncPayload(waypointId, x, y, z));
        });
    }

    @Override
    public void setCustomWorldBorder(double minX, double minZ, double maxX, double maxZ, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            long now = PeakClientServer.currentTick();
            for (UUID id : ids) PeakPlayerStates.of(id).border().setCurrent(minX, minZ, maxX, maxZ, now);
            PeakRecipients.send(ids, new PeakCustomWorldBorderCurrentPayload(minX, minZ, maxX, maxZ));
        });
    }

    @Override
    public void setNextCustomWorldBorder(double minX, double minZ, double maxX, double maxZ, int transitionTicks, Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            long now = PeakClientServer.currentTick();
            for (UUID id : ids) PeakPlayerStates.of(id).border().setTarget(minX, minZ, maxX, maxZ, transitionTicks, now);
            PeakRecipients.send(ids, new PeakCustomWorldBorderTargetPayload(minX, minZ, maxX, maxZ, transitionTicks));
        });
    }

    @Override
    public void clearCustomWorldBorder(Collection<UUID> playerIds) {
        onServer(playerIds, ids -> {
            for (UUID id : ids) PeakPlayerStates.of(id).border().clear();
            PeakRecipients.send(ids, new PeakCustomWorldBorderClearPayload());
        });
    }

    @Override
    public boolean hasClient(UUID playerId) {
        return PeakClients.hasClient(playerId);
    }

    @Override
    public void onClientReady(Consumer<PeakClientReadyEvent> listener) {
        PeakClients.addReadyListener(listener);
    }

    @Override
    public void forgetPlayer(UUID playerId) {
        PeakServerThread.run(() -> PeakPlayerStates.forget(playerId));
    }
}
