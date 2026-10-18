package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class PieChartElementTest implements PeakElementTest {
    @Override public String name() { return "piechart"; }
    @Override public String description() { return "PeakPieChart: entries, donut thickness, start angle, labels, legend position, background"; }

    private static PeakPieChart chart() {
        return PeakPieChart.create()
                .addEntry("Red", 60, PeakPaint.rgb(220, 60, 60))
                .addEntry("Blue", 40, PeakPaint.rgb(60, 100, 220))
                .size(80, 80)
                .absolute(PeakAnchor.CENTER, 0, 0);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("entries: two segments, 60 and 40 percent", 80, doc -> doc.add(chart())),
                PeakElementTestStep.document("three segments with a small third entry", 80, doc ->
                        doc.add(chart().addEntry("Green", 10, PeakPaint.rgb(60, 200, 90)))),
                PeakElementTestStep.document("donutThickness 0 (full pie), 10 and 25", 80, doc -> {
                    doc.add(chart().donutThickness(0).absolute(PeakAnchor.CENTER, -100, 0));
                    doc.add(chart().donutThickness(10).absolute(PeakAnchor.CENTER, 0, 0));
                    doc.add(chart().donutThickness(25).absolute(PeakAnchor.CENTER, 100, 0));
                }),
                PeakElementTestStep.document("startAngle 0, 90 and 180", 80, doc -> {
                    doc.add(chart().startAngle(0).absolute(PeakAnchor.CENTER, -100, 0));
                    doc.add(chart().startAngle(90).absolute(PeakAnchor.CENTER, 0, 0));
                    doc.add(chart().startAngle(180).absolute(PeakAnchor.CENTER, 100, 0));
                }),
                PeakElementTestStep.document("showLabels true (left) vs. false (right)", 80, doc -> {
                    doc.add(chart().showLabels(true).absolute(PeakAnchor.CENTER, -70, 0));
                    doc.add(chart().showLabels(false).absolute(PeakAnchor.CENTER, 70, 0));
                }),
                PeakElementTestStep.document("legendPosition: BOTTOM, RIGHT, TOP, LEFT and NONE", 120, doc -> {
                    PeakLegendPosition[] positions = PeakLegendPosition.values();
                    for (int i = 0; i < positions.length; i++) {
                        doc.add(chart().legendPosition(positions[i]).size(60, 60).absolute(PeakAnchor.TOP_LEFT, 20 + i * 90, 30));
                        doc.add(PeakLabel.create(positions[i].name()).absolute(PeakAnchor.TOP_LEFT, 20 + i * 90, 10));
                    }
                }),
                PeakElementTestStep.document("backgroundColor: a dark translucent plate behind the chart", 80, doc ->
                        doc.add(chart().backgroundColor(PeakPaint.rgba(0, 0, 0, 140)).legendPosition(PeakLegendPosition.BOTTOM)))
        );
    }
}
