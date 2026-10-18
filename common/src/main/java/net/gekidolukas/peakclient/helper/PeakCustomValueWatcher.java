package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakTargets;
import net.gekidolukas.peakclient.server.PeakValueKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PeakCustomValueWatcher<T> extends PeakTargetedTask {
    private final String namespace;
    private final String key;
    private final Supplier<T> sharedSupplier;
    private final Function<UUID, T> perPlayerSupplier;
    private final Function<T, String> formatter;

    private final Map<UUID, T> lastValues = new HashMap<>();

    private PeakCustomValueWatcher(String namespace, String key, Collection<UUID> targetPlayerIds,
                                   Supplier<T> sharedSupplier, Function<UUID, T> perPlayerSupplier,
                                   Function<T, String> formatter) {
        super(targetPlayerIds, 20);
        this.namespace = namespace;
        this.key = key;
        this.sharedSupplier = sharedSupplier;
        this.perPlayerSupplier = perPlayerSupplier;
        this.formatter = formatter;
    }

    public static <T> PeakCustomValueWatcher<T> watch(PeakValueKey<T> key, PeakTargets targets, Supplier<T> valueSupplier) {
        PeakCustomValueWatcher<T> watcher = new PeakCustomValueWatcher<>(key.namespace(), key.key(), targets.ids(), valueSupplier, null, key::format);
        watcher.startTask();
        return watcher;
    }

    public static <T> PeakCustomValueWatcher<T> watchPerPlayer(PeakValueKey<T> key, PeakTargets targets, Function<UUID, T> valueSupplier) {
        PeakCustomValueWatcher<T> watcher = new PeakCustomValueWatcher<>(key.namespace(), key.key(), targets.ids(), null, valueSupplier, key::format);
        watcher.startTask();
        return watcher;
    }

    public PeakCustomValueWatcher<T> intervalTicks(int ticks) { this.intervalTicks = Math.max(1, ticks); return this; }

    @Override
    protected void onTargetRemoved(UUID playerId) {
        lastValues.remove(playerId);
    }

    @Override
    protected void onInterval() {
        if (sharedSupplier != null) {
            syncShared();
        } else {
            syncPerPlayer();
        }
    }

    private void syncShared() {
        T current = sharedSupplier.get();
        List<UUID> changed = new ArrayList<>();
        for (UUID id : targets()) {
            if (!lastValues.containsKey(id) || !Objects.equals(lastValues.get(id), current)) changed.add(id);
        }
        if (changed.isEmpty()) return;
        for (UUID id : changed) lastValues.put(id, current);
        PeakClientApi.get().setCustomValue(namespace, key, formatter.apply(current), changed);
    }

    private void syncPerPlayer() {
        for (UUID id : targets()) {
            T current = perPlayerSupplier.apply(id);
            if (lastValues.containsKey(id) && Objects.equals(lastValues.get(id), current)) continue;
            lastValues.put(id, current);
            PeakClientApi.get().setCustomValue(namespace, key, formatter.apply(current), List.of(id));
        }
    }
}
