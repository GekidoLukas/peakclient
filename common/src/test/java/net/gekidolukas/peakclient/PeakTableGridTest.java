package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakTableGrid;
import net.gekidolukas.peakclient.ui.PeakTrack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakTableGridTest {
    private static List<PeakTableGrid.Cell> singles(int... widths) {
        List<PeakTableGrid.Cell> cells = new ArrayList<>();
        for (int width : widths) cells.add(new PeakTableGrid.Cell(1, 1, width, 10));
        return cells;
    }

    private static PeakTableGrid.Result solve(List<PeakTrack> columns, List<PeakTableGrid.Cell> cells, int available) {
        return PeakTableGrid.solve(columns, List.of(), PeakTrack.auto(), cells, 0, 0, available, -1, 0);
    }

    private static List<PeakTrack> tracks(PeakTrack... tracks) {
        return List.of(tracks);
    }

    @Test
    void autoColumnsTakeTheWidestCell() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.auto(), PeakTrack.auto()), singles(10, 30, 20, 5), -1);

        assertArrayEquals(new int[]{ 20, 30 }, result.columnWidths());
        assertArrayEquals(new int[]{ 10, 10 }, result.rowHeights());
        assertEquals(50, result.contentWidth());
        assertEquals(20, result.contentHeight());
    }

    @Test
    void pixelColumnsAreFixed() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.px(60), PeakTrack.auto()), singles(10, 40), -1);

        assertArrayEquals(new int[]{ 60, 40 }, result.columnWidths());
    }

    @Test
    void percentColumnsUseTheAvailableWidth() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.percent(25), PeakTrack.auto()), singles(10, 40), 200);

        assertArrayEquals(new int[]{ 50, 40 }, result.columnWidths());
    }

    @Test
    void percentColumnsFallBackToAutoWithoutAvailableWidth() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.percent(25), PeakTrack.auto()), singles(10, 40), -1);

        assertArrayEquals(new int[]{ 10, 40 }, result.columnWidths());
    }

    @Test
    void fillColumnsShareTheRestEvenly() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.px(50), PeakTrack.fill(), PeakTrack.fill()), singles(10, 10, 10), 250);

        assertArrayEquals(new int[]{ 50, 100, 100 }, result.columnWidths());
    }

    @Test
    void fillWeightsSplitProportionally() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.fill(1), PeakTrack.fill(2)), singles(10, 10), 300);

        assertArrayEquals(new int[]{ 100, 200 }, result.columnWidths());
    }

    @Test
    void fillColumnsNeverShrinkBelowTheirContent() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.fill(), PeakTrack.fill()), singles(150, 10), 200);

        assertArrayEquals(new int[]{ 150, 50 }, result.columnWidths());
    }

    @Test
    void fillColumnsRespectSpacing() {
        PeakTableGrid.Result result = PeakTableGrid.solve(tracks(PeakTrack.fill(), PeakTrack.fill()), List.of(),
                PeakTrack.auto(), singles(10, 10), 10, 0, 110, -1, 0);

        assertArrayEquals(new int[]{ 50, 50 }, result.columnWidths());
        assertEquals(110, result.contentWidth());
    }

    @Test
    void fillMaximumLimitsAColumn() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.fill().max(40), PeakTrack.fill()), singles(10, 10), 200);

        assertEquals(40, result.columnWidths()[0]);
        assertEquals(100, result.columnWidths()[1]);
    }

    @Test
    void fillMinimumKeepsAColumnWide() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.fill().min(150), PeakTrack.fill()), singles(10, 10), 200);

        assertEquals(150, result.columnWidths()[0]);
        assertEquals(50, result.columnWidths()[1]);
    }

    @Test
    void fillColumnsAreAutoWithoutAvailableWidth() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.fill(), PeakTrack.fill()), singles(30, 10), -1);

        assertArrayEquals(new int[]{ 30, 10 }, result.columnWidths());
    }

    @Test
    void minAndMaxClampAutoColumns() {
        PeakTableGrid.Result small = solve(tracks(PeakTrack.auto().min(50)), singles(10), -1);
        PeakTableGrid.Result large = solve(tracks(PeakTrack.auto().max(60)), singles(80), -1);

        assertEquals(50, small.columnWidths()[0]);
        assertEquals(60, large.columnWidths()[0]);
    }

    @Test
    void minColumnWidthOnlyRaisesFlexibleColumns() {
        PeakTableGrid.Result result = PeakTableGrid.solve(tracks(PeakTrack.auto(), PeakTrack.px(10), PeakTrack.fill()),
                List.of(), PeakTrack.auto(), singles(5, 5, 5), 0, 0, -1, -1, 30);

        assertArrayEquals(new int[]{ 30, 10, 30 }, result.columnWidths());
    }

    @Test
    void columnSpansGrowTheirColumnsEvenly() {
        List<PeakTableGrid.Cell> cells = new ArrayList<>();
        cells.add(new PeakTableGrid.Cell(3, 1, 90, 10));
        cells.addAll(singles(10, 10, 10));

        PeakTableGrid.Result result = solve(tracks(PeakTrack.auto(), PeakTrack.auto(), PeakTrack.auto()), cells, -1);

        assertArrayEquals(new int[]{ 30, 30, 30 }, result.columnWidths());
        assertEquals(new PeakTableGrid.Placement(0, 0, 3, 1), result.placements().get(0));
        assertEquals(new PeakTableGrid.Placement(1, 2, 1, 1), result.placements().get(3));
    }

    @Test
    void spansLeaveFixedColumnsAlone() {
        List<PeakTableGrid.Cell> cells = new ArrayList<>();
        cells.add(new PeakTableGrid.Cell(2, 1, 100, 10));

        PeakTableGrid.Result result = solve(tracks(PeakTrack.px(20), PeakTrack.auto()), cells, -1);

        assertArrayEquals(new int[]{ 20, 80 }, result.columnWidths());
    }

    @Test
    void rowSpansGrowTheirRows() {
        List<PeakTableGrid.Cell> cells = new ArrayList<>();
        cells.add(new PeakTableGrid.Cell(1, 2, 10, 50));
        cells.add(new PeakTableGrid.Cell(1, 1, 10, 10));
        cells.add(new PeakTableGrid.Cell(1, 1, 10, 10));

        PeakTableGrid.Result result = solve(tracks(PeakTrack.auto(), PeakTrack.auto()), cells, -1);

        assertArrayEquals(new int[]{ 25, 25 }, result.rowHeights());
        assertEquals(new PeakTableGrid.Placement(0, 1, 1, 1), result.placements().get(1));
        assertEquals(new PeakTableGrid.Placement(1, 1, 1, 1), result.placements().get(2));
    }

    @Test
    void rowTracksOverrideAndDefault() {
        PeakTableGrid.Result result = PeakTableGrid.solve(tracks(PeakTrack.auto()), tracks(PeakTrack.px(30)),
                PeakTrack.auto(), singles(10, 10), 0, 0, -1, -1, 0);

        assertArrayEquals(new int[]{ 30, 10 }, result.rowHeights());
    }

    @Test
    void rowHeightDefaultAppliesToEveryRow() {
        PeakTableGrid.Result result = PeakTableGrid.solve(tracks(PeakTrack.auto()), List.of(), PeakTrack.px(16),
                singles(10, 10, 10), 0, 0, -1, -1, 0);

        assertArrayEquals(new int[]{ 16, 16, 16 }, result.rowHeights());
    }

    @Test
    void fillRowsShareTheAvailableHeight() {
        PeakTableGrid.Result result = PeakTableGrid.solve(tracks(PeakTrack.auto()),
                tracks(PeakTrack.fill(), PeakTrack.auto()), PeakTrack.auto(), singles(10, 10), 0, 0, -1, 100, 0);

        assertArrayEquals(new int[]{ 90, 10 }, result.rowHeights());
    }

    @Test
    void positionsAndSpansIncludeSpacing() {
        PeakTableGrid.Result result = PeakTableGrid.solve(tracks(PeakTrack.auto(), PeakTrack.auto()), List.of(),
                PeakTrack.auto(), singles(10, 20, 5, 5), 4, 2, -1, -1, 0);

        assertEquals(34, result.contentWidth());
        assertEquals(22, result.contentHeight());
        assertEquals(0, result.columnStart(0));
        assertEquals(14, result.columnStart(1));
        assertEquals(0, result.rowStart(0));
        assertEquals(12, result.rowStart(1));
        assertEquals(34, result.spanWidth(0, 2));
        assertEquals(22, result.spanHeight(0, 2));
    }

    @Test
    void anEmptyTableIsZeroSized() {
        PeakTableGrid.Result result = solve(tracks(PeakTrack.auto(), PeakTrack.auto()), List.of(), -1);

        assertArrayEquals(new int[]{ 0, 0 }, result.columnWidths());
        assertEquals(0, result.rows());
        assertEquals(0, result.contentHeight());
    }

    @Test
    void spannedCellsBlockTheBoundariesTheyCross() {
        List<PeakTableGrid.Cell> cells = new ArrayList<>();
        cells.add(new PeakTableGrid.Cell(2, 1, 40, 10));
        cells.add(new PeakTableGrid.Cell(1, 2, 10, 20));
        cells.add(new PeakTableGrid.Cell(1, 1, 10, 10));
        cells.add(new PeakTableGrid.Cell(1, 1, 10, 10));

        PeakTableGrid.Result result = solve(tracks(PeakTrack.auto(), PeakTrack.auto(), PeakTrack.auto()), cells, -1);

        assertEquals(new PeakTableGrid.Placement(0, 0, 2, 1), result.placements().get(0));
        assertEquals(new PeakTableGrid.Placement(0, 2, 1, 2), result.placements().get(1));
        assertTrue(result.columnBoundaryBlocked(0, 0));
        assertFalse(result.columnBoundaryBlocked(1, 0));
        assertFalse(result.columnBoundaryBlocked(0, 1));
        assertTrue(result.rowBoundaryBlocked(0, 2));
        assertFalse(result.rowBoundaryBlocked(0, 0));
        assertFalse(result.rowBoundaryBlocked(0, 1));
    }

    @Test
    void alignmentPrefersTheCellThenTheTrackThenTheTable() {
        assertEquals(PeakAlign.END, PeakTableGrid.resolveAlign(PeakAlign.END, PeakAlign.CENTER, PeakAlign.START));
        assertEquals(PeakAlign.CENTER, PeakTableGrid.resolveAlign(PeakAlign.AUTO, PeakAlign.CENTER, PeakAlign.END));
        assertEquals(PeakAlign.END, PeakTableGrid.resolveAlign(PeakAlign.AUTO, PeakAlign.AUTO, PeakAlign.END));
        assertEquals(PeakAlign.START, PeakTableGrid.resolveAlign(PeakAlign.AUTO, PeakAlign.AUTO, PeakAlign.AUTO));
    }

    @Test
    void alignmentOffsetsUseTheFreeSpace() {
        assertEquals(0, PeakTableGrid.alignOffset(PeakAlign.START, 20));
        assertEquals(10, PeakTableGrid.alignOffset(PeakAlign.CENTER, 20));
        assertEquals(20, PeakTableGrid.alignOffset(PeakAlign.END, 20));
        assertEquals(0, PeakTableGrid.alignOffset(PeakAlign.END, -5));
        assertEquals(0, PeakTableGrid.alignOffset(PeakAlign.AUTO, 20));
    }

    @Test
    void placementFlowsRowByRowAndWrapsWideCells() {
        List<PeakTableGrid.Span> spans = List.of(new PeakTableGrid.Span(2, 1), new PeakTableGrid.Span(1, 1), new PeakTableGrid.Span(1, 1));

        List<PeakTableGrid.Placement> placements = PeakTableGrid.place(spans, 2);

        assertEquals(new PeakTableGrid.Placement(0, 0, 2, 1), placements.get(0));
        assertEquals(new PeakTableGrid.Placement(1, 0, 1, 1), placements.get(1));
        assertEquals(new PeakTableGrid.Placement(1, 1, 1, 1), placements.get(2));
    }

    @Test
    void spansWiderThanTheTableAreClamped() {
        List<PeakTableGrid.Placement> placements = PeakTableGrid.place(List.of(new PeakTableGrid.Span(5, 1)), 3);

        assertEquals(3, placements.get(0).columnSpan());
    }

    @Test
    void trailingFreeSlotsCountTheOpenCellsOfTheLastRow() {
        assertEquals(1, PeakTableGrid.trailingFreeSlots(List.of(new PeakTableGrid.Span(1, 1), new PeakTableGrid.Span(1, 1)), 3));
        assertEquals(0, PeakTableGrid.trailingFreeSlots(List.of(new PeakTableGrid.Span(1, 1), new PeakTableGrid.Span(1, 1), new PeakTableGrid.Span(1, 1)), 3));
        assertEquals(0, PeakTableGrid.trailingFreeSlots(List.of(), 3));
        assertEquals(1, PeakTableGrid.trailingFreeSlots(List.of(new PeakTableGrid.Span(1, 2), new PeakTableGrid.Span(1, 1), new PeakTableGrid.Span(1, 1),
                new PeakTableGrid.Span(1, 1)), 3));
    }
}
