package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.gekidolukas.peakclient.ui.client.PeakTextEditor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakTextBoxRenderer implements PeakElementRenderer<PeakTextBox> {
    private static final int PADDING = 4;

    @Override
    public PeakPixelSize measureContent(Font font, PeakTextBox field) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakTextBox field, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        PeakStyle style = field.getStyle();
        boolean enabled = field.getInteraction() == null || field.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(field);
        boolean hovered = enabled && PeakMenuInteractionState.isHovered(field);
        int w = Math.max(width, 1);
        int h = Math.max(height, 1);

        PeakPaintRenderer.fill(graphics, style.getBackground(), 0, 0, w, h);
        boolean ring = focused && PeakMenuInteractionState.focusVisible(field);
        PeakPaint borderPaint = ring ? PeakMenuInteractionState.focusColor(field)
                : PeakMenuInteractionState.isInvalid(field) ? PeakMenuInteractionState.invalidColor(field)
                : hovered ? PeakPaint.solid(PeakInputColors.BORDER_HOVER)
                : (style.getBorderColor().isNone() ? PeakPaint.solid(PeakInputColors.BORDER) : style.getBorderColor());
        int border = Math.max(1, ring ? Math.max(style.getBorderWidth(), PeakMenuInteractionState.focusWidth(field)) : style.getBorderWidth());
        PeakPaintRenderer.drawFrame(graphics, borderPaint, w, h, border);

        int innerWidth = Math.max(1, w - PADDING * 2);
        int textY = (h - font.lineHeight) / 2 + 1;
        PeakPaint textPaint = enabled ? field.getColor() : PeakPaint.solid(PeakInputColors.TEXT_DISABLED);
        String value = field.getValue();
        int dropIndex = PeakMenuInteractionState.dropIndex(field);

        PeakTextEditor editor = focused ? PeakMenuInteractionState.editor(field) : null;
        if (value.isEmpty() && editor == null && dropIndex < 0) {
            PeakStyledText placeholderText = PeakTextResolver.resolved(field, field.getPlaceholder());
            if (!placeholderText.isEmpty()) {
                PeakImmediateRender.text(graphics, font, placeholderText, PADDING, textY, 0x707070, false);
            }
            return;
        }

        int scroll = 0;
        if (editor != null) {
            int caretX = font.width(value.substring(0, Math.min(editor.caret(), value.length())));
            scroll = editor.scrollPx();
            if (caretX - scroll > innerWidth - 1) scroll = caretX - innerWidth + 1;
            if (caretX - scroll < 0) scroll = caretX;
            editor.scrollPx(scroll);
        }

        int scissorLeft = Math.round((ctx.absX() + PADDING * ctx.absScale()) * ctx.scissorUnitFactor());
        int scissorRight = Math.round((ctx.absX() + (w - PADDING) * ctx.absScale()) * ctx.scissorUnitFactor());
        int scissorTop = Math.round((ctx.absY() + border * ctx.absScale()) * ctx.scissorUnitFactor());
        int scissorBottom = Math.round((ctx.absY() + (h - border) * ctx.absScale()) * ctx.scissorUnitFactor());
        graphics.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);
        try {
            if (editor != null && editor.hasSelection()) {
                int start = PADDING + font.width(value.substring(0, editor.selectionStart())) - scroll;
                int end = PADDING + font.width(value.substring(0, editor.selectionEnd())) - scroll;
                PeakImmediateRender.fill(graphics, start, textY - 1, end, textY + font.lineHeight, PeakInputColors.SELECTION);
            }
            PeakImmediateRender.text(graphics, font, value, PADDING - scroll, textY, textPaint, false);
            if (dropIndex >= 0) {
                int markerX = PADDING + font.width(value.substring(0, Math.min(dropIndex, value.length()))) - scroll;
                PeakImmediateRender.fill(graphics, markerX, textY - 1, markerX + 1, textY + font.lineHeight, 0xFF55FFFF);
            }
            if (editor != null && (PeakClientTickCounter.current() / 10) % 2 == 0) {
                int caretX = PADDING + font.width(value.substring(0, Math.min(editor.caret(), value.length()))) - scroll;
                PeakImmediateRender.fill(graphics, caretX, textY - 1, caretX + 1, textY + font.lineHeight, PeakInputColors.CARET);
            }
        } finally {
            graphics.disableScissor();
        }
    }
}
