package net.gekidolukas.peakclient;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentValidator;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakSpacer;
import net.gekidolukas.peakclient.ui.PeakTableLayoutPanel;
import net.gekidolukas.peakclient.ui.PeakTableStyle;
import net.gekidolukas.peakclient.ui.PeakTrack;
import net.gekidolukas.peakclient.ui.PeakTrackStyle;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakTableLayoutPanelTest {
    @Test
    void theShorthandCreatesTheMatchingTracks() {
        List<PeakTrack> tracks = PeakTrack.parse("60px 1fr 25% auto fill 2fr 40");

        assertEquals(7, tracks.size());
        assertEquals(PeakTrack.Kind.PIXELS, tracks.get(0).getKind());
        assertEquals(60f, tracks.get(0).getValue());
        assertEquals(PeakTrack.Kind.FILL, tracks.get(1).getKind());
        assertEquals(1f, tracks.get(1).getValue());
        assertEquals(PeakTrack.Kind.PERCENT, tracks.get(2).getKind());
        assertEquals(25f, tracks.get(2).getValue());
        assertEquals(PeakTrack.Kind.AUTO, tracks.get(3).getKind());
        assertEquals(PeakTrack.Kind.FILL, tracks.get(4).getKind());
        assertEquals(2f, tracks.get(5).getValue());
        assertEquals(PeakTrack.Kind.PIXELS, tracks.get(6).getKind());
        assertEquals(40f, tracks.get(6).getValue());
    }

    @Test
    void anInvalidShorthandNamesTheToken() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> PeakTrack.parse("60px wide"));

        assertTrue(error.getMessage().contains("wide"));
    }

    @Test
    void trackFactoriesAndLimitsAreSanitized() {
        assertEquals(1f, PeakTrack.fill(-3).getValue());
        assertEquals(0f, PeakTrack.px(-5).getValue());
        PeakTrack limited = PeakTrack.fill().min(-4).max(50).alignEnd();

        assertEquals(0, limited.getMin());
        assertEquals(50, limited.getMax());
        assertEquals(PeakAlign.END, limited.getAlign());
    }

    @Test
    void columnsCanBeSetByCountTracksOrShorthand() {
        assertEquals(3, PeakTableLayoutPanel.create().columns(3).getColumnCount());
        assertEquals(2, PeakTableLayoutPanel.create().columns(PeakTrack.px(10), PeakTrack.fill()).getColumnCount());
        assertEquals(3, PeakTableLayoutPanel.create().columns("10px 1fr auto").getColumnCount());
        assertEquals(1, PeakTableLayoutPanel.create().getColumnCount());
        assertEquals(1, PeakTableLayoutPanel.create().columns(0).getColumnCount());
    }

    @Test
    void columnTrackExtendsTheListWithAutoColumns() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(1).columnTrack(2, PeakTrack.px(30));

        assertEquals(3, table.getColumnCount());
        assertEquals(PeakTrack.Kind.AUTO, table.getColumns().get(1).getKind());
        assertEquals(PeakTrack.Kind.PIXELS, table.getColumns().get(2).getKind());
    }

    @Test
    void spacingFallsBackToTheGap() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create().gap(5);

        assertEquals(5, table.getColumnSpacing());
        assertEquals(5, table.getRowSpacing());

        table.columnSpacing(2);
        assertEquals(2, table.getColumnSpacing());
        assertEquals(5, table.getRowSpacing());

        table.spacing(7);
        assertEquals(7, table.getColumnSpacing());
        assertEquals(7, table.getRowSpacing());
    }

    @Test
    void rowPadsTheOpenRowBeforeAndAfter() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(3);

        table.row(PeakLabel.create("a", "A"), PeakLabel.create("b", "B"));
        assertEquals(3, table.getChildren().size());
        assertTrue(table.getChildren().get(2) instanceof PeakSpacer);

        table.row(PeakLabel.create("c", "C"));
        assertEquals(6, table.getChildren().size());
        assertTrue(table.getChildren().get(3) instanceof PeakLabel);
        assertTrue(table.getChildren().get(4) instanceof PeakSpacer);
        assertTrue(table.getChildren().get(5) instanceof PeakSpacer);
    }

    @Test
    void rowStartsAfterAnIncompleteAddedRow() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(3);
        table.add(PeakLabel.create("a", "A"));
        table.add(PeakLabel.create("b", "B"));

        table.row(PeakLabel.create("c", "C"));

        assertEquals(6, table.getChildren().size());
        assertEquals("c", table.getChildren().get(3).getId());
    }

    @Test
    void rowCountsSpansWhenPadding() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(3);
        table.add(PeakLabel.create("title", "Title").colSpan(2));

        table.row(PeakLabel.create("b", "B"));

        assertEquals(5, table.getChildren().size());
        assertEquals("b", table.getChildren().get(2).getId());
    }

    @Test
    void emptyAddsOneSpacerAndEmptyRowAFullRow() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(2);

        table.empty();
        assertEquals(1, table.getChildren().size());

        table.emptyRow(8);
        assertEquals(4, table.getChildren().size());
        assertTrue(table.getChildren().get(1) instanceof PeakSpacer);
        assertTrue(table.getChildren().get(3) instanceof PeakSpacer);
    }

    @Test
    void spansAreSanitizedAndPatchable() {
        PeakLabel label = PeakLabel.create("l", "L").colSpan(0).rowSpan(-2);

        assertEquals(1, label.getColumnSpan());
        assertEquals(1, label.getRowSpan());

        label.span(2, 3);
        assertEquals(2, label.getColumnSpan());
        assertEquals(3, label.getRowSpan());

        label.applyPatch(PeakPatch.label().colSpan(4).rowSpan(2).build());
        assertEquals(4, label.getColumnSpan());
        assertEquals(2, label.getRowSpan());
    }

    @Test
    void tablesSurviveTheWireFormat() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create("ns:table")
                .columns(PeakTrack.px(60), PeakTrack.fill(2).min(20).max(90).alignEnd(), PeakTrack.percent(25))
                .rowTrack(1, PeakTrack.px(18))
                .rowHeight(PeakTrack.px(12))
                .spacing(3).cellPadding(1, 2).cellAlign(PeakAlign.CENTER, PeakAlign.END).minColumnWidth(10);
        table.add(PeakLabel.create("ns:table/title", "Title").colSpan(3));
        PeakDocument doc = PeakDocument.create("doc");
        doc.add(table);

        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(doc));
        PeakTableLayoutPanel read = (PeakTableLayoutPanel) back.findById("ns:table");

        assertEquals(3, read.getColumnCount());
        assertEquals(PeakTrack.Kind.FILL, read.getColumns().get(1).getKind());
        assertEquals(2f, read.getColumns().get(1).getValue());
        assertEquals(20, read.getColumns().get(1).getMin());
        assertEquals(90, read.getColumns().get(1).getMax());
        assertEquals(PeakAlign.END, read.getColumns().get(1).getAlign());
        assertEquals(PeakTrack.Kind.PERCENT, read.getColumns().get(2).getKind());
        assertEquals(2, read.getRows().size());
        assertEquals(PeakTrack.Kind.PIXELS, read.getRows().get(1).getKind());
        assertEquals(12f, read.getRowHeight().getValue());
        assertEquals(3, read.getColumnSpacing());
        assertEquals(3, read.getRowSpacing());
        assertEquals(1, read.getCellPadding().getTop());
        assertEquals(2, read.getCellPadding().getLeft());
        assertEquals(PeakAlign.CENTER, read.getCellAlign());
        assertEquals(PeakAlign.END, read.getCellVerticalAlign());
        assertEquals(10, read.getMinColumnWidth());
        assertEquals(3, ((PeakLabel) read.getChildren().get(0)).getColumnSpan());
    }

    @Test
    void defaultTablesStayDefault() {
        PeakDocument doc = PeakDocument.create("doc");
        doc.add(PeakTableLayoutPanel.create("plain").columns(2));

        PeakTableLayoutPanel read = (PeakTableLayoutPanel) PeakUiSerializer
                .documentFromJson(PeakUiSerializer.toJson(doc)).findById("plain");

        assertEquals(2, read.getColumnCount());
        assertTrue(read.getRows().isEmpty());
        assertEquals(PeakTrack.Kind.AUTO, read.getRowHeight().getKind());
        assertEquals(0, read.getColumnSpacing());
        assertEquals(PeakAlign.AUTO, read.getCellAlign());
    }

    @Test
    void decorIsOffByDefaultAndSurvivesTheWireFormat() {
        PeakTableLayoutPanel plain = PeakTableLayoutPanel.create("plain");
        assertFalse(plain.getDecor().isActive());

        PeakTableLayoutPanel styled = PeakTableLayoutPanel.create("styled").columns(2)
                .decor(d -> d.headerRows(1).headerBackground(0x8030507F).stripes(0x20FFFFFF, 0)
                        .lines(1, 0x40FFFFFF, PeakTableStyle.LineMode.ROWS));
        PeakDocument doc = PeakDocument.create("doc");
        doc.add(styled);

        PeakTableLayoutPanel read = (PeakTableLayoutPanel) PeakUiSerializer
                .documentFromJson(PeakUiSerializer.toJson(doc)).findById("styled");

        assertTrue(read.getDecor().isActive());
        assertEquals(1, read.getDecor().getHeaderRows());
        assertEquals(PeakPaint.solid(0x8030507F), read.getDecor().getHeaderBackground());
        assertEquals(PeakPaint.solid(0x20FFFFFF), read.getDecor().getStripeFirst());
        assertEquals(PeakPaint.solid(0), read.getDecor().getStripeSecond());
        assertEquals(1, read.getDecor().getLineWidth());
        assertEquals(PeakTableStyle.LineMode.ROWS, read.getDecor().getLineMode());
    }

    @Test
    void decorPartsAreActivatedIndependently() {
        assertTrue(PeakTableStyle.create().headerRows(1).headerBackground(0x80FFFFFF).hasHeader());
        assertFalse(PeakTableStyle.create().headerRows(1).hasHeader());
        assertFalse(PeakTableStyle.create().headerBackground(0x80FFFFFF).hasHeader());
        assertTrue(PeakTableStyle.create().stripes(0, 0x20FFFFFF).hasStripes());
        assertTrue(PeakTableStyle.create().lines(1, 0x40FFFFFF).hasLines());
        assertFalse(PeakTableStyle.create().lines(0, 0x40FFFFFF).hasLines());
        assertFalse(PeakTableStyle.create().lines(1, 0x40FFFFFF, PeakTableStyle.LineMode.NONE).hasLines());
        assertFalse(PeakTableStyle.create().isActive());
    }

    @Test
    void decorColorsFollowTheColorOrNoneRule() {
        PeakTableStyle style = PeakTableStyle.create().headerBackground(0x3366FF).stripes(0x112233, 0);

        assertEquals(PeakPaint.solid(0xFF3366FF), style.getHeaderBackground());
        assertEquals(PeakPaint.solid(0xFF112233), style.getStripeFirst());
        assertEquals(PeakPaint.solid(0), style.getStripeSecond());
    }

    @Test
    void decorPatchesReachTheNestedFields() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create("t").columns(2);

        table.applyPatch(PeakPatch.table().headerRows(2).headerBackground(0x445566).stripes(0x101010, 0x202020)
                .lines(2, 0x778899).build());

        assertEquals(2, table.getDecor().getHeaderRows());
        assertEquals(PeakPaint.solid(0xFF445566), table.getDecor().getHeaderBackground());
        assertEquals(PeakPaint.solid(0xFF101010), table.getDecor().getStripeFirst());
        assertEquals(PeakPaint.solid(0xFF202020), table.getDecor().getStripeSecond());
        assertEquals(2, table.getDecor().getLineWidth());
        assertEquals(PeakPaint.solid(0xFF778899), table.getDecor().getLineColor());
        assertTrue(table.isPatchKeyKnown("decor.lineMode"));
    }

    @Test
    void trackStylesAreOffByDefaultAndSurviveTheWireFormat() {
        assertFalse(PeakTableStyle.create().hasTrackStyles());
        assertFalse(PeakTableStyle.create().isLinesAcrossSpans());

        PeakTableLayoutPanel styled = PeakTableLayoutPanel.create("styled").columns(3)
                .decor(d -> d.row(2, s -> s.background(0x40FFD700).line(2, 0xFFFFD700))
                        .column(1, s -> s.background(0x2000FF00).noLine())
                        .linesAcrossSpans(true));
        PeakDocument doc = PeakDocument.create("doc");
        doc.add(styled);

        PeakTableLayoutPanel read = (PeakTableLayoutPanel) PeakUiSerializer
                .documentFromJson(PeakUiSerializer.toJson(doc)).findById("styled");

        assertTrue(read.getDecor().isActive());
        assertTrue(read.getDecor().isLinesAcrossSpans());
        assertEquals(PeakPaint.solid(0x40FFD700), read.getDecor().getRowStyle(2).getBackground());
        assertEquals(2, read.getDecor().getRowStyle(2).getLineWidth());
        assertEquals(PeakPaint.solid(0xFFFFD700), read.getDecor().getRowStyle(2).getLineColor());
        assertNull(read.getDecor().getRowStyle(0));
        assertEquals(PeakPaint.solid(0x2000FF00), read.getDecor().getColumnStyle(1).getBackground());
        assertEquals(0, read.getDecor().getColumnStyle(1).getLineWidth());
        assertTrue(read.getDecor().getColumnStyle(1).overridesLine());
    }

    @Test
    void aTrackStyleInheritsTheLineUntilItOverridesIt() {
        PeakTrackStyle inherit = PeakTrackStyle.create().background(0x80FFFFFF);
        PeakTrackStyle none = PeakTrackStyle.create().noLine();
        PeakTrackStyle custom = PeakTrackStyle.create().line(3, 0x112233);

        assertFalse(inherit.overridesLine());
        assertTrue(inherit.hasBackground());
        assertTrue(none.overridesLine());
        assertEquals(0, none.getLineWidth());
        assertEquals(3, custom.getLineWidth());
        assertEquals(PeakPaint.solid(0xFF112233), custom.getLineColor());
    }

    @Test
    void trackStylePatchesReplaceTheMaps() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create("t").columns(2)
                .decor(d -> d.row(0, s -> s.background(0x40FFFFFF)));

        table.applyPatch(PeakPatch.table().rowBackground(3, 0x80FF0000).columnBackground(1, 0x8000FF00)
                .linesAcrossSpans(true).build());

        assertNull(table.getDecor().getRowStyle(0));
        assertEquals(PeakPaint.solid(0x80FF0000), table.getDecor().getRowStyle(3).getBackground());
        assertEquals(PeakPaint.solid(0x8000FF00), table.getDecor().getColumnStyle(1).getBackground());
        assertTrue(table.getDecor().isLinesAcrossSpans());
    }

    @Test
    void theValidatorWarnsAboutTooWideSpans() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create("ns:table").columns(2);
        table.add(PeakLabel.create("ns:table/wide", "Wide").colSpan(3));
        PeakDocument doc = PeakDocument.create("doc");
        doc.add(table);

        List<String> issues = PeakDocumentValidator.validate(doc, 0, 0).issues();

        assertEquals(1, issues.size());
        assertTrue(issues.get(0).contains("ns:table/wide"));
        assertTrue(issues.get(0).contains("3 columns"));
    }

    @Test
    void patchesReplaceTracksAndSpacing() {
        PeakTableLayoutPanel table = PeakTableLayoutPanel.create("t").columns(2);

        JsonObject patch = PeakPatch.table().columns("40px 1fr 30%").spacing(4).rowHeight(PeakTrack.px(14))
                .cellPadding(2).cellAlign(PeakAlign.END, PeakAlign.CENTER).minColumnWidth(12).build();
        table.applyPatch(patch);

        assertEquals(3, table.getColumnCount());
        assertEquals(PeakTrack.Kind.PERCENT, table.getColumns().get(2).getKind());
        assertEquals(4, table.getColumnSpacing());
        assertEquals(14f, table.getRowHeight().getValue());
        assertEquals(2, table.getCellPadding().getRight());
        assertEquals(PeakAlign.END, table.getCellAlign());
        assertEquals(PeakAlign.CENTER, table.getCellVerticalAlign());
        assertEquals(12, table.getMinColumnWidth());
    }
}
