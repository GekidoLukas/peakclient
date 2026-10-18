package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakIconBar;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakProgressSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class PeakIconBarRenderer implements PeakElementRenderer<PeakIconBar> {
    @Override
    public void renderContent(PeakRenderContext ctx, PeakIconBar bar, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        PeakProgressValues values = PeakProgressValueResolver.resolveIconBar(bar, mc);
        double current = values.current(), max = values.max();

        if (bar.isHideIfEmpty() && current <= 0) return;
        if (max <= 0) return;

        double iconsFilled = (current / max) * bar.getMaxIcons();
        for (int i = 0; i < bar.getMaxIcons(); i++) {
            double remaining = iconsFilled - i;
            int x = i * (bar.getIconSize() + bar.getIconSpacing());
            int size = bar.getIconSize();

            boolean isFull = remaining >= 1;
            boolean isHalf = !isFull && bar.isUseHalfIcons() && remaining >= 0.5;

            if (bar.isEmptyBehindFullAndHalf()) {
                drawIcon(ctx.graphics(), bar.getEmptyIconTexture(), bar.getFallbackEmptyColor(), x, size);
                if (isFull) {
                    drawIcon(ctx.graphics(), bar.getFullIconTexture(), bar.getFallbackFullColor(), x, size);
                } else if (isHalf) {
                    drawIcon(ctx.graphics(), bar.getHalfIconTexture(), bar.getFallbackHalfColor(), x, size);
                }
            } else {
                if (isFull) {
                    drawIcon(ctx.graphics(), bar.getFullIconTexture(), bar.getFallbackFullColor(), x, size);
                } else if (isHalf) {
                    drawIcon(ctx.graphics(), bar.getHalfIconTexture(), bar.getFallbackHalfColor(), x, size);
                } else {
                    drawIcon(ctx.graphics(), bar.getEmptyIconTexture(), bar.getFallbackEmptyColor(), x, size);
                }
            }
        }
    }
    @Override
    public PeakPixelSize measureContent(Font font, PeakIconBar bar) {
        return new PeakPixelSize(
                bar.getMaxIcons() * bar.getIconSize() + Math.max(0, bar.getMaxIcons() - 1) * bar.getIconSpacing(),
                bar.getIconSize()
        );
    }
    static void drawIcon(GuiGraphics graphics, String texture, PeakPaint fallbackColor, int x, int size) {
        if (texture != null) {
            try {
                PeakImmediateRender.blit(graphics, PeakValueResolver.requireLocation(texture), x, 0, 0, 0, size, size, size, size);
                return;
            } catch (Exception ignored) { }
        }
        PeakPaintRenderer.fill(graphics, fallbackColor, x, 0, x + size, size);
    }
}
