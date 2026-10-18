package net.gekidolukas.peakclient.ui;

public final class PeakRadioLayout {
    public static final int CIRCLE = 11;
    public static final int TEXT_GAP = 4;

    private final PeakRect[] cells;
    private final int width;
    private final int height;
    private final int rowHeight;

    private PeakRadioLayout(PeakRect[] cells, int width, int height, int rowHeight) {
        this.cells = cells;
        this.width = width;
        this.height = height;
        this.rowHeight = rowHeight;
    }

    public static PeakRadioLayout of(int[] textWidths, int lineHeight, boolean row, int gap) {
        int count = textWidths.length;
        int rowHeight = Math.max(CIRCLE, lineHeight);
        PeakRect[] cells = new PeakRect[count];
        if (count == 0) return new PeakRadioLayout(cells, 0, 0, rowHeight);

        int[] optionWidths = new int[count];
        int widest = 0;
        for (int i = 0; i < count; i++) {
            optionWidths[i] = CIRCLE + (textWidths[i] > 0 ? TEXT_GAP + textWidths[i] : 0);
            widest = Math.max(widest, optionWidths[i]);
        }

        if (row) {
            int x = 0;
            for (int i = 0; i < count; i++) {
                cells[i] = new PeakRect(x, 0, optionWidths[i], rowHeight);
                x += optionWidths[i] + gap;
            }
            return new PeakRadioLayout(cells, x - gap, rowHeight, rowHeight);
        }

        for (int i = 0; i < count; i++) {
            cells[i] = new PeakRect(0, i * (rowHeight + gap), widest, rowHeight);
        }
        return new PeakRadioLayout(cells, widest, count * rowHeight + (count - 1) * gap, rowHeight);
    }

    public int count() { return cells.length; }
    public PeakRect cell(int index) { return cells[index]; }
    public int width() { return width; }
    public int height() { return height; }
    public int rowHeight() { return rowHeight; }

    public int optionAt(int x, int y) {
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].contains(x, y)) return i;
        }
        return -1;
    }
}
