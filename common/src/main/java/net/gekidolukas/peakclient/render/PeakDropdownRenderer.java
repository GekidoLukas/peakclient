package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakDropdownRenderer implements PeakElementRenderer<PeakDropdown> {
    private static final int PADDING = 4;
    private static final int ARROW_AREA = 14;

    @Override
    public PeakPixelSize measureContent(Font font, PeakDropdown dropdown) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakDropdown dropdown, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        boolean enabled = dropdown.getInteraction() == null || dropdown.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(dropdown);
        boolean hovered = enabled && PeakMenuInteractionState.isHovered(dropdown);
        boolean open = PeakMenuInteractionState.isDropdownOpen(dropdown);
        int w = Math.max(width, 1);
        int h = Math.max(height, 1);

        boolean ring = (focused || open) && PeakMenuInteractionState.focusVisible(dropdown);
        PeakPaint border = ring ? PeakMenuInteractionState.focusColor(dropdown)
                : PeakMenuInteractionState.isInvalid(dropdown) ? PeakMenuInteractionState.invalidColor(dropdown)
                : PeakPaint.solid(hovered ? PeakInputColors.BORDER_HOVER : PeakInputColors.BORDER);
        int thickness = Math.max(1, ring ? PeakMenuInteractionState.focusWidth(dropdown) : 1);

        PeakImmediateRender.fill(graphics, 0, 0, w, h, PeakInputColors.FIELD_BACKGROUND);
        PeakPaintRenderer.drawFrame(graphics, border, w, h, thickness);

        PeakStyledText text = PeakTextResolver.resolved(dropdown, dropdown.selectedText());
        boolean placeholder = dropdown.selectedIndex() < 0;
        if (placeholder) text = PeakTextResolver.resolved(dropdown, "placeholder", dropdown.getPlaceholder());
        PeakPaint color = !enabled ? PeakPaint.solid(PeakInputColors.TEXT_DISABLED)
                : placeholder ? PeakPaint.solid(0x909090) : dropdown.getColor();
        if (!text.isEmpty()) {
            int available = Math.max(1, w - PADDING - ARROW_AREA);
            PeakStyledText shown = text.trimToWidth(font, available);
            PeakImmediateRender.text(graphics, font, shown, PADDING, (h - font.lineHeight) / 2 + 1, color, true);
        }

        int arrowColor = enabled ? 0xFFE0E0E0 : 0xFF707070;
        int cx = w - ARROW_AREA / 2 - 1;
        int cy = h / 2;
        for (int row = 0; row < 3; row++) {
            int half = open ? row : 2 - row;
            int y = cy - 1 + row;
            PeakImmediateRender.fill(graphics, cx - half, y, cx + half + 1, y + 1, arrowColor);
        }
    }
}
