package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakCycleOrder;
import net.gekidolukas.peakclient.ui.PeakCycleSequence;
import net.gekidolukas.peakclient.ui.PeakCyclerTimeline;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakCyclerTimelineTest {
    private record Item(String name) {}

    private static final PeakCyclerTimeline.Config SEQUENTIAL =
            new PeakCyclerTimeline.Config(10, 5, 5, PeakCycleOrder.SEQUENTIAL, false, false);
    private static final PeakCyclerTimeline.Config OVERLAP =
            new PeakCyclerTimeline.Config(10, 5, 5, PeakCycleOrder.SEQUENTIAL, true, false);

    private static PeakCyclerTimeline<Item> timeline() {
        return new PeakCyclerTimeline<>(new PeakCycleSequence<>(new Random(1)));
    }

    @Test
    void sequentialRunsEnterHoldExitAndThenTheNextItem() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item a = new Item("a"), b = new Item("b");
        List<Item> items = List.of(a, b);

        var first = timeline.update(0, items, SEQUENTIAL);
        assertEquals(1, first.size());
        assertSame(a, first.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.ENTER, first.get(0).phase());
        assertEquals(0f, first.get(0).progress());

        var entering = timeline.update(2.5, items, SEQUENTIAL);
        assertEquals(0.5f, entering.get(0).progress(), 0.001f);

        var holding = timeline.update(9, items, SEQUENTIAL);
        assertEquals(PeakCyclerTimeline.Phase.HOLD, holding.get(0).phase());

        var exiting = timeline.update(17.5, items, SEQUENTIAL);
        assertSame(a, exiting.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.EXIT, exiting.get(0).phase());
        assertEquals(0.5f, exiting.get(0).progress(), 0.001f);

        var next = timeline.update(20, items, SEQUENTIAL);
        assertEquals(1, next.size());
        assertSame(b, next.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.ENTER, next.get(0).phase());
    }

    @Test
    void overlapShowsTheExitingAndTheEnteringItemTogether() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item a = new Item("a"), b = new Item("b");
        List<Item> items = List.of(a, b);
        timeline.update(0, items, OVERLAP);

        var frame = timeline.update(17.5, items, OVERLAP);

        assertEquals(2, frame.size());
        assertSame(a, frame.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.EXIT, frame.get(0).phase());
        assertEquals(0.5f, frame.get(0).progress(), 0.001f);
        assertSame(b, frame.get(1).item());
        assertEquals(PeakCyclerTimeline.Phase.ENTER, frame.get(1).phase());
        assertEquals(0.5f, frame.get(1).progress(), 0.001f);

        var later = timeline.update(21, items, OVERLAP);
        assertEquals(1, later.size());
        assertSame(b, later.get(0).item());
    }

    @Test
    void aSingleItemStaysVisible() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item only = new Item("only");

        timeline.update(0, List.of(only), SEQUENTIAL);
        var frame = timeline.update(1000, List.of(only), SEQUENTIAL);

        assertEquals(1, frame.size());
        assertSame(only, frame.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.HOLD, frame.get(0).phase());
    }

    @Test
    void nextSkipsTheRestOfTheHold() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item a = new Item("a"), b = new Item("b");
        List<Item> items = List.of(a, b);
        timeline.update(0, items, SEQUENTIAL);
        timeline.update(7, items, SEQUENTIAL);

        timeline.requestNext();
        var frame = timeline.update(8, items, SEQUENTIAL);

        assertSame(a, frame.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.EXIT, frame.get(0).phase());
        var after = timeline.update(13.5, items, SEQUENTIAL);
        assertSame(b, after.get(0).item());
    }

    @Test
    void showJumpsToTheRequestedItemAfterTheExit() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item a = new Item("a"), b = new Item("b"), c = new Item("c");
        List<Item> items = List.of(a, b, c);
        timeline.update(0, items, SEQUENTIAL);

        timeline.requestShow(c);
        timeline.update(1, items, SEQUENTIAL);
        var frame = timeline.update(7, items, SEQUENTIAL);

        assertSame(c, frame.get(0).item());
    }

    @Test
    void pausingFreezesTheProgress() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item a = new Item("a"), b = new Item("b");
        List<Item> items = List.of(a, b);
        PeakCyclerTimeline.Config paused = new PeakCyclerTimeline.Config(10, 5, 5, PeakCycleOrder.SEQUENTIAL, false, true);
        timeline.update(0, items, SEQUENTIAL);
        var before = timeline.update(2.5, items, SEQUENTIAL);

        var during = timeline.update(500, items, paused);
        var resumed = timeline.update(500, items, SEQUENTIAL);

        assertEquals(before.get(0).progress(), during.get(0).progress(), 0.001f);
        assertSame(a, resumed.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.ENTER, resumed.get(0).phase());
    }

    @Test
    void removingTheCurrentItemSwitchesImmediately() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item a = new Item("a"), b = new Item("b");
        timeline.update(0, List.of(a, b), SEQUENTIAL);

        var frame = timeline.update(3, List.of(b), SEQUENTIAL);

        assertSame(b, frame.get(0).item());
        assertEquals(PeakCyclerTimeline.Phase.ENTER, frame.get(0).phase());
        assertEquals(0f, frame.get(0).progress());
    }

    @Test
    void noItemsShowNothing() {
        PeakCyclerTimeline<Item> timeline = timeline();
        assertTrue(timeline.update(0, List.of(), SEQUENTIAL).isEmpty());
    }

    @Test
    void zeroDurationsSwitchWithoutAnimation() {
        PeakCyclerTimeline<Item> timeline = timeline();
        Item a = new Item("a"), b = new Item("b");
        List<Item> items = List.of(a, b);
        PeakCyclerTimeline.Config instant = new PeakCyclerTimeline.Config(4, 0, 0, PeakCycleOrder.SEQUENTIAL, false, false);

        assertSame(a, timeline.update(0, items, instant).get(0).item());
        assertSame(b, timeline.update(4, items, instant).get(0).item());
        assertSame(a, timeline.update(8.5, items, instant).get(0).item());
    }
}
