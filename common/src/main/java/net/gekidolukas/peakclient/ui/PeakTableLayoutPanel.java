package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PeakTableLayoutPanel extends PeakContainer<PeakTableLayoutPanel> {
    private List<PeakTrack> columns = new ArrayList<>(List.of(PeakTrack.auto()));
    private List<PeakTrack> rows = new ArrayList<>();
    private PeakTrack rowHeight = PeakTrack.auto();
    private int columnSpacing = -1;
    private int rowSpacing = -1;
    private int minColumnWidth = 0;
    private PeakInsets cellPadding = PeakInsets.all(0);
    private PeakAlign cellAlign = PeakAlign.AUTO;
    private PeakAlign cellVerticalAlign = PeakAlign.AUTO;
    private PeakTableStyle decor = new PeakTableStyle();

    public PeakTableLayoutPanel() { this(PeakId.random()); }
    public PeakTableLayoutPanel(String id) { super(id); }
    public static PeakTableLayoutPanel create() { return new PeakTableLayoutPanel(); }
    public static PeakTableLayoutPanel create(String id) { return new PeakTableLayoutPanel(id); }

    public PeakTableLayoutPanel columns(int count) {
        columns = new ArrayList<>();
        for (int i = 0; i < Math.max(1, count); i++) columns.add(PeakTrack.auto());
        return this;
    }

    public PeakTableLayoutPanel columns(PeakTrack... tracks) {
        columns = new ArrayList<>(List.of(tracks));
        if (columns.isEmpty()) columns.add(PeakTrack.auto());
        return this;
    }

    public PeakTableLayoutPanel columns(String specification) {
        List<PeakTrack> parsed = PeakTrack.parse(specification);
        columns = parsed.isEmpty() ? new ArrayList<>(List.of(PeakTrack.auto())) : new ArrayList<>(parsed);
        return this;
    }

    public PeakTableLayoutPanel columnTrack(int index, PeakTrack track) {
        int i = Math.max(0, index);
        while (getColumns().size() <= i) columns.add(PeakTrack.auto());
        columns.set(i, track);
        return this;
    }

    public List<PeakTrack> getColumns() {
        if (columns == null || columns.isEmpty()) columns = new ArrayList<>(List.of(PeakTrack.auto()));
        return columns;
    }

    public int getColumnCount() { return getColumns().size(); }

    public PeakTableLayoutPanel rows(PeakTrack... tracks) {
        rows = new ArrayList<>(List.of(tracks));
        return this;
    }

    public PeakTableLayoutPanel rows(String specification) {
        rows = new ArrayList<>(PeakTrack.parse(specification));
        return this;
    }

    public PeakTableLayoutPanel rowTrack(int index, PeakTrack track) {
        int i = Math.max(0, index);
        if (rows == null) rows = new ArrayList<>();
        while (rows.size() <= i) rows.add(PeakTrack.auto());
        rows.set(i, track);
        return this;
    }

    public List<PeakTrack> getRows() { return rows == null ? List.of() : rows; }

    public PeakTableLayoutPanel rowHeight(PeakTrack track) { this.rowHeight = track == null ? PeakTrack.auto() : track; return this; }
    public PeakTableLayoutPanel rowHeight(int pixels) { return rowHeight(PeakTrack.px(pixels)); }
    public PeakTrack getRowHeight() { return rowHeight == null ? PeakTrack.auto() : rowHeight; }

    public PeakTableLayoutPanel spacing(int spacing) {
        this.columnSpacing = Math.max(0, spacing);
        this.rowSpacing = Math.max(0, spacing);
        return this;
    }

    public PeakTableLayoutPanel columnSpacing(int spacing) { this.columnSpacing = Math.max(0, spacing); return this; }
    public int getColumnSpacing() { return columnSpacing >= 0 ? columnSpacing : getGap(); }

    public PeakTableLayoutPanel rowSpacing(int spacing) { this.rowSpacing = Math.max(0, spacing); return this; }
    public int getRowSpacing() { return rowSpacing >= 0 ? rowSpacing : getGap(); }

    public int getMinColumnWidth() { return Math.max(0, minColumnWidth); }
    public PeakTableLayoutPanel minColumnWidth(int width) { this.minColumnWidth = Math.max(0, width); return this; }

    public PeakInsets getCellPadding() { return cellPadding == null ? PeakInsets.all(0) : cellPadding; }
    public PeakTableLayoutPanel cellPadding(int all) { this.cellPadding = PeakInsets.all(Math.max(0, all)); return this; }
    public PeakTableLayoutPanel cellPadding(int vertical, int horizontal) {
        this.cellPadding = PeakInsets.of(vertical, horizontal, vertical, horizontal);
        return this;
    }
    public PeakTableLayoutPanel cellPadding(int top, int right, int bottom, int left) {
        this.cellPadding = PeakInsets.of(top, right, bottom, left);
        return this;
    }

    public PeakAlign getCellAlign() { return cellAlign == null ? PeakAlign.AUTO : cellAlign; }
    public PeakAlign getCellVerticalAlign() { return cellVerticalAlign == null ? PeakAlign.AUTO : cellVerticalAlign; }
    public PeakTableLayoutPanel cellAlign(PeakAlign horizontal) { this.cellAlign = horizontal; return this; }
    public PeakTableLayoutPanel cellAlign(PeakAlign horizontal, PeakAlign vertical) {
        this.cellAlign = horizontal;
        this.cellVerticalAlign = vertical;
        return this;
    }

    public PeakTableStyle getDecor() {
        if (decor == null) decor = new PeakTableStyle();
        return decor;
    }

    public PeakTableLayoutPanel decor(Consumer<PeakTableStyle> customizer) {
        customizer.accept(getDecor());
        return this;
    }

    public PeakTableLayoutPanel row(PeakElement<?>... cells) {
        padRow();
        for (PeakElement<?> cell : cells) add(cell);
        padRow();
        return this;
    }

    public PeakTableLayoutPanel empty() {
        add(new PeakSpacer(0, 0));
        return this;
    }

    public PeakTableLayoutPanel emptyRow(int height) {
        padRow();
        for (int i = 0; i < getColumnCount(); i++) add(new PeakSpacer(0, Math.max(0, height)));
        return this;
    }

    private void padRow() {
        List<PeakTableGrid.Span> spans = new ArrayList<>();
        for (PeakElement<?> child : getChildren()) {
            if (child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            spans.add(new PeakTableGrid.Span(child.getColumnSpan(), child.getRowSpan()));
        }
        int free = PeakTableGrid.trailingFreeSlots(spans, getColumnCount());
        for (int i = 0; i < free; i++) add(new PeakSpacer(0, 0));
    }

    @Override
    public boolean scrollsVertically() { return true; }

    @Override
    public String elementType() { return "table"; }
}
