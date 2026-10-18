package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakChartEntry;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakLegendPosition;
import net.gekidolukas.peakclient.ui.PeakPieChart;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.List;

public final class PeakPieChartRenderer implements PeakElementRenderer<PeakPieChart> {

    @Override
    public void renderContent(PeakRenderContext ctx, PeakPieChart chart, int width, int height) {
        List<PeakChartEntry> entries = chart.getEntries();
        if (entries.isEmpty()) return;

        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        PeakLegendPosition legendPos = chart.isShowLabels() ? chart.getLegendPosition() : PeakLegendPosition.NONE;

        int legendWidth = 0, legendHeight = 0;
        if (legendPos == PeakLegendPosition.RIGHT || legendPos == PeakLegendPosition.LEFT) {
            legendWidth = measureLegendWidth(font, entries) + 4;
        } else if (legendPos == PeakLegendPosition.TOP || legendPos == PeakLegendPosition.BOTTOM) {
            legendHeight = entries.size() * (font.lineHeight + 1) + 2;
        }

        int pieAreaSize = Math.min(width - legendWidth, height - legendHeight);
        pieAreaSize = Math.max(1, pieAreaSize);

        int pieOffsetX = legendPos == PeakLegendPosition.LEFT ? legendWidth : 0;
        int pieOffsetY = legendPos == PeakLegendPosition.TOP ? legendHeight : 0;

        float cx = pieOffsetX + pieAreaSize / 2f;
        float cy = pieOffsetY + pieAreaSize / 2f;
        float outerRadius = pieAreaSize / 2f;
        float innerRadius = chart.getDonutThickness() > 0 ? Math.max(0, outerRadius - chart.getDonutThickness()) : 0;

        if (!chart.getBackgroundColor().isNone()) {
            drawSegment(graphics, cx, cy, innerRadius, outerRadius, 0, 360, chart.getBackgroundColor());
        }

        double total = entries.stream().mapToDouble(PeakChartEntry::value).sum();
        if (total > 0) {
            float cursor = chart.getStartAngle();
            for (PeakChartEntry entry : entries) {
                float sweep = (float) (entry.value() / total * 360.0);
                if (sweep > 0.01f) {
                    drawSegment(graphics, cx, cy, innerRadius, outerRadius, cursor, cursor + sweep, entry.color());
                }
                cursor += sweep;
            }
        }

        if (legendPos != PeakLegendPosition.NONE) {
            drawLegend(graphics, font, entries, legendPos, width, height, pieAreaSize, pieOffsetX, pieOffsetY, legendWidth, legendHeight);
        }
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakPieChart chart) {
        return PeakPixelSize.ZERO;
    }

    private int measureLegendWidth(Font font, List<PeakChartEntry> entries) {
        int max = 0;
        for (PeakChartEntry e : entries) {
            String text = e.label() != null ? e.label() : "";
            max = Math.max(max, 7 + font.width(text));
        }
        return max;
    }

    private void drawLegend(GuiGraphics graphics, Font font, List<PeakChartEntry> entries, PeakLegendPosition pos,
                            int width, int height, int pieAreaSize, int pieOffsetX, int pieOffsetY,
                            int legendWidth, int legendHeight) {
        int startX, startY;
        switch (pos) {
            case RIGHT -> { startX = pieOffsetX + pieAreaSize + 4; startY = 0; }
            case LEFT -> { startX = 0; startY = 0; }
            case TOP -> { startX = 0; startY = 0; }
            case BOTTOM -> { startX = 0; startY = pieOffsetY + pieAreaSize + 2; }
            default -> { return; }
        }

        int y = startY;
        for (PeakChartEntry entry : entries) {
            String text = entry.label() != null ? entry.label() : "";
            PeakPaintRenderer.fill(graphics, entry.color(), startX, y + 1, startX + 5, y + 6);
            PeakImmediateRender.text(graphics, font, text, startX + 7, y, 0xFFFFFF, true);
            y += font.lineHeight + 1;
        }
    }

    private void drawSegment(GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius,
                             float fromDeg, float toDeg, PeakPaint paint) {
        int segments = Math.max(1, Math.round(Math.abs(toDeg - fromDeg) / 6f));
        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        PeakPaintRenderer.Sampler sampler = PeakPaintRenderer.sampler(paint,
                Math.round(cx - outerRadius), Math.round(cy - outerRadius), Math.round(cx + outerRadius), Math.round(cy + outerRadius));

        for (int i = 0; i < segments; i++) {
            float startAngle = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments - 90);
            float endAngle = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / segments - 90);

            float outerStartX = cx + outerRadius * (float) Math.cos(startAngle), outerStartY = cy + outerRadius * (float) Math.sin(startAngle);
            float outerEndX = cx + outerRadius * (float) Math.cos(endAngle), outerEndY = cy + outerRadius * (float) Math.sin(endAngle);

            if (innerRadius > 0) {
                float innerStartX = cx + innerRadius * (float) Math.cos(startAngle), innerStartY = cy + innerRadius * (float) Math.sin(startAngle);
                float innerEndX = cx + innerRadius * (float) Math.cos(endAngle), innerEndY = cy + innerRadius * (float) Math.sin(endAngle);
                sampler.vertex(buffer, matrix, innerStartX, innerStartY);
                sampler.vertex(buffer, matrix, outerStartX, outerStartY);
                sampler.vertex(buffer, matrix, outerEndX, outerEndY);
                sampler.vertex(buffer, matrix, innerStartX, innerStartY);
                sampler.vertex(buffer, matrix, outerEndX, outerEndY);
                sampler.vertex(buffer, matrix, innerEndX, innerEndY);
            } else {
                sampler.vertex(buffer, matrix, cx, cy);
                sampler.vertex(buffer, matrix, outerStartX, outerStartY);
                sampler.vertex(buffer, matrix, outerEndX, outerEndY);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        PeakRenderStats.countShape();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}