package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakNumberFieldLayout;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.gekidolukas.peakclient.ui.client.PeakTextEditor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakNumberFieldRenderer implements PeakElementRenderer<PeakNumberField> {
    @Override
    public PeakPixelSize measureContent(Font font, PeakNumberField field) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakNumberField field, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        boolean enabled = field.getInteraction() == null || field.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(field);
        boolean hovered = enabled && PeakMenuInteractionState.isHovered(field);
        int w = Math.max(width, 1);
        int h = Math.max(height, 1);
        PeakNumberFieldLayout layout = PeakNumberFieldLayout.of(w, h, field.isShowButtons());

        boolean ring = focused && PeakMenuInteractionState.focusVisible(field);
        PeakPaint borderPaint = ring ? PeakMenuInteractionState.focusColor(field)
                : PeakMenuInteractionState.isInvalid(field) ? PeakMenuInteractionState.invalidColor(field)
                : PeakPaint.solid(hovered ? PeakInputColors.BORDER_HOVER : PeakInputColors.BORDER);
        int border = Math.max(1, ring ? PeakMenuInteractionState.focusWidth(field) : 1);

        PeakImmediateRender.fill(graphics, 0, 0, w, h, PeakInputColors.FIELD_BACKGROUND);
        PeakPaintRenderer.drawFrame(graphics, borderPaint, w, h, border);

        double value = field.getValue();
        if (layout.hasButtons()) {
            drawButton(graphics, 0, layout.buttonWidth(), h, false, enabled && value > field.getMin());
            drawButton(graphics, layout.plusX(), layout.buttonWidth(), h, true, enabled && value < field.getMax());
        }

        PeakTextEditor editor = focused ? PeakMenuInteractionState.numberEditor(field) : null;
        String text = editor != null ? editor.value() : field.inputValue();
        int textWidth = font.width(text);
        int textX = layout.textX() + Math.max(2, (layout.textWidth() - textWidth) / 2);
        int textY = (h - font.lineHeight) / 2 + 1;

        if (editor != null && editor.hasSelection()) {
            int start = textX + font.width(text.substring(0, editor.selectionStart()));
            int end = textX + font.width(text.substring(0, editor.selectionEnd()));
            PeakImmediateRender.fill(graphics, start, textY - 1, end, textY + font.lineHeight, PeakInputColors.SELECTION);
        }
        PeakImmediateRender.text(graphics, font, text, textX, textY, enabled ? field.getColor() : PeakPaint.solid(PeakInputColors.TEXT_DISABLED), false);
        if (editor != null && (PeakClientTickCounter.current() / 10) % 2 == 0) {
            int caretX = textX + font.width(text.substring(0, Math.min(editor.caret(), text.length())));
            PeakImmediateRender.fill(graphics, caretX, textY - 1, caretX + 1, textY + font.lineHeight, PeakInputColors.CARET);
        }
    }

    private static void drawButton(GuiGraphics graphics, int x, int buttonWidth, int h, boolean plus, boolean active) {
        int inset = 1;
        PeakImmediateRender.fill(graphics, x + inset, inset, x + buttonWidth - inset, h - inset, active ? 0xFF3A3A3A : 0xFF262626);
        int color = active ? 0xFFFFFFFF : 0xFF707070;
        int cx = x + buttonWidth / 2;
        int cy = h / 2;
        PeakImmediateRender.fill(graphics, cx - 2, cy, cx + 3, cy + 1, color);
        if (plus) PeakImmediateRender.fill(graphics, cx, cy - 2, cx + 1, cy + 3, color);
    }
}
