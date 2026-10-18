package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.ui.PeakAnimState;
import net.gekidolukas.peakclient.ui.PeakPopUpContainer;
import net.gekidolukas.peakclient.ui.PeakRevealState;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakPopUpContainerRenderer extends PeakContainerRenderer<PeakPopUpContainer> {
    @Override
    public void renderContent(PeakRenderContext ctx, PeakPopUpContainer popup, int width, int height) {
        PeakAnimState state = popup.computeAnimState();
        if (popup.isRemovalRequested()) { PeakUiRenderer.requestRemoval(popup.getId()); return; }
        if (state.hidden() || state.reveal().isEmpty()) return;

        GuiGraphics graphics = ctx.graphics();
        graphics.pose().pushPose();
        graphics.pose().translate(state.offsetX(), state.offsetY(), 0);

        PeakRevealState reveal = state.reveal();
        if (state.scale() != 1f || reveal.isScaled()) {
            graphics.pose().translate(width / 2f, height / 2f, 0);
            graphics.pose().scale(state.scale(), state.scale(), 1f);
            graphics.pose().scale(reveal.scaleX(), reveal.scaleY(), 1f);
            graphics.pose().translate(-width / 2f, -height / 2f, 0);
        }

        PeakGroupTransform.ClipSpace clipSpace = new PeakGroupTransform.ClipSpace(width, height,
                ctx.absX(), ctx.absY(), ctx.absScale(), ctx.scissorUnitFactor());
        boolean clipped = PeakGroupTransform.beginClip(graphics, clipSpace, state.offsetX(), state.offsetY(), reveal);

        boolean fading = state.alpha() < 0.999f;
        if (fading) RenderSystem.setShaderColor(1f, 1f, 1f, state.alpha());

        PeakStyle style = popup.getStyle();
        int w = Math.max(width, 1), h = Math.max(height, 1);
        PeakPaintRenderer.fill(graphics, style.getBackground(), 0, 0, w, h);
        drawBorder(graphics, style, w, h);

        PeakUiRenderer.layoutAndRenderChildren(graphics, ctx.font(), popup, 0, 0, width, height,
                ctx.absX(), ctx.absY(), ctx.absScale(), ctx.scissorUnitFactor(),ctx.documentId(),ctx.depth());

        if (fading) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        if (clipped) graphics.disableScissor();
        graphics.pose().popPose();
    }
}
