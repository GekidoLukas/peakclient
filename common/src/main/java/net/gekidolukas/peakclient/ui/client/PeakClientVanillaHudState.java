package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakClientVanillaHudState {
    private static final Map<PeakVanillaElement, PeakVanillaHudEntry> OVERRIDES = new ConcurrentHashMap<>();
    private static final Map<String, Set<PeakVanillaElement>> DOCUMENT_HIDES = new ConcurrentHashMap<>();
    private static final Set<PeakVanillaElement> SUPPORTED = ConcurrentHashMap.newKeySet();
    private static final Set<PeakVanillaElement> WARNED = ConcurrentHashMap.newKeySet();
    private static volatile Set<PeakVanillaElement> documentHidden = EnumSet.noneOf(PeakVanillaElement.class);

    private PeakClientVanillaHudState() {}

    public static void registerSupported(Collection<PeakVanillaElement> elements) {
        SUPPORTED.addAll(elements);
    }

    public static void apply(Map<PeakVanillaElement, PeakVanillaHudEntry> updates) {
        if (updates == null) return;
        for (Map.Entry<PeakVanillaElement, PeakVanillaHudEntry> update : updates.entrySet()) {
            PeakVanillaElement element = update.getKey();
            PeakVanillaHudEntry patch = update.getValue();
            if (element == null || patch == null) continue;
            if (!SUPPORTED.isEmpty() && !SUPPORTED.contains(element) && WARNED.add(element)) {
                PeakLog.warn("The vanilla HUD element {} is not supported by this client and is ignored", element);
            }
            PeakVanillaHudEntry merged = PeakVanillaHudEntry.merge(OVERRIDES.get(element), patch);
            if (merged.isDefault()) OVERRIDES.remove(element);
            else OVERRIDES.put(element, merged);
        }
    }

    public static void setDocumentHides(String documentId, Set<PeakVanillaElement> hidden) {
        if (hidden == null || hidden.isEmpty()) DOCUMENT_HIDES.remove(documentId);
        else DOCUMENT_HIDES.put(documentId, EnumSet.copyOf(hidden));
        rebuildDocumentHidden();
    }

    public static void removeDocument(String documentId) {
        if (DOCUMENT_HIDES.remove(documentId) != null) rebuildDocumentHidden();
    }

    public static void clearDocumentHides() {
        DOCUMENT_HIDES.clear();
        documentHidden = EnumSet.noneOf(PeakVanillaElement.class);
    }

    public static void clearAll() {
        OVERRIDES.clear();
        clearDocumentHides();
    }

    private static void rebuildDocumentHidden() {
        EnumSet<PeakVanillaElement> union = EnumSet.noneOf(PeakVanillaElement.class);
        for (Set<PeakVanillaElement> hidden : DOCUMENT_HIDES.values()) union.addAll(hidden);
        documentHidden = union;
    }

    public static boolean isEnabled(PeakVanillaElement element) {
        if (documentHidden.contains(element)) return false;
        PeakVanillaHudEntry e = OVERRIDES.get(element);
        return e == null || e.isEnabled();
    }

    public static int offsetX(PeakVanillaElement element) {
        PeakVanillaHudEntry e = OVERRIDES.get(element);
        return e == null ? 0 : e.getOffsetX();
    }

    public static int offsetY(PeakVanillaElement element) {
        PeakVanillaHudEntry e = OVERRIDES.get(element);
        return e == null ? 0 : e.getOffsetY();
    }
}
