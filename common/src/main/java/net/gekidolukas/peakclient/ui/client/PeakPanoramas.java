package net.gekidolukas.peakclient.ui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.render.PeakImmediateRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PeakPanoramas {
    private static final int SIDES = 6;
    private static final float DEGREES_PER_TICK = 0.1f;
    private static final Map<String, Panorama> PANORAMAS = new HashMap<>();
    private static final Set<String> MISSING = new HashSet<>();

    private PeakPanoramas() {}

    private static final class Panorama {
        private final CubeMap map;
        private float spin;

        private Panorama(CubeMap map) {
            this.map = map;
        }
    }

    public static boolean draw(GuiGraphics graphics, String basePath, int width, int height,
                               float speed, float pitch, boolean overlay, float partialTick) {
        Panorama panorama = resolve(basePath);
        if (panorama == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        float optionSpeed = minecraft.options.panoramaSpeed().get().floatValue();
        panorama.spin = wrap(panorama.spin + partialTick * speed * optionSpeed * DEGREES_PER_TICK);
        panorama.map.render(minecraft, pitch, -panorama.spin, 1f);
        RenderSystem.disableBlend();
        if (overlay) {
            PeakImmediateRender.blit(graphics, PanoramaRenderer.PANORAMA_OVERLAY, 0, 0, width, height, 0, 0, 16, 128, 16, 128);
        }
        return true;
    }

    public static float wrap(float degrees) {
        float wrapped = degrees % 360f;
        return wrapped < 0f ? wrapped + 360f : wrapped;
    }

    public static void clear() {
        PANORAMAS.clear();
        MISSING.clear();
    }

    private static Panorama resolve(String basePath) {
        if (basePath == null || basePath.isEmpty()) return null;
        Panorama cached = PANORAMAS.get(basePath);
        if (cached != null) return cached;
        if (MISSING.contains(basePath)) return null;

        ResourceLocation base = ResourceLocation.tryParse(basePath);
        if (base == null) {
            MISSING.add(basePath);
            PeakLog.warn("Menu panorama '{}' is not a valid resource location", basePath);
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        for (int side = 0; side < SIDES; side++) {
            ResourceLocation face = base.withPath(base.getPath() + "_" + side + ".png");
            if (minecraft.getResourceManager().getResource(face).isEmpty()) {
                MISSING.add(basePath);
                PeakLog.warn("Menu panorama '{}' is missing the texture '{}'", basePath, face);
                return null;
            }
        }
        Panorama created = new Panorama(new CubeMap(base));
        PANORAMAS.put(basePath, created);
        return created;
    }
}
