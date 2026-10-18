package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.PeakServerThread;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class PeakVanillaHud {
    private final Supplier<Collection<UUID>> targets;
    private final Map<PeakVanillaElement, PeakVanillaHudEntry> desired;

    PeakVanillaHud(Supplier<Collection<UUID>> targets) {
        this.targets = targets;
        this.desired = null;
    }

    private PeakVanillaHud(Supplier<Collection<UUID>> targets, boolean tracked) {
        this.targets = targets;
        this.desired = tracked ? new EnumMap<>(PeakVanillaElement.class) : null;
    }

    public static PeakVanillaHud tracked(Supplier<Collection<UUID>> members) {
        return new PeakVanillaHud(members, true);
    }

    public PeakVanillaHud hide(PeakVanillaElement... elements) {
        return hide(Arrays.asList(elements));
    }

    public PeakVanillaHud hide(Collection<PeakVanillaElement> elements) {
        return patch(elements, PeakVanillaHudEntry::disabled);
    }

    public PeakVanillaHud show(PeakVanillaElement... elements) {
        return show(Arrays.asList(elements));
    }

    public PeakVanillaHud show(Collection<PeakVanillaElement> elements) {
        return patch(elements, PeakVanillaHudEntry::shown);
    }

    public PeakVanillaHud hideAll() {
        return hide(PeakVanillaElement.ALL);
    }

    public PeakVanillaHud hideAllExcept(PeakVanillaElement... kept) {
        Set<PeakVanillaElement> keep = EnumSet.noneOf(PeakVanillaElement.class);
        keep.addAll(Arrays.asList(kept));
        Map<PeakVanillaElement, PeakVanillaHudEntry> changes = new EnumMap<>(PeakVanillaElement.class);
        for (PeakVanillaElement element : PeakVanillaElement.ALL) {
            changes.put(element, keep.contains(element) ? PeakVanillaHudEntry.shown() : PeakVanillaHudEntry.disabled());
        }
        return send(changes);
    }

    public PeakVanillaHud offset(PeakVanillaElement element, int x, int y) {
        if (!element.offsettable()) {
            PeakLog.warn("The vanilla HUD element {} cannot be moved, the offset is ignored", element);
            return this;
        }
        Map<PeakVanillaElement, PeakVanillaHudEntry> changes = new EnumMap<>(PeakVanillaElement.class);
        changes.put(element, PeakVanillaHudEntry.moved(x, y));
        return send(changes);
    }

    public PeakVanillaHud reset() {
        return reset(PeakVanillaElement.ALL);
    }

    public PeakVanillaHud reset(PeakVanillaElement... elements) {
        return reset(Arrays.asList(elements));
    }

    public PeakVanillaHud reset(Collection<PeakVanillaElement> elements) {
        return patch(elements, PeakVanillaHudEntry::create);
    }

    public void replayTo(UUID playerId) {
        if (desired == null) return;
        PeakServerThread.run(() -> {
            if (desired.isEmpty()) return;
            PeakClientApi.get().setVanillaHudElements(copyOf(desired), List.of(playerId));
        });
    }

    public void restoreFor(UUID playerId) {
        if (desired == null) return;
        PeakServerThread.run(() -> {
            if (desired.isEmpty()) return;
            PeakClientApi.get().setVanillaHudElements(resetOf(desired.keySet()), List.of(playerId));
        });
    }

    public void restoreAll() {
        if (desired == null) return;
        PeakServerThread.run(() -> {
            if (desired.isEmpty()) return;
            Map<PeakVanillaElement, PeakVanillaHudEntry> resets = resetOf(desired.keySet());
            desired.clear();
            PeakClientApi.get().setVanillaHudElements(resets, List.copyOf(targets.get()));
        });
    }

    private PeakVanillaHud patch(Collection<PeakVanillaElement> elements, Supplier<PeakVanillaHudEntry> entry) {
        Map<PeakVanillaElement, PeakVanillaHudEntry> changes = new EnumMap<>(PeakVanillaElement.class);
        for (PeakVanillaElement element : elements) changes.put(element, entry.get());
        return send(changes);
    }

    private PeakVanillaHud send(Map<PeakVanillaElement, PeakVanillaHudEntry> changes) {
        if (changes.isEmpty()) return this;
        Map<PeakVanillaElement, PeakVanillaHudEntry> copy = copyOf(changes);
        PeakServerThread.run(() -> {
            if (desired != null) remember(copy);
            PeakClientApi.get().setVanillaHudElements(copy, List.copyOf(targets.get()));
        });
        return this;
    }

    private void remember(Map<PeakVanillaElement, PeakVanillaHudEntry> changes) {
        for (Map.Entry<PeakVanillaElement, PeakVanillaHudEntry> change : changes.entrySet()) {
            PeakVanillaHudEntry merged = PeakVanillaHudEntry.merge(desired.get(change.getKey()), change.getValue());
            if (merged.isDefault()) desired.remove(change.getKey());
            else desired.put(change.getKey(), merged);
        }
    }

    private static Map<PeakVanillaElement, PeakVanillaHudEntry> copyOf(Map<PeakVanillaElement, PeakVanillaHudEntry> source) {
        return new EnumMap<>(source);
    }

    private static Map<PeakVanillaElement, PeakVanillaHudEntry> resetOf(Collection<PeakVanillaElement> elements) {
        Map<PeakVanillaElement, PeakVanillaHudEntry> resets = new EnumMap<>(PeakVanillaElement.class);
        for (PeakVanillaElement element : new ArrayList<>(elements)) resets.put(element, PeakVanillaHudEntry.create());
        return resets;
    }
}
