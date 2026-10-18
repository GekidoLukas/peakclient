package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;

public final class PeakTableGrid {
    public record Span(int columnSpan, int rowSpan) {}

    public record Cell(int columnSpan, int rowSpan, int width, int height) {}

    public record Placement(int row, int column, int columnSpan, int rowSpan) {}

    public record Result(int[] columnWidths, int[] rowHeights, List<Placement> placements,
                         int columnSpacing, int rowSpacing) {
        public int columns() { return columnWidths.length; }

        public int rows() { return rowHeights.length; }

        public int contentWidth() {
            return sum(columnWidths, 0, columnWidths.length) + Math.max(0, columnWidths.length - 1) * columnSpacing;
        }

        public int contentHeight() {
            return sum(rowHeights, 0, rowHeights.length) + Math.max(0, rowHeights.length - 1) * rowSpacing;
        }

        public int columnStart(int column) {
            return sum(columnWidths, 0, column) + column * columnSpacing;
        }

        public int rowStart(int row) {
            return sum(rowHeights, 0, row) + row * rowSpacing;
        }

        public boolean rowBoundaryBlocked(int rowAbove, int column) {
            for (Placement placement : placements) {
                if (placement.rowSpan() > 1 && placement.row() <= rowAbove
                        && placement.row() + placement.rowSpan() > rowAbove + 1
                        && placement.column() <= column && column < placement.column() + placement.columnSpan()) {
                    return true;
                }
            }
            return false;
        }

        public boolean columnBoundaryBlocked(int columnLeft, int row) {
            for (Placement placement : placements) {
                if (placement.columnSpan() > 1 && placement.column() <= columnLeft
                        && placement.column() + placement.columnSpan() > columnLeft + 1
                        && placement.row() <= row && row < placement.row() + placement.rowSpan()) {
                    return true;
                }
            }
            return false;
        }

        public int spanWidth(int column, int span) {
            int end = Math.min(columnWidths.length, column + span);
            return sum(columnWidths, column, end) + Math.max(0, end - column - 1) * columnSpacing;
        }

        public int spanHeight(int row, int span) {
            int end = Math.min(rowHeights.length, row + span);
            return sum(rowHeights, row, end) + Math.max(0, end - row - 1) * rowSpacing;
        }
    }

    private PeakTableGrid() {}

    public static PeakAlign resolveAlign(PeakAlign self, PeakAlign track, PeakAlign table) {
        if (self != null && self != PeakAlign.AUTO) return self;
        if (track != null && track != PeakAlign.AUTO) return track;
        if (table != null && table != PeakAlign.AUTO) return table;
        return PeakAlign.START;
    }

    public static int alignOffset(PeakAlign align, int free) {
        if (free <= 0) return 0;
        return switch (align) {
            case CENTER -> free / 2;
            case END -> free;
            case START, AUTO -> 0;
        };
    }

    public static List<Placement> place(List<Span> spans, int columns) {
        return flow(spans, columns).placements();
    }

    public static int trailingFreeSlots(List<Span> spans, int columns) {
        int columnCount = Math.max(1, columns);
        Flow flow = flow(spans, columnCount);
        if (flow.placements().isEmpty() || flow.column() >= columnCount) return 0;
        boolean[] slots = flow.row() < flow.occupied().size() ? flow.occupied().get(flow.row()) : new boolean[columnCount];
        int free = 0;
        for (int column = flow.column(); column < columnCount; column++) {
            if (!slots[column]) free++;
        }
        return free;
    }

    private record Flow(List<Placement> placements, List<boolean[]> occupied, int row, int column) {}

