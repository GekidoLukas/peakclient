package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakRichTextBoxRenderer;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static com.mojang.blaze3d.platform.InputConstants.KEY_0;
import static com.mojang.blaze3d.platform.InputConstants.KEY_A;
import static com.mojang.blaze3d.platform.InputConstants.KEY_B;
import static com.mojang.blaze3d.platform.InputConstants.KEY_BACKSPACE;
import static com.mojang.blaze3d.platform.InputConstants.KEY_C;
import static com.mojang.blaze3d.platform.InputConstants.KEY_D;
import static com.mojang.blaze3d.platform.InputConstants.KEY_DELETE;
import static com.mojang.blaze3d.platform.InputConstants.KEY_DOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_END;
import static com.mojang.blaze3d.platform.InputConstants.KEY_HOME;
import static com.mojang.blaze3d.platform.InputConstants.KEY_I;
import static com.mojang.blaze3d.platform.InputConstants.KEY_K;
import static com.mojang.blaze3d.platform.InputConstants.KEY_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEDOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEUP;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RETURN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_U;
import static com.mojang.blaze3d.platform.InputConstants.KEY_UP;
import static com.mojang.blaze3d.platform.InputConstants.KEY_V;
import static com.mojang.blaze3d.platform.InputConstants.KEY_X;

final class PeakRichTextBoxController implements PeakInputController<PeakRichTextBox> {
    private final PeakMenuContext context;
    private final PeakMenuChanges changes;
    private final PeakTextHits hits;

    PeakRichTextBoxController(PeakMenuContext context, PeakMenuChanges changes, PeakTextHits hits) {
        this.context = context;
        this.changes = changes;
        this.hits = hits;
    }

    @Override
    public boolean isTyping() {
        return true;
    }

    @Override
    public void pressed(PeakRichTextBox box, PeakLayoutEntry hit, double mouseX, double mouseY) {
        PeakMenuInputState input = context.input();
        boolean wasFocused = input.isFocused(box.getId());
        input.focus(box.getId());
        PeakRichTextEditor editor = input.richEditor(box);
        int count = context.registerClick(box, mouseX, mouseY);
        PeakTextHits.RichIndices indices = hits.richIndices(box, editor, hit, mouseX, mouseY);
        if (count >= 3) {
            editor.selectParagraphAt(indices.caret());
            return;
        }
        if (count == 2) {
            editor.selectWordAt(indices.character() >= 0 ? indices.character() : Math.max(0, indices.caret() - 1));
            return;
        }
        if (wasFocused && editor.hasSelection() && indices.character() >= editor.selectionStart() && indices.character() < editor.selectionEnd()) {
            context.beginDrag(new PeakTextDragDrop(context, changes, hits, box, editor.selectionStart(),
                    editor.selectionEnd(), editor.selectedPlain(), editor.selectedFragment(), mouseX, mouseY));
            return;
        }
        editor.setCaret(indices.caret(), Screen.hasShiftDown());
        context.beginDrag(new PeakTextSelectionDrag(context, hits, box));
    }

    @Override
    public boolean key(PeakRichTextBox box, int keyCode) {
        Minecraft minecraft = context.minecraft();
        PeakRichTextEditor editor = context.input().richEditor(box);
        PeakTextLayout.Measure measure = PeakRichTextBoxRenderer.measureOf(minecraft.font, editor.text());
        boolean ctrl = Screen.hasControlDown();
        boolean shift = Screen.hasShiftDown();
        boolean changed = false;

        switch (keyCode) {
            case KEY_BACKSPACE -> changed = editor.backspace(ctrl);
            case KEY_DELETE -> changed = editor.delete(ctrl);
            case KEY_LEFT -> editor.moveLeft(ctrl, shift);
            case KEY_RIGHT -> editor.moveRight(ctrl, shift);
            case KEY_UP -> editor.moveVertical(-1, shift, hits.richLayout(box, editor, measure), measure);
            case KEY_DOWN -> editor.moveVertical(1, shift, hits.richLayout(box, editor, measure), measure);
            case KEY_PAGEUP, KEY_PAGEDOWN -> {
                PeakLayoutEntry entry = context.entryOf(box);
                int boxHeight = entry == null ? 64 : entry.localHeight();
                int lines = Math.max(1, (boxHeight - 6) / PeakRichTextBoxRenderer.lineHeight(minecraft.font, box) - 1);
                for (int i = 0; i < lines; i++) {
                    editor.moveVertical(keyCode == KEY_PAGEUP ? -1 : 1, shift, hits.richLayout(box, editor, measure), measure);
                }
            }
            case KEY_HOME -> {
                if (ctrl) editor.documentStart(shift);
                else editor.lineStart(hits.richLayout(box, editor, measure), shift);
            }
            case KEY_END -> {
                if (ctrl) editor.documentEnd(shift);
                else editor.lineEnd(hits.richLayout(box, editor, measure), shift);
            }
            case KEY_RETURN, KEY_NUMPADENTER -> {
                if (ctrl) {
                    changes.flush(context.menuId(), box.getId());
                    context.fireChange(box, PeakEvent.SUBMIT, box.getValue());
                } else {
                    changed = editor.insertNewline();
                }
            }
            default -> {
                if (!ctrl) break;
                if (keyCode == KEY_A) {
                    editor.selectAll();
                } else if (keyCode == KEY_C) {
                    if (editor.hasSelection()) minecraft.keyboardHandler.setClipboard(editor.selectedPlain());
                } else if (keyCode == KEY_X) {
                    if (editor.hasSelection()) {
                        minecraft.keyboardHandler.setClipboard(editor.selectedPlain());
                        changed = editor.deleteSelection();
                    }
                } else if (keyCode == KEY_V) {
                    changed = editor.insertText(minecraft.keyboardHandler.getClipboard(), true);
                } else if (box.isFormatting()) {
                    if (keyCode == KEY_B) changed = editor.toggleFlag(PeakRichText.BOLD);
                    else if (keyCode == KEY_I) changed = editor.toggleFlag(PeakRichText.ITALIC);
                    else if (keyCode == KEY_U) changed = editor.toggleFlag(PeakRichText.UNDERLINE);
                    else if (keyCode == KEY_D) changed = editor.toggleFlag(PeakRichText.STRIKE);
                    else if (keyCode == KEY_K) changed = editor.cycleColor(box.getColors(), shift);
                    else if (keyCode == KEY_0) changed = editor.clearFormat();
                }
            }
        }
        if (changed) commit(box, editor);
        return true;
    }

    @Override
    public boolean typed(PeakRichTextBox box, char codePoint) {
        PeakRichTextEditor editor = context.input().richEditor(box);
        if (editor.insertTyped(codePoint)) commit(box, editor);
        return true;
    }

    @Override
    public boolean escape(PeakRichTextBox box) {
        context.input().blur();
        return true;
    }

    private void commit(PeakRichTextBox box, PeakRichTextEditor editor) {
        context.input().commit(box, editor);
        changes.noteRich(box);
    }
}
