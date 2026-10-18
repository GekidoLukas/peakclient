package net.gekidolukas.peakclient.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

public final class PeakRichText {
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINE = 4;
    public static final int STRIKE = 8;
    public static final int OBFUSCATED = 16;

    public static final int[] LEGACY_COLORS = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    private static final String LEGACY_CODES = "0123456789abcdef";
    private static final int[] FLAG_ORDER = { BOLD, STRIKE, UNDERLINE, ITALIC, OBFUSCATED };
    private static final char[] FLAG_CODES = { 'l', 'm', 'n', 'o', 'k' };

    public record Style(int flags, int color) {
        public static final Style NONE = new Style(0, 0);

        public boolean has(int flag) { return (flags & flag) != 0; }
        public Style with(int flag) { return new Style(flags | flag, color); }
        public Style without(int flag) { return new Style(flags & ~flag, color); }
        public Style toggled(int flag) { return has(flag) ? without(flag) : with(flag); }
        public Style withColor(int rgb) { return new Style(flags, rgb == 0 ? 0 : (0xFF000000 | rgb)); }
        public int rgb() { return color & 0xFFFFFF; }
    }

    public record Run(int start, int end, Style style) {}

    private final StringBuilder chars = new StringBuilder();
    private final List<Style> styles = new ArrayList<>();

    public static PeakRichText parse(String legacy) {
        PeakRichText result = new PeakRichText();
        if (legacy == null) return result;
        Style current = Style.NONE;
        int i = 0;
        while (i < legacy.length()) {
            char character = legacy.charAt(i);
            if (character == '§' && i + 1 < legacy.length()) {
                char code = Character.toLowerCase(legacy.charAt(i + 1));
                if (code == 'x' && isHexSequence(legacy, i)) {
                    current = new Style(0, 0xFF000000 | parseHex(legacy, i));
                    i += 14;
                    continue;
                }
                int legacyIndex = LEGACY_CODES.indexOf(code);
                if (legacyIndex >= 0) {
                    current = new Style(0, 0xFF000000 | LEGACY_COLORS[legacyIndex]);
                } else if (code == 'r') {
                    current = Style.NONE;
                } else {
                    int flag = flagOf(code);
                    if (flag != 0) current = current.with(flag);
                }
                i += 2;
                continue;
            }
            result.chars.append(character);
            result.styles.add(current);
            i++;
        }
        return result;
    }

    public int length() { return chars.length(); }

    public char charAt(int index) { return chars.charAt(index); }

    public Style styleAt(int index) { return styles.get(index); }

    public String plain() { return chars.toString(); }

    public String plain(int from, int to) { return chars.substring(from, to); }

    public PeakRichText slice(int from, int to) {
        int start = Math.max(0, Math.min(from, length()));
        int end = Math.max(start, Math.min(to, length()));
        PeakRichText result = new PeakRichText();
        result.chars.append(chars, start, end);
        result.styles.addAll(styles.subList(start, end));
        return result;
    }

    public void insert(int index, String plain, Style style) {
        chars.insert(index, plain);
        styles.addAll(index, Collections.nCopies(plain.length(), style));
    }

    public void delete(int from, int to) {
        if (to <= from) return;
        chars.delete(from, to);
        styles.subList(from, to).clear();
    }

    public void map(int from, int to, UnaryOperator<Style> operator) {
        for (int i = Math.max(0, from); i < Math.min(to, styles.size()); i++) {
            styles.set(i, operator.apply(styles.get(i)));
        }
    }

    public List<Run> runs(int from, int to) {
        List<Run> runs = new ArrayList<>();
        int start = from;
        while (start < to) {
            Style style = styles.get(start);
            int end = start + 1;
            while (end < to && styles.get(end).equals(style)) end++;
            runs.add(new Run(start, end, style));
            start = end;
        }
        return runs;
    }

    public String serialize() {
        return serialize(0, length());
    }

    public String serialize(int from, int to) {
        StringBuilder out = new StringBuilder();
        Style current = Style.NONE;
        for (int i = Math.max(0, from); i < Math.min(to, length()); i++) {
            Style style = styles.get(i);
            if (!style.equals(current)) {
                appendTransition(out, current, style);
                current = style;
            }
            out.append(chars.charAt(i));
        }
        return out.toString();
    }

    public static String flagCodesOf(Style style) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < FLAG_ORDER.length; i++) {
            if (style.has(FLAG_ORDER[i])) out.append('§').append(FLAG_CODES[i]);
        }
        return out.toString();
    }

    private static void appendTransition(StringBuilder out, Style current, Style next) {
        if (!current.equals(Style.NONE) && next.color() == 0) out.append("§r");
        appendCodes(out, next);
    }

    private static void appendCodes(StringBuilder out, Style style) {
        if (style.color() != 0) appendColor(out, style.rgb());
        for (int i = 0; i < FLAG_ORDER.length; i++) {
            if (style.has(FLAG_ORDER[i])) out.append('§').append(FLAG_CODES[i]);
        }
    }

    private static void appendColor(StringBuilder out, int rgb) {
        for (int i = 0; i < LEGACY_COLORS.length; i++) {
            if (LEGACY_COLORS[i] == rgb) {
                out.append('§').append(LEGACY_CODES.charAt(i));
                return;
            }
        }
        out.append("§x");
        String hex = String.format(Locale.ROOT, "%06x", rgb);
        for (int i = 0; i < hex.length(); i++) out.append('§').append(hex.charAt(i));
    }

    private static int flagOf(char code) {
        for (int i = 0; i < FLAG_CODES.length; i++) {
            if (FLAG_CODES[i] == code) return FLAG_ORDER[i];
        }
        return 0;
    }

    private static boolean isHexSequence(String text, int start) {
        if (start + 13 >= text.length()) return false;
        for (int digit = 0; digit < 6; digit++) {
            int separatorIndex = start + 2 + digit * 2;
            if (text.charAt(separatorIndex) != '§') return false;
            if (Character.digit(text.charAt(separatorIndex + 1), 16) < 0) return false;
        }
        return true;
    }

    private static int parseHex(String text, int start) {
        StringBuilder hex = new StringBuilder();
        for (int digit = 0; digit < 6; digit++) hex.append(text.charAt(start + 3 + digit * 2));
        return Integer.parseInt(hex.toString(), 16);
    }
}
