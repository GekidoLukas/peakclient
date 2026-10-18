package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakCarousel;
import net.gekidolukas.peakclient.ui.PeakCarouselMode;
import net.gekidolukas.peakclient.ui.PeakCarouselSpan;
import net.gekidolukas.peakclient.ui.PeakCarouselTrack;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

final class PeakCarouselLayout {
    private PeakCarouselLayout() {}

    static void render(GuiGraphics graphics, Font font, PeakCarousel carousel,
                       int contentX, int contentY, int contentWidth, int contentHeight,
                       int absX, int absY, float absScale, float scissorUnitFactor, String documentId, int depth) {
        boolean horizontal = carousel.getMovement().isHorizontal();
        boolean reversed = carousel.getMovement().isReversed();
        int viewport = horizontal ? contentWidth : contentHeight;
        int crossSize = horizontal ? contentHeight : contentWidth;

        List<PeakElement<?>> items = new ArrayList<>();
        for (PeakElement<?> child : carousel.getChildren()) {
            if (child.isHidden() || PeakUiRenderer.isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            items.add(child);
        }

        double elapsed = carousel.takeElapsedTicks(PeakClientTickCounter.exact());
        ToIntFunction<PeakElement<?>> lengthOf = child -> mainLength(font, child, horizontal);
        int fadeStart = carousel.getFadeStart();
        int fadeEnd = carousel.getFadeEnd();
        List<PeakCarouselTrack.Placement<PeakElement<?>>> placements;

        boolean pingPong = carousel.getMode() == PeakCarouselMode.PINGPONG;
        PeakCarouselSpan.Span<PeakElement<?>> span = null;
        float overflow = 0f;
        if (pingPong || carousel.isOnlyWhenOverflowing()) {
            span = PeakCarouselSpan.layout(items, lengthOf, carousel.getGap());
            overflow = PeakCarouselSpan.overflow(span.total(), viewport);
        }
        boolean fits = span != null && overflow <= 0f;
        double pingPongTicks = pingPong ? carousel.advancePingPong(elapsed) : 0.0;

        if (span != null && (pingPong || fits)) {
            float offset = pingPong && !fits
                    ? PeakCarouselSpan.pingPongOffset(pingPongTicks, overflow, carousel.getSpeed(), carousel.getPauseTicks()) : 0f;
            placements = new ArrayList<>(span.placements().size());
            for (PeakCarouselSpan.Placement<PeakElement<?>> placed : span.placements()) {
                placements.add(new PeakCarouselTrack.Placement<>(placed.item(), placed.start() - offset, placed.length()));
            }
            if (fits) {
                carousel.track().reset();
                fadeStart = 0;
                fadeEnd = 0;
            } else {
                float hiddenBefore = reversed ? overflow - offset : offset;
                float hiddenAfter = overflow - hiddenBefore;
                fadeStart = fadeStart <= 0 ? 0 : Math.round(fadeStart * Math.min(1f, hiddenBefore / fadeStart));
                fadeEnd = fadeEnd <= 0 ? 0 : Math.round(fadeEnd * Math.min(1f, hiddenAfter / fadeEnd));
            }
        } else {
            float advance = (float) (Math.max(0f, carousel.getSpeed()) / 20.0 * elapsed);
            placements = carousel.track().step(items, lengthOf, carousel.getGap(), viewport, carousel.getOrder(), advance);
        }

        boolean fading = (fadeStart > 0 || fadeEnd > 0) && viewport > 0 && !placements.isEmpty();
        Minecraft minecraft = Minecraft.getInstance();
        if (fading) PeakOffscreenOpacity.begin(minecraft, graphics);
        try {
            for (PeakCarouselTrack.Placement<PeakElement<?>> placement : placements) {
                int length = placement.length();
                int mainStart = Math.round(reversed ? viewport - placement.start() - length : placement.start());
                if (mainStart + length <= 0 || mainStart >= viewport) continue;

                PeakElement<?> child = placement.item();
                PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
                PeakInsets margin = child.getStyle().getMargin();
                int crossAvailable = crossSize - (horizontal ? margin.getTop() + margin.getBottom() : margin.getLeft() + margin.getRight());
                int childCross = horizontal ? footprint.height() : footprint.width();
                int crossOffset = switch (carousel.getAlignChildren()) {
                    case CENTER -> Math.max(0, (crossAvailable - childCross) / 2);
                    case END -> Math.max(0, crossAvailable - childCross);
                    case START, AUTO -> 0;
                };
                int x, y;
                if (horizontal) {
                    x = contentX + mainStart + margin.getLeft() + child.getPosition().getOffsetX();
                    y = contentY + margin.getTop() + crossOffset + child.getPosition().getOffsetY();
                } else {
                    x = contentX + margin.getLeft() + crossOffset + child.getPosition().getOffsetX();
                    y = contentY + mainStart + margin.getTop() + child.getPosition().getOffsetY();
                }
                PeakUiRenderer.renderChild(graphics, font, child, x, y, absX, absY, absScale, scissorUnitFactor, documentId, depth + 1);
            }
        } finally {
            if (fading) {
                PeakEdgeFade.finish(minecraft, graphics, absX, absY, absScale, scissorUnitFactor,
                        contentX, contentY, contentWidth, contentHeight,
                        fadeStart, fadeEnd, horizontal);
            }
        }
    }

    private static int mainLength(Font font, PeakElement<?> child, boolean horizontal) {
        PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
        PeakInsets margin = child.getStyle().getMargin();
        return horizontal ? footprint.width() + margin.getLeft() + margin.getRight()
                : footprint.height() + margin.getTop() + margin.getBottom();
    }
}
