package net.gekidolukas.peakclient.ui.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakComponentAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();


    public static JsonElement toTree(Component component) {
        Style style = PeakStyledText.sanitize(component.getStyle());
        ComponentContents contents = component.getContents();
        List<Component> siblings = component.getSiblings();
        if (contents instanceof PlainTextContents plain && style.isEmpty() && siblings.isEmpty()) {
            return new JsonPrimitive(plain.text());
        }

        JsonObject object = new JsonObject();
        if (contents instanceof PlainTextContents plain) {
            object.addProperty("text", plain.text());
        } else if (contents instanceof TranslatableContents translatable) {
            object.addProperty("translate", translatable.getKey());
            if (translatable.getFallback() != null) object.addProperty("fallback", translatable.getFallback());
            if (translatable.getArgs().length > 0) {
                JsonArray args = new JsonArray();
                for (Object arg : translatable.getArgs()) args.add(argToTree(arg));
                object.add("with", args);
            }
        } else if (contents instanceof KeybindContents keybind) {
            object.addProperty("keybind", keybind.getName());
        } else {
            warnUnsupported(contents);
            StringBuilder flattened = new StringBuilder();
            contents.visit(text -> {
                flattened.append(text);
                return Optional.empty();
            });
            object.addProperty("text", flattened.toString());
        }

        writeStyle(style, object);
        if (!siblings.isEmpty()) {
            JsonArray extra = new JsonArray();
            for (Component sibling : siblings) extra.add(toTree(sibling));
            object.add("extra", extra);
        }
        return object;
    }

    public static Component fromTree(JsonElement element) {
        if (element == null || element.isJsonNull()) return Component.empty();
        if (element.isJsonPrimitive()) return Component.literal(element.getAsString());
        if (element.isJsonArray()) {
            MutableComponent root = Component.empty();
            for (JsonElement entry : element.getAsJsonArray()) root.append(fromTree(entry));
            return root;
        }

        JsonObject object = element.getAsJsonObject();
        MutableComponent component;
        if (object.has("text")) {
            component = Component.literal(object.get("text").getAsString());
        } else if (object.has("translate")) {
            String key = object.get("translate").getAsString();
            String fallback = object.has("fallback") ? object.get("fallback").getAsString() : null;
            Object[] args = readArgs(object.get("with"));
            component = fallback == null ? Component.translatable(key, args) : Component.translatableWithFallback(key, fallback, args);
        } else if (object.has("keybind")) {
            component = Component.keybind(object.get("keybind").getAsString());
        } else {
            component = Component.empty();
        }

        component.setStyle(readStyle(object));
        if (object.has("extra")) {
            for (JsonElement entry : object.getAsJsonArray("extra")) component.append(fromTree(entry));
        }
        return component;
    }

    private static JsonElement argToTree(Object arg) {
        if (arg instanceof Component component) return toTree(component);
        if (arg instanceof Number number) return new JsonPrimitive(number);
        if (arg instanceof Boolean flag) return new JsonPrimitive(flag);
        return new JsonPrimitive(String.valueOf(arg));
    }

    private static Object[] readArgs(JsonElement with) {
        if (with == null || !with.isJsonArray()) return new Object[0];
        JsonArray array = with.getAsJsonArray();
        Object[] args = new Object[array.size()];
        for (int i = 0; i < args.length; i++) {
            JsonElement entry = array.get(i);
            if (entry.isJsonPrimitive()) {
                JsonPrimitive primitive = entry.getAsJsonPrimitive();
                if (primitive.isNumber()) args[i] = primitive.getAsNumber();
                else if (primitive.isBoolean()) args[i] = primitive.getAsBoolean();
                else args[i] = primitive.getAsString();
            } else {
                args[i] = fromTree(entry);
            }
        }
        return args;
    }

    private static void writeStyle(Style style, JsonObject object) {
        TextColor color = style.getColor();
        if (color != null) object.addProperty("color", color.serialize());
        writeFlag(object, "bold", style.isBold(), style.applyTo(Style.EMPTY.withBold(true)).isBold());
        writeFlag(object, "italic", style.isItalic(), style.applyTo(Style.EMPTY.withItalic(true)).isItalic());
        writeFlag(object, "underlined", style.isUnderlined(), style.applyTo(Style.EMPTY.withUnderlined(true)).isUnderlined());
        writeFlag(object, "strikethrough", style.isStrikethrough(), style.applyTo(Style.EMPTY.withStrikethrough(true)).isStrikethrough());
        writeFlag(object, "obfuscated", style.isObfuscated(), style.applyTo(Style.EMPTY.withObfuscated(true)).isObfuscated());
        if (!Style.DEFAULT_FONT.equals(style.getFont())) object.addProperty("font", style.getFont().toString());
    }

    private static void writeFlag(JsonObject object, String name, boolean set, boolean stillSetOverTrue) {
        if (set) object.addProperty(name, true);
        else if (!stillSetOverTrue) object.addProperty(name, false);
    }

    private static Style readStyle(JsonObject object) {
        Style style = Style.EMPTY;
        if (object.has("color")) {
            TextColor color = TextColor.parseColor(object.get("color").getAsString()).result().orElse(null);
            if (color != null) style = style.withColor(color);
        }
        style = style.withBold(flag(object, "bold"));
        style = style.withItalic(flag(object, "italic"));
        style = style.withUnderlined(flag(object, "underlined"));
        style = style.withStrikethrough(flag(object, "strikethrough"));
        style = style.withObfuscated(flag(object, "obfuscated"));
        if (object.has("font")) {
            ResourceLocation font = ResourceLocation.tryParse(object.get("font").getAsString());
            if (font != null) style = style.withFont(font);
        }
        return style;
    }

    private static Boolean flag(JsonObject object, String name) {
        return object.has(name) ? object.get(name).getAsBoolean() : null;
    }

    private static void warnUnsupported(ComponentContents contents) {
        String kind = contents.getClass().getSimpleName();
        if (WARNED.add(kind)) {
            PeakLog.warn("Component content {} is not supported and is sent as its plain text", kind);
        }
    }

    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        return toTree(src);
    }

    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return fromTree(json);
    }
}
