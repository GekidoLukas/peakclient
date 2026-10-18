package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakFlow;
import net.gekidolukas.peakclient.ui.PeakFlowLines;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakMainAxisAlign;
import net.gekidolukas.peakclient.ui.PeakSpacer;
import net.gekidolukas.peakclient.ui.PeakTableLayoutPanel;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakFlowTest {
    private static List<PeakFlowLines.Item> items(int... mains) {
        List<PeakFlowLines.Item> items = new ArrayList<>();
        for (int main : mains) items.add(new PeakFlowLines.Item(main, 10));
        return items;
    }

    private static PeakFlowLines.Result layout(List<PeakFlowLines.Item> items, int limit, int boxMain, int boxCross, int gap,
                                               int lineGap, int maxLines, PeakMainAxisAlign mainAlign, PeakAlign crossAlign,
                                               PeakMainAxisAlign lineAlign, boolean reverse) {
        return PeakFlowLines.layout(items, limit, boxMain, boxCross, gap, lineGap, maxLines, mainAlign, crossAlign, lineAlign, reverse);
    }

    private static PeakFlowLines.Result simple(List<PeakFlowLines.Item> items, int limit) {
        return layout(items, limit, limit, -1, 0, 0, 0, PeakMainAxisAlign.START, PeakAlign.START, PeakMainAxisAlign.START, false);
    }

    @Test
    void itemsWrapWhenTheLineIsFull() {
        PeakFlowLines.Result result = simple(items(40, 40, 40, 40, 40), 100);

        assertEquals(3, result.lines().size());
        assertEquals(List.of(0, 1), result.lines().get(0).indices());
        assertEquals(List.of(2, 3), result.lines().get(1).indices());
        assertEquals(List.of(4), result.lines().get(2).indices());
        assertEquals(80, result.contentMain());
        assertEquals(30, result.contentCross());
    }

    @Test
    void gapsCountTowardsTheLimitAndSeparateLines() {
        List<PeakFlowLines.Line> lines = PeakFlowLines.wrap(items(40, 40, 40), 100, 10, 0);
        assertEquals(2, lines.size());
        assertEquals(90, lines.get(0).main());

        PeakFlowLines.Result result = layout(items(40, 40, 40), 100, 100, -1, 10, 6, 0,
                PeakMainAxisAlign.START, PeakAlign.START, PeakMainAxisAlign.START, false);
        assertEquals(26, result.contentCross());
        assertEquals(50, result.placements().get(1).mainOffset());
        assertEquals(16, result.placements().get(2).crossOffset());
    }

    @Test
    void aSingleWideItemStandsAloneAndIsNotCut() {
        PeakFlowLines.Result result = simple(items(30, 150, 30), 100);

        assertEquals(3, result.lines().size());
        assertEquals(List.of(1), result.lines().get(1).indices());
        assertEquals(150, result.contentMain());
    }

    @Test
    void noLimitMeansOneLine() {
        assertEquals(1, PeakFlowLines.wrap(items(500, 500, 500), -1, 4, 0).size());
    }

    @Test
    void maxLinesDropsTheRest() {
        PeakFlowLines.Result result = layout(items(40, 40, 40, 40, 40, 40), 100, 100, -1, 0, 0, 2,
                PeakMainAxisAlign.START, PeakAlign.START, PeakMainAxisAlign.START, false);

        assertEquals(2, result.lines().size());
        assertEquals(4, result.shownCount());
        assertEquals(4, result.placements().size());
    }

    @Test
    void lineBreakMarksForceANewLine() {
        List<PeakFlowLines.Item> items = new ArrayList<>(items(20, 20, 20, 20));
        items.set(2, new PeakFlowLines.Item(20, 10, true));

        List<PeakFlowLines.Line> lines = PeakFlowLines.wrap(items, 500, 0, 0);

        assertEquals(2, lines.size());
        assertEquals(List.of(2, 3), lines.get(1).indices());
    }

    @Test
    void mainAxisAlignIsAppliedPerLine() {
        List<PeakFlowLines.Item> items = items(40, 40, 40);
        PeakFlowLines.Result center = layout(items, 100, 100, -1, 0, 0, 0,
                PeakMainAxisAlign.CENTER, PeakAlign.START, PeakMainAxisAlign.START, false);
        assertEquals(10, center.placements().get(0).mainOffset());
        assertEquals(30, center.placements().get(2).mainOffset());

        PeakFlowLines.Result end = layout(items, 100, 100, -1, 0, 0, 0,
                PeakMainAxisAlign.END, PeakAlign.START, PeakMainAxisAlign.START, false);
        assertEquals(20, end.placements().get(0).mainOffset());
        assertEquals(60, end.placements().get(2).mainOffset());

        PeakFlowLines.Result between = layout(items, 100, 100, -1, 0, 0, 0,
                PeakMainAxisAlign.SPACE_BETWEEN, PeakAlign.START, PeakMainAxisAlign.START, false);
        assertEquals(0, between.placements().get(0).mainOffset());
        assertEquals(60, between.placements().get(1).mainOffset());
        assertEquals(30, between.placements().get(2).mainOffset());
    }

    @Test
    void crossAlignPlacesItemsInsideTheirLine() {
        List<PeakFlowLines.Item> items = List.of(new PeakFlowLines.Item(20, 30), new PeakFlowLines.Item(20, 10));

        PeakFlowLines.Result center = layout(items, 100, 100, -1, 0, 0, 0,
                PeakMainAxisAlign.START, PeakAlign.CENTER, PeakMainAxisAlign.START, false);
        assertEquals(0, center.placements().get(0).crossOffset());
        assertEquals(10, center.placements().get(1).crossOffset());

        PeakFlowLines.Result end = layout(items, 100, 100, -1, 0, 0, 0,
                PeakMainAxisAlign.START, PeakAlign.END, PeakMainAxisAlign.START, false);
        assertEquals(20, end.placements().get(1).crossOffset());
    }

    @Test
    void lineAlignDistributesLinesInATallBox() {
        List<PeakFlowLines.Item> items = items(60, 60);

        PeakFlowLines.Result center = layout(items, 100, 100, 50, 0, 0, 0,
                PeakMainAxisAlign.START, PeakAlign.START, PeakMainAxisAlign.CENTER, false);
        assertEquals(15, center.placements().get(0).crossOffset());
        assertEquals(25, center.placements().get(1).crossOffset());

        PeakFlowLines.Result end = layout(items, 100, 100, 50, 0, 0, 0,
                PeakMainAxisAlign.START, PeakAlign.START, PeakMainAxisAlign.END, false);
        assertEquals(30, end.placements().get(0).crossOffset());

        PeakFlowLines.Result between = layout(items, 100, 100, 50, 0, 0, 0,
                PeakMainAxisAlign.START, PeakAlign.START, PeakMainAxisAlign.SPACE_BETWEEN, false);
        assertEquals(0, between.placements().get(0).crossOffset());
        assertEquals(40, between.placements().get(1).crossOffset());
    }

    @Test
    void reverseMirrorsLinesAndItems() {
        PeakFlowLines.Result result = layout(items(40, 40, 40), 100, 100, -1, 0, 0, 0,
                PeakMainAxisAlign.START, PeakAlign.START, PeakMainAxisAlign.START, true);

        assertEquals(List.of(2), result.lines().get(0).indices());
        assertEquals(List.of(1, 0), result.lines().get(1).indices());
        assertEquals(2, result.placements().get(0).index());
        assertEquals(1, result.placements().get(1).index());
        assertEquals(0, result.placements().get(1).mainOffset());
        assertEquals(40, result.placements().get(2).mainOffset());
    }

    @Test
    void emptyInputGivesAnEmptyResult() {
        PeakFlowLines.Result result = simple(List.of(), 100);
        assertTrue(result.lines().isEmpty());
        assertEquals(0, result.contentMain());
        assertEquals(0, result.contentCross());
    }

    @Test
    void theElementKeepsItsSettingsOnTheWire() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakFlow.create("test:flow").gap(4).lineGap(8).lineAlign(PeakMainAxisAlign.CENTER).reverse(true)
                .maxLines(3).wrapSize(120).alignChildren(PeakAlign.CENTER)
                .add(PeakLabel.create("test:a", "A"), PeakFlow.lineBreak(), PeakLabel.create("test:b", "B")));

        PeakFlow back = assertInstanceOf(PeakFlow.class,
                PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document)).findById("test:flow"));

        assertEquals(PeakLayoutDirection.ROW, back.getDirection());
        assertEquals(4, back.getGap());
        assertEquals(8, back.getLineGap());
        assertEquals(PeakMainAxisAlign.CENTER, back.getLineAlign());
        assertTrue(back.isReverse());
        assertEquals(3, back.getMaxLines());
        assertEquals(120, back.getWrapSize());
        assertInstanceOf(PeakSpacer.class, back.getChildren().get(1));
        assertTrue(((PeakSpacer) back.getChildren().get(1)).isLineBreak());
        assertFalse(((PeakSpacer) PeakSpacer.create(2, 2)).isLineBreak());
    }

    @Test
    void defaultsAreRowWithTheGapAsLineGap() {
        PeakFlow flow = PeakFlow.create("test:flow").gap(6);
        assertEquals(PeakLayoutDirection.ROW, flow.getDirection());
        assertEquals(6, flow.getLineGap());
        assertEquals(PeakMainAxisAlign.START, flow.getLineAlign());
        assertEquals(0, flow.getMaxLines());

        flow.spacing(3);
        assertEquals(3, flow.getGap());
        assertEquals(3, flow.getLineGap());
    }

    @Test
    void patchesChangeTheSettings() {
        PeakFlow flow = PeakFlow.create("test:flow");
        flow.applyPatch(PeakPatch.flow().lineGap(5).lineAlign(PeakMainAxisAlign.END).reverse(true).maxLines(2).wrapSize(90)
                .direction(PeakLayoutDirection.COLUMN).gap(3).build());

        assertEquals(5, flow.getLineGap());
        assertEquals(PeakMainAxisAlign.END, flow.getLineAlign());
        assertTrue(flow.isReverse());
        assertEquals(2, flow.getMaxLines());
        assertEquals(90, flow.getWrapSize());
        assertEquals(PeakLayoutDirection.COLUMN, flow.getDirection());
    }

    @Test
    void containersScrollAlongTheirStackingAxis() {
        assertTrue(PeakFlow.create().scrollsVertically());
        assertFalse(PeakFlow.create().direction(PeakLayoutDirection.COLUMN).scrollsVertically());
        assertTrue(PeakTableLayoutPanel.create().scrollsVertically());
    }
}
