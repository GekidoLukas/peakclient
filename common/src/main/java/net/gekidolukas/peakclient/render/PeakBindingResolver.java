package net.gekidolukas.peakclient.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakFieldTable;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public final class PeakBindingResolver {
    private static final Map<PeakElement<?>, Map<String, FieldState>> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private PeakBindingResolver() {}

    private static final class FieldState {
        JsonElement base;
        JsonElement lastApplied;
        boolean paint;
        boolean verified;
        boolean unusable;
    }

    public static void applyTree(PeakElement<?> element) {
        if (!element.getBindings().isEmpty() || STATES.containsKey(element)) applyBindings(element);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : new ArrayList<>(container.getChildren())) applyTree(child);
        }
    }

    private static void applyBindings(PeakElement<?> element) {
        Map<String, FieldState> states = STATES.computeIfAbsent(element, e -> new HashMap<>());
        restoreRemoved(element, states);
        for (Map.Entry<String, String> entry : new ArrayList<>(element.getBindings().entrySet())) {
            String path = entry.getKey();
            FieldState state = states.computeIfAbsent(path, p -> new FieldState());
            if (state.unusable) continue;
            try {
                applyField(element, path, entry.getValue(), state);
            } catch (Exception e) {
                state.unusable = true;
                PeakValueResolver.warnOnce("binding '" + path + "' on element '" + element.getId() + "' failed: " + e);
            }
        }
    }

    private static void restoreRemoved(PeakElement<?> element, Map<String, FieldState> states) {
        Map<String, String> bindings = element.getBindings();
        states.entrySet().removeIf(entry -> {
            if (bindings.containsKey(entry.getKey())) return false;
            FieldState state = entry.getValue();
            if (state.base != null && state.lastApplied != null && !state.unusable) {
                try {
                    applyValue(element, serialize(element), entry.getKey(), state.base);
                } catch (Exception ignored) {
                }
            }
            return true;
        });
    }

    private static void applyField(PeakElement<?> element, String path, String expression, FieldState state) {
        if (state.base == null) {
            JsonElement current = lookup(serialize(element), path);
            boolean paint = PeakFieldTable.isPaintPath(element.getClass(), path);
            if (current == null || (!paint && !(current instanceof JsonPrimitive))) {
                state.unusable = true;
                PeakValueResolver.warnOnce("binding path '" + path + "' on element '" + element.getId() + "' is not a scalar field");
                return;
            }
            state.base = current;
            state.paint = paint;
        }

        JsonElement target = resolveTarget(path, expression, state.base, state.paint, element);
        if (target.equals(state.lastApplied)) return;

        JsonObject tree = serialize(element);
        applyValue(element, tree, path, target);
        state.lastApplied = target;

        if (!state.verified) {
            state.verified = true;
            JsonElement after = lookup(serialize(element), path);
            boolean matches = state.paint ? after != null
                    : target instanceof JsonPrimitive expected && (sameValue(after, expected)
                    || (isColorPath(path) && sameRgb(after, expected)));
            if (!matches) {
                state.unusable = true;
                PeakValueResolver.warnOnce("binding path '" + path + "' on element '" + element.getId() + "' is not patchable");
            }
        }
    }

    private static JsonElement resolveTarget(String path, String expression, JsonElement base, boolean paint,
                                             PeakElement<?> element) {
        PeakPlaceholderResolver.Result result = PeakPlaceholderResolver.resolveStrict(expression);
        if (!result.complete()) {
            PeakValueResolver.warnOnce("unresolved placeholder in binding '" + path + "' on element '" + element.getId() + "': " + expression);
            return base;
        }
        if (paint) {
            try {
                PeakPaint parsed = PeakPaintAdapter.parse(result.value());
                return PeakPaintAdapter.toTree(parsed);
            } catch (IllegalArgumentException e) {
                PeakValueResolver.warnOnce("value '" + result.value() + "' is not a paint for binding '" + path
                        + "' on element '" + element.getId() + "': " + e.getMessage());
                return base;
            }
        }
        JsonPrimitive converted = convert(path, result.value(), (JsonPrimitive) base);
        if (converted == null) {
            PeakValueResolver.warnOnce("value '" + result.value() + "' does not fit binding '" + path + "' on element '" + element.getId() + "'");
            return base;
        }
        return converted;
    }

    private static JsonPrimitive convert(String path, String text, JsonPrimitive base) {
        if (base.isBoolean()) {
            Boolean value = PeakValueResolver.parseBoolean(text);
            return value == null ? null : new JsonPrimitive(value);
        }
        if (base.isNumber()) {
            if (isColorPath(path)) {
                Integer color = PeakValueResolver.parseColor(text);
                return color == null ? null : new JsonPrimitive(PeakArgb.normalize(color));
            }
            String baseText = base.getAsString();
            boolean floating = baseText.indexOf('.') >= 0 || baseText.indexOf('E') >= 0 || baseText.indexOf('e') >= 0;
            if (floating) {
                Double value = PeakValueResolver.parseDouble(text);
                return value == null ? null : new JsonPrimitive(value);
            }
            Long value = PeakValueResolver.parseLong(text);
            return value == null ? null : new JsonPrimitive(value);
        }
        return new JsonPrimitive(text);
    }

    private static boolean isColorPath(String path) {
        String last = path.substring(path.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return last.endsWith("color");
    }

    private static void applyValue(PeakElement<?> element, JsonObject tree, String path, JsonElement value) {
        String[] segments = path.split("\\.");
        JsonObject patch = new JsonObject();
        if (segments.length == 1 || PeakFieldTable.hasPath(element.getClass(), path)) {
            patch.add(path, value);
        } else {
            JsonElement top = tree.get(segments[0]);
            if (!(top instanceof JsonObject topObject)) throw new IllegalStateException("Path root is not an object");
            JsonObject cursor = topObject;
            for (int i = 1; i < segments.length - 1; i++) {
                if (!(cursor.get(segments[i]) instanceof JsonObject next)) throw new IllegalStateException("Path segment is not an object");
                cursor = next;
            }
            cursor.add(segments[segments.length - 1], value);
            patch.add(segments[0], topObject);
        }
        element.applyPatch(patch);
    }

    private static boolean sameRgb(JsonElement actual, JsonPrimitive expected) {
        if (!(actual instanceof JsonPrimitive primitive) || !primitive.isNumber() || !expected.isNumber()) return false;
        return (primitive.getAsInt() & 0xFFFFFF) == (expected.getAsInt() & 0xFFFFFF);
    }

    private static boolean sameValue(JsonElement actual, JsonPrimitive expected) {
        if (!(actual instanceof JsonPrimitive primitive)) return false;
        if (expected.isNumber() && primitive.isNumber()) {
            return Math.abs(primitive.getAsDouble() - expected.getAsDouble()) < 0.001;
        }
        return primitive.equals(expected);
    }

    private static JsonObject serialize(PeakElement<?> element) {
        return PeakUiSerializer.GSON.toJsonTree(element).getAsJsonObject();
    }

    private static JsonElement lookup(JsonObject tree, String path) {
        JsonElement cursor = tree;
        for (String segment : path.split("\\.")) {
            if (!(cursor instanceof JsonObject object)) return null;
            cursor = object.get(segment);
            if (cursor == null) return null;
        }
        return cursor;
    }
}