    private static Flow flow(List<Span> spans, int columns) {
        int columnCount = Math.max(1, columns);
        List<boolean[]> occupied = new ArrayList<>();
        List<Placement> placements = new ArrayList<>();
        int row = 0;
        int column = 0;

        for (Span span : spans) {
            int columnSpan = Math.max(1, Math.min(span.columnSpan(), columnCount));
            int rowSpan = Math.max(1, span.rowSpan());
            while (true) {
                if (column + columnSpan > columnCount) {
                    row++;
                    column = 0;
                    continue;
                }
                if (isFree(occupied, row, column, columnSpan, rowSpan)) break;
                column++;
            }
            for (int spanRow = row; spanRow < row + rowSpan; spanRow++) {
                boolean[] rowSlots = rowAt(occupied, spanRow, columnCount);
                for (int spanColumn = column; spanColumn < column + columnSpan; spanColumn++) rowSlots[spanColumn] = true;
            }
            placements.add(new Placement(row, column, columnSpan, rowSpan));
            column += columnSpan;
        }
        return new Flow(placements, occupied, row, column);
    }

    private static boolean isFree(List<boolean[]> occupied, int row, int column, int columnSpan, int rowSpan) {
        for (int spanRow = row; spanRow < row + rowSpan; spanRow++) {
            if (spanRow >= occupied.size()) continue;
            boolean[] slots = occupied.get(spanRow);
            for (int spanColumn = column; spanColumn < column + columnSpan; spanColumn++) {
                if (slots[spanColumn]) return false;
            }
        }
        return true;
    }

    private static boolean[] rowAt(List<boolean[]> occupied, int row, int columns) {
        while (occupied.size() <= row) occupied.add(new boolean[columns]);
        return occupied.get(row);
    }

