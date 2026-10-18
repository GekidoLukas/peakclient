package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakBlockElement;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Quaternionf;

public final class PeakBlockElementRenderer implements PeakElementRenderer<PeakBlockElement> {
    @Override
    public boolean writesDepth() { return true; }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakBlockElement element, int width, int height) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        BlockState state = resolveBlockState(element, mc);
        if (state == null || state.isAir()) return;

        int size = Math.min(width, height);

        ctx.graphics().pose().pushPose();
        ctx.graphics().pose().translate(width / 2f, height / 2f, 100f);
        float blockScale = size * 0.625f;
        ctx.graphics().pose().scale(blockScale, -blockScale, blockScale);
        ctx.graphics().pose().mulPose(new Quaternionf()
                .rotateX((float) Math.toRadians(30))
                .rotateY((float) Math.toRadians(45 + element.getYaw())));
        ctx.graphics().pose().translate(-0.5f, -0.5f, -0.5f);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        boolean offscreen = element.getOpacity() < 0.999f;
        if (offscreen) PeakOffscreenOpacity.begin(mc, ctx.graphics());
        try {
            mc.getBlockRenderer().renderSingleBlock(state, ctx.graphics().pose(), bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY);
            bufferSource.endBatch();
        } finally {
            if (offscreen) PeakOffscreenOpacity.end(mc, ctx.graphics(), element.getOpacity());
        }

        ctx.graphics().pose().popPose();
    }
    @Override
    public PeakPixelSize measureContent(Font font, PeakBlockElement element) { return PeakPixelSize.ZERO; }

    private static BlockState resolveBlockState(PeakBlockElement element, Minecraft mc) {
        return switch (element.getSource().kind()) {
            case FIXED -> {
                ResourceLocation blockLocation = PeakValueResolver.location(element.getSource().blockId());
                Block block = blockLocation == null ? null : BuiltInRegistries.BLOCK.get(blockLocation);
                yield block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
            }
            case TARGETED_BLOCK -> {
                if (mc.hitResult instanceof BlockHitResult blockHit
                        && mc.hitResult.getType() != HitResult.Type.MISS) {
                    yield mc.level.getBlockState(blockHit.getBlockPos());
                }
                yield null;
            }
        };
    }
}
