package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakCarouselTrack;
import net.gekidolukas.peakclient.ui.PeakCycleOrder;
import net.gekidolukas.peakclient.ui.PeakCycleSequence;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakCarouselTrackTest {
    private record Item(String name, int length) {}

    private static final ToIntFunction<Item> LENGTH = Item::length;

    private static PeakCarouselTrack<Item> track() {
        return new PeakCarouselTrack<>(new PeakCycleSequence<>(new Random(3)));
    }

    @Test
    void theBandFillsTheViewportAndRepeatsItems() {
        PeakCarouselTrack<Item> track = track();
        Item a = new Item("a", 10), b = new Item("b", 20);

        List<PeakCarouselTrack.Placement<Item>> placements =
                track.step(List.of(a, b), LENGTH, 2, 50f, PeakCycleOrder.SEQUENTIAL, 0f);

        assertEquals(4, placements.size());
        assertSame(a, placements.get(0).item());
        assertEquals(0f, placements.get(0).start());
        assertSame(b, placements.get(1).item());
        assertEquals(12f, placements.get(1).start());
        assertSame(a, placements.get(2).item());
        assertEquals(34f, placements.get(2).start());
        assertSame(b, placements.get(3).item());
    }

    @Test
    void movingPastTheFirstItemDropsItAndKeepsTheSpacing() {
        PeakCarouselTrack<Item> track = track();
        Item a = new Item("a", 10), b = new Item("b", 20);
        track.step(List.of(a, b), LENGTH, 2, 50f, PeakCycleOrder.SEQUENTIAL, 0f);

        List<PeakCarouselTrack.Placement<Item>> placements =
                track.step(List.of(a, b), LENGTH, 2, 50f, PeakCycleOrder.SEQUENTIAL, 11f);

        assertSame(b, placements.get(0).item());
        assertEquals(1f, placements.get(0).start());
        assertSame(a, placements.get(1).item());
        assertEquals(23f, placements.get(1).start());
    }

    @Test
    void aSingleItemIsRepeatedToFillTheViewport() {
        PeakCarouselTrack<Item> track = track();
        Item only = new Item("only", 30);

        List<PeakCarouselTrack.Placement<Item>> placements =
                track.step(List.of(only), LENGTH, 5, 100f, PeakCycleOrder.SEQUENTIAL, 0f);

        assertTrue(placements.size() >= 3);
        for (PeakCarouselTrack.Placement<Item> placement : placements) assertSame(only, placement.item());
    }

    @Test
    void continuousMovementKeepsTheBandFilled() {
        PeakCarouselTrack<Item> track = track();
        List<Item> items = List.of(new Item("a", 7), new Item("b", 13), new Item("c", 5));

        for (int frame = 0; frame < 500; frame++) {
            List<PeakCarouselTrack.Placement<Item>> placements =
                    track.step(items, LENGTH, 3, 60f, PeakCycleOrder.SEQUENTIAL, 0.7f);
            PeakCarouselTrack.Placement<Item> last = placements.get(placements.size() - 1);
            assertTrue(last.start() + last.length() >= 60f, "band ends inside the viewport at frame " + frame);
            assertTrue(placements.get(0).start() + placements.get(0).length() > 0f);
        }
    }

    @Test
    void removedItemsLeaveTheBand() {
        PeakCarouselTrack<Item> track = track();
        Item a = new Item("a", 10), b = new Item("b", 10);
        track.step(List.of(a, b), LENGTH, 0, 40f, PeakCycleOrder.SEQUENTIAL, 0f);

        List<PeakCarouselTrack.Placement<Item>> placements =
                track.step(List.of(a), LENGTH, 0, 40f, PeakCycleOrder.SEQUENTIAL, 0f);

        for (PeakCarouselTrack.Placement<Item> placement : placements) assertSame(a, placement.item());
    }

    @Test
    void randomOrderNeverPlacesTheSameItemNextToItself() {
        PeakCarouselTrack<Item> track = track();
        List<Item> items = List.of(new Item("a", 10), new Item("b", 10), new Item("c", 10));

        for (int frame = 0; frame < 100; frame++) {
            List<PeakCarouselTrack.Placement<Item>> placements =
                    track.step(items, LENGTH, 0, 200f, PeakCycleOrder.RANDOM, 4f);
            List<Item> order = new ArrayList<>();
            for (PeakCarouselTrack.Placement<Item> placement : placements) order.add(placement.item());
            for (int i = 1; i < order.size(); i++) assertNotSame(order.get(i - 1), order.get(i));
        }
    }

    @Test
    void emptyInputGivesAnEmptyBand() {
        PeakCarouselTrack<Item> track = track();
        assertTrue(track.step(List.of(), LENGTH, 0, 40f, PeakCycleOrder.SEQUENTIAL, 1f).isEmpty());
    }
}
