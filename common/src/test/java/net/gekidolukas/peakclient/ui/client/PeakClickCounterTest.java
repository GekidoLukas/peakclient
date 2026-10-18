package net.gekidolukas.peakclient.ui.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeakClickCounterTest {
    @Test
    void aFirstClickCountsOne() {
        assertEquals(1, new PeakClickCounter().register("a", 10, 10, 1000));
    }

    @Test
    void quickClicksOnTheSameElementCountUpToThree() {
        PeakClickCounter counter = new PeakClickCounter();
        assertEquals(1, counter.register("a", 10, 10, 1000));
        assertEquals(2, counter.register("a", 11, 10, 1200));
        assertEquals(3, counter.register("a", 11, 11, 1400));
        assertEquals(3, counter.register("a", 11, 11, 1600));
    }

    @Test
    void aSlowClickStartsOver() {
        PeakClickCounter counter = new PeakClickCounter();
        counter.register("a", 10, 10, 1000);
        assertEquals(1, counter.register("a", 10, 10, 1000 + PeakClickCounter.WINDOW_MILLIS + 1));
    }

    @Test
    void aMovedClickStartsOver() {
        PeakClickCounter counter = new PeakClickCounter();
        counter.register("a", 10, 10, 1000);
        assertEquals(1, counter.register("a", 10 + PeakClickCounter.TOLERANCE_PIXELS + 1, 10, 1100));
    }

    @Test
    void aClickOnAnotherElementStartsOver() {
        PeakClickCounter counter = new PeakClickCounter();
        counter.register("a", 10, 10, 1000);
        assertEquals(1, counter.register("b", 10, 10, 1100));
    }

    @Test
    void theBoundariesAreInclusive() {
        PeakClickCounter counter = new PeakClickCounter();
        counter.register("a", 10, 10, 1000);
        assertEquals(2, counter.register("a", 10 + PeakClickCounter.TOLERANCE_PIXELS, 10,
                1000 + PeakClickCounter.WINDOW_MILLIS));
    }
}
