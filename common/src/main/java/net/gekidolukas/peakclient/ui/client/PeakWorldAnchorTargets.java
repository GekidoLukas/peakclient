package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakAnchorTarget;
import net.gekidolukas.peakclient.ui.PeakWaypoint;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PeakWorldAnchorTargets {
    private static final double HEAD_CLEARANCE = 0.5;

    private static final Map<UUID, Entity> ENTITIES = new HashMap<>();
    private static long entitiesTick = Long.MIN_VALUE;
    private static ClientLevel entitiesLevel;

    private PeakWorldAnchorTargets() {}

    public record Resolved(Vec3 point, Entity entity) {}

    public static Resolved resolve(PeakWorldAnchor anchor, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return null;
        String dimension = anchor.getDimension();
        if (!dimension.isEmpty() && !level.dimension().location().toString().equals(dimension)) return null;

        PeakAnchorTarget target = anchor.getTarget();
        double offsetX = target.getOffsetX(), offsetY = target.getOffsetY(), offsetZ = target.getOffsetZ();
        switch (target.getKind()) {
            case POSITION -> {
                return new Resolved(new Vec3(target.getX() + offsetX, target.getY() + offsetY, target.getZ() + offsetZ), null);
            }
            case WAYPOINT -> {
                PeakWaypoint waypoint = PeakClientWaypointRegistry.find(target.getWaypointId());
                if (waypoint == null) return null;
                return new Resolved(new Vec3(waypoint.getX() + offsetX, waypoint.getY() + offsetY, waypoint.getZ() + offsetZ), null);
            }
            case ENTITY -> {
                UUID uuid = target.parsedEntityUuid();
                Entity entity = uuid == null ? null : findEntity(level, uuid);
                if (entity == null || entity.isRemoved()) return null;
                Vec3 position = entity.getPosition(partialTick);
                double y = switch (target.getPoint()) {
                    case ABOVE_HEAD -> position.y + entity.getBbHeight() + HEAD_CLEARANCE;
                    case CENTER -> position.y + entity.getBbHeight() / 2.0;
                    case FEET -> position.y;
                };
                return new Resolved(new Vec3(position.x + offsetX, y + offsetY, position.z + offsetZ), entity);
            }
        }
        return null;
    }

    private static Entity findEntity(ClientLevel level, UUID uuid) {
        Entity player = level.getPlayerByUUID(uuid);
        if (player != null) return player;
        long tick = PeakClientTickCounter.current();
        if (entitiesLevel != level || entitiesTick != tick) {
            ENTITIES.clear();
            for (Entity entity : level.entitiesForRendering()) ENTITIES.put(entity.getUUID(), entity);
            entitiesLevel = level;
            entitiesTick = tick;
        }
        return ENTITIES.get(uuid);
    }

    public static void clear() {
        ENTITIES.clear();
        entitiesLevel = null;
        entitiesTick = Long.MIN_VALUE;
    }
}
