package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.serialize.PeakComponentValue;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PeakPlaceholderResolver {

    private static final Pattern PATTERN = Pattern.compile("\\{([\\w.]+):([\\w./-]+)(?:=([^#}]+))?(?:#([^}]+))?}");

    private PeakPlaceholderResolver() {}

    public static String resolve(String input) {
        if (input.indexOf('{') < 0) return input;
        Matcher matcher = PATTERN.matcher(input);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String namespace = matcher.group(1).toLowerCase(Locale.ROOT);
            String key = matcher.group(2);
            String rawArg = matcher.group(3);
            String rawDefault = matcher.group(4);
            matcher.appendReplacement(out, Matcher.quoteReplacement(PeakComponentValue.plain(resolvePlaceholder(namespace, key, rawArg, rawDefault))));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    public static PeakStyledText resolve(PeakStyledText input) {
        String plain = input.plain();
        if (plain.indexOf('{') < 0) return input;
        Matcher matcher = PATTERN.matcher(plain);
        PeakStyledText.Builder out = null;
        int last = 0;
        while (matcher.find()) {
            if (out == null) out = PeakStyledText.builder();
            out.append(input.slice(last, matcher.start()));
            String namespace = matcher.group(1).toLowerCase(Locale.ROOT);
            String replacement = resolvePlaceholder(namespace, matcher.group(2), matcher.group(3), matcher.group(4));
            if (PeakComponentValue.isEncoded(replacement)) {
                out.append(PeakStyledText.of(PeakComponentValue.decode(replacement), input.styleAt(matcher.start())));
            } else {
                out.appendLegacy(replacement, input.styleAt(matcher.start()));
            }
            last = matcher.end();
        }
        if (out == null) return input;
        out.append(input.slice(last, plain.length()));
        return out.build();
    }

    public record Result(String value, boolean complete) {}

    public static Result resolveStrict(String input) {
        if (input == null) return new Result(null, false);
        if (input.indexOf('{') < 0) return new Result(input, true);
        Matcher matcher = PATTERN.matcher(input);
        StringBuilder out = new StringBuilder();
        boolean complete = true;
        while (matcher.find()) {
            String namespace = matcher.group(1).toLowerCase(Locale.ROOT);
            String key = matcher.group(2);
            String rawDefault = matcher.group(4);
            Resolved resolved = resolveRaw(namespace, key, matcher.group(3));
            String replacement;
            if (resolved.unknown()) {
                if (rawDefault != null) {
                    replacement = rawDefault;
                } else {
                    complete = false;
                    replacement = "";
                }
            } else if (resolved.value().isEmpty() && rawDefault != null) {
                replacement = rawDefault;
            } else {
                replacement = resolved.value();
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(PeakComponentValue.plain(replacement)));
        }
        matcher.appendTail(out);
        return new Result(out.toString(), complete);
    }

    private static String resolvePlaceholder(String namespace, String key, String rawArg, String rawDefault) {
        Resolved resolved = resolveRaw(namespace, key, rawArg);

        if (resolved.unknown()) {
            return rawDefault != null ? rawDefault : "{" + namespace + ":" + key + "}";
        }
        if (resolved.value().isEmpty() && rawDefault != null) {
            return rawDefault;
        }
        return resolved.value();
    }

    private static Resolved resolveRaw(String namespace, String key, String rawArg) {
        return switch (namespace) {
            case "minecraft", "mc" -> resolveBuiltIn(key, rawArg);
            case "peakclient", "pc" -> Resolved.unknownValue();
            default -> PeakClientCustomValues.contains(namespace, key)
                    ? Resolved.value(PeakClientCustomValues.get(namespace, key))
                    : Resolved.unknownValue();
        };
    }

    private static Resolved resolveBuiltIn(String key, String rawArg) {
        String value = PeakBuiltInPlaceholders.resolve(key, rawArg);
        return value == null ? Resolved.unknownValue() : Resolved.value(value);
    }

    private record Resolved(String value, boolean unknown) {
        static Resolved value(String v) { return new Resolved(v, false); }
        static Resolved unknownValue() { return new Resolved("", true); }
    }
}
