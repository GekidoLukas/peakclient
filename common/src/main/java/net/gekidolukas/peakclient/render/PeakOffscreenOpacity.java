package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class PeakOffscreenOpacity {
    private static TextureTarget target;
    private static int depth;

    private PeakOffscreenOpacity() {}

    static boolean closeBuffer(GuiGraphics graphics) {
        depth = Math.max(0, depth - 1);
        if (depth > 0) return false;
        graphics.flush();
        return true;
    }

    static TextureTarget contentTarget() {
        return target;
    }

    public static void resetDepth() {
        depth = 0;
    }

    public static boolean isActive() {
        return depth > 0;
    }

    public static void begin(Minecraft mc, GuiGraphics graphics) {
        depth++;
        if (depth > 1) return;

        RenderTarget main = mc.getMainRenderTarget();
        if (target == null) {
            target = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        } else if (target.width != main.width || target.height != main.height) {
            target.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        graphics.flush();
        target.setClearColor(0f, 0f, 0f, 0f);
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(true);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void end(Minecraft mc, GuiGraphics graphics, float opacity) {
        depth = Math.max(0, depth - 1);
        if (depth > 0) return;

        graphics.flush();
        mc.getMainRenderTarget().bindWrite(true);

        float width = mc.getWindow().getGuiScaledWidth();
        float height = mc.getWindow().getGuiScaledHeight();
        Matrix4f matrix = new Matrix4f();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, 0, 0, 0).setUv(0f, 1f);
        buffer.addVertex(matrix, 0, height, 0).setUv(0f, 0f);
        buffer.addVertex(matrix, width, height, 0).setUv(1f, 0f);
        buffer.addVertex(matrix, 0, 0, 0).setUv(0f, 1f);
        buffer.addVertex(matrix, width, height, 0).setUv(1f, 0f);
        buffer.addVertex(matrix, width, 0, 0).setUv(1f, 1f);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());
        RenderSystem.setShaderColor(opacity, opacity, opacity, opacity);
        composite(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, opacity);
    }

    public static void endMasked(Minecraft mc, GuiGraphics graphics, float x1, float y1, float x2, float y2,
                                 float fadeStart, float fadeEnd, boolean vertical) {
        depth = Math.max(0, depth - 1);
        if (depth > 0) return;

        graphics.flush();
        mc.getMainRenderTarget().bindWrite(true);

        float width = mc.getWindow().getGuiScaledWidth();
        float height = mc.getWindow().getGuiScaledHeight();
        float length = vertical ? y2 - y1 : x2 - x1;
        float start = Math.max(0f, fadeStart);
        float end = Math.max(0f, fadeEnd);
        if (start + end > length && start + end > 0f) {
            float scale = length / (start + end);
            start *= scale;
            end *= scale;
        }

        float[] positions = {0f, start, length - end, length};
        float[] alphas = {start > 0f ? 0f : 1f, 1f, 1f, end > 0f ? 0f : 1f};

        Matrix4f matrix = new Matrix4f();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int i = 0; i < 3; i++) {
            if (positions[i + 1] - positions[i] <= 0f) continue;
            float a0 = alphas[i], a1 = alphas[i + 1];
            if (vertical) {
                addMaskedQuad(buffer, matrix, x1, y1 + positions[i], x2, y1 + positions[i + 1], a0, a1, true, width, height);
            } else {
                addMaskedQuad(buffer, matrix, x1 + positions[i], y1, x1 + positions[i + 1], y2, a0, a1, false, width, height);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        composite(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endShaped(Minecraft mc, GuiGraphics graphics, float x1, float y1, float x2, float y2,
                                 float radius, float opacity) {
        endShaped(mc, graphics, PeakShapeMask.rounded(x1, y1, x2, y2, radius), opacity);
    }

    public static void endShaped(Minecraft mc, GuiGraphics graphics, PeakShapeMask mask, float opacity) {
        depth = Math.max(0, depth - 1);
        if (depth > 0) return;

        graphics.flush();
        mc.getMainRenderTarget().bindWrite(true);

        float width = mc.getWindow().getGuiScaledWidth();
        float height = mc.getWindow().getGuiScaledHeight();
        if (!mask.hasPopOut()) {
            presentMask(buildShapeBuffer(mask, width, height), opacity);
            return;
        }

        int left = Float.isNaN(mask.leftLine()) ? 0 : Math.round(mask.leftLine());
        int top = Float.isNaN(mask.topLine()) ? 0 : Math.round(mask.topLine());
        int right = Float.isNaN(mask.rightLine()) ? (int) Math.ceil(width) : Math.round(mask.rightLine());
        int bottom = Float.isNaN(mask.bottomLine()) ? (int) Math.ceil(height) : Math.round(mask.bottomLine());
        graphics.enableScissor(left, top, right, bottom);
        presentMask(buildShapeBuffer(mask, width, height), opacity);
        graphics.disableScissor();

        float bandTop = Float.isNaN(mask.topLine()) ? mask.y1() : top;
        float bandBottom = Float.isNaN(mask.bottomLine()) ? mask.y2() : bottom;
        Matrix4f matrix = new Matrix4f();
        BufferBuilder zones = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        if (!Float.isNaN(mask.topLine())) addShapeQuad(zones, matrix, mask.x1(), 0f, mask.x2(), top, width, height);
        if (!Float.isNaN(mask.bottomLine())) addShapeQuad(zones, matrix, mask.x1(), bottom, mask.x2(), height, width, height);
        if (!Float.isNaN(mask.leftLine())) addShapeQuad(zones, matrix, 0f, bandTop, left, bandBottom, width, height);
        if (!Float.isNaN(mask.rightLine())) addShapeQuad(zones, matrix, right, bandTop, width, bandBottom, width, height);
        presentMask(zones, opacity);
    }

    private static BufferBuilder buildShapeBuffer(PeakShapeMask mask, float width, float height) {
        float half = PeakRoundedRect.featherHalf(new Matrix4f());
        Matrix4f matrix = new Matrix4f();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        if (mask.outline() != null) {
            addPolygonMask(buffer, matrix, mask.outline(), half, width, height);
            return buffer;
        }
        float x1 = mask.x1(), y1 = mask.y1(), x2 = mask.x2(), y2 = mask.y2();
        float corner = Math.max(0f, Math.min(mask.radius(), Math.min(x2 - x1, y2 - y1) / 2f));
        if (corner <= 0f) {
            addShapeQuad(buffer, matrix, x1, y1, x2, y2, width, height);
        } else {
            addShapeQuad(buffer, matrix, x1 + corner, y1, x2 - corner, y2, width, height);
            addShapeQuad(buffer, matrix, x1, y1 + corner, x1 + corner, y2 - corner, width, height);
            addShapeQuad(buffer, matrix, x2 - corner, y1 + corner, x2, y2 - corner, width, height);
            addShapeCorner(buffer, matrix, x1 + corner, y1 + corner, corner, half, 180, 270, width, height);
            addShapeCorner(buffer, matrix, x2 - corner, y1 + corner, corner, half, 270, 360, width, height);
            addShapeCorner(buffer, matrix, x1 + corner, y2 - corner, corner, half, 90, 180, width, height);
            addShapeCorner(buffer, matrix, x2 - corner, y2 - corner, corner, half, 0, 90, width, height);
        }
        return buffer;
    }

    private static void presentMask(BufferBuilder buffer, float opacity) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());
        RenderSystem.setShaderColor(opacity, opacity, opacity, opacity);
        composite(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void addPolygonMask(BufferBuilder buffer, Matrix4f matrix, float[] points, float half,
                                       float width, float height) {
        float[] inner = PeakShapeGeometry.offset(points, -half);
        float[] outer = PeakShapeGeometry.offset(points, half);
        float centerX = PeakShapeGeometry.centerX(points), centerY = PeakShapeGeometry.centerY(points);
        int count = points.length / 2;
        for (int i = 0; i < count; i++) {
            int next = (i + 1) % count;
            shapeVertex(buffer, matrix, centerX, centerY, 1f, width, height);
            shapeVertex(buffer, matrix, inner[i * 2], inner[i * 2 + 1], 1f, width, height);
            shapeVertex(buffer, matrix, inner[next * 2], inner[next * 2 + 1], 1f, width, height);

            shapeVertex(buffer, matrix, inner[i * 2], inner[i * 2 + 1], 1f, width, height);
            shapeVertex(buffer, matrix, outer[i * 2], outer[i * 2 + 1], 0f, width, height);
            shapeVertex(buffer, matrix, outer[next * 2], outer[next * 2 + 1], 0f, width, height);
            shapeVertex(buffer, matrix, inner[i * 2], inner[i * 2 + 1], 1f, width, height);
            shapeVertex(buffer, matrix, outer[next * 2], outer[next * 2 + 1], 0f, width, height);
            shapeVertex(buffer, matrix, inner[next * 2], inner[next * 2 + 1], 1f, width, height);
        }
    }

    private static void shapeVertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float alpha, float width, float height) {
        buffer.addVertex(matrix, x, y, 0).setUv(x / width, 1f - y / height).setColor(alpha, alpha, alpha, alpha);
    }

    private static void addShapeQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2,
                                     float width, float height) {
        if (x2 <= x1 || y2 <= y1) return;
        shapeVertex(buffer, matrix, x1, y1, 1f, width, height);
        shapeVertex(buffer, matrix, x1, y2, 1f, width, height);
        shapeVertex(buffer, matrix, x2, y2, 1f, width, height);
        shapeVertex(buffer, matrix, x1, y1, 1f, width, height);
        shapeVertex(buffer, matrix, x2, y2, 1f, width, height);
        shapeVertex(buffer, matrix, x2, y1, 1f, width, height);
    }

    private static void addShapeCorner(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float radius, float half,
                                       float fromDeg, float toDeg, float width, float height) {
        int segments = Math.max(8, Math.min(24, Math.round(radius * 2f)));
        float inner = Math.max(0f, radius - half);
        float outer = radius + half;
        for (int i = 0; i < segments; i++) {
            float angle0 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
            float angle1 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / segments);
            float cos0 = (float) Math.cos(angle0), sin0 = (float) Math.sin(angle0);
            float cos1 = (float) Math.cos(angle1), sin1 = (float) Math.sin(angle1);
            float innerX0 = cx + inner * cos0, innerY0 = cy + inner * sin0;
            float innerX1 = cx + inner * cos1, innerY1 = cy + inner * sin1;
            float outerX0 = cx + outer * cos0, outerY0 = cy + outer * sin0;
            float outerX1 = cx + outer * cos1, outerY1 = cy + outer * sin1;

            shapeVertex(buffer, matrix, cx, cy, 1f, width, height);
            shapeVertex(buffer, matrix, innerX0, innerY0, 1f, width, height);
            shapeVertex(buffer, matrix, innerX1, innerY1, 1f, width, height);

            shapeVertex(buffer, matrix, innerX0, innerY0, 1f, width, height);
            shapeVertex(buffer, matrix, outerX0, outerY0, 0f, width, height);
            shapeVertex(buffer, matrix, outerX1, outerY1, 0f, width, height);
            shapeVertex(buffer, matrix, innerX0, innerY0, 1f, width, height);
            shapeVertex(buffer, matrix, outerX1, outerY1, 0f, width, height);
            shapeVertex(buffer, matrix, innerX1, innerY1, 1f, width, height);
        }
    }

    private static void addMaskedQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2,
                                      float alphaFirst, float alphaSecond, boolean vertical, float width, float height) {
        float leftU = x1 / width, rightU = x2 / width;
        float topV = 1f - y1 / height, bottomV = 1f - y2 / height;
        float topLeft = alphaFirst, topRight = vertical ? alphaFirst : alphaSecond;
        float bottomLeft = vertical ? alphaSecond : alphaFirst, bottomRight = alphaSecond;
        buffer.addVertex(matrix, x1, y1, 0).setUv(leftU, topV).setColor(topLeft, topLeft, topLeft, topLeft);
        buffer.addVertex(matrix, x1, y2, 0).setUv(leftU, bottomV).setColor(bottomLeft, bottomLeft, bottomLeft, bottomLeft);
        buffer.addVertex(matrix, x2, y2, 0).setUv(rightU, bottomV).setColor(bottomRight, bottomRight, bottomRight, bottomRight);
        buffer.addVertex(matrix, x1, y1, 0).setUv(leftU, topV).setColor(topLeft, topLeft, topLeft, topLeft);
        buffer.addVertex(matrix, x2, y2, 0).setUv(rightU, bottomV).setColor(bottomRight, bottomRight, bottomRight, bottomRight);
        buffer.addVertex(matrix, x2, y1, 0).setUv(rightU, topV).setColor(topRight, topRight, topRight, topRight);
    }

    private static void composite(BufferBuilder buffer) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        PeakRenderStats.countShape();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
