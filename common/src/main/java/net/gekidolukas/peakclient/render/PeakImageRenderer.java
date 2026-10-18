package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakImage;
import net.gekidolukas.peakclient.ui.PeakObjectFit;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakRect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakImageRenderer implements PeakElementRenderer<PeakImage> {

    private static final Set<String> WARNED_MISSING_SOURCE_SIZE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void renderContent(PeakRenderContext ctx, PeakImage image, int width, int height) {
        try {
            ResourceLocation texture = PeakValueResolver.requireLocation(image.getTexture());
            PeakRect rect = resolveRect(image, width, height);
            GuiGraphics graphics = ctx.graphics();

            boolean needsScissor = rect.width() > width || rect.height() > height || rect.x() < 0 || rect.y() < 0;
            if (needsScissor) {
                int scissorLeft = Math.round(ctx.absX() * ctx.scissorUnitFactor());
                int scissorTop = Math.round(ctx.absY() * ctx.scissorUnitFactor());
                int scissorRight = Math.round((ctx.absX() + width * ctx.absScale()) * ctx.scissorUnitFactor());
                int scissorBottom = Math.round((ctx.absY() + height * ctx.absScale()) * ctx.scissorUnitFactor());
                graphics.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);
            }

            PeakImmediateRender.blit(graphics, texture, rect.x(), rect.y(), 0, 0, rect.width(), rect.height(), rect.width(), rect.height());

            if (needsScissor) graphics.disableScissor();
        } catch (Exception ignored) {
        }
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakImage image) {
        return PeakPixelSize.ZERO;
    }

    private static PeakRect resolveRect(PeakImage image, int boxWidth, int boxHeight) {
        if (image.getObjectFit() == PeakObjectFit.FILL) {
            return new PeakRect(0, 0, boxWidth, boxHeight);
        }

        if (!image.hasKnownSourceSize()) {
            if (WARNED_MISSING_SOURCE_SIZE.add(image.getId())) {
                PeakLog.warn("PeakImage '{}' uses objectFit={} but has no known source size - "
                                + "use PeakImage.create(texture, sourceWidth, sourceHeight) or .sourceSize(...) "
                                + "for this to work correctly. Falling back to FILL behaviour.",
                        image.getId(), image.getObjectFit());
            }
            return new PeakRect(0, 0, boxWidth, boxHeight);
        }

        float sourceRatio = (float) image.getSourceWidth() / image.getSourceHeight();
        float boxRatio = (float) boxWidth / boxHeight;

        int drawWidth, drawHeight;
        boolean fitByWidth = image.getObjectFit() == PeakObjectFit.CONTAIN
                ? sourceRatio <= boxRatio
                : sourceRatio >= boxRatio;

        if (fitByWidth) {
            drawWidth = boxWidth;
            drawHeight = Math.round(boxWidth / sourceRatio);
        } else {
            drawHeight = boxHeight;
            drawWidth = Math.round(boxHeight * sourceRatio);
        }

        int drawX = (boxWidth - drawWidth) / 2;
        int drawY = (boxHeight - drawHeight) / 2;
        return new PeakRect(drawX, drawY, drawWidth, drawHeight);
    }
}
