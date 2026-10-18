package net.gekidolukas.peakclient.server;

import com.google.common.reflect.TypeToken;
import net.gekidolukas.peakclient.PeakClientNetworking;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.network.PeakCustomValueSetPayload;
import net.gekidolukas.peakclient.network.PeakCustomWorldBorderCurrentPayload;
import net.gekidolukas.peakclient.network.PeakCustomWorldBorderTargetPayload;
import net.gekidolukas.peakclient.network.PeakVanillaHudPayload;
import net.gekidolukas.peakclient.network.PeakWaypointPositionSyncPayload;
import net.gekidolukas.peakclient.network.PeakWaypointUpsertPayload;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import net.gekidolukas.peakclient.ui.serialize.PeakGzip;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakPlayerStates {
    public static final long RETENTION_TICKS = 30L * 60L * 20L;
    private static final long PURGE_INTERVAL_TICKS = 1200L;

    private static final Map<UUID, PeakPlayerState> STATES = new ConcurrentHashMap<>();

    private PeakPlayerStates() {}

    public static PeakPlayerState of(UUID playerId) {
        return STATES.computeIfAbsent(playerId, id -> new PeakPlayerState());
    }

    public static PeakPlayerState peek(UUID playerId) {
        return STATES.get(playerId);
    }

    public static void forget(UUID playerId) {
        STATES.remove(playerId);
    }

    public static void clearAll() {
        STATES.clear();
    }

    public static void markOffline(UUID playerId) {
        PeakPlayerState state = STATES.get(playerId);
        if (state == null) return;
        if (state.isEmpty()) {
            STATES.remove(playerId);
            return;
        }
        state.markOffline(PeakClientServer.currentTick());
    }

    public static void markOnline(UUID playerId) {
        PeakPlayerState state = STATES.get(playerId);
        if (state != null) state.markOnline();
    }

    public static void tick(long nowTick) {
        if (nowTick % PURGE_INTERVAL_TICKS != 0) return;
        STATES.entrySet().removeIf(entry -> {
            long offlineSince = entry.getValue().offlineSinceTick();
            return offlineSince >= 0 && nowTick - offlineSince > RETENTION_TICKS;
        });
    }

    public static PeakReplaySummary replay(ServerPlayer player) {
        PeakPlayerState state = STATES.get(player.getUUID());
        if (state == null || state.isEmpty()) return PeakReplaySummary.EMPTY;

        int customValueCount = 0;
        for (Map.Entry<String, Map<String, String>> namespace : state.customValues().entrySet()) {
            for (Map.Entry<String, String> value : namespace.getValue().entrySet()) {
                PeakClientNetworking.sendToClient(player, new PeakCustomValueSetPayload(namespace.getKey(), value.getKey(), value.getValue()));
                customValueCount++;
            }
        }

        for (Map.Entry<String, PeakPlayerState.WaypointEntry> waypoint : state.waypoints().entrySet()) {
            PeakPlayerState.WaypointEntry entry = waypoint.getValue();
            PeakClientNetworking.sendToClient(player, new PeakWaypointUpsertPayload(PeakGzip.compress(entry.json())));
            PeakPlayerState.Position position = entry.position();
            if (position != null) {
                PeakClientNetworking.sendToClient(player, new PeakWaypointPositionSyncPayload(waypoint.getKey(), position.x(), position.y(), position.z()));
            }
        }

        if (!state.vanillaHud().isEmpty()) {
            var type = new TypeToken<Map<PeakVanillaElement, PeakVanillaHudEntry>>() {}.getType();
            String json = PeakUiSerializer.GSON.toJson(state.vanillaHud(), type);
            PeakClientNetworking.sendToClient(player, new PeakVanillaHudPayload(PeakGzip.compress(json)));
        }

        boolean border = state.border().isActive();
        if (border) {
            long now = PeakClientServer.currentTick();
            PeakServerBorderState.Bounds current = state.border().currentBounds(now);
            PeakClientNetworking.sendToClient(player, new PeakCustomWorldBorderCurrentPayload(current.minX(), current.minZ(), current.maxX(), current.maxZ()));
            int remaining = state.border().remainingTicks(now);
            if (remaining > 0) {
                PeakServerBorderState.Bounds target = state.border().targetBounds();
                PeakClientNetworking.sendToClient(player, new PeakCustomWorldBorderTargetPayload(target.minX(), target.minZ(), target.maxX(), target.maxZ(), remaining));
            }
        }

        return new PeakReplaySummary(customValueCount, state.waypoints().size(), state.vanillaHud().size(), border);
    }
}
