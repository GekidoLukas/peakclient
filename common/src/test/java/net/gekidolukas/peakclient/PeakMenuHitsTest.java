package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDiv;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.client.PeakMenuHits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakMenuHitsTest {
    private static PeakLayoutEntry entry(PeakElement<?> element, int depth, int x, int y, int w, int h) {
        return new PeakLayoutEntry("menu", element.getId(), element.getClass().getSimpleName(), w, h, x, y, depth,
                element, -1, w, h, 1f, -1, -1, false,
                PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MAX, PeakLayoutEntry.NO_CLIP_MAX);
    }

    @Test
    void aClickOnTheLabelInsideAButtonHitsTheButton() {
        PeakLayoutProbe probe = new PeakLayoutProbe();
        PeakPanel panel = PeakPanel.create("panel");
        PeakButton button = PeakButton.text("button", "Buy").onClick(PeakAction.send("buy"));
        PeakLabel label = PeakLabel.create("label", "Buy");
        probe.record(entry(panel, 1, 0, 0, 200, 100));
        probe.record(entry(button, 2, 10, 10, 80, 20));
        probe.record(entry(label, 3, 20, 14, 40, 10));

        PeakLayoutEntry hit = PeakMenuHits.interactiveAt(probe, 25, 18);

        assertNotNull(hit);
        assertSame(button, hit.element());
    }

    @Test
    void clicksOutsideInteractiveElementsHitNothing() {
        PeakLayoutProbe probe = new PeakLayoutProbe();
        PeakPanel panel = PeakPanel.create("panel");
        PeakButton button = PeakButton.text("button", "Buy").onClick(PeakAction.send("buy"));
        probe.record(entry(panel, 1, 0, 0, 200, 100));
        probe.record(entry(button, 2, 10, 10, 80, 20));

        assertNull(PeakMenuHits.interactiveAt(probe, 150, 80));
    }

    @Test
    void emptyContainersDoNotBlockTheButtonsBelowThem() {
        PeakLayoutProbe probe = new PeakLayoutProbe();
        PeakButton button = PeakButton.text("button", "Buy").onClick(PeakAction.send("buy"));
        PeakDiv overlay = PeakDiv.create("overlay");
        probe.record(entry(button, 1, 10, 10, 80, 20));
        probe.record(entry(overlay, 1, 0, 0, 200, 100));

        PeakLayoutEntry hit = PeakMenuHits.interactiveAt(probe, 20, 15);

        assertNotNull(hit);
        assertSame(button, hit.element());
    }

    @Test
    void paintedContainersBlockTheButtonsBelowThem() {
        PeakLayoutProbe probe = new PeakLayoutProbe();
        PeakButton button = PeakButton.text("button", "Buy").onClick(PeakAction.send("buy"));
        PeakPanel cover = PeakPanel.create("cover");
        probe.record(entry(button, 1, 10, 10, 80, 20));
        probe.record(entry(cover, 1, 0, 0, 200, 100));

        assertNull(PeakMenuHits.interactiveAt(probe, 20, 15));
    }
}
