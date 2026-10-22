package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PeakLabelText {
    public record NumberSpan(int start, int end, double value, int decimals, boolean explicitPlus) {}

    private PeakLabelText() {}

    public static NumberSpan findNumber(PeakStyledText styled) {
        String text = styled.plain();
        int index = 0;
        while (index < text.length()) {
            char character = text.charAt(index);
            boolean signed = (character == '-' || character == '+') && index + 1 < text.length() && isDigit(text.charAt(index + 1));
            if (signed || isDigit(character)) {
                int start = index;
                int cursor = signed ? index + 1 : index;
                while (cursor < text.length() && isDigit(text.charAt(cursor))) cursor++;
                int decimals = 0;
                if (cursor + 1 < text.length() && text.charAt(cursor) == '.' && isDigit(text.charAt(cursor + 1))) {
                    int fraction = cursor + 1;
                    while (fraction < text.length() && isDigit(text.charAt(fraction))) fraction++;
                    decimals = fraction - cursor - 1;
                    cursor = fraction;
                }
                double value;
                try {
                    value = Double.parseDouble(text.substring(signed && text.charAt(start) == '+' ? start + 1 : start, cursor));
                } catch (NumberFormatException e) {
                    index = cursor;
                    continue;
                }
                return new NumberSpan(start, cursor, value, decimals, signed && text.charAt(start) == '+');
            }
            index++;
        }
        return null;
    }

    public static PeakStyledText replaceNumber(PeakStyledText text, NumberSpan span, double value) {
        String formatted = span.decimals() > 0
                ? String.format(Locale.ROOT, "%." + span.decimals() + "f", value)
                : Long.toString(Math.round(value));
        if (span.explicitPlus() && !formatted.startsWith("-")) formatted = "+" + formatted;
        PeakStyledText number = PeakStyledText.of(formatted, text.styleAt(span.start()));
        return text.slice(0, span.start()).concat(number).concat(text.slice(span.end(), text.length()));
    }

    public static String skeleton(PeakStyledText text, NumberSpan span) {
        String plain = text.plain();
        return plain.substring(0, span.start()) + "#" + plain.substring(span.end());
    }

    public static int visibleLength(PeakStyledText text) {
        return text.length();
    }

    public static PeakStyledText truncateVisible(PeakStyledText text, int count) {
        if (count <= 0) return PeakStyledText.EMPTY;
        return text.slice(0, count);
    }

    public static List<PeakStyledText> truncateLines(List<PeakStyledText> lines, int count) {
        List<PeakStyledText> result = new ArrayList<>(lines.size());
        int remaining = Math.max(0, count);
        for (PeakStyledText line : lines) {
            int length = visibleLength(line);
            if (remaining >= length) {
                result.add(line);
                remaining -= length;
            } else {
                result.add(truncateVisible(line, remaining));
                remaining = 0;
            }
        }
        return result;
    }

    private static boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }
}
