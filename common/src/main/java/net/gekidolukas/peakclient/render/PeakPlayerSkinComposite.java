package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakPlayerSkinComposite {
    private static final ResourceLocation FAILED = ResourceLocation.fromNamespaceAndPath("peakclient", "dynamic/skin_composite_failed");
    private static final Map<ResourceLocation, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    private static final List<Region> REGIONS = List.of(
            new Region(8, 8, 8, 8, 40, 8),
            new Region(20, 20, 8, 12, 20, 36),
            new Region(44, 20, 4, 12, 44, 36),
            new Region(36, 52, 4, 12, 52, 52),
            new Region(4, 20, 4, 12, 4, 36),
            new Region(20, 52, 4, 12, 4, 52)
    );

    private record Region(int x, int y, int width, int height, int overlayX, int overlayY) {}

    private PeakPlayerSkinComposite() {}

    public static ResourceLocation getOrCreate(ResourceLocation skinTexture) {
        ResourceLocation result = CACHE.computeIfAbsent(skinTexture, PeakPlayerSkinComposite::build);
        return result == FAILED ? null : result;
    }

    private static ResourceLocation build(ResourceLocation skinTexture) {
        NativeImage source = PeakChibiSkinTextureCache.readBack(skinTexture);
        if (source == null) return FAILED;

        NativeImage result = new NativeImage(64, 64, true);
        try {
            for (int y = 0; y < 64; y++) {
                for (int x = 0; x < 64; x++) {
                    result.setPixelRGBA(x, y, source.getPixelRGBA(x, y));
                }
            }
            for (Region region : REGIONS) {
                for (int dy = 0; dy < region.height(); dy++) {
                    for (int dx = 0; dx < region.width(); dx++) {
                        int base = source.getPixelRGBA(region.x() + dx, region.y() + dy);
                        int overlay = source.getPixelRGBA(region.overlayX() + dx, region.overlayY() + dy);
                        result.setPixelRGBA(region.x() + dx, region.y() + dy, blend(base, overlay));
                    }
                }
            }
        } catch (Exception e) {
            PeakLog.error("Failed to compose skin layers", e);
            source.close();
            result.close();
            return FAILED;
        }
        source.close();

        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("peakclient",
                "dynamic/skin_composite_" + Integer.toHexString(skinTexture.toString().hashCode()));
        Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(result));
        return location;
    }

    private static int blend(int base, int overlay) {
        float overlayAlpha = ((overlay >>> 24) & 0xFF) / 255f;
        float baseAlpha = ((base >>> 24) & 0xFF) / 255f;
        float outAlpha = overlayAlpha + baseAlpha * (1f - overlayAlpha);
        if (outAlpha <= 0f) return 0;

        int out = Math.round(outAlpha * 255f) << 24;
        for (int shift = 0; shift <= 16; shift += 8) {
            float overlayChannel = (overlay >>> shift) & 0xFF;
            float baseChannel = (base >>> shift) & 0xFF;
            float channel = (overlayChannel * overlayAlpha + baseChannel * baseAlpha * (1f - overlayAlpha)) / outAlpha;
            out |= (Math.round(channel) & 0xFF) << shift;
        }
        return out;
    }
}
