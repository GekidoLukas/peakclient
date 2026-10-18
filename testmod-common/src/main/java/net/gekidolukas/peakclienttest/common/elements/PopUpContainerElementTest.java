package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;

import java.util.ArrayList;
import java.util.List;

public final class PopUpContainerElementTest implements PeakElementTest {
    @Override public String name() { return "popup"; }
    @Override public String description() { return "PeakPopUpContainer: every animation, scale origin, slide distance, durations, animateEnter and animateExit"; }

    private static PeakPopUpContainer popup(String text) {
        PeakPopUpContainer popup = PeakPopUpContainer.create().timeToLiveTicks(40)
                .absolute(PeakAnchor.CENTER, 0, 0).size(160, 60).background(0xFF202030);
        popup.add(PeakLabel.create(text).absolute(PeakAnchor.CENTER));
        return popup;
    }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();
        for (PeakPopupAnimation animation : PeakPopupAnimation.values()) {
            steps.add(PeakElementTestStep.document("animation " + animation.name() + " (enter and exit)", 80, doc ->
                    doc.add(popup(animation.name()).enterAnimation(animation).exitAnimation(animation))));
        }
        steps.add(PeakElementTestStep.document("SCALE grows from the center, not from a corner", 80, doc ->
                doc.add(popup("Grows from the center").enterAnimation(PeakPopupAnimation.SCALE).size(150, 60))));
        steps.add(PeakElementTestStep.document("slideDistance: 20 (top) vs. 120 (bottom) with SLIDE_FROM_LEFT", 100, doc -> {
            doc.add(popup("Distance 20").enterAnimation(PeakPopupAnimation.SLIDE_FROM_LEFT).slideDistance(20).absolute(PeakAnchor.CENTER, 0, -50));
            doc.add(popup("Distance 120").enterAnimation(PeakPopupAnimation.SLIDE_FROM_LEFT).slideDistance(120).absolute(PeakAnchor.CENTER, 0, 50));
        }));
        steps.add(PeakElementTestStep.document("durations: a slow enter of 40 ticks and a slow exit of 40 ticks with FADE", 140, doc ->
                doc.add(popup("Slow fade").enterAnimation(PeakPopupAnimation.FADE).exitAnimation(PeakPopupAnimation.FADE)
                        .enterDurationTicks(40).exitDurationTicks(40).timeToLiveTicks(60))));
        steps.add(PeakElementTestStep.document("animateEnter=false: the popup is there at once and leaves with the exit animation", 100, doc ->
                doc.add(popup("No enter animation").enterAnimation(PeakPopupAnimation.SCALE).animateEnter(false)
                        .exitAnimation(PeakPopupAnimation.FADE))));
        steps.add(PeakElementTestStep.document("animateExit=false: the popup disappears without an animation after its time to live", 100, doc ->
                doc.add(popup("No exit animation").enterAnimation(PeakPopupAnimation.FADE)
                        .exitAnimation(PeakPopupAnimation.SCALE).animateExit(false))));
        steps.add(PeakElementTestStep.of("notifySuccess, notifyWarning and notifyError: green, orange and red notifications one after another", 160,
                (player, docId) -> PeakTestTimeline.create(player)
                        .atSeconds(0.5, () -> PeakClient.to(player).notifySuccess("notifySuccess: green"))
                        .atSeconds(1.5, () -> PeakClient.to(player).notifyWarning("notifyWarning: orange"))
                        .atSeconds(2.5, () -> PeakClient.to(player).notifyError("notifyError: red"))
                        .start()));
        return steps;
    }
}
