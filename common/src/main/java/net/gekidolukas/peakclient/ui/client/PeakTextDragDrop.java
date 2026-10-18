package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakImmediateRender;
import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

final class PeakTextDragDrop implements PeakMenuDrag {
    private static final double ACTIVATION_DISTANCE = 4;
    private static final int GHOST_LIMIT = 32;

    private final PeakMenuContext context;
    private final PeakMenuChanges changes;
    private final PeakTextHits hits;
    private final PeakElement<?> source;
    private final int start;
    private final int end;
    private final String plain;
    private final PeakRichText fragment;
    private final double startX;
    private final double startY;
    private boolean active;
    private PeakElement<?> target;
    private int targetIndex = -1;

    PeakTextDragDrop(PeakMenuContext context, PeakMenuChanges changes, PeakTextHits hits, PeakElement<?> source,
                     int start, int end, String plain, PeakRichText fragment, double startX, double startY) {
        this.context = context;
        this.changes = changes;
        this.hits = hits;
        this.source = source;
        this.start = start;
        this.end = end;
        this.plain = plain;
        this.fragment = fragment;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public void dragged(double mouseX, double mouseY) {
        if (!active) {
            if (Math.hypot(mouseX - startX, mouseY - startY) < ACTIVATION_DISTANCE) return;
            active = true;
        }
        target = null;
        targetIndex = -1;
        PeakLayoutEntry hit = PeakMenuHits.interactiveAt(context.probe(), (int) mouseX, (int) mouseY);
        if (hit == null || !PeakMenuContext.isEnabled(hit.element())) return;
        PeakElement<?> element = hit.element();
        if (element instanceof PeakTextBox box) {
            PeakTextEditor editor = context.input().editor(box);
            float x = PeakMenuContext.localX(hit, mouseX) - PeakTextHits.TEXT_PADDING + editor.scrollPx();
            target = box;
            targetIndex = context.caretIndexAt(editor.value(), x);
        } else if (element instanceof PeakRichTextBox box) {
            PeakRichTextEditor editor = context.input().richEditor(box);
            target = box;
            targetIndex = hits.richIndexAt(box, editor, hit, mouseX, mouseY);
        }
    }

    @Override
    public void released(double mouseX, double mouseY) {
        PeakMenuInputState input = context.input();
        if (!active) {
            PeakLayoutEntry entry = context.entryOf(source);
            if (entry == null) return;
            if (source instanceof PeakTextBox box) {
                PeakTextEditor editor = input.editor(box);
                editor.setCaret(context.caretIndexAt(editor.value(),
                        PeakMenuContext.localX(entry, mouseX) - PeakTextHits.TEXT_PADDING + editor.scrollPx()), false);
            } else if (source instanceof PeakRichTextBox box) {
                PeakRichTextEditor editor = input.richEditor(box);
                editor.setCaret(hits.richIndexAt(box, editor, entry, mouseX, mouseY), false);
            }
            return;
        }
        if (target == null || targetIndex < 0) return;

        boolean copy = Screen.hasControlDown();
        boolean same = target == source;
        int index = targetIndex;
        if (same && index >= start && index <= end) return;

        int inserted;
        if (target instanceof PeakTextBox targetBox) {
            PeakTextEditor editor = input.editor(targetBox);
            int before = editor.value().length();
            editor.setCaret(index, false);
            editor.insert(plain);
            inserted = editor.value().length() - before;
            editor.selectRange(index, index + inserted);
            input.commit(targetBox, editor);
            changes.noteText(targetBox);
        } else if (target instanceof PeakRichTextBox targetBox) {
            PeakRichTextEditor editor = input.richEditor(targetBox);
            int before = editor.text().length();
            editor.setCaret(index, false);
            if (fragment != null) editor.insertRich(fragment);
            else editor.insertText(plain, true);
            inserted = editor.text().length() - before;
            editor.selectRange(index, index + inserted);
            input.commit(targetBox, editor);
            changes.noteRich(targetBox);
        } else {
            return;
        }
        if (inserted <= 0 || copy || inserted < end - start) return;

        int sourceStart = start;
        int sourceEnd = end;
        if (same && index <= sourceStart) {
            sourceStart += inserted;
            sourceEnd += inserted;
        }
        if (source instanceof PeakTextBox sourceBox) {
            PeakTextEditor editor = input.editor(sourceBox);
            editor.deleteRange(sourceStart, sourceEnd);
            if (same) {
                int selectionStart = index > end ? index - inserted : index;
                editor.selectRange(selectionStart, selectionStart + inserted);
            }
            input.commit(sourceBox, editor);
            changes.noteText(sourceBox);
        } else if (source instanceof PeakRichTextBox sourceBox) {
            PeakRichTextEditor editor = input.richEditor(sourceBox);
            editor.deleteRange(sourceStart, sourceEnd);
            if (same) {
                int selectionStart = index > end ? index - inserted : index;
                editor.selectRange(selectionStart, selectionStart + inserted);
            }
            input.commit(sourceBox, editor);
            changes.noteRich(sourceBox);
        }
    }

    @Override
    public boolean keepsScrollbarHover() {
        return true;
    }

    @Override
    public boolean cancelledByEscape() {
        return true;
    }

    @Override
    public boolean silencesHoverSound() {
        return true;
    }

    @Override
    public boolean hidesTooltip() {
        return active;
    }

    @Override
    public void beforeRender() {
        if (active && target != null) PeakMenuInteractionState.dropTarget(target.getId(), targetIndex);
    }

    @Override
    public void afterRender(GuiGraphics graphics, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!active) return;
        Font font = context.minecraft().font;
        String flat = plain.replace('\n', ' ');
        if (flat.length() > GHOST_LIMIT) flat = flat.substring(0, GHOST_LIMIT - 1) + "…";
        String label = (Screen.hasControlDown() ? "+ " : "") + flat;
        int width = font.width(label);
        int x = Math.min(mouseX + 10, screenWidth - width - 8);
        int y = Math.min(mouseY + 10, screenHeight - font.lineHeight - 6);
        PeakImmediateRender.fill(graphics, x, y, x + width + 6, y + font.lineHeight + 4, 0xE0101018);
        PeakImmediateRender.fill(graphics, x, y, x + width + 6, y + 1, 0xFF55FFFF);
        PeakImmediateRender.text(graphics, font, label, x + 3, y + 2, 0xFFFFFF, false);
    }
}
