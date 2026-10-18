package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PeakLabelText {
    public record NumberSpan(int start, int end, double value, int decimals, boolean explicitPlus) {}

    private PeakLabelText() {}

    public static NumberSpan findNumber(String text) {
        int index = 0;
        while (index < text.length()) {
            char character = text.charAt(index);
            if (character == '§' && index + 1 < text.length()) {
                index += codeLength(text, index);
                continue;
            }
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

    public static String replaceNumber(String text, NumberSpan span, double value) {
        String formatted = span.decimals() > 0
                ? String.format(Locale.ROOT, "%." + span.decimals() + "f", value)
                : Long.toString(Math.round(value));
        if (span.explicitPlus() && !formatted.startsWith("-")) formatted = "+" + formatted;
        return text.substring(0, span.start()) + formatted + text.substring(span.end());
    }

    public static String skeleton(String text, NumberSpan span) {
        return text.substring(0, span.start()) + "#" + text.substring(span.end());
    }

    public static int visibleLength(String text) {
        int count = 0;
        int index = 0;
        while (index < text.length()) {
            if (text.charAt(index) == '§' && index + 1 < text.length()) {
                index += codeLength(text, index);
                continue;
            }
            count++;
            index++;
        }
        return count;
    }

    public static String truncateVisible(String text, int count) {
        if (count <= 0) return "";
        int visible = 0;
        int index = 0;
        while (index < text.length()) {
            if (text.charAt(index) == '§' && index + 1 < text.length()) {
                index += codeLength(text, index);
                continue;
            }
            visible++;
            index++;
            if (visible >= count) return text.substring(0, index);
        }
        return text;
    }

    public static List<String> truncateLines(List<String> lines, int count) {
        List<String> result = new ArrayList<>(lines.size());
        int remaining = Math.max(0, count);
        for (String line : lines) {
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

    private static int codeLength(String text, int index) {
        if (text.charAt(index + 1) == 'x' && isHexSequence(text, index)) return 14;
        return 2;
    }

    private static boolean isHexSequence(String text, int start) {
        if (start + 13 >= text.length()) return false;
        for (int digit = 0; digit < 6; digit++) {
            if (text.charAt(start + 2 + digit * 2) != '§') return false;
        }
        return true;
    }

    private static boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }
}
