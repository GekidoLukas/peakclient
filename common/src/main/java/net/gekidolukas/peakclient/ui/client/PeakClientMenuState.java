package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakUiOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakClientMenuState {
    public static final int DEFAULT_MAX_CACHED = 64;

    private final Map<String, PeakDocumentModel> menus = new LinkedHashMap<>(16, 0.75f, true);
    private final int maxCached;

    public PeakClientMenuState() {
        this(DEFAULT_MAX_CACHED);
    }

    public PeakClientMenuState(int maxCached) {
        this.maxCached = Math.max(1, maxCached);
    }

    public List<String> put(PeakDocument menu, Collection<String> protectedIds) {
        menus.put(menu.getId(), new PeakDocumentModel(menu));
        List<String> evicted = new ArrayList<>();
        Iterator<Map.Entry<String, PeakDocumentModel>> iterator = menus.entrySet().iterator();
        while (menus.size() - evicted.size() > maxCached && iterator.hasNext()) {
            String id = iterator.next().getKey();
            if (id.equals(menu.getId()) || protectedIds.contains(id)) continue;
            evicted.add(id);
        }
        for (String id : evicted) {
            menus.remove(id);
            PeakLog.warn("Menu '{}' was evicted from the client cache because more than {} menus are cached", id, maxCached);
        }
        return evicted;
    }

    public PeakDocument get(String menuId) {
        PeakDocumentModel model = menus.get(menuId);
        return model == null ? null : model.document();
    }

    public PeakDocumentModel model(String menuId) {
        return menus.get(menuId);
    }

    public boolean contains(String menuId) {
        return menus.containsKey(menuId);
    }

    public boolean applyOperations(String menuId, List<PeakUiOperation> operations) {
        PeakDocumentModel model = menus.get(menuId);
        if (model == null) {
            PeakLog.warn("Update received for unknown menu '{}' - ignored", menuId);
            return false;
        }
        for (PeakUiOperation operation : operations) {
            try {
                model.applyOperation(operation);
            } catch (Exception e) {
                PeakLog.error("Failed to apply an operation in menu '" + menuId + "'", e);
            }
        }
        return true;
    }

    public boolean remove(String menuId) {
        return menus.remove(menuId) != null;
    }

    public void clear() {
        menus.clear();
    }

    public Set<String> ids() {
        return Set.copyOf(menus.keySet());
    }

    public int size() {
        return menus.size();
    }
}
