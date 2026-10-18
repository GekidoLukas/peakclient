package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.client.PeakMenuFocus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakMenuFocusTest {
    private static PeakLayoutEntry entry(PeakElement<?> element, int depth, int x, int y, int w, int h, boolean invisible) {
        return new PeakLayoutEntry("menu", element.getId(), element.getClass().getSimpleName(), w, h, x, y, depth,
                element, -1, w, h, 1f, -1, -1, invisible,
                PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MAX, PeakLayoutEntry.NO_CLIP_MAX);
    }

    private static PeakLayoutEntry entry(PeakElement<?> element, int depth, int x, int y, int w, int h) {
        return entry(element, depth, x, y, w, h, false);
    }

    private static PeakPanel scrollList(String id) {
        return PeakPanel.create(id).column().padding(0).overflow(PeakOverflow.SCROLL);
    }

    @Test
    void anItemBelowTheViewportScrollsDownJustEnough() {
        PeakPanel list = scrollList("list");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 50));
        probe.record(entry(item, 2, 0, 60, 80, 20));

        PeakMenuFocus.Plan plan = PeakMenuFocus.plan(probe, probe.entries().get(1));

        assertTrue(plan.reachable());
        assertEquals(1, plan.scrolls().size());
        assertEquals("list", plan.scrolls().get(0).containerId());
        assertEquals(30, plan.scrolls().get(0).delta());
    }

    @Test
    void anItemAboveTheViewportScrollsUp() {
        PeakPanel list = scrollList("list");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 50));
        probe.record(entry(item, 2, 0, -30, 80, 20));

        PeakMenuFocus.Plan plan = PeakMenuFocus.plan(probe, probe.entries().get(1));

        assertEquals(-30, plan.scrolls().get(0).delta());
    }

    @Test
    void aVisibleItemNeedsNoScrolling() {
        PeakPanel list = scrollList("list");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 50));
        probe.record(entry(item, 2, 0, 10, 80, 20));

        PeakMenuFocus.Plan plan = PeakMenuFocus.plan(probe, probe.entries().get(1));

        assertTrue(plan.reachable());
        assertTrue(plan.scrolls().isEmpty());
    }

    @Test
    void anItemTallerThanTheViewportAlignsToTheTop() {
        PeakPanel list = scrollList("list");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 50));
        probe.record(entry(item, 2, 0, 20, 80, 90));

        assertEquals(20, PeakMenuFocus.plan(probe, probe.entries().get(1)).scrolls().get(0).delta());
    }

    @Test
    void thePaddingOfTheContainerCountsAsOutsideTheViewport() {
        PeakPanel list = PeakPanel.create("list").column().padding(10).overflow(PeakOverflow.SCROLL);
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 50));
        probe.record(entry(item, 2, 10, 25, 80, 20));

        assertEquals(5, PeakMenuFocus.plan(probe, probe.entries().get(1)).scrolls().get(0).delta());
    }

    @Test
    void rowsScrollAlongTheXAxis() {
        PeakPanel strip = PeakPanel.create("strip").row().padding(0).overflow(PeakOverflow.SCROLL);
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(strip, 1, 0, 0, 100, 30));
        probe.record(entry(item, 2, 120, 0, 30, 20));

        PeakMenuFocus.Plan plan = PeakMenuFocus.plan(probe, probe.entries().get(1));

        assertEquals(50, plan.scrolls().get(0).delta());
    }

    @Test
    void nestedListsScrollFromTheInsideOut() {
        PeakPanel outer = scrollList("outer");
        PeakPanel inner = scrollList("inner");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(outer, 1, 0, 0, 200, 100));
        probe.record(entry(inner, 2, 0, 150, 100, 40));
        probe.record(entry(item, 3, 0, 200, 80, 20));

        PeakMenuFocus.Plan plan = PeakMenuFocus.plan(probe, probe.entries().get(2));

        assertTrue(plan.reachable());
        assertEquals(2, plan.scrolls().size());
        assertEquals("inner", plan.scrolls().get(0).containerId());
        assertEquals(30, plan.scrolls().get(0).delta());
        assertEquals("outer", plan.scrolls().get(1).containerId());
        assertEquals(90, plan.scrolls().get(1).delta());
    }

    @Test
    void anItemClippedByAHiddenContainerIsNotReachable() {
        PeakPanel clip = PeakPanel.create("clip").column().padding(0).overflow(PeakOverflow.HIDDEN);
        PeakButton inside = PeakButton.create("inside");
        PeakButton outside = PeakButton.create("outside");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(clip, 1, 0, 0, 100, 50));
        probe.record(entry(inside, 2, 0, 10, 80, 20));
        probe.record(entry(outside, 2, 0, 80, 80, 20));

        assertTrue(PeakMenuFocus.isReachable(probe, probe.entries().get(1)));
        assertFalse(PeakMenuFocus.isReachable(probe, probe.entries().get(2)));
    }

    @Test
    void anItemInsideAScrollListInsideAHiddenBoxIsReachableAfterScrolling() {
        PeakPanel clip = PeakPanel.create("clip").column().padding(0).overflow(PeakOverflow.HIDDEN);
        PeakPanel list = scrollList("list");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(clip, 1, 0, 0, 200, 200));
        probe.record(entry(list, 2, 0, 0, 100, 50));
        probe.record(entry(item, 3, 0, 120, 80, 20));

        PeakMenuFocus.Plan plan = PeakMenuFocus.plan(probe, probe.entries().get(2));

        assertTrue(plan.reachable());
        assertEquals(90, plan.scrolls().get(0).delta());
    }

    @Test
    void invisibleEntriesAreNeverReachable() {
        PeakPanel list = scrollList("list");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 50));
        probe.record(entry(item, 2, 0, 10, 80, 20, true));

        assertFalse(PeakMenuFocus.isReachable(probe, probe.entries().get(1)));
    }
}
