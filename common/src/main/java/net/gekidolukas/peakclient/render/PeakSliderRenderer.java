package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakSliderRenderer implements PeakElementRenderer<PeakSlider> {
    public static final int HANDLE_WIDTH = 6;

    @Override
    public PeakPixelSize measureContent(Font font, PeakSlider slider) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakSlider slider, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        boolean enabled = slider.getInteraction() == null || slider.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(slider);
        boolean active = enabled && (PeakMenuInteractionState.isHovered(slider) || PeakMenuInteractionState.isPressed(slider));
        int w = Math.max(width, HANDLE_WIDTH + 2);
        int h = Math.max(height, 4);

        int trackTop = h / 2 - 3;
        int trackBottom = trackTop + 6;
        boolean ring = focused && PeakMenuInteractionState.focusVisible(slider);
        PeakPaint ringColor = ring ? PeakMenuInteractionState.focusColor(slider) : PeakPaint.WHITE;
        PeakPaint trackBorder = ring ? ringColor
                : PeakMenuInteractionState.isInvalid(slider) ? PeakMenuInteractionState.invalidColor(slider)
                : PeakPaint.solid(PeakInputColors.BORDER);
        PeakPaintRenderer.fill(graphics, trackBorder, 0, trackTop, w, trackBottom);
        PeakImmediateRender.fill(graphics, 1, trackTop + 1, w - 1, trackBottom - 1, PeakInputColors.FIELD_BACKGROUND);

        int handleX = (int) Math.round(slider.fraction() * (w - HANDLE_WIDTH));
        if (handleX > 1) {
            PeakImmediateRender.fill(graphics, 1, trackTop + 1, handleX + 1, trackBottom - 1, enabled ? 0xFF3C78C8 : 0xFF3A4A5E);
        }
        PeakPaint handleColor = !enabled ? PeakPaint.solid(0xFF606060) : ring ? ringColor
                : PeakPaint.solid(active ? 0xFFE0E0E0 : 0xFFB0B0B0);
        PeakPaintRenderer.fill(graphics, handleColor, handleX, 0, handleX + HANDLE_WIDTH, h);
        PeakImmediateRender.fill(graphics, handleX + 1, 1, handleX + HANDLE_WIDTH - 1, h - 1, enabled ? 0xFF404040 : 0xFF303030);

        if (slider.isShowValue()) {
            String text = slider.displayText();
            int textX = (w - font.width(text)) / 2;
            int textY = (h - font.lineHeight) / 2 + 1;
            PeakImmediateRender.text(graphics, font, text, textX, textY, enabled ? 0xFFFFFF : 0x909090, true);
        }
    }
}
