package net.gekidolukas.peakclient.ui.client;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakUiOperation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public record PeakActionScope(String id, boolean menu) {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public static PeakActionScope menu(String id) {
        return new PeakActionScope(id, true);
    }

    public static PeakActionScope document(String id) {
        return new PeakActionScope(id, false);
    }

    public PeakDocumentModel model() {
        return menu ? PeakClientMenus.model(id) : PeakClientUiState.model(id);
    }

    public boolean apply(PeakUiOperation operation) {
        if (menu) return PeakClientMenus.applyLocalOperation(id, operation);
        PeakDocumentModel model = PeakClientUiState.model(id);
        return model != null && model.applyOperation(operation);
    }

    public void toggleHidden(String elementId) {
        PeakDocumentModel model = model();
        PeakElement<?> target = model == null ? null : model.find(elementId);
        if (target == null) {
            warnOnce("toggle", elementId, "Toggle action in " + describe() + " targets '" + elementId + "', which does not exist");
            return;
        }
        JsonObject patch = new JsonObject();
        patch.addProperty("hidden", !target.isHidden());
        apply(new PeakUiOperation.Update(elementId, patch));
    }

    public void warnOnce(String kind, String detail, String message) {
        if (WARNED.add(kind + "/" + id + "/" + detail)) PeakLog.warn(message);
    }

    public String describe() {
        return (menu ? "menu '" : "document '") + id + "'";
    }
}
