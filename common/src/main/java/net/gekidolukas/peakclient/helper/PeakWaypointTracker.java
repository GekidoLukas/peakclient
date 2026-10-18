package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakTargets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public final class PeakWaypointTracker extends PeakTargetedTask {
    private final String waypointId;
    private final UUID trackedEntityUuid;

    private double minMovementThreshold = 0.5;

    private double lastSyncedX, lastSyncedY, lastSyncedZ;
    private boolean hasSyncedOnce = false;
    private boolean trackedIsPlayer = false;
    private final Set<UUID> syncedTargets = new CopyOnWriteArraySet<>();

    private PeakWaypointTracker(String waypointId, UUID trackedEntityUuid, Collection<UUID> targetPlayerIds) {
        super(targetPlayerIds, 10);
        this.waypointId = waypointId;
        this.trackedEntityUuid = trackedEntityUuid;
    }

    public static PeakWaypointTracker create(String waypointId, Entity trackedEntity, PeakTargets targets) {
        return create(waypointId, trackedEntity.getUUID(), targets);
    }

    public static PeakWaypointTracker create(String waypointId, UUID trackedEntityUuid, PeakTargets targets) {
        PeakWaypointTracker tracker = new PeakWaypointTracker(waypointId, trackedEntityUuid, targets.ids());
        tracker.startTask();
        return tracker;
    }

    public PeakWaypointTracker intervalTicks(int ticks) { this.intervalTicks = Math.max(1, ticks); return this; }
    public PeakWaypointTracker minMovementThreshold(double threshold) { this.minMovementThreshold = threshold; return this; }

    @Override
    protected void onTargetRemoved(UUID playerId) {
        syncedTargets.remove(playerId);
    }

    @Override
    protected void onInterval() {
        MinecraftServer server = resolveServer();
        if (server == null) return;

        Entity entity = findEntityByUuid(server, trackedEntityUuid);
        if (entity == null) {
            if (trackedIsPlayer && server.getPlayerList().getPlayer(trackedEntityUuid) == null) removeAndStop();
            return;
        }
        if (entity instanceof ServerPlayer) {
            trackedIsPlayer = true;
        } else if (isDestroyed(entity)) {
            removeAndStop();
            return;
        }

        double x = entity.getX(), y = entity.getY(), z = entity.getZ();
        boolean moved = true;
        if (hasSyncedOnce) {
            double dx = x - lastSyncedX, dy = y - lastSyncedY, dz = z - lastSyncedZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            moved = distSq >= minMovementThreshold * minMovementThreshold;
        }

        Collection<UUID> current = targets();
        List<UUID> recipients = new ArrayList<>();
        for (UUID id : current) {
            if (moved || !syncedTargets.contains(id)) recipients.add(id);
        }
        if (recipients.isEmpty()) return;

        if (moved) {
            lastSyncedX = x; lastSyncedY = y; lastSyncedZ = z;
            hasSyncedOnce = true;
            PeakClientApi.get().syncWaypointPosition(waypointId, x, y, z, recipients);
        } else {
            PeakClientApi.get().syncWaypointPosition(waypointId, lastSyncedX, lastSyncedY, lastSyncedZ, recipients);
        }
        syncedTargets.addAll(recipients);
    }

    private MinecraftServer resolveServer() {
        for (UUID id : targets()) {
            ServerPlayer player = PeakClientServer.resolvePlayer(id);
            if (player != null) return player.getServer();
        }
        return null;
    }

    private void removeAndStop() {
        PeakClientApi.get().removeWaypoint(waypointId, targets());
        stop();
    }

    private static boolean isDestroyed(Entity entity) {
        if (entity.isRemoved()) {
            Entity.RemovalReason reason = entity.getRemovalReason();
            return reason != null && reason.shouldDestroy();
        }
        return entity instanceof LivingEntity living && living.isDeadOrDying();
    }

    private static Entity findEntityByUuid(MinecraftServer server, UUID uuid) {
        for (var level : server.getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e != null) return e;
        }
        return null;
    }
}
