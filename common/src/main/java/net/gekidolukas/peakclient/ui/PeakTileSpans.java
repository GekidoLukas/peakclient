package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;

public final class PeakTileSpans {
    public record Span(int destOffset, int srcOffset, int length) {}

    private PeakTileSpans() {}

    public static int count(int destSize, int srcSize) {
        if (destSize <= 0 || srcSize <= 0) return 0;
        return destSize / srcSize + 2;
    }

    public static List<Span> centered(int destSize, int srcSize) {
        List<Span> spans = new ArrayList<>();
        if (destSize <= 0 || srcSize <= 0) return spans;
        int first = Math.floorMod((destSize - srcSize) / 2, srcSize);
        int origin = first == 0 ? 0 : first - srcSize;
        for (; origin < destSize; origin += srcSize) {
            int visibleStart = Math.max(0, origin);
            int visibleEnd = Math.min(destSize, origin + srcSize);
            if (visibleEnd > visibleStart) spans.add(new Span(visibleStart, visibleStart - origin, visibleEnd - visibleStart));
        }
        return spans;
    }
}
