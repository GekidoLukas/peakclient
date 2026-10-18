package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakRadioGroup;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakTextBox;

import java.util.HashMap;
import java.util.Map;

final class PeakInputControllers {
    private final PeakMenuContext context;
    private final Map<Class<?>, PeakInputController<?>> byType = new HashMap<>();
    private final PeakInputController<?> fallback;
    private final PeakDropdownController dropdowns;
    private final PeakNumberFieldController numbers;

    PeakInputControllers(PeakMenuContext context, PeakMenuChanges changes, PeakTextHits hits) {
        this.context = context;
        this.dropdowns = new PeakDropdownController(context);
        this.numbers = new PeakNumberFieldController(context, changes);
        this.fallback = new PeakActivationController<PeakElement<?>>(context, this, false);
        byType.put(PeakTextBox.class, new PeakTextBoxController(context, changes, hits));
        byType.put(PeakRichTextBox.class, new PeakRichTextBoxController(context, changes, hits));
        byType.put(PeakNumberField.class, numbers);
        byType.put(PeakSlider.class, new PeakSliderController(context));
        byType.put(PeakColorPicker.class, new PeakColorPickerController(context));
        byType.put(PeakRadioGroup.class, new PeakRadioGroupController(context));
        byType.put(PeakDropdown.class, dropdowns);
        byType.put(PeakCheckbox.class, new PeakActivationController<PeakCheckbox>(context, this, true));
    }

    @SuppressWarnings("unchecked")
    PeakInputController<PeakElement<?>> of(PeakElement<?> element) {
        return (PeakInputController<PeakElement<?>>) byType.getOrDefault(element.getClass(), fallback);
    }

    boolean handles(Class<?> type) {
        return byType.containsKey(type);
    }

    PeakDropdownController dropdowns() {
        return dropdowns;
    }

    PeakNumberFieldController numbers() {
        return numbers;
    }

    void activate(PeakElement<?> element) {
        if (element instanceof PeakCheckbox checkbox) {
            checkbox.checked(!checkbox.isChecked());
            context.fireChange(checkbox, PeakEvent.CHANGE, checkbox.inputValue());
        }
        context.fire(element, PeakEvent.CLICK, Map.of());
    }
}
