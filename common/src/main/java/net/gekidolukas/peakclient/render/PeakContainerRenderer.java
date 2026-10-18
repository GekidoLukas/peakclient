package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class PeakContainerRenderer<T extends PeakContainer<?>> implements PeakElementRenderer<T> {
    @Override
    public void renderContent(PeakRenderContext ctx, T container, int width, int height) {
        drawBox(ctx.graphics(), PeakInteractionStyles.resolve(container), width, height);

        PeakUiRenderer.layoutAndRenderChildren(ctx.graphics(), ctx.font(), container, 0, 0, width, height,
                ctx.absX(), ctx.absY(), ctx.absScale(), ctx.scissorUnitFactor(), ctx.documentId(), ctx.depth());
    }

    static void drawBox(GuiGraphics graphics, PeakStyle style, int width, int height) {
        int w = Math.max(width, 1), h = Math.max(height, 1);

        drawShadow(graphics, style, w, h);

        if (style.getBorderRadius() > 0) {
            PeakPaintRenderer.fillRoundedWithBorder(graphics, 0, 0, w, h, style.getBorderRadius(),
                    style.getBorderWidth(), style.getBorderColor(), style.getBackground());
        } else {
            PeakPaintRenderer.fill(graphics, style.getBackground(), 0, 0, w, h);
            drawBorder(graphics, style, w, h);
        }
    }

    protected static void drawShadow(GuiGraphics graphics, PeakStyle style, int width, int height) {
        PeakPaint shadow = style.getShadowColor();
        if (shadow.isNone()) return;

        int shadowX = style.getShadowOffsetX(), shadowY = style.getShadowOffsetY();
        if (style.getShadowBlur() <= 0) {
            PeakPaintRenderer.fillRounded(graphics, shadow, shadowX, shadowY, shadowX + width, shadowY + height,
                    style.getBorderRadius());
            return;
        }

        int extent = Math.min(style.getShadowBlur(), 8) * 2;
        PeakPaintRenderer.fillFeathered(graphics, shadow, shadowX, shadowY, shadowX + width, shadowY + height,
                style.getBorderRadius(), extent);
    }

    protected static void drawBorder(GuiGraphics graphics, PeakStyle style, int width, int height) {
        PeakPaintRenderer.drawFrame(graphics, style.getBorderColor(), width, height, style.getBorderWidth());
    }

    @Override
    public PeakPixelSize measureContent(Font font, T container) {
        return PeakUiRenderer.measureContainerContent(font, container);
    }
}
