package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.helper.PeakCustomValueWatcher;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PeakAudienceValues {
    private record StoredValue(String namespace, String key, String value) {}

    private final PeakAudience audience;
    private final Map<String, StoredValue> stored = new ConcurrentHashMap<>();

    PeakAudienceValues(PeakAudience audience) {
        this.audience = audience;
    }

    public <T> void set(PeakValueKey<T> key, T value) {
        String text = key.format(value);
        stored.put(key.id(), new StoredValue(key.namespace(), key.key(), text));
        PeakClientApi.get().setCustomValue(key.namespace(), key.key(), text, audience.members());
    }

    public <T> void clear(PeakValueKey<T> key) {
        stored.remove(key.id());
        PeakClientApi.get().clearCustomValue(key.namespace(), key.key(), audience.members());
    }

    public <T> PeakCustomValueWatcher<T> bind(PeakValueKey<T> key, Supplier<T> supplier, int intervalTicks) {
        PeakCustomValueWatcher<T> watcher = PeakCustomValueWatcher
                .watch(key, PeakClient.to(List.<UUID>of()), supplier)
                .intervalTicks(intervalTicks);
        return audience.bind(watcher);
    }

    public <T> PeakCustomValueWatcher<T> bindPerPlayer(PeakValueKey<T> key, Function<UUID, T> function, int intervalTicks) {
        PeakCustomValueWatcher<T> watcher = PeakCustomValueWatcher
                .watchPerPlayer(key, PeakClient.to(List.<UUID>of()), function)
                .intervalTicks(intervalTicks);
        return audience.bind(watcher);
    }

    void onJoin(UUID playerId) {
        for (StoredValue value : stored.values()) {
            PeakClientApi.get().setCustomValue(value.namespace(), value.key(), value.value(), List.of(playerId));
        }
    }
}
