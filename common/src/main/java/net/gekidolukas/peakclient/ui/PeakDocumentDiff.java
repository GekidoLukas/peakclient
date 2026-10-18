package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakDocumentDiff {
    private static final Set<String> IGNORED_KEYS = Set.of("type", "id", "children");

    private PeakDocumentDiff() {}

    public static List<PeakUiOperation> diff(PeakDocument oldDocument, PeakDocument newDocument) {
        if (!oldDocument.getId().equals(newDocument.getId())) {
            throw new IllegalArgumentException("Documents with different ids cannot be compared");
        }

        Map<String, PeakElement<?>> oldElements = new LinkedHashMap<>();
        Map<String, String> oldParents = new HashMap<>();
        collect(oldDocument, null, oldElements, oldParents);
        Map<String, PeakElement<?>> newElements = new LinkedHashMap<>();
        Map<String, String> newParents = new HashMap<>();
        collect(newDocument, null, newElements, newParents);

        Set<String> replaced = new HashSet<>();
        for (Map.Entry<String, PeakElement<?>> entry : newElements.entrySet()) {
            PeakElement<?> previous = oldElements.get(entry.getKey());
            if (previous != null && previous.getClass() != entry.getValue().getClass()) replaced.add(entry.getKey());
        }

        List<PeakUiOperation> deletes = new ArrayList<>();
        List<PeakUiOperation> updates = new ArrayList<>();
        List<PeakUiOperation> adds = new ArrayList<>();

        for (String id : oldElements.keySet()) {
            boolean removed = !newElements.containsKey(id) || replaced.contains(id);
            if (!removed) continue;
            String parent = oldParents.get(id);
            boolean parentAlsoRemoved = parent != null && (!newElements.containsKey(parent) || replaced.contains(parent));
            if (!parentAlsoRemoved) deletes.add(new PeakUiOperation.Delete(id));
        }

        for (Map.Entry<String, PeakElement<?>> entry : newElements.entrySet()) {
            String id = entry.getKey();
            PeakElement<?> element = entry.getValue();
            boolean isNew = !oldElements.containsKey(id) || replaced.contains(id);
            if (isNew) {
                String parent = newParents.get(id);
                boolean parentIsNew = parent != null && (!oldElements.containsKey(parent) || replaced.contains(parent));
                if (parent != null && !parentIsNew) adds.add(new PeakUiOperation.Add(parent, element));
                continue;
            }
            JsonObject patch = changedKeys(oldElements.get(id), element);
            if (!patch.isEmpty()) updates.add(new PeakUiOperation.Update(id, patch));
        }

        List<PeakUiOperation> result = new ArrayList<>(deletes);
        result.addAll(updates);
        result.addAll(adds);
        return result;
    }

    private static JsonObject changedKeys(PeakElement<?> previous, PeakElement<?> next) {
        JsonObject before = PeakUiSerializer.GSON.toJsonTree(previous).getAsJsonObject();
        JsonObject after = PeakUiSerializer.GSON.toJsonTree(next).getAsJsonObject();
        JsonObject patch = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : after.entrySet()) {
            String key = entry.getKey();
            if (IGNORED_KEYS.contains(key)) continue;
            if (!next.isPatchKeyKnown(key)) continue;
            if (!entry.getValue().equals(before.get(key))) patch.add(key, entry.getValue());
        }
        return patch;
    }

    private static void collect(PeakElement<?> element, String parentId,
                                Map<String, PeakElement<?>> elements, Map<String, String> parents) {
        elements.put(element.getId(), element);
        if (parentId != null) parents.put(element.getId(), parentId);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) collect(child, element.getId(), elements, parents);
        }
    }
}
