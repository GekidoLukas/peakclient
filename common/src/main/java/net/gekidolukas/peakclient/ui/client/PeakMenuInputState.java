package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakTextFilter;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTextBox;

import java.util.HashMap;
import java.util.Map;

public final class PeakMenuInputState {
    private final Map<String, PeakTextEditor> editors = new HashMap<>();
    private final Map<String, PeakRichTextEditor> richEditors = new HashMap<>();
    private final Map<String, PeakColorPickerState> pickers = new HashMap<>();
    private final Map<String, PeakTextEditor> numberEditors = new HashMap<>();
    private String openDropdownId;
    private int dropdownScroll;
    private int dropdownCursor = -1;
    private String focusedId;

    public String focusedId() {
        return focusedId;
    }

    public boolean isFocused(String elementId) {
        return focusedId != null && focusedId.equals(elementId);
    }

    public void focus(String elementId) {
        focusedId = elementId;
    }

    public void blur() {
        focusedId = null;
    }

    public PeakRichTextEditor existingRichEditor(String elementId) {
        return richEditors.get(elementId);
    }

    public PeakTextEditor editor(PeakTextBox field) {
        PeakTextEditor editor = editors.computeIfAbsent(field.getId(), id -> new PeakTextEditor());
        editor.configure(field.getMaxLength(), field.getFilter());
        if (editor.needsSync(field.getValue())) {
            editor.setValue(field.getValue());
            editor.synced(field.getValue());
        }
        return editor;
    }

    public void commit(PeakTextBox field, PeakTextEditor editor) {
        field.value(editor.value());
        editor.synced(field.getValue());
    }

    public PeakRichTextEditor richEditor(PeakRichTextBox box) {
        PeakRichTextEditor editor = richEditors.computeIfAbsent(box.getId(), id -> new PeakRichTextEditor());
        editor.configure(box.getMaxLength(), box.getMaxLines());
        if (editor.needsSync(box.getValue())) {
            editor.setValue(box.getValue());
            editor.synced(box.getValue());
        }
        return editor;
    }

    public void commit(PeakRichTextBox box, PeakRichTextEditor editor) {
        box.value(editor.value());
        editor.synced(box.getValue());
    }

    public String openDropdownId() { return openDropdownId; }

    public boolean isDropdownOpen(String elementId) { return openDropdownId != null && openDropdownId.equals(elementId); }

    public int dropdownScroll() { return dropdownScroll; }

    public int dropdownCursor() { return dropdownCursor; }

    public void openDropdown(String elementId, int scroll, int cursor) {
        openDropdownId = elementId;
        dropdownScroll = Math.max(0, scroll);
        dropdownCursor = cursor;
    }

    public void dropdownScroll(int scroll) { this.dropdownScroll = Math.max(0, scroll); }

    public void dropdownCursor(int cursor) { this.dropdownCursor = cursor; }

    public void closeDropdown() {
        openDropdownId = null;
        dropdownScroll = 0;
        dropdownCursor = -1;
    }

    public PeakTextEditor numberEditor(PeakNumberField field) {
        PeakTextEditor editor = numberEditors.computeIfAbsent(field.getId(), id -> new PeakTextEditor());
        editor.configure(16, PeakTextFilter.DECIMAL);
        String shown = field.inputValue();
        if (!isFocused(field.getId()) && editor.needsSync(shown)) {
            editor.setValue(shown);
            editor.synced(shown);
        }
        return editor;
    }

    public void resetNumberEditor(PeakNumberField field) {
        PeakTextEditor editor = numberEditor(field);
        String shown = field.inputValue();
        editor.setValue(shown);
        editor.synced(shown);
    }

    public PeakColorPickerState picker(PeakColorPicker picker) {
        return pickers.computeIfAbsent(picker.getId(), id -> new PeakColorPickerState()).sync(picker);
    }

    public boolean followTarget(PeakColorPicker picker) {
        String target = picker.getTarget();
        if (target.isEmpty() || !isFocused(target)) return false;
        PeakRichTextEditor editor = richEditors.get(target);
        if (editor == null) return false;

        PeakRichText.Style style = editor.styleAtCaret();
        if (style.color() == 0) return false;
        int rgb = style.rgb() & 0xFFFFFF;

        PeakColorPickerState state = picker(picker);
        if (state.lastTargetColor() == rgb) return false;
        state.seenTargetColor(rgb);
        if ((picker.getValue() & 0xFFFFFF) == rgb) return false;

        int alpha = picker.isAlpha() ? (picker.getValue() & 0xFF000000) : 0xFF000000;
        picker.value(alpha | rgb);
        picker(picker);
        return true;
    }

    public void forget(String elementId) {
        editors.remove(elementId);
        richEditors.remove(elementId);
        pickers.remove(elementId);
        numberEditors.remove(elementId);
        if (isDropdownOpen(elementId)) closeDropdown();
        if (isFocused(elementId)) focusedId = null;
    }

    public void clear() {
        editors.clear();
        richEditors.clear();
        pickers.clear();
        numberEditors.clear();
        closeDropdown();
        focusedId = null;
    }
}
