package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.minecraft.client.gui.screens.Screen;

import static com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RETURN;

final class PeakTextBoxController implements PeakInputController<PeakTextBox> {
    private final PeakMenuContext context;
    private final PeakMenuChanges changes;
    private final PeakTextHits hits;

    PeakTextBoxController(PeakMenuContext context, PeakMenuChanges changes, PeakTextHits hits) {
        this.context = context;
        this.changes = changes;
        this.hits = hits;
    }

    @Override
    public boolean isTyping() {
        return true;
    }

    @Override
    public void pressed(PeakTextBox field, PeakLayoutEntry hit, double mouseX, double mouseY) {
        PeakMenuInputState input = context.input();
        boolean wasFocused = input.isFocused(field.getId());
        input.focus(field.getId());
        PeakTextEditor editor = input.editor(field);
        int count = context.registerClick(field, mouseX, mouseY);
        float x = PeakMenuContext.localX(hit, mouseX) - PeakTextHits.TEXT_PADDING + editor.scrollPx();
        int nearest = context.caretIndexAt(editor.value(), x);
        int under = context.charIndexAt(editor.value(), x);
        if (count >= 3) {
            editor.selectAll();
            return;
        }
        if (count == 2) {
            editor.selectWordAt(under >= 0 ? under : Math.max(0, nearest - 1));
            return;
        }
        if (wasFocused && editor.hasSelection() && under >= editor.selectionStart() && under < editor.selectionEnd()) {
            context.beginDrag(new PeakTextDragDrop(context, changes, hits, field, editor.selectionStart(),
                    editor.selectionEnd(), editor.selectedText(), null, mouseX, mouseY));
            return;
        }
        editor.setCaret(nearest, Screen.hasShiftDown());
        context.beginDrag(new PeakTextSelectionDrag(context, hits, field));
    }

    @Override
    public boolean key(PeakTextBox field, int keyCode) {
        PeakTextEditor editor = context.input().editor(field);
        boolean changed = false;
        if (keyCode == KEY_RETURN || keyCode == KEY_NUMPADENTER) {
            changes.flush(context.menuId(), field.getId());
            context.fireChange(field, PeakEvent.SUBMIT, field.getValue());
        } else {
            changed = PeakTextEditorKeys.apply(editor, keyCode, Screen.hasControlDown(), Screen.hasShiftDown(),
                    context.minecraft());
        }
        if (changed) commit(field, editor);
        return true;
    }

    @Override
    public boolean typed(PeakTextBox field, char codePoint) {
        PeakTextEditor editor = context.input().editor(field);
        if (editor.insert(String.valueOf(codePoint))) commit(field, editor);
        return true;
    }

    @Override
    public boolean escape(PeakTextBox field) {
        context.input().blur();
        return true;
    }

    @Override
    public void focusedByTab(PeakTextBox field) {
        context.input().editor(field).selectAll();
    }

    private void commit(PeakTextBox field, PeakTextEditor editor) {
        context.input().commit(field, editor);
        PeakColorPickerLinks.onHexEdited(context.menuId(), field);
        changes.noteText(field);
    }
}
