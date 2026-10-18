package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakShape;
import net.gekidolukas.peakclient.ui.PeakShapeKind;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakShapeRenderer extends PeakContainerRenderer<PeakShape> {
    @Override
    public void renderContent(PeakRenderContext ctx, PeakShape shape, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        PeakStyle style = PeakInteractionStyles.resolve(shape);
        PeakShapeKind kind = shape.getShape();
        boolean polygon = kind.isPolygon();
        int radius = shape.effectiveRadius(width, height);

        int border = style.getBorderWidth() > 0 && !style.getBorderColor().isNone()
                ? Math.min(style.getBorderWidth(), Math.min(width, height) / 2) : 0;
        int innerRadius = Math.max(0, radius - border);

        if (polygon) {
            drawPolygonBox(graphics, style, kind, width, height, radius, border);
        } else {
            drawBox(graphics, style.withBorderRadius(radius), width, height);
        }

        Minecraft minecraft = Minecraft.getInstance();
        float[] savedColor = RenderSystem.getShaderColor().clone();
        boolean masked = (polygon || innerRadius > 0) && !PeakOffscreenOpacity.isActive();
        if (masked) PeakOffscreenOpacity.begin(minecraft, graphics);
        try {
            PeakUiRenderer.layoutAndRenderChildren(graphics, ctx.font(), shape, 0, 0, width, height,
                    ctx.absX(), ctx.absY(), ctx.absScale(), ctx.scissorUnitFactor(), ctx.documentId(), ctx.depth());
        } finally {
            if (masked) {
                float unit = ctx.absScale() * ctx.scissorUnitFactor();
                float x1 = (ctx.absX() + border * ctx.absScale()) * ctx.scissorUnitFactor();
                float y1 = (ctx.absY() + border * ctx.absScale()) * ctx.scissorUnitFactor();
                float innerWidth = width - border * 2f, innerHeight = height - border * 2f;
                float x2 = x1 + innerWidth * unit;
                float y2 = y1 + innerHeight * unit;
                float[] outline = null;
                if (polygon) {
                    outline = PeakShapeGeometry.outline(kind, 0f, 0f, width, height, radius);
                    if (border > 0) outline = PeakShapeGeometry.offset(outline, -border);
                    outline = PeakShapeGeometry.translate(PeakShapeGeometry.scaled(outline, unit),
                            ctx.absX() * ctx.scissorUnitFactor(), ctx.absY() * ctx.scissorUnitFactor());
                }
                float top = shape.getPopOutTop() > 0f ? y1 + (y2 - y1) * shape.getPopOutTop() : PeakShapeMask.NONE;
                float bottom = shape.getPopOutBottom() > 0f ? y2 - (y2 - y1) * shape.getPopOutBottom() : PeakShapeMask.NONE;
                float left = shape.getPopOutLeft() > 0f ? x1 + (x2 - x1) * shape.getPopOutLeft() : PeakShapeMask.NONE;
                float right = shape.getPopOutRight() > 0f ? x2 - (x2 - x1) * shape.getPopOutRight() : PeakShapeMask.NONE;
                PeakOffscreenOpacity.endShaped(minecraft, graphics,
                        new PeakShapeMask(x1, y1, x2, y2, innerRadius * unit, outline, top, bottom, left, right), savedColor[3]);
                RenderSystem.setShaderColor(savedColor[0], savedColor[1], savedColor[2], savedColor[3]);
            }
        }
    }

    private static boolean isOpaque(PeakPaint paint) {
        return paint.isSolid() ? (paint.colorAt(0f) >>> 24) == 0xFF
                : (paint.colorAt(0f) >>> 24) == 0xFF && (paint.colorAt(0.5f) >>> 24) == 0xFF && (paint.colorAt(1f) >>> 24) == 0xFF;
    }

    private static void drawPolygonBox(GuiGraphics graphics, PeakStyle style, PeakShapeKind kind, int width, int height,
                                       int radius, int border) {
        int w = Math.max(width, 1), h = Math.max(height, 1);
        float[] outer = PeakShapeGeometry.outline(kind, 0f, 0f, w, h, radius);

        PeakPaint shadow = style.getShadowColor();
        if (!shadow.isNone()) {
            float[] shifted = PeakShapeGeometry.translate(outer, style.getShadowOffsetX(), style.getShadowOffsetY());
            PeakPolygonRenderer.fill(graphics, shadow, shifted, 0, 0, w, h);
        }

        float half = PeakRoundedRect.featherHalf(graphics.pose().last().pose());
        PeakPaint borderPaint = style.getBorderColor();
        boolean opaqueBorder = border > 0 && isOpaque(borderPaint);
        float[] background = outer;
        if (opaqueBorder) {
            float inset = Math.max(half, Math.min(2f * half, border - half));
            background = PeakShapeGeometry.offset(outer, -inset);
        }
        PeakPolygonRenderer.fill(graphics, style.getBackground(), background, 0, 0, w, h);

        if (border > 0) {
            float[] inner = PeakShapeGeometry.offset(outer, -border);
            float[] overlapped = PeakShapeGeometry.offset(inner, -half);
            PeakPolygonRenderer.ring(graphics, borderPaint, outer, overlapped, 0, 0, w, h);
        }
    }
}
