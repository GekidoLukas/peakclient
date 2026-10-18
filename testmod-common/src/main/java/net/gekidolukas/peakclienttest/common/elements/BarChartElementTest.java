package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class BarChartElementTest implements PeakElementTest {
    @Override public String name() { return "barchart"; }
    @Override public String description() { return "PeakBarChart: entries, maxValue, labels, label position, spacing, padding, colors"; }

    private static PeakBarChart chart() {
        return PeakBarChart.create()
                .addEntry("Map A", 12, PeakPaint.rgb(85, 170, 255))
                .addEntry("Map B", 8, PeakPaint.rgb(255, 170, 85))
                .addEntry("Map C", 15, PeakPaint.rgb(85, 255, 120))
                .size(200, 100)
                .absolute(PeakAnchor.CENTER, 0, 0);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("entries: three bars scaled to the largest value", 80, doc -> doc.add(chart())),
                PeakElementTestStep.document("maxValue 30: the bars only reach half of the height", 80, doc -> doc.add(chart().maxValue(30))),
                PeakElementTestStep.document("valueLabelPosition INSIDE vs. OUTSIDE", 80, doc -> {
                    doc.add(chart().valueLabelPosition(PeakValueLabelPosition.INSIDE).absolute(PeakAnchor.CENTER, -110, 0));
                    doc.add(chart().valueLabelPosition(PeakValueLabelPosition.OUTSIDE).absolute(PeakAnchor.CENTER, 110, 0));
                }),
                PeakElementTestStep.document("showValueLabels=false (left) and showCategoryLabels=false (right)", 80, doc -> {
                    doc.add(chart().showValueLabels(false).absolute(PeakAnchor.CENTER, -110, 0));
                    doc.add(chart().showCategoryLabels(false).absolute(PeakAnchor.CENTER, 110, 0));
                }),
                PeakElementTestStep.document("barSpacing 2 (left) vs. 16 (right)", 80, doc -> {
                    doc.add(chart().barSpacing(2).absolute(PeakAnchor.CENTER, -110, 0));
                    doc.add(chart().barSpacing(16).absolute(PeakAnchor.CENTER, 110, 0));
                }),
                PeakElementTestStep.document("topPadding 0 (left) vs. 30 (right)", 80, doc -> {
                    doc.add(chart().topPadding(0).absolute(PeakAnchor.CENTER, -110, 0));
                    doc.add(chart().topPadding(30).absolute(PeakAnchor.CENTER, 110, 0));
                }),
                PeakElementTestStep.document("colors: backgroundColor, categoryLabelColor and valueLabelColor", 80, doc ->
                        doc.add(chart().backgroundColor(PeakPaint.rgba(0, 0, 0, 140)).categoryLabelColor(0xFFFF55)
                                .valueLabelColor(0x55FFFF).valueLabelPosition(PeakValueLabelPosition.OUTSIDE))),
                PeakElementTestStep.document("many entries: eight bars with long labels", 80, doc -> {
                    PeakBarChart many = PeakBarChart.create().size(260, 100).absolute(PeakAnchor.CENTER, 0, 0);
                    for (int i = 1; i <= 8; i++) many.addEntry("Entry " + i, i * 3, PeakPaint.rgb(30 * i, 255 - 25 * i, 160));
                    doc.add(many);
                })
        );
    }
}
