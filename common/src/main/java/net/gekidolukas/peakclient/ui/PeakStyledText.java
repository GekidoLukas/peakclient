package net.gekidolukas.peakclient.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class PeakStyledText {
    public static final PeakStyledText EMPTY = new PeakStyledText("", new Style[0]);

    private final String plain;
    private final Style[] styles;

    private PeakStyledText(String plain, Style[] styles) {
        this.plain = plain;
        this.styles = styles;
    }

    public static PeakStyledText of(Component component) {
        if (component == null) return EMPTY;
        Builder builder = new Builder();
        component.visit((style, text) -> {
            builder.appendLegacy(text, sanitize(style));
            return Optional.empty();
        }, Style.EMPTY);
        return builder.build();
    }

    public static PeakStyledText of(Component component, Style base) {
        if (component == null) return EMPTY;
        Builder builder = new Builder();
        component.visit((style, text) -> {
            builder.appendLegacy(text, sanitize(style));
            return Optional.empty();
        }, sanitize(base));
        return builder.build();
    }

    public static PeakStyledText ofLegacy(String text) {
        if (text == null || text.isEmpty()) return EMPTY;
        return new Builder().appendLegacy(text, Style.EMPTY).build();
    }

    public static PeakStyledText of(String text, Style style) {
        if (text == null || text.isEmpty()) return EMPTY;
        return new Builder().appendRaw(text, style).build();
    }

    public static Style sanitize(Style style) {
        return style.withClickEvent(null).withHoverEvent(null).withInsertion(null);
    }

    public String plain() {
        return plain;
    }

    public int length() {
        return plain.length();
    }

    public boolean isEmpty() {
        return plain.isEmpty();
    }

    public Style styleAt(int index) {
        return styles[index];
    }

    public PeakStyledText slice(int from, int to) {
        int start = Math.max(0, from);
        int end = Math.min(plain.length(), to);
        if (start >= end) return EMPTY;
        if (start == 0 && end == plain.length()) return this;
        return new PeakStyledText(plain.substring(start, end), Arrays.copyOfRange(styles, start, end));
    }

    public PeakStyledText concat(PeakStyledText other) {
        if (other.isEmpty()) return this;
        if (isEmpty()) return other;
        Style[] merged = Arrays.copyOf(styles, styles.length + other.styles.length);
        System.arraycopy(other.styles, 0, merged, styles.length, other.styles.length);
        return new PeakStyledText(plain + other.plain, merged);
    }

    public List<PeakStyledText> splitLines() {
        List<PeakStyledText> lines = new ArrayList<>();
        int start = 0;
        for (int index = 0; index < plain.length(); index++) {
            if (plain.charAt(index) == '\n') {
                lines.add(slice(start, index));
                start = index + 1;
            }
        }
        lines.add(slice(start, plain.length()));
        return lines;
    }

    public FormattedCharSequence sequence() {
        return sink -> {
            int index = 0;
            while (index < plain.length()) {
                char character = plain.charAt(index);
                int codepoint = character;
                int step = 1;
                if (Character.isHighSurrogate(character) && index + 1 < plain.length()
                        && Character.isLowSurrogate(plain.charAt(index + 1))) {
                    codepoint = Character.toCodePoint(character, plain.charAt(index + 1));
                    step = 2;
                }
                if (!sink.accept(index, styles[index], codepoint)) return false;
                index += step;
            }
            return true;
        };
    }

    public int width(Font font) {
        return isEmpty() ? 0 : font.width(sequence());
    }

    public PeakStyledText trimToWidth(Font font, int maxWidth) {
        if (width(font) <= maxWidth) return this;
        int low = 0;
        int high = plain.length();
        while (low < high) {
            int middle = (low + high + 1) / 2;
            if (slice(0, middle).width(font) <= maxWidth) low = middle;
            else high = middle - 1;
        }
        return slice(0, low);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PeakStyledText text && plain.equals(text.plain) && Arrays.equals(styles, text.styles);
    }

    @Override
    public int hashCode() {
        return 31 * plain.hashCode() + Arrays.hashCode(styles);
    }

    @Override
    public String toString() {
        return plain;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final StringBuilder plain = new StringBuilder();
        private final List<Style> styles = new ArrayList<>();

        public Builder append(PeakStyledText text) {
            plain.append(text.plain);
            for (Style style : text.styles) styles.add(style);
            return this;
        }

        public Builder appendRaw(String text, Style style) {
            for (int index = 0; index < text.length(); index++) {
                plain.append(text.charAt(index));
                styles.add(style);
            }
            return this;
        }

        public Builder appendLegacy(String text, Style base) {
            Style current = base;
            int length = text.length();
            int index = 0;
            while (index < length) {
                char character = text.charAt(index);
                if (character == '§' && index + 1 < length) {
                    char code = text.charAt(index + 1);
                    if (code == 'x' && isHexSequence(text, index)) {
                        current = base.withColor(parseHexSequence(text, index));
                        index += 14;
                        continue;
                    }
                    ChatFormatting formatting = ChatFormatting.getByCode(code);
                    if (formatting == ChatFormatting.RESET) {
                        current = base;
                    } else if (formatting != null && formatting.isColor()) {
                        current = base.withColor(formatting);
                    } else if (formatting != null) {
                        current = current.applyFormat(formatting);
                    }
                    index += 2;
                    continue;
                }
                plain.append(character);
                styles.add(current);
                index++;
            }
            return this;
        }

        public PeakStyledText build() {
            if (plain.length() == 0) return EMPTY;
            return new PeakStyledText(plain.toString(), styles.toArray(new Style[0]));
        }
    }

    private static boolean isHexSequence(String text, int start) {
        for (int digit = 0; digit < 6; digit++) {
            int separatorIndex = start + 2 + digit * 2;
            if (separatorIndex + 1 >= text.length() || text.charAt(separatorIndex) != '§') return false;
            if (Character.digit(text.charAt(separatorIndex + 1), 16) < 0) return false;
        }
        return true;
    }

    private static int parseHexSequence(String text, int start) {
        int value = 0;
        for (int digit = 0; digit < 6; digit++) value = value * 16 + Character.digit(text.charAt(start + 3 + digit * 2), 16);
        return value;
    }
}