    public static Result solve(List<PeakTrack> columnTracks, List<PeakTrack> rowTracks, PeakTrack rowDefault,
                               List<Cell> cells, int columnSpacing, int rowSpacing,
                               int availableWidth, int availableHeight, int minColumnWidth) {
        int columns = Math.max(1, columnTracks.size());
        List<Span> spans = new ArrayList<>();
        for (Cell cell : cells) spans.add(new Span(cell.columnSpan(), cell.rowSpan()));
        List<Placement> placements = place(spans, columns);

        int rows = 0;
        for (Placement placement : placements) rows = Math.max(rows, placement.row() + placement.rowSpan());

        List<PeakTrack> columnList = new ArrayList<>(columns);
        for (int column = 0; column < columns; column++) {
            columnList.add(column < columnTracks.size() ? columnTracks.get(column) : PeakTrack.auto());
        }
        List<PeakTrack> rowList = new ArrayList<>(rows);
        for (int row = 0; row < rows; row++) {
            rowList.add(row < rowTracks.size() ? rowTracks.get(row) : (rowDefault == null ? PeakTrack.auto() : rowDefault));
        }

        int[] naturalWidths = new int[columns];
        int[] naturalHeights = new int[rows];
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            Placement placement = placements.get(i);
            if (placement.columnSpan() == 1) {
                naturalWidths[placement.column()] = Math.max(naturalWidths[placement.column()], cell.width());
            }
            if (placement.rowSpan() == 1) {
                naturalHeights[placement.row()] = Math.max(naturalHeights[placement.row()], cell.height());
            }
        }
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            Placement placement = placements.get(i);
            if (placement.columnSpan() > 1) {
                spread(naturalWidths, columnList, placement.column(), placement.columnSpan(), cell.width(), columnSpacing);
            }
            if (placement.rowSpan() > 1) {
                spread(naturalHeights, rowList, placement.row(), placement.rowSpan(), cell.height(), rowSpacing);
            }
        }

        int[] widths = resolve(columnList, naturalWidths, availableWidth, columnSpacing, minColumnWidth);
        int[] heights = resolve(rowList, naturalHeights, availableHeight, rowSpacing, 0);
        return new Result(widths, heights, placements, columnSpacing, rowSpacing);
    }

    private static void spread(int[] natural, List<PeakTrack> tracks, int start, int span, int needed, int spacing) {
        int end = Math.min(natural.length, start + span);
        int current = Math.max(0, end - start - 1) * spacing;
        for (int i = start; i < end; i++) {
            PeakTrack track = tracks.get(i);
            current += track.getKind() == PeakTrack.Kind.PIXELS ? Math.round(track.getValue()) : natural[i];
        }
        int excess = needed - current;
        if (excess <= 0) return;

        List<Integer> growable = new ArrayList<>();
        for (int i = start; i < end; i++) {
            PeakTrack.Kind kind = tracks.get(i).getKind();
            if (kind == PeakTrack.Kind.AUTO || kind == PeakTrack.Kind.FILL) growable.add(i);
        }
        if (growable.isEmpty()) return;

        int each = excess / growable.size();
        int remainder = excess - each * growable.size();
        for (int growIndex = 0; growIndex < growable.size(); growIndex++) {
            natural[growable.get(growIndex)] += each + (growIndex < remainder ? 1 : 0);
        }
    }

    private static int[] resolve(List<PeakTrack> tracks, int[] natural, int available, int spacing, int minAuto) {
        int count = natural.length;
        int[] sizes = new int[count];
        boolean[] fill = new boolean[count];
        boolean hasFill = false;

        for (int i = 0; i < count; i++) {
            PeakTrack track = tracks.get(i);
            int floor = track.getMin();
            switch (track.getKind()) {
                case PIXELS -> sizes[i] = Math.round(track.getValue());
                case PERCENT -> sizes[i] = available >= 0
                        ? Math.round(available * track.getValue() / 100f)
                        : Math.max(natural[i], minAuto);
                case AUTO -> sizes[i] = Math.max(natural[i], minAuto);
                case FILL -> {
                    sizes[i] = Math.max(natural[i], minAuto);
                    fill[i] = true;
                    hasFill = true;
                }
            }
            if (!fill[i]) sizes[i] = clamp(sizes[i], floor, track.getMax());
        }

        if (hasFill && available >= 0) {
            int pool = available - Math.max(0, count - 1) * spacing;
            for (int i = 0; i < count; i++) {
                if (!fill[i]) pool -= sizes[i];
            }
            boolean[] settled = new boolean[count];
            for (int pass = 0; pass <= count; pass++) {
                float weightSum = 0f;
                int poolNow = pool;
                for (int i = 0; i < count; i++) {
                    if (fill[i] && !settled[i]) weightSum += tracks.get(i).getValue();
                    else if (fill[i]) poolNow -= sizes[i];
                }
                if (weightSum <= 0f) break;

                boolean changed = false;
                for (int i = 0; i < count; i++) {
                    if (!fill[i] || settled[i]) continue;
                    float share = poolNow * tracks.get(i).getValue() / weightSum;
                    int minimum = Math.max(natural[i], Math.max(minAuto, tracks.get(i).getMin()));
                    if (share < minimum) {
                        sizes[i] = clamp(minimum, tracks.get(i).getMin(), tracks.get(i).getMax());
                        settled[i] = true;
                        changed = true;
                        break;
                    }
                }
                if (changed) continue;

                float weightLeft = weightSum;
                int poolLeft = poolNow;
                for (int i = 0; i < count; i++) {
                    if (!fill[i] || settled[i]) continue;
                    float weight = tracks.get(i).getValue();
                    int share = Math.max(0, Math.round(poolLeft * weight / weightLeft));
                    poolLeft -= share;
                    weightLeft -= weight;
                    sizes[i] = clamp(share, tracks.get(i).getMin(), tracks.get(i).getMax());
                }
                break;
            }
        } else if (hasFill) {
            for (int i = 0; i < count; i++) {
                if (fill[i]) sizes[i] = clamp(sizes[i], tracks.get(i).getMin(), tracks.get(i).getMax());
            }
        }
        return sizes;
    }

    private static int clamp(int value, int min, int max) {
        int result = value;
        if (min > 0) result = Math.max(result, min);
        if (max > 0) result = Math.min(result, max);
        return result;
    }

    private static int sum(int[] values, int from, int to) {
        int total = 0;
        for (int i = Math.max(0, from); i < Math.min(to, values.length); i++) total += values[i];
        return total;
    }
}
