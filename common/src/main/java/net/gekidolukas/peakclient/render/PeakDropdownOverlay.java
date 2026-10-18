package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakDropdownLayout;
import net.gekidolukas.peakclient.ui.PeakOption;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class PeakDropdownOverlay {
    private PeakDropdownOverlay() {}

    public static int rowHeight(Font font) {
        return font.lineHeight + 4;
    }

    public static int widestText(Font font, PeakDropdown dropdown) {
        int widest = 0;
        for (PeakOption option : dropdown.getOptions()) {
            widest = Math.max(widest, font.width(PeakPlaceholderResolver.resolve(option.getText())));
        }
        return widest;
    }

    public static void draw(GuiGraphics graphics, Font font, PeakDropdown dropdown, PeakDropdownLayout layout,
                            int highlightIndex, PeakPaint borderColor) {
        int x = layout.x();
        int y = layout.y();
        int w = layout.width();
        int h = layout.height();

        PeakPaintRenderer.fill(graphics, borderColor, x, y, x + w, y + h);
        PeakImmediateRender.fill(graphics, x + 1, y + 1, x + w - 1, y + h - 1, 0xFF181818);

        List<PeakOption> options = dropdown.getOptions();
        int selected = dropdown.selectedIndex();
        int textRight = x + w - 1 - (layout.scrolls() ? PeakDropdownLayout.SCROLLBAR_WIDTH + 2 : 0);
        for (int row = 0; row < layout.visibleRows(); row++) {
            int index = layout.firstRow() + row;
            if (index >= options.size()) break;
            PeakOption option = options.get(index);
            int rowTop = layout.rowY(index);
            if (index == highlightIndex && option.isEnabled()) {
                PeakImmediateRender.fill(graphics, x + 1, rowTop, textRight, rowTop + layout.rowHeight(), 0xFF2A4A7A);
            }
            String text = PeakPlaceholderResolver.resolve(option.getText());
            int available = Math.max(1, textRight - (x + 1 + PeakDropdownLayout.TEXT_PADDING));
            if (font.width(text) > available) text = font.plainSubstrByWidth(text, available);
            PeakPaint color = !option.isEnabled() ? PeakPaint.solid(PeakInputColors.TEXT_DISABLED)
                    : index == selected ? PeakPaint.solid(0x55FF55) : dropdown.getColor();
            PeakImmediateRender.text(graphics, font, text, x + 1 + PeakDropdownLayout.TEXT_PADDING,
                    rowTop + (layout.rowHeight() - font.lineHeight) / 2 + 1, color, true);
        }

        if (layout.scrolls()) {
            int trackX = x + w - 1 - PeakDropdownLayout.SCROLLBAR_WIDTH;
            int trackTop = y + 1;
            int trackHeight = h - 2;
            PeakImmediateRender.fill(graphics, trackX, trackTop, trackX + PeakDropdownLayout.SCROLLBAR_WIDTH,
                    trackTop + trackHeight, 0x50000000);
            int thumbHeight = Math.max(8, trackHeight * layout.visibleRows() / Math.max(1, layout.count()));
            int travel = trackHeight - thumbHeight;
            int thumbTop = trackTop + (layout.maxFirstRow() == 0 ? 0 : travel * layout.firstRow() / layout.maxFirstRow());
            PeakImmediateRender.fill(graphics, trackX, thumbTop, trackX + PeakDropdownLayout.SCROLLBAR_WIDTH,
                    thumbTop + thumbHeight, 0xC0DDDDDD);
        }
    }
}
