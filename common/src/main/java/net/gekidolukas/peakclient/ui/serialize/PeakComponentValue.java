package net.gekidolukas.peakclient.ui.serialize;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;

public final class PeakComponentValue {
    private static final char MARKER = '\u0001';

    private PeakComponentValue() {}

    public static String encode(Component component) {
        return MARKER + PeakComponentAdapter.toTree(component == null ? Component.empty() : component).toString();
    }

    public static boolean isEncoded(String value) {
        return value != null && !value.isEmpty() && value.charAt(0) == MARKER;
    }

    public static Component decode(String value) {
        if (!isEncoded(value)) return Component.literal(value == null ? "" : value);
        try {
            return PeakComponentAdapter.fromTree(JsonParser.parseString(value.substring(1)));
        } catch (JsonParseException | IllegalStateException exception) {
            return Component.empty();
        }
    }

    public static String plain(String value) {
        return isEncoded(value) ? decode(value).getString() : value;
    }
}
