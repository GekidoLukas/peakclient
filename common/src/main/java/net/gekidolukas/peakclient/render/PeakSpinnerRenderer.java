package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakSpinner;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakSpinnerRenderer implements PeakElementRenderer<PeakSpinner> {
    @Override
    public PeakPixelSize measureContent(Font font, PeakSpinner spinner) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakSpinner spinner, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        int size = Math.max(4, Math.min(width, height));
        int dots = spinner.getDots();
        int dotSize = size >= 24 ? 3 : 2;
        double radius = (size - dotSize) / 2.0;
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        double progress = (PeakClientTickCounter.current() % spinner.getPeriodTicks()) / (double) spinner.getPeriodTicks();
        int active = (int) Math.floor(progress * dots);

        for (int i = 0; i < dots; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / dots;
            int x = (int) Math.round(centerX + Math.cos(angle) * radius - dotSize / 2.0);
            int y = (int) Math.round(centerY + Math.sin(angle) * radius - dotSize / 2.0);
            int behind = Math.floorMod(active - i, dots);
            float strength = 1f - behind / (float) dots;
            int alpha = Math.max(40, Math.round(255 * strength));
            float across = (float) ((x + dotSize / 2.0 - (centerX - size / 2.0)) / size);
            int dotColor = spinner.getColor().colorAt(across);
            PeakImmediateRender.fill(graphics, x, y, x + dotSize, y + dotSize, PeakArgb.withAlpha(dotColor, alpha));
        }
    }
}
