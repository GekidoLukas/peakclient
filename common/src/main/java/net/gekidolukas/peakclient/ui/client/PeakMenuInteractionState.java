package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakScrollbars;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakFormat;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakScrollbarStyle;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTheme;

import java.util.ArrayList;
import java.util.List;

public final class PeakMenuInteractionState {
    private static boolean active;
    private static PeakElement<?> hovered;
    private static PeakElement<?> pressed;
    private static PeakMenuScrollState scroll;
    private static PeakMenuInputState input;
    private static final List<PeakScrollbarRegion> SCROLLBARS = new ArrayList<>();
    private static String hoveredScrollbar;
    private static PeakMenuSettings settings;
    private static String dropElementId;
    private static int dropElementIndex = -1;
    private static String draggedScrollbar;

    private PeakMenuInteractionState() {}

    public static void begin(PeakElement<?> hoveredElement, PeakElement<?> pressedElement) {
        begin(hoveredElement, pressedElement, new PeakMenuScrollState(), new PeakMenuInputState());
    }

    public static void begin(PeakElement<?> hoveredElement, PeakElement<?> pressedElement, PeakMenuScrollState scrollState) {
        begin(hoveredElement, pressedElement, scrollState, new PeakMenuInputState());
    }

    public static void begin(PeakElement<?> hoveredElement, PeakElement<?> pressedElement,
                             PeakMenuScrollState scrollState, PeakMenuInputState inputState) {
        active = true;
        hovered = hoveredElement;
        pressed = pressedElement;
        scroll = scrollState;
        input = inputState;
        SCROLLBARS.clear();
        hoveredScrollbar = null;
        draggedScrollbar = null;
        dropElementId = null;
        dropElementIndex = -1;
        settings = null;
    }

    public static void end() {
        active = false;
        hovered = null;
        pressed = null;
        scroll = null;
        input = null;
        SCROLLBARS.clear();
        hoveredScrollbar = null;
        draggedScrollbar = null;
        settings = null;
    }

    public static boolean isActive() { return active; }

    public static boolean isHovered(PeakElement<?> element) { return active && hovered == element; }

    public static boolean isPressed(PeakElement<?> element) { return active && pressed == element; }

    public static boolean isFocused(PeakElement<?> element) {
        return active && input != null && input.isFocused(element.getId());
    }

    public static PeakTextEditor editor(PeakTextBox field) {
        return input == null ? null : input.editor(field);
    }

    public static PeakRichTextEditor richEditor(PeakRichTextBox box) {
        return input == null ? null : input.richEditor(box);
    }

    public static PeakTextEditor numberEditor(PeakNumberField field) {
        return input == null ? null : input.numberEditor(field);
    }

    public static PeakColorPickerState pickerState(PeakColorPicker picker) {
        return input == null ? PeakColorPickerState.of(picker) : input.picker(picker);
    }

    public static int reportScroll(String elementId, int max) {
        return scroll == null ? 0 : scroll.report(elementId, max);
    }

    public static void scrollTo(String elementId, int offset) {
        if (scroll != null) scroll.set(elementId, offset);
    }

    public static void reportScrollbar(PeakScrollbarRegion region) {
        if (active) SCROLLBARS.add(region);
    }

    public static List<PeakScrollbarRegion> scrollbars() {
        return List.copyOf(SCROLLBARS);
    }

    public static void scrollbarFocus(String hoveredOwner, String draggedOwner) {
        hoveredScrollbar = hoveredOwner;
        draggedScrollbar = draggedOwner;
    }

    public static void useSettings(PeakMenuSettings menuSettings) {
        settings = menuSettings;
    }

    public static PeakScrollbarStyle scrollbar(PeakScrollbarStyle own) {
        return PeakTheme.scrollbar(settings, own);
    }

    public static boolean isDropdownOpen(PeakElement<?> element) {
        return active && input != null && input.isDropdownOpen(element.getId());
    }

    public static boolean isInvalid(PeakElement<?> element) {
        return element.getInteraction() != null && element.getInteraction().isInvalid();
    }

    public static PeakPaint invalidColor(PeakElement<?> element) {
        return PeakTheme.invalidColor(settings, element.getInteraction());
    }

    public static PeakPaint focusColor(PeakElement<?> element) {
        return PeakTheme.focusColor(settings, element.getInteraction());
    }

    public static int focusWidth(PeakElement<?> element) {
        return PeakTheme.focusWidth(settings, element.getInteraction());
    }

    public static boolean focusVisible(PeakElement<?> element) {
        return PeakTheme.focusVisible(settings, element.getInteraction());
    }

    public static void dropTarget(String elementId, int index) {
        dropElementId = elementId;
        dropElementIndex = index;
    }

    public static int dropIndex(PeakElement<?> element) {
        return active && dropElementId != null && dropElementId.equals(element.getId()) ? dropElementIndex : -1;
    }

    public static boolean formatActive(PeakAction action) {
        if (!active || input == null || action.type() != PeakActionType.FORMAT) return false;
        PeakRichTextEditor editor = input.existingRichEditor(action.target());
        if (editor == null || !input.isFocused(action.target())) return false;
        PeakFormat kind = action.formatKind();
        if (kind == null) return false;
        PeakRichText.Style style = editor.styleAtCaret();
        return switch (kind) {
            case BOLD -> style.has(PeakRichText.BOLD);
            case ITALIC -> style.has(PeakRichText.ITALIC);
            case UNDERLINE -> style.has(PeakRichText.UNDERLINE);
            case STRIKE -> style.has(PeakRichText.STRIKE);
            case COLOR -> style.color() != 0 && style.rgb() == action.formatColorValue();
            case COLOR_NEXT, COLOR_PREVIOUS, CLEAR -> false;
        };
    }

    public static int scrollbarState(String ownerId) {
        if (ownerId == null) return PeakScrollbars.NORMAL;
        if (ownerId.equals(draggedScrollbar)) return PeakScrollbars.DRAG;
        if (ownerId.equals(hoveredScrollbar)) return PeakScrollbars.HOVER;
        return PeakScrollbars.NORMAL;
    }
}
