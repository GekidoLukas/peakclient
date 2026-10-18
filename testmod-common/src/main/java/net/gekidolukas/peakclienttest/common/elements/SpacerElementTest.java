package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class SpacerElementTest implements PeakElementTest {
    @Override public String name() { return "spacer"; }
    @Override public String description() { return "PeakSpacer: fixed gap between flow elements in a row and in a column"; }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("row without a spacer (labels touching)", 60, doc ->
                        doc.add(PeakPanel.create().row().absolute(PeakAnchor.CENTER, 0, 0)
                                .add(PeakLabel.create("Left"), PeakLabel.create("Right")))),
                PeakElementTestStep.document("row with a 40px spacer between", 60, doc ->
                        doc.add(PeakPanel.create().row().absolute(PeakAnchor.CENTER, 0, 0)
                                .add(PeakLabel.create("Left"), PeakSpacer.create(40, 1), PeakLabel.create("Right")))),
                PeakElementTestStep.document("column with a 20px high spacer between", 60, doc ->
                        doc.add(PeakPanel.create().column().absolute(PeakAnchor.CENTER, 0, 0)
                                .add(PeakLabel.create("Top"), PeakSpacer.create(1, 20), PeakLabel.create("Bottom"))))
        );
    }
}
