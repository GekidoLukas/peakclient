package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakEffectOverlay;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

final class PeakEffectOverlayRenderer {
    private static final int OUTLINE_WIDTH = 2;
    private static final int GLOW_MAX_EXTENT = 10;
    private static final int SHINE_SEGMENTS = 6;
    private static final float SHINE_LEAN = 0.4f;

    private PeakEffectOverlayRenderer() {}

    static boolean followsContent(PeakEffectOverlay overlay) {
        return overlay.kind() == PeakEffectOverlay.Kind.FLASH || overlay.kind() == PeakEffectOverlay.Kind.SHINE;
    }

    static void drawUnder(GuiGraphics graphics, PeakEffectOverlay overlay, int width, int height, int radius) {
        if (overlay.kind() != PeakEffectOverlay.Kind.GLOW) return;
        int extent = Math.max(3, Math.min(GLOW_MAX_EXTENT, Math.min(width, height) / 4));
        PeakPaint paint = PeakPaint.solid(scaled(overlay));
        PeakPaintRenderer.fillFeathered(graphics, paint, 0, 0, width, height, radius, extent);
    }

    static void drawOver(GuiGraphics graphics, PeakEffectOverlay overlay, int width, int height, int radius) {
        switch (overlay.kind()) {
            case FLASH -> PeakPaintRenderer.fillRounded(graphics, PeakPaint.solid(scaled(overlay)), 0, 0, width, height, radius);
            case OUTLINE -> {
                PeakPaint paint = PeakPaint.solid(scaled(overlay));
                if (radius > 0) {
                    PeakPaintRenderer.fillRoundedWithBorder(graphics, 0, 0, width, height, radius, OUTLINE_WIDTH, paint, PeakPaint.NONE);
                } else {
                    PeakPaintRenderer.drawFrame(graphics, paint, width, height, OUTLINE_WIDTH);
                }
            }
            case SHINE -> drawShine(graphics, overlay, width, height, false);
            case GLOW -> { }
        }
    }

    static void drawAtop(GuiGraphics graphics, PeakEffectOverlay overlay, int width, int height) {
        switch (overlay.kind()) {
            case FLASH -> {
                int color = scaled(overlay);
                fillAtop(graphics, 0, 0, width, height, color, color);
            }
            case SHINE -> drawShine(graphics, overlay, width, height, true);
            default -> { }
        }
    }

    private static int scaled(PeakEffectOverlay overlay) {
        int color = PeakArgb.normalize(overlay.color());
        float base = ((color >>> 24) & 0xFF) / 255f;
        return PeakArgb.withAlpha(color, base * overlay.strength());
    }

    private static void drawShine(GuiGraphics graphics, PeakEffectOverlay overlay, int width, int height, boolean atop) {
        if (width <= 0 || height <= 0 || overlay.progress() >= 1f) return;
        int color = PeakArgb.normalize(overlay.color());
        float baseAlpha = ((color >>> 24) & 0xFF) / 255f * overlay.strength();
        float bandWidth = Math.max(10f, width * 0.35f);
        float halfBand = bandWidth / 2f;
        float center = -halfBand + overlay.progress() * (width + bandWidth);
        float topShift = center - SHINE_LEAN * height / 2f;
        float bottomShift = center + SHINE_LEAN * height / 2f;

        for (int segment = -SHINE_SEGMENTS; segment < SHINE_SEGMENTS; segment++) {
            float fromOffset = halfBand * segment / SHINE_SEGMENTS;
            float toOffset = halfBand * (segment + 1) / SHINE_SEGMENTS;
            float fromAlpha = baseAlpha * profile(fromOffset, halfBand);
            float toAlpha = baseAlpha * profile(toOffset, halfBand);
            List<float[]> polygon = new ArrayList<>(List.of(
                    new float[]{topShift + fromOffset, 0f, fromAlpha},
                    new float[]{topShift + toOffset, 0f, toAlpha},
                    new float[]{bottomShift + toOffset, height, toAlpha},
                    new float[]{bottomShift + fromOffset, height, fromAlpha}));
            polygon = clipHorizontally(polygon, 0f, true);
            polygon = clipHorizontally(polygon, width, false);
            if (polygon.size() >= 3) fillPolygon(graphics, polygon, color, atop);
        }
    }

    private static float profile(float offset, float halfBand) {
        double position = Math.max(-1.0, Math.min(1.0, offset / halfBand));
        return (float) (0.5 + 0.5 * Math.cos(Math.PI * position));
    }

    private static List<float[]> clipHorizontally(List<float[]> polygon, float limit, boolean keepGreater) {
        List<float[]> result = new ArrayList<>();
        for (int i = 0; i < polygon.size(); i++) {
            float[] current = polygon.get(i);
            float[] previous = polygon.get((i + polygon.size() - 1) % polygon.size());
            boolean currentInside = keepGreater ? current[0] >= limit : current[0] <= limit;
            boolean previousInside = keepGreater ? previous[0] >= limit : previous[0] <= limit;
            if (currentInside != previousInside) {
                float t = (limit - previous[0]) / (current[0] - previous[0]);
                result.add(new float[]{limit, previous[1] + (current[1] - previous[1]) * t, previous[2] + (current[2] - previous[2]) * t});
            }
            if (currentInside) result.add(current);
        }
        return result;
    }

    private static void fillPolygon(GuiGraphics graphics, List<float[]> polygon, int color, boolean atop) {
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float red = ((color >> 16) & 0xFF) / 255f, green = ((color >> 8) & 0xFF) / 255f, blue = (color & 0xFF) / 255f;
        for (int i = 1; i < polygon.size() - 1; i++) {
            polygonVertex(buffer, matrix, polygon.get(0), red, green, blue, atop);
            polygonVertex(buffer, matrix, polygon.get(i), red, green, blue, atop);
            polygonVertex(buffer, matrix, polygon.get(i + 1), red, green, blue, atop);
        }
        if (!atop) {
            PeakRoundedRect.upload(buffer);
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        PeakRenderStats.countFill();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void polygonVertex(BufferBuilder buffer, Matrix4f matrix, float[] point, float red, float green, float blue,
                                      boolean atop) {
        float alpha = Math.max(0f, Math.min(1f, point[2]));
        float scale = atop ? alpha : 1f;
        buffer.addVertex(matrix, point[0], point[1], 0).setColor(red * scale, green * scale, blue * scale, alpha);
    }

    private static void fillAtop(GuiGraphics graphics, int x1, int y1, int x2, int y2, int first, int second) {
        if (x2 <= x1 || y2 <= y1) return;
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        atopVertex(buffer, matrix, x1, y1, first);
        atopVertex(buffer, matrix, x1, y2, first);
        atopVertex(buffer, matrix, x2, y2, second);
        atopVertex(buffer, matrix, x1, y1, first);
        atopVertex(buffer, matrix, x2, y2, second);
        atopVertex(buffer, matrix, x2, y1, second);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        PeakRenderStats.countFill();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void atopVertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int argb) {
        float alpha = ((argb >>> 24) & 0xFF) / 255f;
        buffer.addVertex(matrix, x, y, 0).setColor(
                ((argb >> 16) & 0xFF) / 255f * alpha, ((argb >> 8) & 0xFF) / 255f * alpha, (argb & 0xFF) / 255f * alpha, alpha);
    }
}
