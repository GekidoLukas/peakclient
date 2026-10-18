package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakValueResolver;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakClientCustomValues {
    private static final Map<String, String> VALUES = new ConcurrentHashMap<>();

    private PeakClientCustomValues() {}

    public static void set(String namespace, String key, String value) {
        VALUES.put(composite(namespace, key), value);
    }

    public static void clear(String namespace, String key) {
        VALUES.remove(composite(namespace, key));
    }

    public static void clearNamespace(String namespace) {
        String prefix = namespace.toLowerCase(Locale.ROOT) + ":";
        VALUES.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public static void clearAll() {
        VALUES.clear();
        PeakValueResolver.clearState();
    }

    public static String get(String namespace, String key) {
        return VALUES.getOrDefault(composite(namespace, key), "");
    }

    private static String composite(String namespace, String key) {
        return namespace.toLowerCase(Locale.ROOT) + ":" + key;
    }

    public static boolean contains(String namespace, String key) {
        return VALUES.containsKey(composite(namespace, key));
    }
}
