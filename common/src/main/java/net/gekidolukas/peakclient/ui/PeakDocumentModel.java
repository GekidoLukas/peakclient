package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakLog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PeakDocumentModel {
    public interface Listener {
        default void onIndexed(PeakElement<?> element) {}
        default void onRemoved(PeakElement<?> element) {}
        default void onAdded(PeakElement<?> element) {}
    }

    private static final Listener NO_LISTENER = new Listener() {};

    private final PeakDocument document;
    private final Map<String, PeakElement<?>> index = new HashMap<>();
    private final Map<String, String> parentOf = new HashMap<>();
    private final Listener listener;

    public PeakDocumentModel(PeakDocument document) {
        this(document, NO_LISTENER);
    }

    public PeakDocumentModel(PeakDocument document, Listener listener) {
        this.document = document;
        this.listener = listener;
        indexRecursive(document, null);
    }

    public PeakDocument document() { return document; }

    public PeakElement<?> find(String elementId) { return index.get(elementId); }

    public boolean contains(String elementId) { return index.containsKey(elementId); }

    public Set<String> ids() { return index.keySet(); }

    public Collection<PeakElement<?>> elements() { return index.values(); }

    public String parentOf(String elementId) { return parentOf.get(elementId); }

    public boolean add(String parentId, PeakElement<?> element) {
        return add(parentId, element, null);
    }

    public boolean add(String parentId, PeakElement<?> element, Integer insertIndex) {
        if (index.containsKey(element.getId())) return false;
        if (!(index.get(parentId) instanceof PeakContainer<?> container)) return false;
        if (insertIndex == null) {
            container.getChildren().add(element);
        } else {
            container.getChildren().add(Math.max(0, Math.min(insertIndex, container.getChildren().size())), element);
        }
        indexRecursive(element, parentId);
        listener.onAdded(element);
        return true;
    }

    public boolean remove(String elementId) {
        String parentId = parentOf.get(elementId);
        if (parentId == null) return false;
        if (index.get(parentId) instanceof PeakContainer<?> container) {
            container.getChildren().removeIf(child -> child.getId().equals(elementId));
        }
        removeFromIndexRecursive(elementId);
        return true;
    }

    public void applyToClass(String className, JsonObject patch) {
        for (PeakElement<?> element : index.values()) {
            if (!element.hasClass(className)) continue;
            try {
                element.applyPatch(patch);
            } catch (Exception e) {
                PeakLog.error("Error applying a class patch on element '" + element.getId() + "'", e);
            }
        }
    }

    public boolean applyOperation(PeakUiOperation operation) {
        if (operation instanceof PeakUiOperation.Update update) {
            PeakElement<?> element = index.get(update.elementId());
            if (element == null) {
                PeakLog.warn("Update targets unknown element '{}' in document '{}' - ignored", update.elementId(), document.getId());
                return false;
            }
            element.applyPatch(update.patch());
            return true;
        }
        if (operation instanceof PeakUiOperation.UpdateByClass updateByClass) {
            applyToClass(updateByClass.className(), updateByClass.patch());
            return true;
        }
        if (operation instanceof PeakUiOperation.Delete delete) {
            return remove(delete.elementId());
        }
        if (operation instanceof PeakUiOperation.Add add) {
            if (!index.containsKey(add.parentId())) {
                PeakLog.warn("Add operation references unknown parent '{}' in document '{}' - ignored", add.parentId(), document.getId());
                return false;
            }
            return add(add.parentId(), add.element(), add.insertIndex());
        }
        return false;
    }

    private void indexRecursive(PeakElement<?> element, String parentId) {
        element.syncInteraction();
        index.put(element.getId(), element);
        if (parentId != null) parentOf.put(element.getId(), parentId);
        listener.onIndexed(element);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) {
                indexRecursive(child, element.getId());
            }
        }
    }

    private void removeFromIndexRecursive(String elementId) {
        PeakElement<?> element = index.remove(elementId);
        parentOf.remove(elementId);
        if (element != null) listener.onRemoved(element);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) {
                removeFromIndexRecursive(child.getId());
            }
        }
    }
}
