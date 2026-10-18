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
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

final class PeakDepthComposite {
    private static final int DEPTH_LEQUAL = GL11.GL_LEQUAL;
    private static final int DEPTH_GREATER = GL11.GL_GREATER;
    private static final float MIN_ALPHA = 0.004f;

    private PeakDepthComposite() {}

    static void present(Minecraft minecraft, GuiGraphics graphics, float ndcZ, float visibleAlpha, float hiddenAlpha) {
        if (!PeakOffscreenOpacity.closeBuffer(graphics)) return;
        TextureTarget content = PeakOffscreenOpacity.contentTarget();
        TextureTarget world = PeakDepthCapture.target();
        RenderTarget main = minecraft.getMainRenderTarget();
        main.bindWrite(true);
        if (content == null || world == null || !PeakDepthCapture.isValid()) return;

        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (scissor) GlStateManager._disableScissorTest();
        main.copyDepthFrom(world);
        main.bindWrite(true);
        if (scissor) GlStateManager._enableScissorTest();

        float clampedZ = Math.max(-1f, Math.min(1f, ndcZ));
        Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting savedSorting = RenderSystem.getVertexSorting();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);
        modelView.pushMatrix();
        modelView.identity();
        RenderSystem.applyModelViewMatrix();
        try {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, content.getColorTextureId());
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            if (visibleAlpha > MIN_ALPHA) {
                RenderSystem.depthFunc(DEPTH_LEQUAL);
                drawQuad(clampedZ, visibleAlpha);
            }
            if (hiddenAlpha > MIN_ALPHA) {
                RenderSystem.depthFunc(DEPTH_GREATER);
                drawQuad(clampedZ, hiddenAlpha * visibleAlpha);
            }
        } finally {
            RenderSystem.depthFunc(DEPTH_LEQUAL);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(savedProjection, savedSorting);
            if (scissor) GlStateManager._disableScissorTest();
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            if (scissor) GlStateManager._enableScissorTest();
        }
    }

    private static void drawQuad(float z, float alpha) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = new Matrix4f();
        buffer.addVertex(matrix, -1f, 1f, z).setUv(0f, 1f);
        buffer.addVertex(matrix, -1f, -1f, z).setUv(0f, 0f);
        buffer.addVertex(matrix, 1f, -1f, z).setUv(1f, 0f);
        buffer.addVertex(matrix, -1f, 1f, z).setUv(0f, 1f);
        buffer.addVertex(matrix, 1f, -1f, z).setUv(1f, 0f);
        buffer.addVertex(matrix, 1f, 1f, z).setUv(1f, 1f);
        RenderSystem.setShaderColor(alpha, alpha, alpha, alpha);
        PeakRenderStats.countShape();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
