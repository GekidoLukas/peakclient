package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInput;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PeakInputValues {
    private PeakInputValues() {}

    public static Map<String, String> arguments(PeakDocumentModel model, PeakAction action, Map<String, String> extra) {
        Map<String, String> result = new LinkedHashMap<>();
        if (model != null) {
            if (action.collectsAll()) {
                for (PeakElement<?> element : model.elements()) {
                    if (element instanceof PeakInput input) result.put(element.getId(), input.inputValue());
                }
            }
            for (String id : action.collected()) {
                if (model.find(id) instanceof PeakInput input) result.put(id, input.inputValue());
            }
        }
        result.putAll(action.args());
        for (Map.Entry<String, String> entry : extra.entrySet()) result.putIfAbsent(entry.getKey(), entry.getValue());
        return result;
    }
}
