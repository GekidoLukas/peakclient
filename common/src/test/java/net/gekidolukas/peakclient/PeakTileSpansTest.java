package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakTileSpans;
import net.gekidolukas.peakclient.ui.PeakTileSpans.Span;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakTileSpansTest {
    @Test
    void theMiddleTileIsCenteredAndBothEndsAreFilledEqually() {
        List<Span> spans = PeakTileSpans.centered(34, 10);

        assertEquals(List.of(new Span(0, 8, 2), new Span(2, 0, 10), new Span(12, 0, 10), new Span(22, 0, 10), new Span(32, 0, 2)), spans);
    }

    @Test
    void anExactMultipleHasNoPartialTiles() {
        List<Span> spans = PeakTileSpans.centered(30, 10);

        assertEquals(3, spans.size());
        assertEquals(new Span(0, 0, 10), spans.get(0));
        assertEquals(new Span(20, 0, 10), spans.get(2));
    }

    @Test
    void theSpansCoverTheWholeLengthWithoutGaps() {
        for (int dest = 1; dest < 80; dest++) {
            int covered = 0;
            for (Span span : PeakTileSpans.centered(dest, 7)) {
                assertEquals(covered, span.destOffset());
                assertTrue(span.srcOffset() >= 0 && span.srcOffset() + span.length() <= 7);
                covered += span.length();
            }
            assertEquals(dest, covered);
        }
    }

    @Test
    void aRegionSmallerThanOneTileShowsItsCenter() {
        List<Span> spans = PeakTileSpans.centered(4, 10);

        assertEquals(List.of(new Span(0, 3, 4)), spans);
    }

    @Test
    void emptyInputGivesNoSpans() {
        assertTrue(PeakTileSpans.centered(0, 10).isEmpty());
        assertTrue(PeakTileSpans.centered(10, 0).isEmpty());
        assertEquals(0, PeakTileSpans.count(0, 3));
    }
}
