package net.gekidolukas.peakclient.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PeakTextLayout {
    public interface Measure {
        int width(int from, int to);
    }

    public record Line(int start, int end) {}

    private final List<Line> lines;

    private PeakTextLayout(List<Line> lines) {
        this.lines = Collections.unmodifiableList(lines);
    }

    public static PeakTextLayout build(PeakRichText text, int maxWidth, Measure measure) {
        List<Line> lines = new ArrayList<>();
        int length = text.length();
        int paragraphStart = 0;
        for (int i = 0; i <= length; i++) {
            if (i == length || text.charAt(i) == '\n') {
                addParagraph(lines, text, paragraphStart, i, maxWidth, measure);
                paragraphStart = i + 1;
            }
        }
        return new PeakTextLayout(lines);
    }

    private static void addParagraph(List<Line> lines, PeakRichText text, int start, int end, int maxWidth, Measure measure) {
        if (start >= end) {
            lines.add(new Line(start, start));
            return;
        }
        int lineStart = start;
        while (true) {
            if (maxWidth <= 0 || measure.width(lineStart, end) <= maxWidth) {
                lines.add(new Line(lineStart, end));
                return;
            }
            int fit = largestFit(lineStart, end, maxWidth, measure);
            int lineEnd = fit;
            if (fit < end) {
                for (int breakAt = fit; breakAt > lineStart; breakAt--) {
                    if (text.charAt(breakAt) == ' ') {
                        lineEnd = breakAt + 1;
                        break;
                    }
                }
            }
            if (lineEnd >= end) {
                lines.add(new Line(lineStart, end));
                return;
            }
            lines.add(new Line(lineStart, lineEnd));
            lineStart = lineEnd;
        }
    }

    private static int largestFit(int start, int end, int maxWidth, Measure measure) {
        int low = start + 1;
        int high = end;
        int best = start + 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (measure.width(start, mid) <= maxWidth) {
                best = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return best;
    }

    public List<Line> lines() { return lines; }

    public int lineCount() { return lines.size(); }

    public Line line(int index) { return lines.get(Math.max(0, Math.min(index, lines.size() - 1))); }

    public int lineOf(int index) {
        int low = 0;
        int high = lines.size() - 1;
        int result = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (lines.get(mid).start() <= index) {
                result = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    public int xOf(int index, Measure measure) {
        Line line = lines.get(lineOf(index));
        return measure.width(line.start(), Math.max(line.start(), Math.min(index, line.end())));
    }

    public int charIndexAt(int lineIndex, int x, Measure measure) {
        Line line = line(lineIndex);
        if (x < 0) return -1;
        for (int i = line.start(); i < line.end(); i++) {
            if (x < measure.width(line.start(), i + 1)) return i;
        }
        return -1;
    }

    public int indexAt(int lineIndex, int x, Measure measure) {
        Line line = line(lineIndex);
        int best = line.start();
        int bestDistance = Math.abs(x);
        for (int i = line.start() + 1; i <= line.end(); i++) {
            int width = measure.width(line.start(), i);
            int distance = Math.abs(x - width);
            if (distance < bestDistance) {
                best = i;
                bestDistance = distance;
            }
            if (width > x) break;
        }
        return best;
    }
}
