package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakNumberFieldLayout;
import net.minecraft.client.gui.screens.Screen;

import static com.mojang.blaze3d.platform.InputConstants.KEY_DOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RETURN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_UP;

final class PeakNumberFieldController implements PeakInputController<PeakNumberField> {
    private final PeakMenuContext context;
    private final PeakMenuChanges changes;

    PeakNumberFieldController(PeakMenuContext context, PeakMenuChanges changes) {
        this.context = context;
        this.changes = changes;
    }

    @Override
    public boolean isTyping() {
        return true;
    }

    @Override
    public void pressed(PeakNumberField number, PeakLayoutEntry hit, double mouseX, double mouseY) {
        PeakMenuInputState input = context.input();
        PeakNumberFieldLayout layout = PeakNumberFieldLayout.of(hit.localWidth(), hit.localHeight(), number.isShowButtons());
        float x = PeakMenuContext.localX(hit, mouseX);
        PeakNumberFieldLayout.Part part = layout.partAt(Math.round(x), Math.round(PeakMenuContext.localY(hit, mouseY)));
        boolean wasFocused = input.isFocused(number.getId());
        input.focus(number.getId());
        PeakTextEditor editor = input.numberEditor(number);
        int multiplier = Screen.hasShiftDown() ? 10 : 1;

        if (part == PeakNumberFieldLayout.Part.MINUS || part == PeakNumberFieldLayout.Part.PLUS) {
            if (wasFocused) commitText(number);
            step(number, part == PeakNumberFieldLayout.Part.PLUS ? 1 : -1, multiplier);
            input.numberEditor(number).selectAll();
            return;
        }

        int count = context.registerClick(number, mouseX, mouseY);
        if (!wasFocused || count >= 2) {
            editor.selectAll();
            return;
        }
        String text = editor.value();
        float textStart = layout.textX() + Math.max(2, (layout.textWidth() - context.minecraft().font.width(text)) / 2f);
        editor.setCaret(context.caretIndexAt(text, x - textStart), Screen.hasShiftDown());
    }

    @Override
    public boolean key(PeakNumberField number, int keyCode) {
        PeakTextEditor editor = context.input().numberEditor(number);
        boolean shift = Screen.hasShiftDown();
        if (keyCode == KEY_RETURN || keyCode == KEY_NUMPADENTER) {
            commitText(number);
            changes.flush(context.menuId(), number.getId());
            context.input().numberEditor(number).selectAll();
        } else if (keyCode == KEY_UP || keyCode == KEY_DOWN) {
            commitText(number);
            step(number, keyCode == KEY_UP ? 1 : -1, shift ? 10 : 1);
            context.input().numberEditor(number).selectAll();
        } else {
            PeakTextEditorKeys.apply(editor, keyCode, Screen.hasControlDown(), shift, context.minecraft());
        }
        return true;
    }

    @Override
    public boolean typed(PeakNumberField number, char codePoint) {
        context.input().numberEditor(number).insert(String.valueOf(codePoint));
        return true;
    }

    @Override
    public boolean wheel(PeakNumberField number, double scrollY) {
        if (scrollY == 0) return false;
        if (context.input().isFocused(number.getId())) commitText(number);
        step(number, scrollY > 0 ? 1 : -1, Screen.hasShiftDown() ? 10 : 1);
        return true;
    }

    @Override
    public boolean escape(PeakNumberField number) {
        context.input().resetNumberEditor(number);
        context.input().blur();
        return true;
    }

    void commitOnBlur(String elementId) {
        PeakDocumentModel model = PeakClientMenus.model(context.menuId());
        if (model != null && model.find(elementId) instanceof PeakNumberField number) commitText(number);
    }

    private void commitText(PeakNumberField number) {
        PeakMenuInputState input = context.input();
        PeakTextEditor editor = input.numberEditor(number);
        Double parsed = PeakNumberField.parse(editor.value());
        double before = number.getValue();
        if (parsed != null) number.value(parsed);
        input.resetNumberEditor(number);
        if (number.getValue() != before) changes.noteNumber(number);
    }

    private void step(PeakNumberField number, int direction, int multiplier) {
        double before = number.getValue();
        number.value(number.stepped(before, direction * multiplier));
        context.input().resetNumberEditor(number);
        if (number.getValue() != before) {
            context.playClick(number.getInteraction());
            changes.noteNumber(number);
        }
    }
}
