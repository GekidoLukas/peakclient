package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

final class PeakPolygonRenderer {
    private static final int GRADIENT_RINGS = 6;

    private PeakPolygonRenderer() {}

    static void fill(GuiGraphics graphics, PeakPaint paint, float[] points, int boxX1, int boxY1, int boxX2, int boxY2) {
        if (paint.isNone() || points.length < 6) return;
        Matrix4f matrix = graphics.pose().last().pose();
        float half = PeakRoundedRect.featherHalf(matrix);
        PeakPaintRenderer.Sampler source = PeakPaintRenderer.sampler(paint, boxX1, boxY1, boxX2, boxY2);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        float[] inner = PeakShapeGeometry.offset(points, -half);
        float[] outer = PeakShapeGeometry.offset(points, half);
        float centerX = PeakShapeGeometry.centerX(points), centerY = PeakShapeGeometry.centerY(points);
        int count = points.length / 2;
        int rings = source.constant() ? 1 : GRADIENT_RINGS;

        for (int ring = 0; ring < rings; ring++) {
            float fromScale = ring / (float) rings, toScale = (ring + 1) / (float) rings;
            for (int i = 0; i < count; i++) {
                int next = (i + 1) % count;
                float toX0 = centerX + (inner[i * 2] - centerX) * toScale, toY0 = centerY + (inner[i * 2 + 1] - centerY) * toScale;
                float toX1 = centerX + (inner[next * 2] - centerX) * toScale, toY1 = centerY + (inner[next * 2 + 1] - centerY) * toScale;
                if (ring == 0) {
                    PeakPaintRenderer.vertex(buffer, matrix, centerX, centerY, source, 1f);
                    PeakPaintRenderer.vertex(buffer, matrix, toX0, toY0, source, 1f);
                    PeakPaintRenderer.vertex(buffer, matrix, toX1, toY1, source, 1f);
                } else {
                    float fromX0 = centerX + (inner[i * 2] - centerX) * fromScale, fromY0 = centerY + (inner[i * 2 + 1] - centerY) * fromScale;
                    float fromX1 = centerX + (inner[next * 2] - centerX) * fromScale, fromY1 = centerY + (inner[next * 2 + 1] - centerY) * fromScale;
                    quad(buffer, matrix, source, fromX0, fromY0, fromX1, fromY1, toX1, toY1, toX0, toY0, 1f, 1f);
                }
            }
        }

        for (int i = 0; i < count; i++) {
            int next = (i + 1) % count;
            quad(buffer, matrix, source, inner[i * 2], inner[i * 2 + 1], inner[next * 2], inner[next * 2 + 1],
                    outer[next * 2], outer[next * 2 + 1], outer[i * 2], outer[i * 2 + 1], 1f, 0f);
        }
        PeakRoundedRect.upload(buffer);
    }

    static void ring(GuiGraphics graphics, PeakPaint paint, float[] outerPoints, float[] innerPoints,
                     int boxX1, int boxY1, int boxX2, int boxY2) {
        if (paint.isNone() || outerPoints.length < 6 || outerPoints.length != innerPoints.length) return;
        Matrix4f matrix = graphics.pose().last().pose();
        float half = PeakRoundedRect.featherHalf(matrix);
        PeakPaintRenderer.Sampler source = PeakPaintRenderer.sampler(paint, boxX1, boxY1, boxX2, boxY2);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        float[] outerSolid = PeakShapeGeometry.offset(outerPoints, -half);
        float[] outerFade = PeakShapeGeometry.offset(outerPoints, half);
        int count = outerPoints.length / 2;
        for (int i = 0; i < count; i++) {
            int next = (i + 1) % count;
            quad(buffer, matrix, source, outerSolid[i * 2], outerSolid[i * 2 + 1], outerSolid[next * 2], outerSolid[next * 2 + 1],
                    innerPoints[next * 2], innerPoints[next * 2 + 1], innerPoints[i * 2], innerPoints[i * 2 + 1], 1f, 1f);
            quad(buffer, matrix, source, outerSolid[i * 2], outerSolid[i * 2 + 1], outerSolid[next * 2], outerSolid[next * 2 + 1],
                    outerFade[next * 2], outerFade[next * 2 + 1], outerFade[i * 2], outerFade[i * 2 + 1], 1f, 0f);
        }
        PeakRoundedRect.upload(buffer);
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix, PeakPaintRenderer.Sampler source,
                             float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy,
                             float alphaFirstEdge, float alphaSecondEdge) {
        PeakPaintRenderer.vertex(buffer, matrix, ax, ay, source, alphaFirstEdge);
        PeakPaintRenderer.vertex(buffer, matrix, bx, by, source, alphaFirstEdge);
        PeakPaintRenderer.vertex(buffer, matrix, cx, cy, source, alphaSecondEdge);
        PeakPaintRenderer.vertex(buffer, matrix, ax, ay, source, alphaFirstEdge);
        PeakPaintRenderer.vertex(buffer, matrix, cx, cy, source, alphaSecondEdge);
        PeakPaintRenderer.vertex(buffer, matrix, dx, dy, source, alphaSecondEdge);
    }
}
