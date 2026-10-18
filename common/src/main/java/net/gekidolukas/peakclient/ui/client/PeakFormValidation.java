package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInput;

import java.util.ArrayList;
import java.util.List;

public final class PeakFormValidation {
    public record Result(List<String> failed, List<String> passed) {}

    private PeakFormValidation() {}

    public static Result validate(PeakDocumentModel model, PeakAction action) {
        List<String> failed = new ArrayList<>();
        List<String> passed = new ArrayList<>();
        for (PeakElement<?> element : candidates(model, action)) {
            if (!(element instanceof PeakInput input)) continue;
            if (element.getInteraction() == null || !element.getInteraction().isRequired()) continue;
            if (isFilled(element, input)) passed.add(element.getId());
            else failed.add(element.getId());
        }
        return new Result(failed, passed);
    }

    private static List<PeakElement<?>> candidates(PeakDocumentModel model, PeakAction action) {
        List<PeakElement<?>> result = new ArrayList<>();
        if (action.collectsAll()) {
            result.addAll(model.elements());
        }
        for (String id : action.collected()) {
            PeakElement<?> element = model.find(id);
            if (element != null && !result.contains(element)) result.add(element);
        }
        return result;
    }

    static boolean isFilled(PeakElement<?> element, PeakInput input) {
        if (element instanceof PeakCheckbox checkbox) return checkbox.isChecked();
        String value = input.inputValue();
        return value != null && !value.trim().isEmpty();
    }
}
