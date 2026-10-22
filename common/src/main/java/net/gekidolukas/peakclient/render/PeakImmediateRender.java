package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakSolidColor;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class PeakImmediateRender {
    private PeakImmediateRender() {}

    private static final ByteBufferBuilder TEXT_BUFFER_POOL = new ByteBufferBuilder(4096);

    public static void fill(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int opaque = PeakArgb.ensureOpaque(color);
        float a = ((opaque >> 24) & 0xFF) / 255f, r = ((opaque >> 16) & 0xFF) / 255f, g = ((opaque >> 8) & 0xFF) / 255f, b = (opaque & 0xFF) / 255f;

        buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, 0).setColor(r, g, b, a);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        PeakRenderStats.countFill();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void fillGradientStrips(GuiGraphics graphics, int x1, int y1, int x2, int y2,
                                          boolean horizontal, int[] boundaryColors) {
        int segments = boundaryColors.length - 1;
        if (segments < 1 || x2 <= x1 || y2 <= y1) return;

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int length = horizontal ? x2 - x1 : y2 - y1;
        int origin = horizontal ? x1 : y1;
        for (int i = 0; i < segments; i++) {
            int stripStart = origin + Math.round((float) i / segments * length);
            int stripEnd = origin + Math.round((float) (i + 1) / segments * length);
            if (stripEnd <= stripStart) continue;
            int startColor = PeakArgb.ensureOpaque(boundaryColors[i]);
            int endColor = PeakArgb.ensureOpaque(boundaryColors[i + 1]);
            if (horizontal) {
                addColoredVertex(buffer, matrix, stripStart, y1, startColor);
                addColoredVertex(buffer, matrix, stripStart, y2, startColor);
                addColoredVertex(buffer, matrix, stripEnd, y2, endColor);
                addColoredVertex(buffer, matrix, stripStart, y1, startColor);
                addColoredVertex(buffer, matrix, stripEnd, y2, endColor);
                addColoredVertex(buffer, matrix, stripEnd, y1, endColor);
            } else {
                addColoredVertex(buffer, matrix, x1, stripStart, startColor);
                addColoredVertex(buffer, matrix, x1, stripEnd, endColor);
                addColoredVertex(buffer, matrix, x2, stripEnd, endColor);
                addColoredVertex(buffer, matrix, x1, stripStart, startColor);
                addColoredVertex(buffer, matrix, x2, stripEnd, endColor);
                addColoredVertex(buffer, matrix, x2, stripStart, startColor);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        PeakRenderStats.countFill();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void fillQuadGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2,
                                        int topLeft, int topRight, int bottomLeft, int bottomRight, int cells) {
        if (x2 <= x1 || y2 <= y1) return;
        int columns = Math.max(1, Math.min(cells, x2 - x1));
        int rows = Math.max(1, Math.min(cells, y2 - y1));

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int row = 0; row < rows; row++) {
            float v0 = row / (float) rows;
            float v1 = (row + 1) / (float) rows;
            int cellTop = y1 + Math.round(v0 * (y2 - y1));
            int cellBottom = y1 + Math.round(v1 * (y2 - y1));
            if (cellBottom <= cellTop) continue;
            for (int column = 0; column < columns; column++) {
                float u0 = column / (float) columns;
                float u1 = (column + 1) / (float) columns;
                int cellLeft = x1 + Math.round(u0 * (x2 - x1));
                int cellRight = x1 + Math.round(u1 * (x2 - x1));
                if (cellRight <= cellLeft) continue;

                int cellTopLeft = bilinear(topLeft, topRight, bottomLeft, bottomRight, u0, v0);
                int cellTopRight = bilinear(topLeft, topRight, bottomLeft, bottomRight, u1, v0);
                int cellBottomLeft = bilinear(topLeft, topRight, bottomLeft, bottomRight, u0, v1);
                int cellBottomRight = bilinear(topLeft, topRight, bottomLeft, bottomRight, u1, v1);
                addColoredVertex(buffer, matrix, cellLeft, cellTop, cellTopLeft);
                addColoredVertex(buffer, matrix, cellLeft, cellBottom, cellBottomLeft);
                addColoredVertex(buffer, matrix, cellRight, cellBottom, cellBottomRight);
                addColoredVertex(buffer, matrix, cellLeft, cellTop, cellTopLeft);
                addColoredVertex(buffer, matrix, cellRight, cellBottom, cellBottomRight);
                addColoredVertex(buffer, matrix, cellRight, cellTop, cellTopRight);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        PeakRenderStats.countFill();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static int bilinear(int topLeft, int topRight, int bottomLeft, int bottomRight, float u, float v) {
        int top = PeakArgb.lerp(topLeft, topRight, u);
        int bottom = PeakArgb.lerp(bottomLeft, bottomRight, u);
        return PeakArgb.lerp(top, bottom, v);
    }

    private static void addColoredVertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int color) {
        buffer.addVertex(matrix, x, y, 0).setColor(
                ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int destX, int destY, int width, int height,
                            int srcX, int srcY, int srcWidth, int srcHeight, int texWidth, int texHeight) {
        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);

        float u0 = srcX / (float) texWidth;
        float u1 = (srcX + srcWidth) / (float) texWidth;
        float v0 = srcY / (float) texHeight;
        float v1 = (srcY + srcHeight) / (float) texHeight;

        buffer.addVertex(matrix, destX, destY, 0).setUv(u0, v0);
        buffer.addVertex(matrix, destX, destY + height, 0).setUv(u0, v1);
        buffer.addVertex(matrix, destX + width, destY + height, 0).setUv(u1, v1);
        buffer.addVertex(matrix, destX, destY, 0).setUv(u0, v0);
        buffer.addVertex(matrix, destX + width, destY + height, 0).setUv(u1, v1);
        buffer.addVertex(matrix, destX + width, destY, 0).setUv(u1, v0);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        PeakRenderStats.countBlit();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int destX, int destY,
                            int srcX, int srcY, int srcWidth, int srcHeight, int texWidth, int texHeight) {
        blit(graphics, texture, destX, destY, srcWidth, srcHeight, srcX, srcY, srcWidth, srcHeight, texWidth, texHeight);
    }

    public static void text(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean shadow) {
        text(graphics, font, PeakStyledText.ofLegacy(text), x, y, color, shadow);
    }

    public static void text(GuiGraphics graphics, Font font, PeakStyledText text, int x, int y, int color, boolean shadow) {
        if (text.isEmpty()) return;
        beginText();
        Matrix4f matrix = graphics.pose().last().pose();
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(TEXT_BUFFER_POOL);

        if (shadow) {
            font.drawInBatch(shadowSequence(text, color), x + 1, y + 1, vanillaShadowColor(color), false, matrix, immediate,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
            immediate.endBatch();
        }
        font.drawInBatch(text.sequence(), x, y, color, false, matrix, immediate, Font.DisplayMode.NORMAL, 0, 0xF000F0);

        PeakRenderStats.countText();
        immediate.endBatch();
        endText();
    }

    private static int glyphColor(PeakPaint paint, float fraction, float spanWidth, int lineHeight) {
        if (PeakPaintRenderer.usesStrips(paint)) return paint.colorAt(fraction);
        return paint.colorAt(fraction, 0.5f, Math.max(1f, spanWidth) / Math.max(1, lineHeight));
    }

    public static void text(GuiGraphics graphics, Font font, String text, int x, int y, PeakPaint paint, boolean shadow) {
        text(graphics, font, PeakStyledText.ofLegacy(text), x, y, paint, shadow);
    }

    public static void text(GuiGraphics graphics, Font font, PeakStyledText text, int x, int y, PeakPaint paint, boolean shadow) {
        text(graphics, font, text, x, y, paint, shadow, x, Math.max(1, text.width(font)));
    }

    public static void text(GuiGraphics graphics, Font font, String text, int x, int y, PeakPaint paint, boolean shadow,
                            float spanX, float spanWidth) {
        text(graphics, font, PeakStyledText.ofLegacy(text), x, y, paint, shadow, spanX, spanWidth);
    }

    public static void text(GuiGraphics graphics, Font font, PeakStyledText text, int x, int y, PeakPaint paint, boolean shadow,
                            float spanX, float spanWidth) {
        if (paint.isNone() || text.isEmpty()) return;
        if (paint instanceof PeakSolidColor solid) {
            text(graphics, font, text, x, y, solid.argb(), shadow);
            return;
        }

        beginText();
        Matrix4f matrix = graphics.pose().last().pose();
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(TEXT_BUFFER_POOL);

        GlyphCollector collector = new GlyphCollector(font, paint, x, spanX, spanWidth);
        text.sequence().accept(collector);

        if (shadow) {
            for (Glyph glyph : collector.glyphs) {
                Style style = glyph.style().getColor() == null ? glyph.style()
                        : glyph.style().withColor(shadowRgb(glyph.style().getColor().getValue()));
                font.drawInBatch(FormattedCharSequence.codepoint(glyph.codepoint(), style), glyph.x() + 1, y + 1,
                        vanillaShadowColor(glyph.color()), false, matrix, immediate, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            }
            immediate.endBatch();
        }
        for (Glyph glyph : collector.glyphs) {
            font.drawInBatch(FormattedCharSequence.codepoint(glyph.codepoint(), glyph.style()), glyph.x(), y, glyph.color(), false,
                    matrix, immediate, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }

        PeakRenderStats.countText();
        immediate.endBatch();
        endText();
    }

    private static void beginText() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    private static void endText() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static FormattedCharSequence shadowSequence(PeakStyledText text, int baseColor) {
        FormattedCharSequence source = text.sequence();
        int baseRgb = baseColor & 0xFFFFFF;
        return sink -> source.accept((index, style, codepoint) -> {
            TextColor color = style.getColor();
            return sink.accept(index, style.withColor(shadowRgb(color != null ? color.getValue() : baseRgb)), codepoint);
        });
    }

    private record Glyph(Style style, int codepoint, float x, int color) {}

    private static final class GlyphCollector implements FormattedCharSink {
        private final Font font;
        private final PeakPaint paint;
        private final float spanX;
        private final float spanWidth;
        private final List<Glyph> glyphs = new ArrayList<>();
        private float cursor;

        private GlyphCollector(Font font, PeakPaint paint, float start, float spanX, float spanWidth) {
            this.font = font;
            this.paint = paint;
            this.spanX = spanX;
            this.spanWidth = spanWidth;
            this.cursor = start;
        }

        @Override
        public boolean accept(int index, Style style, int codepoint) {
            float width = font.width(FormattedCharSequence.codepoint(codepoint, style));
            TextColor styleColor = style.getColor();
            int color = styleColor != null ? 0xFF000000 | styleColor.getValue()
                    : glyphColor(paint, (cursor - spanX + width / 2f) / Math.max(1f, spanWidth), spanWidth, font.lineHeight);
            glyphs.add(new Glyph(style, codepoint, cursor, color));
            cursor += width;
            return true;
        }
    }

    private static int shadowRgb(int rgb) {
        return (rgb & 0xFCFCFC) >> 2;
    }

    private static int vanillaShadowColor(int color) {
        return (color & 0xFF000000) | ((color & 0xFCFCFC) >> 2);
    }
}
