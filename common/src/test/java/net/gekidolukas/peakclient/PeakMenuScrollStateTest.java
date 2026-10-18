package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.gekidolukas.peakclient.ui.client.PeakMenuScrollState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakMenuScrollStateTest {
    @AfterEach
    void reset() {
        PeakMenuInteractionState.end();
    }

    @Test
    void offsetsStayWithinTheReportedMaximum() {
        PeakMenuScrollState state = new PeakMenuScrollState();
        state.report("list", 100);

        assertTrue(state.scrollBy("list", 30));
        assertEquals(30, state.offset("list"));
        assertTrue(state.scrollBy("list", 500));
        assertEquals(100, state.offset("list"));
        assertFalse(state.scrollBy("list", 10));
        assertTrue(state.scrollBy("list", -500));
        assertEquals(0, state.offset("list"));
        assertFalse(state.scrollBy("list", -10));
    }

    @Test
    void aShrinkingContentClampsTheOffset() {
        PeakMenuScrollState state = new PeakMenuScrollState();
        state.report("list", 100);
        state.scrollBy("list", 80);

        int clamped = state.report("list", 30);

        assertEquals(30, clamped);
        assertEquals(30, state.offset("list"));
        assertEquals(0, state.report("list", 0));
    }

    @Test
    void containersWithoutContentCannotScroll() {
        PeakMenuScrollState state = new PeakMenuScrollState();

        assertFalse(state.scrollBy("unknown", 20));
        assertEquals(0, state.offset("unknown"));
    }

    @Test
    void offsetsAreIndependentPerContainer() {
        PeakMenuScrollState state = new PeakMenuScrollState();
        state.report("outer", 100);
        state.report("inner", 20);

        state.scrollBy("inner", 15);

        assertEquals(15, state.offset("inner"));
        assertEquals(0, state.offset("outer"));
    }

    @Test
    void theRendererReportsThroughTheInteractionState() {
        PeakMenuScrollState state = new PeakMenuScrollState();
        PeakMenuInteractionState.begin(null, null, state);

        assertEquals(0, PeakMenuInteractionState.reportScroll("list", 50));
        state.scrollBy("list", 20);
        assertEquals(20, PeakMenuInteractionState.reportScroll("list", 50));
        assertEquals(50, state.max("list"));
    }
}
