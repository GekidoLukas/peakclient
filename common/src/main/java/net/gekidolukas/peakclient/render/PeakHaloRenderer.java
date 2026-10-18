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
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakEffectOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class PeakHaloRenderer {
    private static final int GL_FUNC_ADD = 0x8006;
    private static final int GL_MAX = 0x8008;
    private static final float OUTLINE_THICKNESS = 1.5f;
    private static final int GL_LINEAR = 9729;
    private static final float GLOW_SIGMA = 3.3f;
    private static final float GLOW_GAIN = 1.9f;
    private static final float GLOW_HOLD = 1.2f;
    private static final int GLOW_TAPS = 9;
    private static final float GLOW_REACH = GLOW_SIGMA * 3f + GLOW_HOLD;
    private static final float SAMPLE_SPACING = 1.4f;
    private static final int MAX_DIRECTIONS = 48;

    private static TextureTarget silhouette;
    private static TextureTarget dilated;

    private PeakHaloRenderer() {}

    static boolean isHalo(PeakEffectOverlay overlay) {
        return overlay.kind() == PeakEffectOverlay.Kind.GLOW || overlay.kind() == PeakEffectOverlay.Kind.OUTLINE;
    }

    static void finish(Minecraft mc, GuiGraphics graphics, PeakEffectOverlay overlay, int width, int height, float opacity) {
        Matrix4f pose = new Matrix4f(graphics.pose().last().pose());
        if (!PeakOffscreenOpacity.closeBuffer(graphics)) return;

        TextureTarget content = PeakOffscreenOpacity.contentTarget();
        RenderTarget main = mc.getMainRenderTarget();
        float screenWidth = mc.getWindow().getGuiScaledWidth();
        float screenHeight = mc.getWindow().getGuiScaledHeight();
        silhouette = ensure(silhouette, main);
        dilated = ensure(dilated, main);

        boolean glow = overlay.kind() == PeakEffectOverlay.Kind.GLOW;
        float reach = glow ? GLOW_REACH : OUTLINE_THICKNESS;
        float[] region = region(pose, width, height, reach + 2f, screenWidth, screenHeight);

        silhouette.setClearColor(0f, 0f, 0f, 0f);
        silhouette.clear(Minecraft.ON_OSX);
        silhouette.bindWrite(true);
        drawTexture(content, region, 0f, 0f, screenWidth, screenHeight, 0f, 0f, 0f, 1f, false);
        drawSolid(region, PeakArgb.normalize(overlay.color()));

        dilated.setClearColor(0f, 0f, 0f, 0f);
        dilated.clear(Minecraft.ON_OSX);
        dilated.bindWrite(true);
        RenderSystem.blendEquation(GL_MAX);
        drawTexture(silhouette, region, 0f, 0f, screenWidth, screenHeight, 1f, 1f, 1f, 1f, true);
        if (glow) {
            drawRing(region, GLOW_HOLD, 1f, screenWidth, screenHeight);
        } else {
            drawRing(region, OUTLINE_THICKNESS * 0.5f, 1f, screenWidth, screenHeight);
            drawRing(region, OUTLINE_THICKNESS, 1f, screenWidth, screenHeight);
        }
        RenderSystem.blendEquation(GL_FUNC_ADD);
        if (glow) blurGlow(region, screenWidth, screenHeight);
        punchOut(content, region, screenWidth, screenHeight);

        main.bindWrite(true);
        float strength = overlay.strength() * opacity;
        compositeFullscreen(dilated, screenWidth, screenHeight, strength);
        compositeFullscreen(content, screenWidth, screenHeight, opacity);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void blurGlow(float[] region, float screenWidth, float screenHeight) {
        float step = GLOW_SIGMA / 3f;
        float[] weights = new float[GLOW_TAPS * 2 + 1];
        float total = 0f;
        for (int i = -GLOW_TAPS; i <= GLOW_TAPS; i++) {
            float distance = i * step;
            float weight = (float) Math.exp(-(distance * distance) / (2.0 * GLOW_SIGMA * GLOW_SIGMA));
            weights[i + GLOW_TAPS] = weight;
            total += weight;
        }
        silhouette.setClearColor(0f, 0f, 0f, 0f);
        silhouette.clear(Minecraft.ON_OSX);
        silhouette.bindWrite(true);
        for (int i = -GLOW_TAPS; i <= GLOW_TAPS; i++) {
            float weight = weights[i + GLOW_TAPS] / total * GLOW_GAIN;
            drawTexture(dilated, region, i * step, 0f, screenWidth, screenHeight, weight, weight, weight, weight, true);
        }
        dilated.setClearColor(0f, 0f, 0f, 0f);
        dilated.clear(Minecraft.ON_OSX);
        dilated.bindWrite(true);
        for (int i = -GLOW_TAPS; i <= GLOW_TAPS; i++) {
            float weight = weights[i + GLOW_TAPS] / total;
            drawTexture(silhouette, region, 0f, i * step, screenWidth, screenHeight, weight, weight, weight, weight, true);
        }
    }

    private static TextureTarget ensure(TextureTarget current, RenderTarget main) {
        if (current == null) {
            TextureTarget created = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            created.setFilterMode(GL_LINEAR);
            return created;
        }
        if (current.width != main.width || current.height != main.height) current.resize(main.width, main.height, Minecraft.ON_OSX);
        return current;
    }

    private static float[] region(Matrix4f pose, int width, int height, float margin, float screenWidth, float screenHeight) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float[][] corners = {{0, 0}, {width, 0}, {0, height}, {width, height}};
        for (float[] corner : corners) {
            Vector3f point = pose.transformPosition(corner[0], corner[1], 0f, new Vector3f());
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        return new float[]{
                Math.max(0f, minX - margin), Math.max(0f, minY - margin),
                Math.min(screenWidth, maxX + margin), Math.min(screenHeight, maxY + margin)};
    }

    private static void drawRing(float[] region, float radius, float falloff, float screenWidth, float screenHeight) {
        int directions = Math.max(8, Math.min(MAX_DIRECTIONS, Math.round((float) (2.0 * Math.PI * radius / SAMPLE_SPACING))));
        for (int i = 0; i < directions; i++) {
            double angle = 2.0 * Math.PI * i / directions;
            drawTexture(silhouette, region, (float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius,
                    screenWidth, screenHeight, falloff, falloff, falloff, falloff, true);
        }
    }

    private static void drawTexture(TextureTarget source, float[] region, float offsetX, float offsetY,
                                    float screenWidth, float screenHeight, float red, float green, float blue, float alpha,
                                    boolean blend) {
        float x1 = region[0], y1 = region[1], x2 = region[2], y2 = region[3];
        if (x2 <= x1 || y2 <= y1) return;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        texturedQuad(buffer, x1, y1, x2, y2, offsetX, offsetY, screenWidth, screenHeight);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, source.getColorTextureId());
        RenderSystem.setShaderColor(red, green, blue, alpha);
        if (blend) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.disableBlend();
        }
        draw(buffer);
    }

    private static void drawSolid(float[] region, int argb) {
        float x1 = region[0], y1 = region[1], x2 = region[2], y2 = region[3];
        if (x2 <= x1 || y2 <= y1) return;
        float red = ((argb >> 16) & 0xFF) / 255f, green = ((argb >> 8) & 0xFF) / 255f, blue = (argb & 0xFF) / 255f;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        solidVertex(buffer, x1, y1, red, green, blue);
        solidVertex(buffer, x1, y2, red, green, blue);
        solidVertex(buffer, x2, y2, red, green, blue);
        solidVertex(buffer, x1, y1, red, green, blue);
        solidVertex(buffer, x2, y2, red, green, blue);
        solidVertex(buffer, x2, y1, red, green, blue);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ZERO,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        draw(buffer);
    }

    private static void punchOut(TextureTarget content, float[] region, float screenWidth, float screenHeight) {
        float x1 = region[0], y1 = region[1], x2 = region[2], y2 = region[3];
        if (x2 <= x1 || y2 <= y1) return;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        texturedQuad(buffer, x1, y1, x2, y2, 0f, 0f, screenWidth, screenHeight);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, content.getColorTextureId());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        draw(buffer);
    }

    private static void compositeFullscreen(TextureTarget source, float screenWidth, float screenHeight, float factor) {
        if (factor <= 0f) return;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        texturedQuad(buffer, 0f, 0f, screenWidth, screenHeight, 0f, 0f, screenWidth, screenHeight);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, source.getColorTextureId());
        RenderSystem.setShaderColor(factor, factor, factor, factor);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        draw(buffer);
    }

    private static void texturedQuad(BufferBuilder buffer, float x1, float y1, float x2, float y2, float offsetX, float offsetY,
                                     float screenWidth, float screenHeight) {
        float u1 = x1 / screenWidth, u2 = x2 / screenWidth;
        float v1 = 1f - y1 / screenHeight, v2 = 1f - y2 / screenHeight;
        buffer.addVertex(x1 + offsetX, y1 + offsetY, 0).setUv(u1, v1);
        buffer.addVertex(x1 + offsetX, y2 + offsetY, 0).setUv(u1, v2);
        buffer.addVertex(x2 + offsetX, y2 + offsetY, 0).setUv(u2, v2);
        buffer.addVertex(x1 + offsetX, y1 + offsetY, 0).setUv(u1, v1);
        buffer.addVertex(x2 + offsetX, y2 + offsetY, 0).setUv(u2, v2);
        buffer.addVertex(x2 + offsetX, y1 + offsetY, 0).setUv(u2, v1);
    }

    private static void solidVertex(BufferBuilder buffer, float x, float y, float red, float green, float blue) {
        buffer.addVertex(x, y, 0).setColor(red, green, blue, 1f);
    }

    private static void draw(BufferBuilder buffer) {
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
