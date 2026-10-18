package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakTooltipBackgroundStyle;
import net.gekidolukas.peakclient.ui.PeakTooltipLabel;
import net.gekidolukas.peakclient.ui.PeakTooltipStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public final class PeakTooltipLabelRenderer implements PeakElementRenderer<PeakTooltipLabel> {

    private static final int DRAWN_BOX_BASE_PADDING_HORIZONTAL = 8;
    private static final int DRAWN_BOX_BASE_PADDING_VERTICAL = 8;
    private static final int TOOLTIP_BORDER_X = 4;
    private static final int TOOLTIP_BORDER_Y = 4;
    private static final PeakPaint TOOLTIP_BACKGROUND = PeakPaint.solid(0xF0100010);
    private static final int VANILLA_PADDING = 3;
    private static final PeakPaint VANILLA_BACKGROUND = PeakPaint.solid(0xF0100010);
    private static final PeakPaint VANILLA_BORDER = PeakTooltipStyle.defaultBorder();

    @Override
    public void renderContent(PeakRenderContext ctx, PeakTooltipLabel tooltip, int width, int height) {
        String text = PeakPlaceholderResolver.resolve(resolveTooltipText(tooltip));

        switch (tooltip.getBackgroundStyle()) {
            case VANILLA -> {
                int contentWidth = width - 2 * TOOLTIP_BORDER_X;
                int contentHeight = height - 2 * TOOLTIP_BORDER_Y;
                drawVanillaBackground(ctx.graphics(), TOOLTIP_BORDER_X, TOOLTIP_BORDER_Y, contentWidth, contentHeight);
            }
            case CUSTOM_TEXTURE -> {
                if (tooltip.getBackgroundTexture() != null) {
                    try {
                        ResourceLocation texture = PeakValueResolver.requireLocation(tooltip.getBackgroundTexture());
                        PeakUiRenderer.blitNineSlice(ctx.graphics(), texture, 0, 0, width, height,
                                tooltip.getTextureWidth(), tooltip.getTextureHeight(), tooltip.getCornerSize());
                    } catch (Exception ignored) { }
                }
            }
            case DRAWN_BOX -> {
                PeakPaintRenderer.fill(ctx.graphics(), TOOLTIP_BACKGROUND, 0, 0, width, height);
                PeakPaintRenderer.drawFrame(ctx.graphics(), tooltip.getBorderColor(), width, height, 1);
            }
        }

        Padding padding = effectiveTooltipPadding(tooltip);
        int textX = padding.horizontal() / 2;
        int textY = padding.vertical() / 2;

        if (tooltip.getBackgroundStyle() == PeakTooltipBackgroundStyle.VANILLA) {
            textX += TOOLTIP_BORDER_X;
            textY += TOOLTIP_BORDER_Y;
        }

        PeakImmediateRender.text(ctx.graphics(),ctx.font(), text, textX, textY, tooltip.getColor(), true);
    }
    @Override
    public PeakPixelSize measureContent(Font font, PeakTooltipLabel tooltip) {
        String text = PeakPlaceholderResolver.resolve(resolveTooltipText(tooltip));
        return tooltipFootprintSize(font, tooltip, text);
    }

    public static void drawVanillaBackground(GuiGraphics graphics, int x, int y, int contentWidth, int contentHeight) {
        drawTooltipBox(graphics, x, y, contentWidth, contentHeight, VANILLA_PADDING, 1,
                VANILLA_BACKGROUND, VANILLA_BORDER);
    }

    public static void drawTooltipBox(GuiGraphics graphics, int x, int y, int contentWidth, int contentHeight,
                                      PeakTooltipStyle style) {
        drawTooltipBox(graphics, x, y, contentWidth, contentHeight, style.getPadding(), style.getBorderWidth(),
                style.getBackground(), style.getBorder());
    }

    private static void drawTooltipBox(GuiGraphics graphics, int x, int y, int contentWidth, int contentHeight,
                                       int padding, int borderWidth, PeakPaint background, PeakPaint border) {
        int left = x - padding;
        int top = y - padding;
        int w = contentWidth + 2 * padding;
        int h = contentHeight + 2 * padding;

        if (!background.isNone()) {
            int bx1 = left - 1, by1 = top - 1, bx2 = left + w + 1, by2 = top + h + 1;
            PeakPaintRenderer.fill(graphics, background, left, top - 1, left + w, top, bx1, by1, bx2, by2);
            PeakPaintRenderer.fill(graphics, background, left, top + h, left + w, top + h + 1, bx1, by1, bx2, by2);
            PeakPaintRenderer.fill(graphics, background, left, top, left + w, top + h, bx1, by1, bx2, by2);
            PeakPaintRenderer.fill(graphics, background, left - 1, top, left, top + h, bx1, by1, bx2, by2);
            PeakPaintRenderer.fill(graphics, background, left + w, top, left + w + 1, top + h, bx1, by1, bx2, by2);
        }

        int line = Math.min(borderWidth, Math.min(w, h) / 2);
        if (line <= 0 || border.isNone()) return;
        PeakPaintRenderer.fill(graphics, border, left, top, left + w, top + line, left, top, left + w, top + h);
        PeakPaintRenderer.fill(graphics, border, left, top + h - line, left + w, top + h, left, top, left + w, top + h);
        PeakPaintRenderer.fill(graphics, border, left, top + line, left + line, top + h - line, left, top, left + w, top + h);
        PeakPaintRenderer.fill(graphics, border, left + w - line, top + line, left + w, top + h - line, left, top, left + w, top + h);
    }

    private static String resolveTooltipText(PeakTooltipLabel label) {
        if (label.getTranslationKey() != null) {
            Object[] args = label.getTranslationArgs().toArray();
            return I18n.get(label.getTranslationKey(), args);
        }
        return label.getText();
    }

    private static PeakPixelSize tooltipFootprintSize(Font font, PeakTooltipLabel tooltip, String text) {
        PeakPixelSize content = tooltipContentSize(font, tooltip, text);
        if (tooltip.getBackgroundStyle() == PeakTooltipBackgroundStyle.VANILLA) {
            return new PeakPixelSize(content.width() + 2 * TOOLTIP_BORDER_X, content.height() + 2 * TOOLTIP_BORDER_Y);
        }
        return content;
    }

    private static PeakPixelSize tooltipContentSize(Font font, PeakTooltipLabel tooltip, String text) {
        Padding padding = effectiveTooltipPadding(tooltip);
        return new PeakPixelSize(font.width(text) + padding.horizontal(), font.lineHeight + padding.vertical());
    }

    private static Padding effectiveTooltipPadding(PeakTooltipLabel tooltip) {
        int horizontal = tooltip.getTooltipPaddingHorizontal();
        int vertical = tooltip.getTooltipPaddingVertical();
        if (tooltip.getBackgroundStyle() == PeakTooltipBackgroundStyle.DRAWN_BOX) {
            horizontal += DRAWN_BOX_BASE_PADDING_HORIZONTAL;
            vertical += DRAWN_BOX_BASE_PADDING_VERTICAL;
        }
        return new Padding(horizontal, vertical);
    }

    private record Padding(int horizontal, int vertical) {}
}
