package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakCarouselSpan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakCarouselSpanTest {
    private record Item(String name, int length) {}

    @Test
    void layoutPlacesEveryItemOnceWithGaps() {
        List<Item> items = List.of(new Item("a", 30), new Item("b", 20), new Item("c", 10));

        PeakCarouselSpan.Span<Item> span = PeakCarouselSpan.layout(items, Item::length, 5);

        assertEquals(List.of(0, 35, 60), span.placements().stream().map(PeakCarouselSpan.Placement::start).toList());
        assertEquals(70, span.total());
    }

    @Test
    void emptyLayoutHasNoLength() {
        assertEquals(0, PeakCarouselSpan.layout(List.<Item>of(), Item::length, 5).total());
    }

    @Test
    void overflowIsTheContentBeyondTheViewport() {
        assertEquals(30f, PeakCarouselSpan.overflow(130, 100));
        assertEquals(0f, PeakCarouselSpan.overflow(80, 100));
        assertEquals(0f, PeakCarouselSpan.overflow(100, 100));
    }

    @Test
    void pingPongHoldsMovesBackAndHoldsAgain() {
        float overflow = 40f;
        float speed = 20f;
        int pause = 10;

        assertEquals(0f, PeakCarouselSpan.pingPongOffset(0, overflow, speed, pause), 0.001f);
        assertEquals(0f, PeakCarouselSpan.pingPongOffset(9, overflow, speed, pause), 0.001f);
        assertEquals(20f, PeakCarouselSpan.pingPongOffset(30, overflow, speed, pause), 0.001f);
        assertEquals(40f, PeakCarouselSpan.pingPongOffset(50, overflow, speed, pause), 0.001f);
        assertEquals(40f, PeakCarouselSpan.pingPongOffset(59, overflow, speed, pause), 0.001f);
        assertEquals(20f, PeakCarouselSpan.pingPongOffset(80, overflow, speed, pause), 0.001f);
        assertEquals(0f, PeakCarouselSpan.pingPongOffset(100, overflow, speed, pause), 0.001f);
        assertEquals(0f, PeakCarouselSpan.pingPongOffset(105, overflow, speed, pause), 0.001f);
    }

    @Test
    void pingPongRepeatsWithItsPeriod() {
        float first = PeakCarouselSpan.pingPongOffset(30, 40f, 20f, 10);
        float later = PeakCarouselSpan.pingPongOffset(30 + 3 * 100, 40f, 20f, 10);
        assertEquals(first, later, 0.001f);
    }

    @Test
    void pingPongNeverLeavesTheOverflowRange() {
        for (double ticks = 0; ticks < 500; ticks += 3.7) {
            float offset = PeakCarouselSpan.pingPongOffset(ticks, 37f, 25f, 15);
            assertTrue(offset >= 0f && offset <= 37f, "offset " + offset + " at " + ticks);
        }
    }

    @Test
    void pingPongStandsStillWithoutOverflowOrSpeed() {
        assertEquals(0f, PeakCarouselSpan.pingPongOffset(50, 0f, 20f, 10));
        assertEquals(0f, PeakCarouselSpan.pingPongOffset(50, 40f, 0f, 10));
    }

    @Test
    void pingPongWithoutPauseTurnsAtOnce() {
        assertEquals(40f, PeakCarouselSpan.pingPongOffset(40, 40f, 20f, 0), 0.001f);
        assertEquals(20f, PeakCarouselSpan.pingPongOffset(60, 40f, 20f, 0), 0.001f);
    }
}
