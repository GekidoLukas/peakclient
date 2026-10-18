package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakOcclusion;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

final class PeakOcclusionRaycast {
    private static final double TOLERANCE = 0.01;

    private PeakOcclusionRaycast() {}

    static boolean isOccluded(PeakOcclusion occlusion, ClientLevel level, Vec3 from, Vec3 to, Entity viewer, Entity target) {
        double total = to.distanceToSqr(from);
        if (blockOccludes(occlusion, level, from, to, viewer, total)) return true;
        return occlusion.checksEntities() && entityOccludes(occlusion, level, from, to, viewer, target, total);
    }

    private static boolean blockOccludes(PeakOcclusion occlusion, ClientLevel level, Vec3 from, Vec3 to, Entity viewer, double total) {
        ClipContext.Block shape = occlusion.getBlockShape() == PeakOcclusion.BlockShape.COLLIDER
                ? ClipContext.Block.COLLIDER : ClipContext.Block.VISUAL;
        BlockHitResult hit = level.clip(new ClipContext(from, to, shape, ClipContext.Fluid.NONE, viewer));
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        if (hit.getLocation().distanceToSqr(from) >= total - TOLERANCE) return false;
        return !(occlusion.isIgnoreTarget() && hit.getBlockPos().equals(BlockPos.containing(to)));
    }

    private static boolean entityOccludes(PeakOcclusion occlusion, ClientLevel level, Vec3 from, Vec3 to, Entity viewer,
                                          Entity target, double total) {
        AABB area = new AABB(from, to).inflate(1.0);
        for (Entity entity : level.getEntities(viewer, area, candidate -> counts(occlusion, candidate, viewer, target))) {
            AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> entry = box.clip(from, to);
            if (entry.isPresent() && entry.get().distanceToSqr(from) < total - TOLERANCE) return true;
        }
        return false;
    }

    private static boolean counts(PeakOcclusion occlusion, Entity entity, Entity viewer, Entity target) {
        if (entity == viewer || entity.isSpectator()) return false;
        if (viewer != null && (entity.hasPassenger(viewer) || viewer.hasPassenger(entity) || viewer.getVehicle() == entity)) return false;
        if (target != null && occlusion.isIgnoreTarget() && entity == target) return false;
        if (entity.isInvisible() && !entity.isCurrentlyGlowing()) return false;
        return switch (occlusion.getEntities()) {
            case PLAYERS -> entity instanceof Player;
            case LIVING -> entity instanceof LivingEntity;
            case ALL -> true;
        };
    }
}
