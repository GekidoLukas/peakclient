package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakPaintRenderer;
import net.gekidolukas.peakclient.render.PeakImmediateRender;
import net.gekidolukas.peakclient.render.PeakPlaceholderResolver;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakRect;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class PeakMenuBackdrop {
    private PeakMenuBackdrop() {}

    static void draw(GuiGraphics graphics, PeakMenuSettings settings, int width, int height, float partialTick) {
        String panorama = PeakPlaceholderResolver.resolve(settings.getBackdropPanorama());
        if (!panorama.isEmpty()) {
            PeakPanoramas.draw(graphics, panorama, width, height, settings.getBackdropPanoramaSpeed(),
                    settings.getBackdropPanoramaPitch(), settings.isBackdropPanoramaOverlay(), partialTick);
        }

        String texturePath = PeakPlaceholderResolver.resolve(settings.getBackdropTexture());
        if (!texturePath.isEmpty()) {
            ResourceLocation texture = ResourceLocation.tryParse(texturePath);
            if (texture != null) {
                PeakRect rect = PeakTheme.backdropRect(settings, width, height);
                PeakImmediateRender.blit(graphics, texture, rect.x(), rect.y(), 0, 0, rect.width(), rect.height(), rect.width(), rect.height());
            }
        }

        PeakPaintRenderer.fill(graphics, settings.getBackdrop(), 0, 0, width, height);
    }
}
