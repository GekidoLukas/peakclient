package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.client.PeakMenuScrollKeys;
import net.gekidolukas.peakclient.ui.client.PeakMenuScrollKeys.Axis;
import net.gekidolukas.peakclient.ui.client.PeakMenuScrollKeys.Command;
import net.gekidolukas.peakclient.ui.client.PeakMenuScrollState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakMenuScrollKeysTest {
    private static PeakLayoutEntry entry(PeakElement<?> element, int depth, int x, int y, int w, int h) {
        return new PeakLayoutEntry("menu", element.getId(), element.getClass().getSimpleName(), w, h, x, y, depth,
                element, -1, w, h, 1f, -1, -1, false,
                PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MAX, PeakLayoutEntry.NO_CLIP_MAX);
    }

    private static PeakPanel column(String id) {
        return PeakPanel.create(id).column().padding(0).overflow(PeakOverflow.SCROLL);
    }

    private static PeakPanel row(String id) {
        return PeakPanel.create(id).row().padding(0).overflow(PeakOverflow.SCROLL);
    }

    private static PeakMenuScrollState scrolled(String id, int max, int offset) {
        PeakMenuScrollState state = new PeakMenuScrollState();
        state.report(id, max);
        state.set(id, offset);
        return state;
    }

    @Test
    void aLineMovesByTheWheelStepInBothDirections() {
        PeakPanel list = column("list");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 100));
        probe.record(entry(item, 2, 0, 0, 80, 20));
        PeakLayoutEntry focus = probe.entries().get(1);
        PeakMenuScrollState state = scrolled("list", 100, 40);

        PeakMenuScrollKeys.Step down = PeakMenuScrollKeys.plan(probe, focus, null, state, Command.LINE_FORWARD, Axis.VERTICAL);
        PeakMenuScrollKeys.Step up = PeakMenuScrollKeys.plan(probe, focus, null, state, Command.LINE_BACK, Axis.VERTICAL);

        assertEquals("list", down.containerId());
        assertEquals(PeakMenuScrollState.WHEEL_STEP, down.delta());
        assertEquals(-PeakMenuScrollState.WHEEL_STEP, up.delta());
    }

    @Test
    void aPageIsTheBoxMinusOneLine() {
        PeakPanel list = column("list");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 100));
        PeakMenuScrollState state = scrolled("list", 500, 0);

        PeakMenuScrollKeys.Step page = PeakMenuScrollKeys.plan(probe, probe.entries().get(0), null, state,
                Command.PAGE_FORWARD, Axis.VERTICAL);

        assertEquals(100 - PeakMenuScrollState.WHEEL_STEP, page.delta());
    }

    @Test
    void startAndEndJumpToTheEdges() {
        PeakPanel list = column("list");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 100));
        PeakLayoutEntry focus = probe.entries().get(0);
        PeakMenuScrollState state = scrolled("list", 300, 120);

        PeakMenuScrollKeys.Step end = PeakMenuScrollKeys.plan(probe, focus, null, state, Command.END, Axis.ANY);
        PeakMenuScrollKeys.Step start = PeakMenuScrollKeys.plan(probe, focus, null, state, Command.START, Axis.ANY);

        assertTrue(end.delta() >= 300);
        assertTrue(-start.delta() >= 120);
    }

    @Test
    void nothingHappensAtTheEdges() {
        PeakPanel list = column("list");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 100));
        PeakLayoutEntry focus = probe.entries().get(0);

        assertNull(PeakMenuScrollKeys.plan(probe, focus, null, scrolled("list", 100, 0), Command.LINE_BACK, Axis.VERTICAL));
        assertNull(PeakMenuScrollKeys.plan(probe, focus, null, scrolled("list", 100, 100), Command.LINE_FORWARD, Axis.VERTICAL));
        assertNull(PeakMenuScrollKeys.plan(probe, focus, null, scrolled("list", 0, 0), Command.LINE_FORWARD, Axis.VERTICAL));
    }

    @Test
    void theAxisMustMatchTheContainer() {
        PeakPanel list = column("list");
        PeakPanel strip = row("strip");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 100));
        probe.record(entry(strip, 1, 0, 120, 100, 30));
        PeakMenuScrollState state = new PeakMenuScrollState();
        state.report("list", 100);
        state.report("strip", 100);

        PeakMenuScrollKeys.Step horizontal = PeakMenuScrollKeys.plan(probe, null, probe.entries().get(1), state,
                Command.LINE_FORWARD, Axis.HORIZONTAL);
        PeakMenuScrollKeys.Step vertical = PeakMenuScrollKeys.plan(probe, null, probe.entries().get(1), state,
                Command.LINE_FORWARD, Axis.VERTICAL);

        assertEquals("strip", horizontal.containerId());
        assertNull(vertical);
    }

    @Test
    void theInnermostMovableContainerWinsAndTheOuterOneTakesOver() {
        PeakPanel outer = column("outer");
        PeakPanel inner = column("inner");
        PeakButton item = PeakButton.create("item");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(outer, 1, 0, 0, 200, 200));
        probe.record(entry(inner, 2, 0, 0, 150, 80));
        probe.record(entry(item, 3, 0, 0, 100, 20));
        PeakLayoutEntry focus = probe.entries().get(2);
        PeakMenuScrollState state = new PeakMenuScrollState();
        state.report("outer", 400);
        state.report("inner", 60);

        PeakMenuScrollKeys.Step first = PeakMenuScrollKeys.plan(probe, focus, null, state, Command.LINE_FORWARD, Axis.VERTICAL);
        state.set("inner", 60);
        PeakMenuScrollKeys.Step second = PeakMenuScrollKeys.plan(probe, focus, null, state, Command.LINE_FORWARD, Axis.VERTICAL);

        assertEquals("inner", first.containerId());
        assertEquals("outer", second.containerId());
    }

    @Test
    void theFocusBeatsTheMouseAndTheMouseBeatsTheOnlyContainerRule() {
        PeakPanel left = column("left");
        PeakPanel right = column("right");
        PeakButton focused = PeakButton.create("focused");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(left, 1, 0, 0, 100, 100));
        probe.record(entry(focused, 2, 0, 0, 80, 20));
        probe.record(entry(right, 1, 150, 0, 100, 100));
        PeakMenuScrollState state = new PeakMenuScrollState();
        state.report("left", 100);
        state.report("right", 100);

        PeakMenuScrollKeys.Step byFocus = PeakMenuScrollKeys.plan(probe, probe.entries().get(1), probe.entries().get(2),
                state, Command.LINE_FORWARD, Axis.VERTICAL);
        PeakMenuScrollKeys.Step byMouse = PeakMenuScrollKeys.plan(probe, null, probe.entries().get(2),
                state, Command.LINE_FORWARD, Axis.VERTICAL);
        PeakMenuScrollKeys.Step ambiguous = PeakMenuScrollKeys.plan(probe, null, null,
                state, Command.LINE_FORWARD, Axis.VERTICAL);

        assertEquals("left", byFocus.containerId());
        assertEquals("right", byMouse.containerId());
        assertNull(ambiguous);
    }

    @Test
    void aSingleContainerIsUsedWithoutFocusAndMouse() {
        PeakPanel list = column("list");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry(list, 1, 0, 0, 100, 100));
        PeakMenuScrollState state = scrolled("list", 100, 0);

        PeakMenuScrollKeys.Step step = PeakMenuScrollKeys.plan(probe, null, null, state, Command.LINE_FORWARD, Axis.VERTICAL);

        assertEquals("list", step.containerId());
    }

    @Test
    void aScaledContainerPagesInItsOwnUnits() {
        PeakPanel list = column("list");
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(new PeakLayoutEntry("menu", "list", "PeakPanel", 200, 200, 0, 0, 1, list, -1, 100, 100, 2f,
                -1, -1, false, PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MIN,
                PeakLayoutEntry.NO_CLIP_MAX, PeakLayoutEntry.NO_CLIP_MAX));
        PeakMenuScrollState state = scrolled("list", 500, 0);

        PeakMenuScrollKeys.Step page = PeakMenuScrollKeys.plan(probe, probe.entries().get(0), null, state,
                Command.PAGE_FORWARD, Axis.VERTICAL);

        assertEquals(100 - PeakMenuScrollState.WHEEL_STEP, page.delta());
    }
}
