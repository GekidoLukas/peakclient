package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakRichTextBoxRenderer;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.minecraft.client.gui.Font;

final class PeakTextHits {
    record RichIndices(int caret, int character) {}

    static final int TEXT_PADDING = 4;

    private final PeakMenuContext context;

    PeakTextHits(PeakMenuContext context) {
        this.context = context;
    }

    PeakTextLayout richLayout(PeakRichTextBox box, PeakRichTextEditor editor, PeakTextLayout.Measure measure) {
        PeakLayoutEntry entry = context.entryOf(box);
        int boxWidth = entry == null ? 160 : entry.localWidth();
        int boxHeight = entry == null ? 64 : entry.localHeight();
        int wrap = PeakRichTextBoxRenderer.wrapWidth(context.minecraft().font, box,
                PeakTheme.scrollbar(context.settings(), box.getScrollbar()), editor, boxWidth, boxHeight);
        return editor.layout(wrap, measure);
    }

    RichIndices richIndices(PeakRichTextBox box, PeakRichTextEditor editor, PeakLayoutEntry entry, double mouseX, double mouseY) {
        Font font = context.minecraft().font;
        PeakTextLayout.Measure measure = PeakRichTextBoxRenderer.measureOf(font, editor.text());
        PeakTextLayout layout = richLayout(box, editor, measure);
        int lineHeight = PeakRichTextBoxRenderer.lineHeight(font, box);
        int offset = PeakClientMenus.scrollState(context.menuId()).offset(box.getId());
        float x = PeakMenuContext.localX(entry, mouseX) - PeakRichTextBoxRenderer.PADDING_X + (box.isWrap() ? 0 : editor.scrollX());
        float y = PeakMenuContext.localY(entry, mouseY) - PeakRichTextBoxRenderer.PADDING_Y + offset;
        int line = Math.max(0, Math.min(layout.lineCount() - 1, (int) Math.floor(y / lineHeight)));
        return new RichIndices(layout.indexAt(line, Math.round(x), measure), layout.charIndexAt(line, Math.round(x), measure));
    }

    int richIndexAt(PeakRichTextBox box, PeakRichTextEditor editor, PeakLayoutEntry entry, double mouseX, double mouseY) {
        return richIndices(box, editor, entry, mouseX, mouseY).caret();
    }
}
