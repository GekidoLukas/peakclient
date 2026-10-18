package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.gekidolukas.peakclient.debug.PeakDebugState;
import net.gekidolukas.peakclient.ui.PeakChibiPlayer;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class PeakChibiPlayerRenderer implements PeakElementRenderer<PeakChibiPlayer> {
    @Override
    public boolean writesDepth() { return true; }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakChibiPlayer element, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        PlayerSkin skin = PeakPlayerSkinLookup.resolve(element.getSource(), element.getTargetUuid(), element.getTexturePath(), element.isTextureSlim(), mc);
        if (skin == null) return;

        float rotationRadians = (float) Math.toRadians(element.getRotationYDegrees());
        float unit = PeakChibiPlayerAssets.pixelsPerModelUnit(width, height, rotationRadians);
        if (unit <= 0f) return;

        ModelPart model = PeakChibiPlayerAssets.get(skin.model() == PlayerSkin.Model.SLIM);

        GuiGraphics graphics = ctx.graphics();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        Lighting.setupForEntityInInventory();

        pose.translate(width / 2f, height / 2f - PeakChibiPlayerAssets.MODEL_CENTER_Y * unit, 0f);

        float physicalPixelsPerModelUnit = (float) (unit * ctx.absScale() * mc.getWindow().getGuiScale() * ctx.scissorUnitFactor());
        float rawHeadBoxHeight = 8f * PeakChibiPlayerAssets.HEAD_SCALE;
        float roundedHeadScale = PeakChibiPlayerAssets.HEAD_SCALE;
        if (physicalPixelsPerModelUnit > 0f) {
            float physicalHeadHeight = Math.max(1f, Math.round(rawHeadBoxHeight * physicalPixelsPerModelUnit));
            roundedHeadScale = physicalHeadHeight / physicalPixelsPerModelUnit / 8f;
        }

        if (PeakDebugState.isEnabled()) {
            double guiScale = mc.getWindow().getGuiScale();
            double neckY = (ctx.absY() + (height / 2f - PeakChibiPlayerAssets.MODEL_CENTER_Y * unit) * ctx.absScale())
                    * ctx.scissorUnitFactor() * guiScale;
            PeakChibiMetrics.record(element.getId(), new PeakChibiMetrics.Sample(unit, physicalPixelsPerModelUnit,
                    ctx.absScale(), ctx.scissorUnitFactor(), guiScale, PeakChibiPlayerAssets.HEAD_SCALE, roundedHeadScale,
                    rawHeadBoxHeight * physicalPixelsPerModelUnit, Math.round(rawHeadBoxHeight * physicalPixelsPerModelUnit),
                    neckY, neckY - 8f * roundedHeadScale * physicalPixelsPerModelUnit,
                    neckY - 2.45 * physicalPixelsPerModelUnit, neckY + 16.25 * physicalPixelsPerModelUnit));
        }

        ModelPart head = model.getChild("head");
        head.xScale = roundedHeadScale * PeakChibiPlayerAssets.HEAD_SIDE_CLEARANCE;
        head.yScale = roundedHeadScale;
        head.zScale = roundedHeadScale;

        ModelPart hat = model.getChild("hat");
        hat.xScale = roundedHeadScale;
        hat.yScale = roundedHeadScale;
        hat.zScale = roundedHeadScale;

        float blockScale = unit * 16f;
        pose.scale(blockScale, blockScale, blockScale);
        pose.mulPose(new Quaternionf().rotateY(rotationRadians));

        ResourceLocation chibiTexture = PeakChibiSkinTextureCache.getOrCreate(skin.texture(), skin.model() == PlayerSkin.Model.SLIM);
        boolean offscreen = element.getOpacity() < 0.999f;
        if (offscreen) PeakOffscreenOpacity.begin(Minecraft.getInstance(), graphics);
        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();
        RenderType renderType = RenderType.entityCutoutNoCull(chibiTexture);
        var vertexConsumer = bufferSource.getBuffer(renderType);

        RenderSystem.enableDepthTest();
        model.render(pose, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        bufferSource.endBatch(renderType);
        if (offscreen) PeakOffscreenOpacity.end(Minecraft.getInstance(), graphics, element.getOpacity());
        RenderSystem.disableDepthTest();

        pose.popPose();
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakChibiPlayer element) {
        return PeakChibiPlayerAssets.naturalSize((float) Math.toRadians(element.getRotationYDegrees()));
    }
}
