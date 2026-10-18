package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.ui.PeakEffectOverlay;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakRevealState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

final class PeakGroupTransform {
    private PeakGroupTransform() {}

    static void paintWithOverlay(GuiGraphics graphics, int width, int height, int radius, PeakEffectTransform transform, Runnable draw) {
        PeakEffectOverlay overlay = transform.hasOverlay() ? transform.overlay() : null;
        if (overlay == null) {
            paint(graphics, width / 2f, height / 2f, transform, draw);
            return;
        }
        boolean followsContent = PeakEffectOverlayRenderer.followsContent(overlay) && !PeakOffscreenOpacity.isActive();
        Minecraft minecraft = Minecraft.getInstance();
        if (PeakHaloRenderer.isHalo(overlay) && !PeakOffscreenOpacity.isActive()) {
            PeakEffectTransform opaque = new PeakEffectTransform(transform.offsetX(), transform.offsetY(), transform.scale(),
                    transform.rotationDegrees(), 1f, overlay);
            paint(graphics, width / 2f, height / 2f, opaque, () -> {
                PeakOffscreenOpacity.begin(minecraft, graphics);
                boolean finished = false;
                try {
                    draw.run();
                    PeakHaloRenderer.finish(minecraft, graphics, overlay, width, height, transform.alpha());
                    finished = true;
                } finally {
                    if (!finished) PeakOffscreenOpacity.end(minecraft, graphics, 1f);
                }
            });
            return;
        }
        paint(graphics, width / 2f, height / 2f, transform, () -> {
            boolean ownBuffer = followsContent && !PeakOffscreenOpacity.isActive();
            if (ownBuffer) PeakOffscreenOpacity.begin(minecraft, graphics);
            try {
                PeakEffectOverlayRenderer.drawUnder(graphics, overlay, width, height, radius);
                draw.run();
                if (followsContent) PeakEffectOverlayRenderer.drawAtop(graphics, overlay, width, height);
                else PeakEffectOverlayRenderer.drawOver(graphics, overlay, width, height, radius);
            } finally {
                if (ownBuffer) PeakOffscreenOpacity.end(minecraft, graphics, 1f);
            }
        });
    }

    record ClipSpace(float boxWidth, float boxHeight, int absX, int absY, float absScale, float unitFactor) {}

    static void paint(GuiGraphics graphics, float pivotX, float pivotY, PeakEffectTransform transform, Runnable draw) {
        paint(graphics, pivotX, pivotY, transform, null, draw);
    }

    static boolean beginClip(GuiGraphics graphics, ClipSpace space, float originX, float originY, PeakRevealState reveal) {
        if (space == null || !reveal.isClipped()) return false;
        float left = originX + space.boxWidth() * reveal.clipLeft();
        float top = originY + space.boxHeight() * reveal.clipTop();
        float right = originX + space.boxWidth() * reveal.clipRight();
        float bottom = originY + space.boxHeight() * reveal.clipBottom();
        int screenLeft = Math.round((space.absX() + left * space.absScale()) * space.unitFactor());
        int screenTop = Math.round((space.absY() + top * space.absScale()) * space.unitFactor());
        int screenRight = Math.round((space.absX() + right * space.absScale()) * space.unitFactor());
        int screenBottom = Math.round((space.absY() + bottom * space.absScale()) * space.unitFactor());
        if (screenRight <= screenLeft || screenBottom <= screenTop) return false;
        graphics.enableScissor(screenLeft, screenTop, screenRight, screenBottom);
        return true;
    }

    static void paint(GuiGraphics graphics, float pivotX, float pivotY, PeakEffectTransform transform, ClipSpace space,
                      Runnable draw) {
        if (transform.isIdentity()) {
            draw.run();
            return;
        }
        PeakRevealState reveal = transform.reveal();
        if (reveal.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        float[] savedColor = RenderSystem.getShaderColor().clone();
        graphics.pose().pushPose();
        graphics.pose().translate(transform.offsetX(), transform.offsetY(), 0);
        if (transform.scale() != 1f || transform.rotationDegrees() != 0f || reveal.isScaled()) {
            graphics.pose().translate(pivotX, pivotY, 0);
            if (transform.rotationDegrees() != 0f) {
                graphics.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(transform.rotationDegrees())));
            }
            if (transform.scale() != 1f) graphics.pose().scale(transform.scale(), transform.scale(), 1f);
            if (reveal.isScaled()) graphics.pose().scale(reveal.scaleX(), reveal.scaleY(), 1f);
            graphics.pose().translate(-pivotX, -pivotY, 0);
        }

        boolean clipped = space != null && beginClip(graphics, space,
                pivotX - space.boxWidth() / 2f + transform.offsetX(), pivotY - space.boxHeight() / 2f + transform.offsetY(), reveal);
        boolean faded = transform.alpha() < 0.999f;
        boolean group = faded && !PeakOffscreenOpacity.isActive();
        if (group) PeakOffscreenOpacity.begin(minecraft, graphics);
        else if (faded) RenderSystem.setShaderColor(1f, 1f, 1f, transform.alpha());
        try {
            draw.run();
        } finally {
            if (group) PeakOffscreenOpacity.end(minecraft, graphics, transform.alpha());
            if (clipped) graphics.disableScissor();
            RenderSystem.setShaderColor(savedColor[0], savedColor[1], savedColor[2], savedColor[3]);
            graphics.pose().popPose();
        }
    }
}
