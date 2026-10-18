package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class TableLayoutPanelElementTest implements PeakElementTest {
    @Override public String name() { return "table"; }
    @Override public String description() { return "PeakTableLayoutPanel: tracks (px, percent, fill, min/max), spans, row(...), margins, spacing"; }

    private static PeakPanel box(String text, int color) {
        return PeakPanel.create().padding(2, 4).background(color).add(PeakLabel.create(text));
    }

    private void fill(PeakTableLayoutPanel table, int count) {
        for (int i = 1; i <= count; i++) table.add(PeakLabel.create("Cell " + i));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("3 columns, 9 cells", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(3).columnSpacing(6).rowSpacing(2)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    fill(table, 9);
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("minColumnWidth forcing even columns", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(3).minColumnWidth(60)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.add(PeakLabel.create("A"));
                    table.add(PeakLabel.create("Bcdefgh"));
                    table.add(PeakLabel.create("Z"));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("emptyRow(12) between rows", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(2)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.add(PeakLabel.create("Row 1a"));
                    table.add(PeakLabel.create("Row 1b"));
                    table.emptyRow(12);
                    table.add(PeakLabel.create("Row 3a"));
                    table.add(PeakLabel.create("Row 3b"));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Tracks: columns(\"60px 1fr 25%\") in a 240 px wide table", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns("60px 1fr 25%").spacing(2)
                            .width(240).background(0x60000000)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    for (int i = 1; i <= 6; i++) table.add(box("C" + i, 0x6040A0FF));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("fill weights 1fr 2fr and min/max limits", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel weights = PeakTableLayoutPanel.create().columns("1fr 2fr").spacing(2)
                            .width(180).background(0x60000000)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, -50));
                    weights.row(box("1fr", 0x6040A0FF), box("2fr", 0x60FFA040));
                    PeakTableLayoutPanel limits = PeakTableLayoutPanel.create()
                            .columns(PeakTrack.px(40), PeakTrack.fill().min(60).max(90), PeakTrack.fill())
                            .spacing(2).width(300).background(0x60000000)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 10));
                    limits.row(box("40px", 0x6040A0FF),
                            box("fill 60..90", 0x60FFA040),
                            box("fill", 0x6040FFA0));
                    doc.add(weights);
                    doc.add(limits);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("row(...) pads incomplete rows and empty() skips a slot", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(3).spacing(3)
                            .background(0x60000000).absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.row(box("A1", 0x6040A0FF), box("A2", 0x6040A0FF));
                    table.row(box("B1", 0x60FFA040));
                    table.add(box("C1", 0x6040FFA0));
                    table.empty();
                    table.add(box("C3", 0x6040FFA0));
                    table.row(box("D1", 0x60FF60A0));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Margins count into the columns, gap is the spacing fallback", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns(2).gap(6)
                            .background(0x60000000).absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.row(box("Margin 8", 0x6040A0FF).margin(8), box("Plain", 0x60FFA040));
                    table.row(box("Plain", 0x60FFA040), box("Margin 4/12", 0x6040A0FF).margin(4, 12));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Spans: colSpan(3) title and rowSpan(2) cell", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns("70px 70px 70px").spacing(3)
                            .background(0x60000000).absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.add(box("Title across three columns", 0x60FFA040).colSpan(3));
                    table.add(box("Tall\ncell", 0x6040A0FF).rowSpan(2));
                    table.add(box("B1", 0x6040FFA0));
                    table.add(box("C1", 0x6040FFA0));
                    table.add(box("B2", 0x6040FFA0));
                    table.add(box("C2", 0x6040FFA0));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Alignment: column tracks (start, center, end) and one cell override", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create()
                            .columns(PeakTrack.px(100).alignStart(), PeakTrack.px(100).alignCenter(), PeakTrack.px(100).alignEnd())
                            .spacing(2).background(0x60000000).absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.row(box("start", 0x6040A0FF), box("center", 0x6040A0FF),
                            box("end", 0x6040A0FF));
                    table.row(box("a", 0x60FFA040), box("b", 0x60FFA040),
                            box("end (override: start)", 0x60FFA040).alignSelf(PeakAlign.START));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Vertical alignment in 30 px rows: rows track, cellAlign and verticalAlign", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns("70px 70px 70px").rowHeight(30)
                            .spacing(2).cellAlign(PeakAlign.CENTER, PeakAlign.CENTER)
                            .background(0x60000000).absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.row(box("centered", 0x6040A0FF),
                            box("top", 0x60FFA040).verticalAlign(PeakAlign.START),
                            box("bottom", 0x6040FFA0).verticalAlign(PeakAlign.END).alignSelf(PeakAlign.END));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Percent sizes refer to the cell: widthPercent(100) fills the column, heightPercent(100) the row", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns("80px 1fr").rowHeight(24)
                            .spacing(2).width(240).background(0x60000000).absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.row(box("full", 0x6040A0FF).widthPercent(100).heightPercent(100),
                            box("half", 0x60FFA040).widthPercent(50).heightPercent(100));
                    table.row(box("narrow", 0x6040FFA0).widthPercent(50),
                            box("fill column", 0x60FF60A0).widthPercent(100));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Decor: header row, zebra stripes and grid lines", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create()
                            .columns(PeakTrack.px(24).alignCenter(), PeakTrack.fill(), PeakTrack.px(60).alignEnd())
                            .spacing(4).cellPadding(2, 4).width(240).background(0x90000000)
                            .decor(d -> d.headerRows(1).headerBackground(0xC03050A0)
                                    .stripes(0x30FFFFFF, 0x10FFFFFF).lines(1, 0x60FFFFFF))
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.row(PeakLabel.create("#").color(0xFFFF55), PeakLabel.create("Player").color(0xFFFF55),
                            PeakLabel.create("Score").color(0xFFFF55));
                    for (int i = 1; i <= 6; i++) {
                        table.row(PeakLabel.create(String.valueOf(i)), PeakLabel.create("Player " + i),
                                PeakLabel.create(String.valueOf(i * 120)));
                    }
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Decor: row lines only, no spacing, two header rows", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns("80px 80px")
                            .cellPadding(3).background(0x90000000)
                            .decor(d -> d.headerRows(2).headerBackground(0xC0A05030)
                                    .lines(1, 0x80FFFFFF, PeakTableStyle.LineMode.ROWS))
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.add(PeakLabel.create("Team").colSpan(2).color(0xFFFF55));
                    table.row(PeakLabel.create("Name"), PeakLabel.create("Kills"));
                    for (int i = 1; i <= 4; i++) table.row(PeakLabel.create("P" + i), PeakLabel.create(String.valueOf(i * 3)));
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Decor per index: highlighted row 3, tinted column 2, thick line below the header", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel table = PeakTableLayoutPanel.create()
                            .columns(PeakTrack.px(24).alignCenter(), PeakTrack.fill(), PeakTrack.px(60).alignEnd())
                            .spacing(4).cellPadding(2, 4).width(240).background(0x90000000)
                            .decor(d -> d.headerRows(1).headerBackground(0xC03050A0).lines(1, 0x40FFFFFF)
                                    .row(0, s -> s.line(3, 0xFFFFD700))
                                    .row(3, s -> s.background(0x60FFD700).noLine())
                                    .column(2, s -> s.background(0x3055FF55)))
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));
                    table.row(PeakLabel.create("#").color(0xFFFF55), PeakLabel.create("Player").color(0xFFFF55),
                            PeakLabel.create("Score").color(0xFFFF55));
                    for (int i = 1; i <= 5; i++) {
                        table.row(PeakLabel.create(String.valueOf(i)), PeakLabel.create("Player " + i),
                                PeakLabel.create(String.valueOf(i * 120)));
                    }
                    doc.add(table);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("Lines and spans: interrupted by default (left), linesAcrossSpans(true) (right)", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    for (int side = 0; side < 2; side++) {
                        boolean across = side == 1;
                        PeakTableLayoutPanel table = PeakTableLayoutPanel.create().columns("50px 50px 50px")
                                .cellPadding(3).background(0x90000000)
                                .decor(d -> d.lines(1, 0xFFFFFFFF).linesAcrossSpans(across))
                                .absolute(PeakPosition.of(PeakAnchor.CENTER, across ? 90 : -90, 0));
                        table.add(PeakLabel.create("Title (3 columns)").colSpan(3));
                        table.add(PeakLabel.create("Tall").rowSpan(2));
                        table.add(PeakLabel.create("B1"));
                        table.add(PeakLabel.create("C1"));
                        table.add(PeakLabel.create("B2"));
                        table.add(PeakLabel.create("C2"));
                        doc.add(table);
                    }
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("columnSpacing 0 (left) vs. 20 and rowSpacing 0 vs. 8 (right)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakTableLayoutPanel tight = PeakTableLayoutPanel.create().columns(2).columnSpacing(0).rowSpacing(0)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, -70, 0));
                    PeakTableLayoutPanel wide = PeakTableLayoutPanel.create().columns(2).columnSpacing(20).rowSpacing(8)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 70, 0));
                    fill(tight, 4);
                    fill(wide, 4);
                    doc.add(tight);
                    doc.add(wide);
                    PeakClient.to(player).sendUi(doc);
                })
        );
    }
}