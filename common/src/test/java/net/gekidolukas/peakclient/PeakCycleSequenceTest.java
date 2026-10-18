package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakCycleOrder;
import net.gekidolukas.peakclient.ui.PeakCycleSequence;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakCycleSequenceTest {
    private record Item(String name) {}

    private static List<Item> items(String... names) {
        List<Item> items = new ArrayList<>();
        for (String name : names) items.add(new Item(name));
        return items;
    }

    @Test
    void sequentialWalksTheListAndWraps() {
        PeakCycleSequence<Item> sequence = new PeakCycleSequence<>(new Random(1));
        List<Item> items = items("a", "b", "c");

        Item first = sequence.next(PeakCycleOrder.SEQUENTIAL, items, null);
        Item second = sequence.next(PeakCycleOrder.SEQUENTIAL, items, first);
        Item third = sequence.next(PeakCycleOrder.SEQUENTIAL, items, second);
        Item fourth = sequence.next(PeakCycleOrder.SEQUENTIAL, items, third);

        assertSame(items.get(0), first);
        assertSame(items.get(1), second);
        assertSame(items.get(2), third);
        assertSame(items.get(0), fourth);
    }

    @Test
    void sequentialRestartsWhenTheCurrentItemIsGone() {
        PeakCycleSequence<Item> sequence = new PeakCycleSequence<>(new Random(1));
        List<Item> items = items("a", "b");
        Item removed = new Item("gone");

        assertSame(items.get(0), sequence.next(PeakCycleOrder.SEQUENTIAL, items, removed));
    }

    @Test
    void randomVisitsEveryItemOncePerRoundAndNeverRepeatsTwice() {
        PeakCycleSequence<Item> sequence = new PeakCycleSequence<>(new Random(42));
        List<Item> items = items("a", "b", "c", "d");

        Item current = null;
        for (int round = 0; round < 25; round++) {
            Set<Item> seen = new HashSet<>();
            for (int i = 0; i < items.size(); i++) {
                Item next = sequence.next(PeakCycleOrder.RANDOM, items, current);
                assertNotSame(current, next);
                assertTrue(seen.add(next), "duplicate inside one round");
                current = next;
            }
            assertEquals(items.size(), seen.size());
        }
    }

    @Test
    void randomDropsItemsThatDisappear() {
        PeakCycleSequence<Item> sequence = new PeakCycleSequence<>(new Random(7));
        List<Item> items = items("a", "b", "c");
        Item current = sequence.next(PeakCycleOrder.RANDOM, items, null);

        List<Item> reduced = new ArrayList<>(items);
        reduced.remove(items.get(2));
        for (int i = 0; i < 20; i++) {
            Item next = sequence.next(PeakCycleOrder.RANDOM, reduced, current);
            assertTrue(reduced.contains(next));
            current = next;
        }
    }

    @Test
    void emptyAndSingleLists() {
        PeakCycleSequence<Item> sequence = new PeakCycleSequence<>(new Random(1));
        assertNull(sequence.next(PeakCycleOrder.RANDOM, List.of(), null));

        List<Item> single = items("only");
        assertSame(single.get(0), sequence.next(PeakCycleOrder.RANDOM, single, single.get(0)));
        assertSame(single.get(0), sequence.next(PeakCycleOrder.SEQUENTIAL, single, single.get(0)));
    }
}
