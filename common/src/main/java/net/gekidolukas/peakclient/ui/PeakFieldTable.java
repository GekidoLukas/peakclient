package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakFieldTable {
    private static final Map<Class<?>, PeakFieldTable> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> BLOCKED = Set.of("id", "children", "bindings");

    private final Map<String, Field> fields = new HashMap<>();

    private PeakFieldTable(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) continue;
                if (BLOCKED.contains(field.getName())) continue;
                if (Modifier.isFinal(modifiers) && !Collection.class.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);
                fields.putIfAbsent(field.getName(), field);
            }
        }
    }

    public static PeakFieldTable of(Class<?> type) {
        return CACHE.computeIfAbsent(type, PeakFieldTable::new);
    }

    public boolean has(String key) {
        return fields.containsKey(key);
    }

    public boolean isOpaqueColor(String key) {
        Field field = fields.get(key);
        return field != null && field.isAnnotationPresent(PeakOpaqueColor.class);
    }

    private static boolean descendable(Class<?> type) {
        return !type.isPrimitive() && !type.isEnum() && !type.isArray() && !type.getName().startsWith("java.");
    }

    public static boolean hasPath(Class<?> type, String path) {
        String[] segments = path.split("\\.");
        PeakFieldTable table = of(type);
        for (int i = 0; i < segments.length; i++) {
            Field field = table.fields.get(segments[i]);
            if (field == null) return false;
            if (i < segments.length - 1) {
                if (!descendable(field.getType())) return false;
                table = of(field.getType());
            }
        }
        return true;
    }

    public static Class<?> typeAt(Class<?> type, String path) {
        String[] segments = path.split("\\.");
        PeakFieldTable table = of(type);
        Class<?> current = null;
        for (int i = 0; i < segments.length; i++) {
            Field field = table.fields.get(segments[i]);
            if (field == null) return null;
            current = field.getType();
            if (i < segments.length - 1) {
                if (!descendable(current)) return null;
                table = of(current);
            }
        }
        return current;
    }

    public static boolean isPaintPath(Class<?> type, String path) {
        Class<?> fieldType = typeAt(type, path);
        return fieldType != null && PeakPaint.class.isAssignableFrom(fieldType);
    }

    public static boolean applyPath(Object root, String path, JsonElement value) throws IllegalAccessException {
        String[] segments = path.split("\\.");
        Object target = root;
        PeakFieldTable table = of(root.getClass());
        for (int i = 0; i < segments.length - 1; i++) {
            Field field = table.fields.get(segments[i]);
            if (field == null) return false;
            Object next = field.get(target);
            if (next == null || !descendable(next.getClass())) return false;
            target = next;
            table = of(next.getClass());
        }
        return table.apply(target, segments[segments.length - 1], value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean apply(Object target, String key, JsonElement value) throws IllegalAccessException {
        Field field = fields.get(key);
        if (field == null) return false;

        Object parsed = PeakUiSerializer.GSON.fromJson(value, field.getGenericType());
        if (Collection.class.isAssignableFrom(field.getType()) && Modifier.isFinal(field.getModifiers())) {
            Collection current = (Collection) field.get(target);
            current.clear();
            if (parsed != null) current.addAll((Collection) parsed);
            return true;
        }
        if (parsed instanceof Integer color) {
            if (field.isAnnotationPresent(PeakOpaqueColor.class)) parsed = PeakArgb.ensureOpaque(color);
            else if (field.isAnnotationPresent(PeakColorOrNone.class)) parsed = PeakArgb.normalize(color);
        }
        if (parsed instanceof PeakPaint paint) {
            if (field.isAnnotationPresent(PeakOpaqueColor.class)) parsed = paint.opaque();
            else if (field.isAnnotationPresent(PeakColorOrNone.class)) parsed = paint.orNone();
        } else if (parsed == null && PeakPaint.class.isAssignableFrom(field.getType())) {
            parsed = PeakPaint.NONE;
        }
        field.set(target, parsed);
        return true;
    }
}
