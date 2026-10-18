package net.gekidolukas.peakclient.ui;

public final class PeakDropdownLayout {
    public static final int SCROLLBAR_WIDTH = 4;
    public static final int TEXT_PADDING = 4;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final boolean above;
    private final int visibleRows;
    private final int firstRow;
    private final int count;
    private final int rowHeight;

    private PeakDropdownLayout(int x, int y, int width, int height, boolean above, int visibleRows, int firstRow,
                               int count, int rowHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.above = above;
        this.visibleRows = visibleRows;
        this.firstRow = firstRow;
        this.count = count;
        this.rowHeight = rowHeight;
    }

    public static PeakDropdownLayout of(int fieldX, int fieldY, int fieldWidth, int fieldHeight,
                                        int screenWidth, int screenHeight, int count, int maxVisible,
                                        int rowHeight, int widestText, int scrollRow) {
        int rows = Math.max(0, count);
        int wanted = Math.max(1, Math.min(rows == 0 ? 1 : rows, Math.max(1, maxVisible)));
        int below = screenHeight - (fieldY + fieldHeight);
        int aboveSpace = fieldY;
        int needed = wanted * rowHeight + 2;
        boolean placeAbove = needed > below && aboveSpace > below;
        int space = placeAbove ? aboveSpace : below;
        int fits = Math.max(1, (space - 2) / Math.max(1, rowHeight));
        int visible = Math.max(1, Math.min(wanted, fits));
        int height = visible * rowHeight + 2;

        boolean scrolls = rows > visible;
        int width = Math.max(fieldWidth, widestText + 2 * TEXT_PADDING + 2 + (scrolls ? SCROLLBAR_WIDTH + 2 : 0));
        width = Math.min(width, Math.max(fieldWidth, screenWidth));
        int left = fieldX;
        if (left + width > screenWidth) left = Math.max(0, screenWidth - width);
        int top = placeAbove ? fieldY - height : fieldY + fieldHeight;

        int maxFirst = Math.max(0, rows - visible);
        int first = Math.max(0, Math.min(scrollRow, maxFirst));
        return new PeakDropdownLayout(left, top, width, height, placeAbove, visible, first, rows, rowHeight);
    }

    public int x() { return x; }
    public int y() { return y; }
    public int width() { return width; }
    public int height() { return height; }
    public boolean above() { return above; }
    public int visibleRows() { return visibleRows; }
    public int firstRow() { return firstRow; }
    public int count() { return count; }
    public int rowHeight() { return rowHeight; }
    public boolean scrolls() { return count > visibleRows; }

    public boolean contains(int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    public int rowAt(int pointX, int pointY) {
        if (!contains(pointX, pointY)) return -1;
        int row = (pointY - y - 1) / Math.max(1, rowHeight);
        if (pointY - y - 1 < 0 || row >= visibleRows) return -1;
        int index = firstRow + row;
        return index < count ? index : -1;
    }

    public int rowY(int index) {
        return y + 1 + (index - firstRow) * rowHeight;
    }

    public int scrollToShow(int index) {
        if (index < 0) return firstRow;
        if (index < firstRow) return index;
        if (index >= firstRow + visibleRows) return index - visibleRows + 1;
        return firstRow;
    }

    public int maxFirstRow() {
        return Math.max(0, count - visibleRows);
    }
}
