package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PeakFlowLines {
    private PeakFlowLines() {}

    public record Item(int main, int cross, boolean breakBefore) {
        public Item(int main, int cross) {
            this(main, cross, false);
        }
    }

    public record Line(List<Integer> indices, int main, int cross) {}

    public record Placement(int index, int mainOffset, int crossOffset) {}

    public record Result(List<Line> lines, List<Placement> placements, int contentMain, int contentCross) {
        public int shownCount() {
            int count = 0;
            for (Line line : lines) count += line.indices().size();
            return count;
        }
    }

    public static List<Line> wrap(List<Item> items, int limit, int gap, int maxLines) {
        List<Line> lines = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        int currentMain = 0, currentCross = 0;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean full = !current.isEmpty()
                    && (item.breakBefore() || (limit >= 0 && currentMain + gap + item.main() > limit));
            if (full) {
                lines.add(new Line(current, currentMain, currentCross));
                current = new ArrayList<>();
                currentMain = 0;
                currentCross = 0;
            }
            currentMain += (current.isEmpty() ? 0 : gap) + item.main();
            currentCross = Math.max(currentCross, item.cross());
            current.add(i);
        }
        if (!current.isEmpty()) lines.add(new Line(current, currentMain, currentCross));
        if (maxLines > 0 && lines.size() > maxLines) return new ArrayList<>(lines.subList(0, maxLines));
        return lines;
    }

    public static Result layout(List<Item> items, int limit, int boxMain, int boxCross, int gap, int lineGap, int maxLines,
                                PeakMainAxisAlign mainAlign, PeakAlign crossAlign, PeakMainAxisAlign lineAlign,
                                boolean reverse) {
        List<Line> lines = wrap(items, limit, gap, maxLines);
        if (reverse) {
            List<Line> mirrored = new ArrayList<>();
            for (Line line : lines) {
                List<Integer> indices = new ArrayList<>(line.indices());
                Collections.reverse(indices);
                mirrored.add(new Line(indices, line.main(), line.cross()));
            }
            Collections.reverse(mirrored);
            lines = mirrored;
        }

        int contentMain = 0, contentCross = 0;
        for (Line line : lines) {
            contentMain = Math.max(contentMain, line.main());
            contentCross += line.cross();
        }
        contentCross += Math.max(0, lines.size() - 1) * lineGap;

        int availableMain = boxMain < 0 ? contentMain : boxMain;
        int availableCross = boxCross < 0 ? contentCross : boxCross;

        int crossLeftover = Math.max(0, availableCross - contentCross);
        int[] lineSpread = spread(lineAlign, crossLeftover, lines.size());
        int crossCursor = lineSpread[0];

        List<Placement> placements = new ArrayList<>();
        for (Line line : lines) {
            int mainLeftover = Math.max(0, availableMain - line.main());
            int[] mainSpread = spread(mainAlign, mainLeftover, line.indices().size());
            int mainCursor = mainSpread[0];
            for (int slot = 0; slot < line.indices().size(); slot++) {
                int index = line.indices().get(slot);
                Item item = items.get(index);
                int crossOffset = switch (crossAlign == PeakAlign.AUTO ? PeakAlign.START : crossAlign) {
                    case CENTER -> Math.max(0, (line.cross() - item.cross()) / 2);
                    case END -> Math.max(0, line.cross() - item.cross());
                    case START, AUTO -> 0;
                };
                placements.add(new Placement(index, mainCursor, crossCursor + crossOffset));
                mainCursor += item.main() + gap + mainSpread[1];
            }
            crossCursor += line.cross() + lineGap + lineSpread[1];
        }
        return new Result(lines, placements, contentMain, contentCross);
    }

    static int[] spread(PeakMainAxisAlign align, int leftover, int count) {
        return switch (align) {
            case START -> new int[]{0, 0};
            case CENTER -> new int[]{leftover / 2, 0};
            case END -> new int[]{leftover, 0};
            case SPACE_BETWEEN -> count > 1 ? new int[]{0, leftover / (count - 1)} : new int[]{leftover / 2, 0};
            case SPACE_AROUND -> {
                if (count <= 0) yield new int[]{0, 0};
                int edge = leftover / (2 * count);
                yield new int[]{edge, edge * 2};
            }
        };
    }
}
