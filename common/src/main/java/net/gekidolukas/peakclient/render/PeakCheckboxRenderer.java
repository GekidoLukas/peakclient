package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakCheckboxLook;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakCheckboxRenderer implements PeakElementRenderer<PeakCheckbox> {
    private static final int BOX = 11;
    private static final int GAP = 4;
    private static final int SWITCH_WIDTH = 22;
    private static final int SWITCH_HEIGHT = 12;

    @Override
    public PeakPixelSize measureContent(Font font, PeakCheckbox checkbox) {
        PeakStyledText text = PeakTextResolver.resolved(checkbox, checkbox.getText());
        boolean asSwitch = checkbox.getLook() == PeakCheckboxLook.SWITCH;
        int markWidth = asSwitch ? SWITCH_WIDTH : BOX;
        int markHeight = asSwitch ? SWITCH_HEIGHT : BOX;
        int width = markWidth + (text.isEmpty() ? 0 : GAP + text.width(font));
        return new PeakPixelSize(width, Math.max(markHeight, font.lineHeight));
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakCheckbox checkbox, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        boolean enabled = checkbox.getInteraction() == null || checkbox.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(checkbox);
        boolean hovered = enabled && PeakMenuInteractionState.isHovered(checkbox);

        boolean ring = focused && PeakMenuInteractionState.focusVisible(checkbox);
        PeakPaint border = ring ? PeakMenuInteractionState.focusColor(checkbox)
                : PeakMenuInteractionState.isInvalid(checkbox) ? PeakMenuInteractionState.invalidColor(checkbox)
                : PeakPaint.solid(hovered ? PeakInputColors.BORDER_HOVER : PeakInputColors.BORDER);

        int markWidth;
        if (checkbox.getLook() == PeakCheckboxLook.SWITCH) {
            markWidth = SWITCH_WIDTH;
            drawSwitch(graphics, checkbox, enabled, border, height);
        } else {
            markWidth = BOX;
            int boxY = Math.max(0, (height - BOX) / 2);
            PeakPaintRenderer.fill(graphics, border, 0, boxY, BOX, boxY + BOX);
            PeakImmediateRender.fill(graphics, 1, boxY + 1, BOX - 1, boxY + BOX - 1, PeakInputColors.FIELD_BACKGROUND);
            if (checkbox.isChecked()) {
                PeakImmediateRender.fill(graphics, 3, boxY + 3, BOX - 3, boxY + BOX - 3, enabled ? 0xFF55FF55 : 0xFF557755);
            }
        }

        PeakStyledText text = PeakTextResolver.resolved(checkbox, checkbox.getText());
        if (!text.isEmpty()) {
            int textY = Math.max(0, (height - font.lineHeight) / 2) + 1;
            PeakImmediateRender.text(graphics, font, text, markWidth + GAP, textY, enabled ? checkbox.getColor() : PeakPaint.solid(PeakInputColors.TEXT_DISABLED), true);
        }
    }

    private static void drawSwitch(GuiGraphics graphics, PeakCheckbox checkbox, boolean enabled, PeakPaint border, int height) {
        int top = Math.max(0, (height - SWITCH_HEIGHT) / 2);
        boolean checked = checkbox.isChecked();
        int track = checked ? (enabled ? 0xFF2E7D32 : 0xFF2A4A2C) : 0xFF303030;
        PeakPaintRenderer.fill(graphics, border, 0, top, SWITCH_WIDTH, top + SWITCH_HEIGHT);
        PeakImmediateRender.fill(graphics, 1, top + 1, SWITCH_WIDTH - 1, top + SWITCH_HEIGHT - 1, track);
        int knobSize = SWITCH_HEIGHT - 4;
        int knobX = checked ? SWITCH_WIDTH - 2 - knobSize : 2;
        PeakImmediateRender.fill(graphics, knobX, top + 2, knobX + knobSize, top + 2 + knobSize,
                enabled ? 0xFFE0E0E0 : 0xFF808080);
    }
}
