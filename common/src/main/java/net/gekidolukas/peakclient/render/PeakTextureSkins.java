package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakTextureSkins {
    private static final Map<String, Optional<PlayerSkin>> CACHE = new ConcurrentHashMap<>();

    private PeakTextureSkins() {}

    public static PlayerSkin get(String texturePath, boolean slim) {
        if (texturePath == null) return null;
        return CACHE.computeIfAbsent(texturePath + "|" + slim, key -> build(texturePath, slim)).orElse(null);
    }

    private static Optional<PlayerSkin> build(String texturePath, boolean slim) {
        try {
            ResourceLocation texture = ResourceLocation.parse(texturePath);
            PlayerSkin.Model model = slim ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
            return Optional.of(new PlayerSkin(texture, null, null, null, model, true));
        } catch (Exception e) {
            PeakLog.warn("Invalid player skin texture path '{}': {}", texturePath, e.toString());
            return Optional.empty();
        }
    }
}
