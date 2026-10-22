package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class PeakTextWrap {
    private PeakTextWrap() {}

    public static List<PeakStyledText> layout(ToIntFunction<PeakStyledText> width, PeakStyledText text, int wrapWidth) {
        List<PeakStyledText> result = new ArrayList<>();
        for (PeakStyledText paragraph : text.splitLines()) {
            if (wrapWidth > 0) {
                result.addAll(wrap(width, paragraph, wrapWidth));
            } else {
                result.add(paragraph);
            }
        }
        if (result.isEmpty()) result.add(PeakStyledText.EMPTY);
        return result;
    }

    private static List<PeakStyledText> wrap(ToIntFunction<PeakStyledText> width, PeakStyledText text, int maxWidth) {
        List<PeakStyledText> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add(PeakStyledText.EMPTY);
            return lines;
        }

        int lineStart = 0;
        int lineEnd = 0;
        int position = 0;
        for (String word : text.plain().split(" ")) {
            int wordStart = position;
            int wordEnd = wordStart + word.length();
            position = wordEnd + 1;
            boolean hasContent = lineEnd > lineStart;
            if (!hasContent) {
                lineStart = wordStart;
                lineEnd = wordEnd;
            } else if (width.applyAsInt(text.slice(lineStart, wordEnd)) > maxWidth) {
                lines.add(text.slice(lineStart, lineEnd));
                lineStart = wordStart;
                lineEnd = wordEnd;
            } else {
                lineEnd = wordEnd;
            }
        }
        lines.add(text.slice(lineStart, lineEnd));
        return lines;
    }
}
