package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakChibiSkinTextureCache {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    private PeakChibiSkinTextureCache() {}

    public static ResourceLocation getOrCreate(ResourceLocation sourceTexture, boolean slim) {
        String key = sourceTexture + ":" + (slim ? "slim" : "classic");
        return CACHE.computeIfAbsent(key, k -> build(sourceTexture, slim));
    }

    private static ResourceLocation build(ResourceLocation sourceTexture, boolean slim) {
        NativeImage source = readBack(sourceTexture);
        if (source == null) return sourceTexture;

        NativeImage result = new NativeImage(source.getWidth(), source.getHeight(), false);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                result.setPixelRGBA(x, y, source.getPixelRGBA(x, y));
            }
        }

        int armWidth = slim ? 3 : 4;
        int depth = 4;

        cropDropMiddleTwo(source, result, 16, 16 + depth, 2 * (8 + depth));
        cropDropMiddleTwo(source, result, 40, 16 + depth, 2 * (armWidth + depth));
        cropDropMiddleTwo(source, result, 32, 48 + depth, 2 * (armWidth + depth));
        cropEveryOtherFromBottom(source, result, 0, 16 + depth, 2 * (4 + depth));
        cropEveryOtherFromBottom(source, result, 16, 48 + depth, 2 * (4 + depth));

        cropDropMiddleTwo(source, result, 16, 32 + depth, 2 * (8 + depth));
        cropDropMiddleTwo(source, result, 40, 32 + depth, 2 * (armWidth + depth));
        cropDropMiddleTwo(source, result, 48, 48 + depth, 2 * (armWidth + depth));
        cropEveryOtherFromBottom(source, result, 0, 32 + depth, 2 * (4 + depth));
        cropEveryOtherFromBottom(source, result, 0, 48 + depth, 2 * (4 + depth));

        source.close();

        DynamicTexture texture = new DynamicTexture(result);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("peakclient",
                "dynamic/chibi_" + Integer.toHexString((sourceTexture.toString() + slim).hashCode()));
        Minecraft.getInstance().getTextureManager().register(location, texture);
        return location;
    }

    private static void cropDropMiddleTwo(NativeImage src, NativeImage dst, int bandX, int bandY, int bandWidth) {
        for (int destRow = 0; destRow < 10; destRow++) {
            int srcRow = destRow < 5 ? destRow : destRow + 2;
            copyRow(src, dst, bandX, bandY + srcRow, bandX, bandY + destRow, bandWidth);
        }
    }

    private static void cropEveryOtherFromBottom(NativeImage src, NativeImage dst, int bandX, int bandY, int bandWidth) {
        for (int destRow = 0; destRow < 6; destRow++) {
            int srcRow = 1 + destRow * 2;
            copyRow(src, dst, bandX, bandY + srcRow, bandX, bandY + destRow, bandWidth);
        }
    }

    private static void copyRow(NativeImage src, NativeImage dst, int srcX, int srcY, int dstX, int dstY, int width) {
        for (int x = 0; x < width; x++) {
            dst.setPixelRGBA(dstX + x, dstY, src.getPixelRGBA(srcX + x, srcY));
        }
    }

    static NativeImage readBack(ResourceLocation texture) {
        try {
            var abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
            NativeImage image = new NativeImage(64, 64, false);
            RenderSystem.bindTexture(abstractTexture.getId());
            image.downloadTexture(0, false);
            return image;
        } catch (Exception e) {
            PeakLog.error("Failed to read back skin texture for chibi cropping", e);
            return null;
        }
    }
}