package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakDocument extends PeakContainer<PeakDocument> {
    public static final int CURRENT_VERSION = 1;

    private final int version;
    private PeakScaling scaling = PeakScaling.GUI_SCALE_DEPENDENT;
    private transient Map<String, PeakElement<?>> index;
    private int documentTimeToLiveTicks = 0;
    private boolean deleteIfEmpty = false;
    private Set<PeakVanillaElement> hiddenVanilla = new LinkedHashSet<>();
    private PeakMenuSettings menu;

    public PeakDocument() {
        this(PeakId.random());
    }

    public PeakDocument(String id) {
        this(id, CURRENT_VERSION);
    }

    public PeakDocument(String id, int version) {
        super(id);
        this.version = version;
    }
    public static PeakDocument create() { return new PeakDocument(); }
    public static PeakDocument create(String id) { return new PeakDocument(id); }
    public static PeakDocument create(String id, int version) { return new PeakDocument(id, version); }

    public int getVersion() { return version; }
    public PeakScaling getScaling() { return scaling; }
    public PeakDocument scaling(PeakScaling scaling) { this.scaling = scaling; return this; }
    public int getDocumentTimeToLiveTicks() { return documentTimeToLiveTicks; }
    public PeakDocument documentTimeToLiveTicks(int ticks) { this.documentTimeToLiveTicks = Math.max(0, ticks); return this; }
    public boolean isDeleteIfEmpty() { return deleteIfEmpty; }
    public PeakDocument deleteIfEmpty(boolean deleteIfEmpty) { this.deleteIfEmpty = deleteIfEmpty; return this; }

    public PeakMenuSettings getMenu() { return menu; }
    public boolean isMenu() { return menu != null; }
    public PeakDocument menu(PeakMenuSettings settings) { this.menu = settings; return this; }

    public Set<PeakVanillaElement> getHiddenVanilla() {
        return hiddenVanilla == null ? Set.of() : hiddenVanilla;
    }

    public PeakDocument hideVanilla(PeakVanillaElement... elements) {
        return hideVanilla(Arrays.asList(elements));
    }

    public PeakDocument hideVanilla(Collection<PeakVanillaElement> elements) {
        if (hiddenVanilla == null) hiddenVanilla = new LinkedHashSet<>();
        hiddenVanilla.addAll(elements);
        return this;
    }

    @Override
    public String elementType() { return "document"; }

    @Override
    public PeakDocument add(PeakElement<?> child) {
        registerRecursive(child);
        return super.add(child);
    }

    private Map<String, PeakElement<?>> index() {
        if (index == null) {
            index = new LinkedHashMap<>();
            for (PeakElement<?> child : getChildren()) indexRecursive(child, index);
        }
        return index;
    }

    private static void indexRecursive(PeakElement<?> element, Map<String, PeakElement<?>> target) {
        target.put(element.getId(), element);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> grandChild : container.getChildren()) indexRecursive(grandChild, target);
        }
    }

    private void registerRecursive(PeakElement<?> element) {
        Map<String, PeakElement<?>> current = index();
        if (current.containsKey(element.getId())) {
            throw new IllegalStateException("Element ID exists already: " + element.getId());
        }
        current.put(element.getId(), element);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> grandChild : container.getChildren()) {
                registerRecursive(grandChild);
            }
        }
    }

    public PeakElement<?> findById(String id) {
        return index().get(id);
    }

    public List<PeakElement<?>> findByClass(String className) {
        List<PeakElement<?>> result = new ArrayList<>();
        for (PeakElement<?> element : index().values()) {
            if (element.hasClass(className)) result.add(element);
        }
        return result;
    }
}
