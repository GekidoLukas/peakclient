package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakOcclusion;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class PeakOcclusionTracker {
    private static final Map<PeakWorldAnchor, PeakOcclusionState> STATES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final PeakOcclusionState.Budget BUDGET = new PeakOcclusionState.Budget();

    private PeakOcclusionTracker() {}

    public static float factor(PeakWorldAnchor anchor, Vec3 camera, Vec3 point, Entity target) {
        PeakOcclusion occlusion = anchor.getOcclusion();
        PeakOcclusionState state = STATES.computeIfAbsent(anchor, key -> new PeakOcclusionState());
        if (!occlusion.isEnabled()) {
            state.disable();
            return 1f;
        }

        long tick = PeakClientTickCounter.current();
        if (state.due(tick) && (BUDGET.tryAcquire(tick) || !state.isKnown())) {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level != null) {
                boolean occluded = PeakOcclusionRaycast.isOccluded(occlusion, level, camera, point, minecraft.getCameraEntity(), target);
                state.record(occluded, tick, occlusion.getCheckIntervalTicks());
            }
        }
        if (!state.isKnown()) return 0f;

        float goal = state.isOccluded() ? (occlusion.getBehavior() == PeakOcclusion.Behavior.HIDE ? 0f : occlusion.getDimTo()) : 1f;
        return state.advance(PeakClientTickCounter.exact(), goal, occlusion.getFadeTicks());
    }

    public static boolean isOccluded(PeakWorldAnchor anchor) {
        PeakOcclusionState state = STATES.get(anchor);
        return state != null && state.isOccluded();
    }

    public static float currentFactor(PeakWorldAnchor anchor) {
        PeakOcclusionState state = STATES.get(anchor);
        return state == null ? 1f : state.factor();
    }

    public static void reset(PeakWorldAnchor anchor) {
        STATES.remove(anchor);
    }
}
