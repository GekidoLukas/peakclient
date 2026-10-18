package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakColorMath;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTextBox;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakColorPickerLinks {
    private static final Set<String> WARNED = new HashSet<>();

    private PeakColorPickerLinks() {}

    public static void commitState(String menuId, PeakColorPicker picker, PeakColorPickerState state, boolean writeHex) {
        picker.value(state.argb(picker.isAlpha()));
        state.applied(picker.getValue());
        propagate(menuId, picker, writeHex);
    }

    public static void applyColor(String menuId, PeakColorPicker picker, int argb, boolean writeHex) {
        picker.value(argb);
        PeakClientMenus.inputState(menuId).picker(picker);
        propagate(menuId, picker, writeHex);
    }

    public static void fireChange(String menuId, PeakColorPicker picker) {
        PeakEventDispatch.fire(PeakActionScope.menu(menuId), picker, PeakEvent.CHANGE, Map.of("value", picker.inputValue()));
    }

    public static void onHexEdited(String menuId, PeakTextBox field) {
        PeakDocumentModel model = PeakClientMenus.model(menuId);
        if (model == null) return;
        for (PeakElement<?> element : List.copyOf(model.elements())) {
            if (!(element instanceof PeakColorPicker picker) || !field.getId().equals(picker.getHexField())) continue;
            Integer parsed = PeakColorMath.tryParseHex(field.getValue(), picker.isAlpha());
            if (parsed == null || parsed == picker.getValue()) continue;
            applyColor(menuId, picker, parsed, false);
            fireChange(menuId, picker);
        }
    }

    public static void setColor(String menuId, PeakAction action) {
        PeakDocumentModel model = PeakClientMenus.model(menuId);
        PeakElement<?> element = model == null ? null : model.find(action.target());
        if (!(element instanceof PeakColorPicker picker)) {
            if (WARNED.add(menuId + "/" + action.target())) {
                PeakLog.warn("Set-color action in menu '{}' targets '{}', which is not a color picker", menuId, action.target());
            }
            return;
        }
        applyColor(menuId, picker, action.colorValue(), true);
        fireChange(menuId, picker);
    }

    public static void followTargets(String menuId) {
        PeakDocumentModel model = PeakClientMenus.model(menuId);
        if (model == null) return;
        PeakMenuInputState input = PeakClientMenus.inputState(menuId);
        for (PeakElement<?> element : List.copyOf(model.elements())) {
            if (!(element instanceof PeakColorPicker picker) || picker.getTarget().isEmpty()) continue;
            if (!input.followTarget(picker)) continue;
            String hexField = picker.getHexField();
            if (!hexField.isEmpty() && model.find(hexField) instanceof PeakTextBox field) {
                String text = picker.inputValue();
                if (!text.equals(field.getValue())) field.value(text);
            }
        }
    }

    public static void clearWarnings() {
        WARNED.clear();
    }

    private static void propagate(String menuId, PeakColorPicker picker, boolean writeHex) {
        PeakDocumentModel model = PeakClientMenus.model(menuId);
        if (model == null) return;
        PeakMenuInputState input = PeakClientMenus.inputState(menuId);

        String target = picker.getTarget();
        if (!target.isEmpty() && model.find(target) instanceof PeakRichTextBox box && box.isFormatting()) {
            PeakRichTextEditor editor = input.richEditor(box);
            if (editor.applyColor(picker.getValue() & 0xFFFFFF)) input.commit(box, editor);
        }

        String hexField = picker.getHexField();
        if (writeHex && !hexField.isEmpty() && model.find(hexField) instanceof PeakTextBox field) {
            String text = picker.inputValue();
            if (!text.equals(field.getValue())) field.value(text);
        }
    }
}
