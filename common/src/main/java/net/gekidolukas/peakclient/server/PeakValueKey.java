package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.serialize.PeakComponentValue;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Function;

public final class PeakValueKey<T> {
    private final String namespace;
    private final String key;
    private final Function<T, String> formatter;

    private PeakValueKey(String namespace, String key, Function<T, String> formatter) {
        this.namespace = namespace;
        this.key = key;
        this.formatter = formatter;
    }

    public static <T> PeakValueKey<T> of(String namespacedKey, Function<T, String> formatter) {
        int separator = namespacedKey.indexOf(':');
        if (separator <= 0 || separator == namespacedKey.length() - 1) {
            throw new IllegalArgumentException("A value key must look like 'namespace:key', got '" + namespacedKey + "'");
        }
        PeakId.require(namespacedKey, "Value key");
        return new PeakValueKey<>(namespacedKey.substring(0, separator), namespacedKey.substring(separator + 1), formatter);
    }

    public static PeakValueKey<String> ofString(String namespacedKey) { return of(namespacedKey, Function.identity()); }
    public static PeakValueKey<Integer> ofInt(String namespacedKey) { return of(namespacedKey, String::valueOf); }
    public static PeakValueKey<Long> ofLong(String namespacedKey) { return of(namespacedKey, String::valueOf); }
    public static PeakValueKey<Component> ofComponent(String namespacedKey) { return of(namespacedKey, PeakComponentValue::encode); }
    public static PeakValueKey<Boolean> ofBoolean(String namespacedKey) { return of(namespacedKey, String::valueOf); }
    public static PeakValueKey<Double> ofDouble(String namespacedKey, String format) {
        return of(namespacedKey, value -> String.format(Locale.ROOT, format, value));
    }

    public String namespace() { return namespace; }
    public String key() { return key; }
    public String id() { return namespace + ":" + key; }
    public String format(T value) { return formatter.apply(value); }
    public String placeholder() { return "{" + namespace + ":" + key + "}"; }
    public String placeholder(String fallback) { return "{" + namespace + ":" + key + "#" + fallback + "}"; }
}
