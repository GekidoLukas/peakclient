package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class PeakRoundedRect {
    private PeakRoundedRect() {}

    public static void fillRounded(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int color) {
        int w = x2 - x1, h = y2 - y1;
        int cornerRadius = Math.min(radius, Math.min(w, h) / 2);
        if (cornerRadius <= 0) { PeakImmediateRender.fill(graphics, x1, y1, x2, y2, color); return; }

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        addFilledRoundedRegion(buffer, matrix, x1, y1, x2, y2, cornerRadius, color, true);

        upload(buffer);
    }

    public static void fillRoundedWithBorder(GuiGraphics graphics, int x1, int y1, int x2, int y2,
                                             int outerRadius, int borderWidth, int borderColor, int backgroundColor) {
        int w = x2 - x1, h = y2 - y1;
        int outer = Math.min(outerRadius, Math.min(w, h) / 2);
        int border = Math.min(borderWidth, Math.min(w, h) / 2);

        if (border <= 0) { fillRounded(graphics, x1, y1, x2, y2, outer, backgroundColor); return; }

        int innerX1 = x1 + border, innerY1 = y1 + border, innerX2 = x2 - border, innerY2 = y2 - border;
        int inner = Math.max(0, outer - border);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        addQuad(buffer, matrix, x1 + outer, y1, x2 - outer, y1 + border, borderColor);
        addQuad(buffer, matrix, x1 + outer, y2 - border, x2 - outer, y2, borderColor);
        addQuad(buffer, matrix, x1, y1 + outer, x1 + border, y2 - outer, borderColor);
        addQuad(buffer, matrix, x2 - border, y1 + outer, x2, y2 - outer, borderColor);

        addQuarterRing(buffer, matrix, x1 + outer, y1 + outer, inner, outer, 180, 270, borderColor);
        addQuarterRing(buffer, matrix, x2 - outer, y1 + outer, inner, outer, 270, 360, borderColor);
        addQuarterRing(buffer, matrix, x1 + outer, y2 - outer, inner, outer, 90, 180, borderColor);
        addQuarterRing(buffer, matrix, x2 - outer, y2 - outer, inner, outer, 0, 90, borderColor);

        if (backgroundColor != 0) addFilledRoundedRegion(buffer, matrix, innerX1, innerY1, innerX2, innerY2, inner, backgroundColor);

        upload(buffer);
    }

    public static void fillRoundedGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, boolean underBorder,
                                           boolean horizontal, int[] boundaryColors) {
        int w = x2 - x1, h = y2 - y1;
        if (w <= 0 || h <= 0 || boundaryColors.length < 2) return;
        int cornerRadius = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        boolean smooth = !underBorder && cornerRadius > 0;

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int length = horizontal ? w : h;
        int cross = horizontal ? h : w;
        for (int i = 0; i < length; i++) {
            int inset = smooth ? (i < cornerRadius || i >= length - cornerRadius ? cornerRadius : 0)
                    : cornerInset(i, length, cornerRadius, underBorder);
            if (cross - inset * 2 <= 0) continue;
            int startColor = PeakArgb.ensureOpaque(sampleBoundary(boundaryColors, i / (float) length));
            int endColor = PeakArgb.ensureOpaque(sampleBoundary(boundaryColors, (i + 1) / (float) length));
            if (horizontal) {
                addGradientQuad(buffer, matrix, x1 + i, y1 + inset, x1 + i + 1, y2 - inset, startColor, endColor, true);
            } else {
                addGradientQuad(buffer, matrix, x1 + inset, y1 + i, x2 - inset, y1 + i + 1, startColor, endColor, false);
            }
        }

        if (smooth) {
            int originAxis = horizontal ? x1 : y1;
            addGradientCorner(buffer, matrix, x1 + cornerRadius, y1 + cornerRadius, cornerRadius, 180, 270,
                    horizontal, originAxis, length, boundaryColors);
            addGradientCorner(buffer, matrix, x2 - cornerRadius, y1 + cornerRadius, cornerRadius, 270, 360,
                    horizontal, originAxis, length, boundaryColors);
            addGradientCorner(buffer, matrix, x1 + cornerRadius, y2 - cornerRadius, cornerRadius, 90, 180,
                    horizontal, originAxis, length, boundaryColors);
            addGradientCorner(buffer, matrix, x2 - cornerRadius, y2 - cornerRadius, cornerRadius, 0, 90,
                    horizontal, originAxis, length, boundaryColors);
        }

        upload(buffer);
    }

    private static void addGradientCorner(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, int radius,
                                          float fromDeg, float toDeg, boolean horizontal, int originAxis, int length,
                                          int[] boundaryColors) {
        int segments = Math.max(8, Math.min(24, radius * 2));
        float half = featherHalf(matrix);
        float inner = radius - half;
        float outer = radius + half;
        int centerColor = cornerColor(cx, cy, horizontal, originAxis, length, boundaryColors);
        for (int i = 0; i < segments; i++) {
            float angle0 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
            float angle1 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / segments);
            float cos0 = (float) Math.cos(angle0), sin0 = (float) Math.sin(angle0);
            float cos1 = (float) Math.cos(angle1), sin1 = (float) Math.sin(angle1);
            float innerX0 = cx + inner * cos0, innerY0 = cy + inner * sin0;
            float innerX1 = cx + inner * cos1, innerY1 = cy + inner * sin1;
            float outerX0 = cx + outer * cos0, outerY0 = cy + outer * sin0;
            float outerX1 = cx + outer * cos1, outerY1 = cy + outer * sin1;
            int innerColor0 = cornerColor(innerX0, innerY0, horizontal, originAxis, length, boundaryColors);
            int innerColor1 = cornerColor(innerX1, innerY1, horizontal, originAxis, length, boundaryColors);
            int outerColor0 = cornerColor(outerX0, outerY0, horizontal, originAxis, length, boundaryColors) & 0x00FFFFFF;
            int outerColor1 = cornerColor(outerX1, outerY1, horizontal, originAxis, length, boundaryColors) & 0x00FFFFFF;

            addColorVertex(buffer, matrix, cx, cy, centerColor);
            addColorVertex(buffer, matrix, innerX0, innerY0, innerColor0);
            addColorVertex(buffer, matrix, innerX1, innerY1, innerColor1);

            addColorVertex(buffer, matrix, innerX0, innerY0, innerColor0);
            addColorVertex(buffer, matrix, outerX0, outerY0, outerColor0);
            addColorVertex(buffer, matrix, outerX1, outerY1, outerColor1);
            addColorVertex(buffer, matrix, innerX0, innerY0, innerColor0);
            addColorVertex(buffer, matrix, outerX1, outerY1, outerColor1);
            addColorVertex(buffer, matrix, innerX1, innerY1, innerColor1);
        }
    }

    static float featherHalf(Matrix4f matrix) {
        float guiScale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        float matrixScale = (float) Math.sqrt(matrix.m00() * matrix.m00() + matrix.m01() * matrix.m01());
        float devicePixelsPerUnit = Math.max(0.01f, guiScale * matrixScale);
        return Math.max(0.1f, Math.min(0.5f, 0.5f / devicePixelsPerUnit));
    }

    private static int cornerColor(float x, float y, boolean horizontal, int originAxis, int length, int[] boundaryColors) {
        float along = horizontal ? x : y;
        float t = (along - originAxis) / Math.max(1, length);
        return PeakArgb.ensureOpaque(sampleBoundary(boundaryColors, t));
    }

    private static int cornerInset(int index, int length, int radius, boolean generous) {
        if (radius <= 0) return 0;
        boolean firstHalf = index + 0.5 < length / 2.0;
        double position = generous ? (firstHalf ? index + 1 : index) : index + 0.5;
        double edge = position < radius ? radius - position : (position > length - radius ? position - (length - radius) : -1);
        if (edge < 0) return 0;
        double inset = radius - Math.sqrt(Math.max(0, (double) radius * radius - edge * edge));
        return generous ? (int) Math.floor(inset) : (int) Math.round(inset);
    }

    static int sampleBoundary(int[] colors, float t) {
        int segments = colors.length - 1;
        float scaled = Math.max(0f, Math.min(1f, t)) * segments;
        int index = Math.min((int) scaled, segments - 1);
        return PeakArgb.lerp(colors[index], colors[index + 1], scaled - index);
    }

    private static void addGradientQuad(BufferBuilder buffer, Matrix4f matrix, int x1, int y1, int x2, int y2,
                                        int startColor, int endColor, boolean horizontal) {
        int topLeft = startColor, bottomLeft = horizontal ? startColor : endColor;
        int topRight = horizontal ? endColor : startColor, bottomRight = endColor;
        addColorVertex(buffer, matrix, x1, y1, topLeft);
        addColorVertex(buffer, matrix, x1, y2, bottomLeft);
        addColorVertex(buffer, matrix, x2, y2, bottomRight);
        addColorVertex(buffer, matrix, x1, y1, topLeft);
        addColorVertex(buffer, matrix, x2, y2, bottomRight);
        addColorVertex(buffer, matrix, x2, y1, topRight);
    }

    private static void addColorVertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int color) {
        buffer.addVertex(matrix, x, y, 0).setColor(
                ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
    }

    public static void fillFeathered(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int extent, int color) {
        if (extent <= 0 || x2 <= x1 || y2 <= y1) return;
        float cornerRadius = Math.max(0, Math.min(radius, Math.min(x2 - x1, y2 - y1) / 2f));
        int opaque = PeakArgb.ensureOpaque(color);
        float baseAlpha = ((opaque >> 24) & 0xFF) / 255f;
        float red = ((opaque >> 16) & 0xFF) / 255f, green = ((opaque >> 8) & 0xFF) / 255f, blue = (opaque & 0xFF) / 255f;

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int segments = 8;
        int points = 4 * (segments + 1);
        float[] innerX = new float[points], innerY = new float[points];
        float[] outerX = new float[points], outerY = new float[points];
        buildOutline(x1, y1, x2, y2, cornerRadius, 0f, segments, innerX, innerY);

        float centerX = (x1 + x2) / 2f, centerY = (y1 + y2) / 2f;
        for (int i = 0; i < points; i++) {
            int next = (i + 1) % points;
            buffer.addVertex(matrix, centerX, centerY, 0).setColor(red, green, blue, baseAlpha);
            buffer.addVertex(matrix, innerX[i], innerY[i], 0).setColor(red, green, blue, baseAlpha);
            buffer.addVertex(matrix, innerX[next], innerY[next], 0).setColor(red, green, blue, baseAlpha);
        }

        for (int step = 0; step < extent; step++) {
            float innerAlpha = baseAlpha * feather(step / (float) extent);
            float outerAlpha = baseAlpha * feather((step + 1) / (float) extent);
            buildOutline(x1, y1, x2, y2, cornerRadius, step + 1, segments, outerX, outerY);
            for (int i = 0; i < points; i++) {
                int next = (i + 1) % points;
                buffer.addVertex(matrix, innerX[i], innerY[i], 0).setColor(red, green, blue, innerAlpha);
                buffer.addVertex(matrix, outerX[i], outerY[i], 0).setColor(red, green, blue, outerAlpha);
                buffer.addVertex(matrix, outerX[next], outerY[next], 0).setColor(red, green, blue, outerAlpha);
                buffer.addVertex(matrix, innerX[i], innerY[i], 0).setColor(red, green, blue, innerAlpha);
                buffer.addVertex(matrix, outerX[next], outerY[next], 0).setColor(red, green, blue, outerAlpha);
                buffer.addVertex(matrix, innerX[next], innerY[next], 0).setColor(red, green, blue, innerAlpha);
            }
            System.arraycopy(outerX, 0, innerX, 0, points);
            System.arraycopy(outerY, 0, innerY, 0, points);
        }

        upload(buffer);
    }

    static float feather(float t) {
        float clamped = Math.max(0f, Math.min(1f, t));
        return 1f - clamped * clamped * (3f - 2f * clamped);
    }

    static void buildOutline(float x1, float y1, float x2, float y2, float radius, float grow,
                                     int segments, float[] xs, float[] ys) {
        float arc = radius + grow;
        float[][] centers = {
                { x1 + radius, y1 + radius }, { x2 - radius, y1 + radius },
                { x2 - radius, y2 - radius }, { x1 + radius, y2 - radius }
        };
        float[] startDegrees = { 180f, 270f, 0f, 90f };
        int index = 0;
        for (int corner = 0; corner < 4; corner++) {
            for (int segment = 0; segment <= segments; segment++) {
                float radians = (float) Math.toRadians(startDegrees[corner] + 90f * segment / segments);
                xs[index] = centers[corner][0] + arc * (float) Math.cos(radians);
                ys[index] = centers[corner][1] + arc * (float) Math.sin(radians);
                index++;
            }
        }
    }

    static void addFilledRoundedRegion(BufferBuilder buffer, Matrix4f matrix,
                                       int x1, int y1, int x2, int y2, int radius, int color) {
        addFilledRoundedRegion(buffer, matrix, x1, y1, x2, y2, radius, color, false);
    }

    static void addFilledRoundedRegion(BufferBuilder buffer, Matrix4f matrix,
                                       int x1, int y1, int x2, int y2, int radius, int color, boolean smooth) {
        if (radius <= 0) { addQuad(buffer, matrix, x1, y1, x2, y2, color); return; }

        addQuad(buffer, matrix, x1 + radius, y1 + radius, x2 - radius, y2 - radius, color);
        addQuad(buffer, matrix, x1 + radius, y1, x2 - radius, y1 + radius, color);
        addQuad(buffer, matrix, x1 + radius, y2 - radius, x2 - radius, y2, color);
        addQuad(buffer, matrix, x1, y1 + radius, x1 + radius, y2 - radius, color);
        addQuad(buffer, matrix, x2 - radius, y1 + radius, x2, y2 - radius, color);

        float half = smooth ? featherHalf(matrix) : 0f;
        float fanRadius = radius - half;
        addQuarterCircle(buffer, matrix, x1 + radius, y1 + radius, fanRadius, 180, 270, color);
        addQuarterCircle(buffer, matrix, x2 - radius, y1 + radius, fanRadius, 270, 360, color);
        addQuarterCircle(buffer, matrix, x1 + radius, y2 - radius, fanRadius, 90, 180, color);
        addQuarterCircle(buffer, matrix, x2 - radius, y2 - radius, fanRadius, 0, 90, color);
        if (smooth) {
            float outerRadius = radius + half;
            addQuarterRing(buffer, matrix, x1 + radius, y1 + radius, fanRadius, outerRadius, 180, 270, color, true);
            addQuarterRing(buffer, matrix, x2 - radius, y1 + radius, fanRadius, outerRadius, 270, 360, color, true);
            addQuarterRing(buffer, matrix, x1 + radius, y2 - radius, fanRadius, outerRadius, 90, 180, color, true);
            addQuarterRing(buffer, matrix, x2 - radius, y2 - radius, fanRadius, outerRadius, 0, 90, color, true);
        }
    }

    static void addQuad(BufferBuilder buffer, Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
        if (x2 <= x1 || y2 <= y1) return;
        int opaque = PeakArgb.ensureOpaque(color);
        float a = ((opaque >> 24) & 0xFF) / 255f, r = ((opaque >> 16) & 0xFF) / 255f, g = ((opaque >> 8) & 0xFF) / 255f, b = (opaque & 0xFF) / 255f;
        buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, 0).setColor(r, g, b, a);
    }

    private static void addQuarterCircle(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float radius,
                                         float fromDeg, float toDeg, int color) {
        int segments = Math.max(8, Math.min(24, Math.round(radius * 2)));
        int opaque = PeakArgb.ensureOpaque(color);
        float a = ((opaque >> 24) & 0xFF) / 255f, r = ((opaque >> 16) & 0xFF) / 255f, g = ((opaque >> 8) & 0xFF) / 255f, b = (opaque & 0xFF) / 255f;
        for (int i = 0; i < segments; i++) {
            float rad0 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
            float rad1 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / segments);
            float startX = cx + radius * (float) Math.cos(rad0), startY = cy + radius * (float) Math.sin(rad0);
            float endX = cx + radius * (float) Math.cos(rad1), endY = cy + radius * (float) Math.sin(rad1);
            buffer.addVertex(matrix, cx, cy, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, startX, startY, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, endX, endY, 0).setColor(r, g, b, a);
        }
    }

    private static void addQuarterRing(BufferBuilder buffer, Matrix4f matrix, float cx, float cy,
                                       float innerRadius, float outerRadius, float fromDeg, float toDeg, int color) {
        addQuarterRing(buffer, matrix, cx, cy, innerRadius, outerRadius, fromDeg, toDeg, color, false);
    }

    private static void addQuarterRing(BufferBuilder buffer, Matrix4f matrix, float cx, float cy,
                                       float innerRadius, float outerRadius, float fromDeg, float toDeg, int color,
                                       boolean fadeOut) {
        int segments = Math.max(8, Math.min(24, Math.round(outerRadius * 2)));
        int opaque = PeakArgb.ensureOpaque(color);
        float a = ((opaque >> 24) & 0xFF) / 255f, r = ((opaque >> 16) & 0xFF) / 255f, g = ((opaque >> 8) & 0xFF) / 255f, b = (opaque & 0xFF) / 255f;
        float outerAlpha = fadeOut ? 0f : a;
        for (int i = 0; i < segments; i++) {
            float rad0 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
            float rad1 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / segments);
            float ox0 = cx + outerRadius * (float) Math.cos(rad0), oy0 = cy + outerRadius * (float) Math.sin(rad0);
            float ox1 = cx + outerRadius * (float) Math.cos(rad1), oy1 = cy + outerRadius * (float) Math.sin(rad1);
            float ix0 = cx + innerRadius * (float) Math.cos(rad0), iy0 = cy + innerRadius * (float) Math.sin(rad0);
            float ix1 = cx + innerRadius * (float) Math.cos(rad1), iy1 = cy + innerRadius * (float) Math.sin(rad1);

            buffer.addVertex(matrix, ix0, iy0, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, ox0, oy0, 0).setColor(r, g, b, outerAlpha);
            buffer.addVertex(matrix, ox1, oy1, 0).setColor(r, g, b, outerAlpha);
            buffer.addVertex(matrix, ix0, iy0, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, ox1, oy1, 0).setColor(r, g, b, outerAlpha);
            buffer.addVertex(matrix, ix1, iy1, 0).setColor(r, g, b, a);
        }
    }

    static void upload(BufferBuilder buffer) {
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