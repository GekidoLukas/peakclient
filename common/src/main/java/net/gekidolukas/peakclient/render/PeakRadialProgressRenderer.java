package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakRadialProgress;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class PeakRadialProgressRenderer implements PeakElementRenderer<PeakRadialProgress> {
    @Override
    public void renderContent(PeakRenderContext ctx, PeakRadialProgress radial, int width, int height) {
        int size = width;
        PeakProgressValues values = PeakProgressValueResolver.resolve(radial);
        if (radial.isHideIfFull() && values.isFull()) return;
        if (radial.isHideIfEmpty() && values.isEmpty()) return;

        float fillFraction = (float) values.fraction();
        int direction = radial.isClockwise() ? 1 : -1;
        float totalArc = radial.getArcDegrees() * direction;
        float sweep = radial.getArcDegrees() * fillFraction * direction;

        float center = size / 2f;
        float outerRadius = size / 2f;
        float innerRadius = radial.getThickness() > 0 ? Math.max(0, outerRadius - radial.getThickness()) : 0;

        drawRing(ctx.graphics(), center, center, innerRadius, outerRadius, 0, totalArc,
                radial.getBackgroundColor(), radial.getStartAngle());
        if (Math.abs(sweep) > 0.01f) {
            drawRing(ctx.graphics(), center, center, innerRadius, outerRadius, 0, sweep,
                    radial.getFillColor(), radial.getStartAngle());
        }
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakRadialProgress radial) {
        return PeakPixelSize.ZERO;
    }

    private void drawRing(GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius,
                          float fromDeg, float toDeg, PeakPaint paint, float baseAngleDeg) {
        int segments = Math.max(1, Math.round(Math.abs(toDeg - fromDeg) / 4f));
        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            float segmentStart = fromDeg + (toDeg - fromDeg) * i / segments;
            float segmentEnd = fromDeg + (toDeg - fromDeg) * (i + 1) / segments;
            float startAngle = (float) Math.toRadians(baseAngleDeg + segmentStart - 90);
            float endAngle = (float) Math.toRadians(baseAngleDeg + segmentEnd - 90);

            int startColor = paint.colorAt((float) i / segments);
            int endColor = paint.colorAt((float) (i + 1) / segments);

            float outerStartX = cx + outerRadius * (float) Math.cos(startAngle), outerStartY = cy + outerRadius * (float) Math.sin(startAngle);
            float outerEndX = cx + outerRadius * (float) Math.cos(endAngle), outerEndY = cy + outerRadius * (float) Math.sin(endAngle);
            float innerStartX = cx + innerRadius * (float) Math.cos(startAngle), innerStartY = cy + innerRadius * (float) Math.sin(startAngle);
            float innerEndX = cx + innerRadius * (float) Math.cos(endAngle), innerEndY = cy + innerRadius * (float) Math.sin(endAngle);

            addVertex(buffer, matrix, innerStartX, innerStartY, startColor);
            addVertex(buffer, matrix, outerStartX, outerStartY, startColor);
            addVertex(buffer, matrix, outerEndX, outerEndY, endColor);

            addVertex(buffer, matrix, innerStartX, innerStartY, startColor);
            addVertex(buffer, matrix, outerEndX, outerEndY, endColor);
            addVertex(buffer, matrix, innerEndX, innerEndY, endColor);
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        PeakRenderStats.countShape();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void addVertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int color) {
        int opaque = PeakArgb.ensureOpaque(color);
        float a = ((opaque >> 24) & 0xFF) / 255f;
        float r = ((opaque >> 16) & 0xFF) / 255f;
        float g = ((opaque >> 8) & 0xFF) / 255f;
        float b = (opaque & 0xFF) / 255f;
        buffer.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
    }
}
