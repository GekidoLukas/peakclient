package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.client.PeakMenuNavigator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakMenuNavigatorTest {
    @Test
    void startsClosed() {
        PeakMenuNavigator navigator = new PeakMenuNavigator();
        assertFalse(navigator.isOpen());
        assertNull(navigator.current());
        assertTrue(navigator.history().isEmpty());
    }

    @Test
    void openingPushesAndBackPops() {
        PeakMenuNavigator navigator = new PeakMenuNavigator();
        navigator.open("a", false);
        navigator.open("b", false);
        navigator.open("c", false);

        assertEquals("c", navigator.current());
        assertEquals(List.of("a", "b", "c"), navigator.history());

        navigator.back();
        assertEquals("b", navigator.current());
        navigator.back();
        assertEquals("a", navigator.current());
        navigator.back();
        assertFalse(navigator.isOpen());
    }

    @Test
    void replacingKeepsTheDepth() {
        PeakMenuNavigator navigator = new PeakMenuNavigator();
        navigator.open("a", false);
        navigator.open("b", false);
        navigator.open("c", true);

        assertEquals(List.of("a", "c"), navigator.history());
    }

    @Test
    void openingAMenuAlreadyInTheHistoryGoesBackToIt() {
        PeakMenuNavigator navigator = new PeakMenuNavigator();
        navigator.open("a", false);
        navigator.open("b", false);
        navigator.open("c", false);
        navigator.open("a", false);

        assertEquals(List.of("a"), navigator.history());
    }

    @Test
    void removingAMenuRemovesItFromTheHistory() {
        PeakMenuNavigator navigator = new PeakMenuNavigator();
        navigator.open("a", false);
        navigator.open("b", false);
        navigator.open("c", false);

        navigator.remove("b");
        assertEquals(List.of("a", "c"), navigator.history());
        assertEquals("c", navigator.current());

        navigator.remove("c");
        assertEquals("a", navigator.current());

        navigator.remove("a");
        assertFalse(navigator.isOpen());
    }

    @Test
    void closeAllClearsTheHistory() {
        PeakMenuNavigator navigator = new PeakMenuNavigator();
        navigator.open("a", false);
        navigator.open("b", false);

        navigator.closeAll();

        assertFalse(navigator.isOpen());
        assertTrue(navigator.history().isEmpty());
    }

    @Test
    void theHistoryIsLimited() {
        PeakMenuNavigator navigator = new PeakMenuNavigator();
        for (int i = 0; i < PeakMenuNavigator.MAX_DEPTH + 5; i++) navigator.open("m" + i, false);

        assertEquals(PeakMenuNavigator.MAX_DEPTH, navigator.history().size());
        assertEquals("m" + (PeakMenuNavigator.MAX_DEPTH + 4), navigator.current());
    }
}
