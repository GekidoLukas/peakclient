package net.gekidolukas.peakclient.debug;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakImmediateRender;
import net.gekidolukas.peakclient.render.PeakRenderStats;
import net.gekidolukas.peakclient.ui.client.PeakMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public final class PeakDebugOverlayRenderer {
    private static int mouseX = -1;
    private static int mouseY = -1;
    private static final int MAX_DETAIL_LINE_WIDTH = 240;
    private static final int COLUMN_GAP = 10;
    private PeakDebugOverlayRenderer() {}

    private static boolean menuMode;

    static void setMousePosition(int x, int y) { mouseX = x; mouseY = y; }

    public static void render(GuiGraphics graphics, Minecraft mc) {
        if (mc.screen instanceof PeakMenuScreen) return;
        renderOverlay(graphics, mc);
    }

    public static void renderForMenu(GuiGraphics graphics, Minecraft mc, int x, int y) {
        mouseX = x;
        mouseY = y;
        menuMode = true;
        try {
            renderOverlay(graphics, mc);
        } finally {
            menuMode = false;
        }
    }

    private static void renderOverlay(GuiGraphics graphics, Minecraft mc) {
        if (!PeakDebugState.isEnabled()) return;

        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        List<PeakLayoutEntry> entries = PeakDebugState.getEntries();
        PeakLayoutEntry selected = PeakDebugState.resolveSelected(mouseX, mouseY);
        PeakLayoutEntry under = PeakDebugState.baseAt(mouseX, mouseY);

        for (PeakLayoutEntry entry : entries) {
            int color = entry.invisible() ? 0xFF666666 : colorForDepth(entry.depth());
            drawBoxOutline(graphics, entry, color);
        }

        if (selected != null) {
            fillClipped(graphics, selected, selected.absX(), selected.absY(),
                    selected.absX() + selected.width(), selected.absY() + selected.height(), 0x30FFFF00);
            if (under != null && under != selected) {
                drawBoxOutline(graphics, under, 0xFFFFFFFF);
            }
            drawBoxOutline(graphics, selected, 0xFFFFFF00);
            drawDetailPanel(graphics, font, selected, under, mouseX, mouseY, screenWidth, screenHeight);
        }

        PeakImmediateRender.text(graphics, font,
                "§ePeakClient Debug (F3+0 or ESC to close, click to copy ID, scroll up/down to select parent/child)",
                4, screenHeight - 12, 0xFFFFFF, true);
        PeakImmediateRender.text(graphics, font, "§7" + PeakRenderStats.summary(), 4, screenHeight - 24, 0xFFFFFF, true);
    }

    private static int colorForDepth(int depth) {
        int[] palette = { 0xFF55AAFF, 0xFF55FF55, 0xFFFFAA00, 0xFFFF55FF, 0xFF00FFFF };
        return palette[depth % palette.length];
    }

    private static void fillClipped(GuiGraphics graphics, PeakLayoutEntry entry, int x1, int y1, int x2, int y2, int color) {
        int left = Math.max(x1, entry.clipLeft());
        int top = Math.max(y1, entry.clipTop());
        int right = Math.min(x2, entry.clipRight());
        int bottom = Math.min(y2, entry.clipBottom());
        if (right > left && bottom > top) PeakImmediateRender.fill(graphics, left, top, right, bottom, color);
    }

    private static void drawBoxOutline(GuiGraphics graphics, PeakLayoutEntry entry, int color) {
        int x = entry.absX();
        int y = entry.absY();
        int width = Math.max(entry.width(), 1);
        int height = Math.max(entry.height(), 1);
        fillClipped(graphics, entry, x, y, x + width, y + 1, color);
        fillClipped(graphics, entry, x, y + height - 1, x + width, y + height, color);
        fillClipped(graphics, entry, x, y, x + 1, y + height, color);
        fillClipped(graphics, entry, x + width - 1, y, x + width, y + height, color);
    }

    private static List<String> hierarchyLines(PeakLayoutEntry selected, PeakLayoutEntry under) {
        List<String> lines = new ArrayList<>();
        PeakLayoutEntry leaf = under != null ? under : selected;
        List<PeakLayoutEntry> chain = PeakDebugState.chainOf(leaf);
        lines.add("§8--- hierarchy (scroll: level " + (chain.size() - PeakDebugState.levelsUp()) + "/" + chain.size() + ") ---");
        for (int i = 0; i < chain.size(); i++) {
            PeakLayoutEntry entry = chain.get(i);
            String indent = "  ".repeat(i);
            boolean isSelected = entry == selected;
            lines.add(indent + (isSelected ? "§e> " + entry.elementId() : "§7" + entry.elementId()) + " §8" + entry.elementType());
        }
        return lines;
    }

    private static void drawDetailPanel(GuiGraphics graphics, Font font, PeakLayoutEntry selected, PeakLayoutEntry under,
                                        int mx, int my, int screenWidth, int screenHeight) {
        List<String> lines = new ArrayList<>(hierarchyLines(selected, under));
        lines.addAll(PeakElementInspector.inspect(selected));

        List<String> truncated = new ArrayList<>();
        for (String line : lines) truncated.add(truncateToWidth(font, line, MAX_DETAIL_LINE_WIDTH));
        lines = truncated;

        int lineHeight = font.lineHeight;
        int maxRows = Math.max(1, (screenHeight - 6 - 4) / lineHeight);
        int columns = (lines.size() + maxRows - 1) / maxRows;
        int rows = (lines.size() + columns - 1) / columns;

        int[] columnWidths = new int[columns];
        int panelWidth = 8 + (columns - 1) * COLUMN_GAP;
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                int index = column * rows + row;
                if (index < lines.size()) columnWidths[column] = Math.max(columnWidths[column], font.width(lines.get(index)));
            }
            panelWidth += columnWidths[column];
        }
        int panelHeight = rows * lineHeight + 6;

        int panelX = mx + 12;
        if (panelX + panelWidth > screenWidth) panelX = mx - 12 - panelWidth;
        panelX = Math.max(2, Math.min(panelX, screenWidth - panelWidth - 2));
        int panelY = my + 12;
        if (panelY + panelHeight > screenHeight) panelY = my - 12 - panelHeight;
        panelY = Math.max(2, Math.min(panelY, screenHeight - panelHeight - 2));

        PeakImmediateRender.fill(graphics, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0101018);
        int columnX = panelX + 4;
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                int index = column * rows + row;
                if (index >= lines.size()) break;
                PeakImmediateRender.text(graphics, font, lines.get(index), columnX, panelY + 3 + row * lineHeight, 0xFFFFFF, false);
            }
            columnX += columnWidths[column] + COLUMN_GAP;
        }
    }

    private static String truncateToWidth(Font font, String line, int maxWidth) {
        if (font.width(line) <= maxWidth) return line;

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        int budget = Math.max(0, maxWidth - ellipsisWidth);

        StringBuilder result = new StringBuilder();
        int currentWidth = 0;
        int i = 0;
        while (i < line.length()) {
            if (line.charAt(i) == '§' && i + 1 < line.length()) {
                result.append(line, i, i + 2);
                i += 2;
                continue;
            }
            String character = String.valueOf(line.charAt(i));
            int characterWidth = font.width(character);
            if (currentWidth + characterWidth > budget) break;
            result.append(character);
            currentWidth += characterWidth;
            i++;
        }

        return result + ellipsis;
    }
}
