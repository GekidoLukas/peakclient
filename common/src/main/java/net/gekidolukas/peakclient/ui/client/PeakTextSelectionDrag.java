package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTextBox;

final class PeakTextSelectionDrag implements PeakMenuDrag {
    private final PeakMenuContext context;
    private final PeakTextHits hits;
    private final PeakElement<?> element;

    PeakTextSelectionDrag(PeakMenuContext context, PeakTextHits hits, PeakElement<?> element) {
        this.context = context;
        this.hits = hits;
        this.element = element;
    }

    @Override
    public void dragged(double mouseX, double mouseY) {
        PeakLayoutEntry entry = context.entryOf(element);
        if (entry == null) return;
        if (element instanceof PeakTextBox box) {
            PeakTextEditor editor = context.input().editor(box);
            float x = PeakMenuContext.localX(entry, mouseX) - PeakTextHits.TEXT_PADDING + editor.scrollPx();
            editor.setCaret(context.caretIndexAt(editor.value(), x), true);
        } else if (element instanceof PeakRichTextBox box) {
            PeakRichTextEditor editor = context.input().richEditor(box);
            editor.setCaret(hits.richIndexAt(box, editor, entry, mouseX, mouseY), true);
        }
    }

    @Override
    public void released(double mouseX, double mouseY) {}
}
