package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakProgressBar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class PeakProgressBarRenderer implements PeakElementRenderer<PeakProgressBar> {
    @Override
    public void renderContent(PeakRenderContext ctx, PeakProgressBar bar, int width, int height) {

        PeakProgressValues values = PeakProgressValueResolver.resolve(bar);
        if (bar.isHideIfFull() && values.isFull()) return;
        if (bar.isHideIfEmpty() && values.isEmpty()) return;
        float fillFraction = (float) values.fraction();

        if (bar.getBackgroundTexture() != null) {
            blitStretched(ctx.graphics(), bar.getBackgroundTexture(), width, height);
        } else {
            PeakPaintRenderer.fill(ctx.graphics(), bar.getBackgroundColor(), 0, 0, width, height);
        }

        int fillWidth = Math.round(width * fillFraction);
        if (fillWidth <= 0) return;

        if (bar.getFillTexture() != null) {
            try {
                ResourceLocation texture = PeakValueResolver.requireLocation(bar.getFillTexture());
                PeakImmediateRender.blit(ctx.graphics(), texture, 0, 0, 0, 0, fillWidth, height, width, height);
            } catch (Exception ignored) { }
        } else {
            PeakPaintRenderer.fill(ctx.graphics(), bar.getFillColor(), 0, 0, fillWidth, height);
        }
    }
    @Override
    public PeakPixelSize measureContent(Font font, PeakProgressBar bar) { return PeakPixelSize.ZERO; }

    private static void blitStretched(GuiGraphics graphics, String textureId, int width, int height) {
        try {
            ResourceLocation texture = PeakValueResolver.requireLocation(textureId);
            PeakImmediateRender.blit(graphics, texture, 0, 0, 0, 0, width, height, width, height);
        } catch (Exception ignored) { }
    }
}
