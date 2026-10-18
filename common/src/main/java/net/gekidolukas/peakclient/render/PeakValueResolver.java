package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakValueResolver {
    private static final int LOCATION_CACHE_LIMIT = 256;
    private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();
    private static final Map<String, Optional<ResourceLocation>> LOCATIONS = new ConcurrentHashMap<>();

    private PeakValueResolver() {}

    public static String string(String raw) {
        if (raw == null) return null;
        PeakPlaceholderResolver.Result result = PeakPlaceholderResolver.resolveStrict(raw);
        if (!result.complete()) {
            warnOnce("unresolved placeholder in '" + raw + "'");
            return null;
        }
        return result.value();
    }

    public static ResourceLocation location(String raw) {
        String resolved = string(raw);
        if (resolved == null || resolved.isEmpty()) return null;
        if (LOCATIONS.size() > LOCATION_CACHE_LIMIT) LOCATIONS.clear();
        Optional<ResourceLocation> location = LOCATIONS.computeIfAbsent(resolved, value -> {
            ResourceLocation parsed = ResourceLocation.tryParse(value);
            if (parsed == null) warnOnce("invalid resource location '" + value + "'");
            return Optional.ofNullable(parsed);
        });
        return location.orElse(null);
    }

    public static ResourceLocation requireLocation(String raw) {
        ResourceLocation location = location(raw);
        if (location == null) throw new IllegalArgumentException("Unresolved resource location");
        return location;
    }

    public static UUID uuid(String raw) {
        String resolved = string(raw);
        if (resolved == null || resolved.isEmpty()) return null;
        try {
            return UUID.fromString(resolved.trim());
        } catch (IllegalArgumentException e) {
            warnOnce("invalid UUID '" + resolved + "'");
            return null;
        }
    }

    public static Boolean parseBoolean(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "on" -> Boolean.TRUE;
            case "false", "0", "no", "off" -> Boolean.FALSE;
            default -> null;
        };
    }

    public static Long parseLong(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            Double parsed = parseDouble(value);
            return parsed == null ? null : Math.round(parsed);
        }
    }

    public static Double parseDouble(String value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseColor(String value) {
        if (value == null) return null;
        String text = value.trim();
        try {
            if (text.startsWith("#")) {
                String hex = text.substring(1);
                if (hex.length() == 6) return 0xFF000000 | Integer.parseUnsignedInt(hex, 16);
                if (hex.length() == 8) return (int) Long.parseLong(hex, 16);
                return null;
            }
            if (text.startsWith("0x") || text.startsWith("0X")) {
                return (int) Long.parseLong(text.substring(2), 16);
            }
            return (int) Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void warnOnce(String message) {
        if (LOGGED.add(message)) PeakLog.warn("Placeholder value problem: {}", message);
    }

    public static void clearState() {
        LOGGED.clear();
        LOCATIONS.clear();
    }
}
