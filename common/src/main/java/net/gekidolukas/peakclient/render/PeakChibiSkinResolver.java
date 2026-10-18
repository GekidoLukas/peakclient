package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakChibiSkinResolver {
    private static final long RETRY_AFTER_NANOS = 60_000_000_000L;

    private static final Map<UUID, PlayerSkin> RESOLVED = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> IN_FLIGHT = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> FAILED = new ConcurrentHashMap<>();

    private PeakChibiSkinResolver() {}

    public static PlayerSkin resolve(UUID uuid) {
        PlayerSkin cached = RESOLVED.get(uuid);
        if (cached != null) return cached;

        Long failedAt = FAILED.get(uuid);
        if (failedAt != null) {
            if (System.nanoTime() - failedAt < RETRY_AFTER_NANOS) return null;
            FAILED.remove(uuid, failedAt);
        }

        if (IN_FLIGHT.putIfAbsent(uuid, Boolean.TRUE) != null) return null;

        Minecraft mc = Minecraft.getInstance();
        CompletableFuture.supplyAsync(() -> {
            try {
                var profileResult = mc.getMinecraftSessionService().fetchProfile(uuid, true);
                return profileResult != null ? profileResult.profile() : null;
            } catch (Exception e) {
                PeakLog.warn("Failed to fetch profile for chibi skin (uuid {}): {}", uuid, e.toString());
                return null;
            }
        }, Util.backgroundExecutor()).thenCompose(profile -> {
            if (profile == null) return CompletableFuture.completedFuture(null);
            return mc.getSkinManager().getOrLoad(profile);
        }).thenAccept(skin -> {
            IN_FLIGHT.remove(uuid);
            if (skin != null) {
                RESOLVED.put(uuid, skin);
                FAILED.remove(uuid);
            } else {
                FAILED.put(uuid, System.nanoTime());
            }
        }).exceptionally(e -> {
            IN_FLIGHT.remove(uuid);
            FAILED.put(uuid, System.nanoTime());
            PeakLog.warn("Failed to resolve chibi skin for uuid {}: {}", uuid, e.toString());
            return null;
        });

        return null;
    }
}
