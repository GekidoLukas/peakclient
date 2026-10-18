package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.server.PeakMenuSession;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.gekidolukas.peakclient.server.PeakMenuSession.Result.*;
import static org.junit.jupiter.api.Assertions.*;

class PeakMenuSessionTest {
    private static final Map<String, String> NO_ARGS = Map.of();

    @Test
    void actionsOfKnownMenusAreAccepted() {
        PeakMenuSession session = new PeakMenuSession();
        session.markKnown("shop");

        assertEquals(ACCEPTED, session.checkAction("shop", "buy-button", "buy", Map.of("item", "sword"), 0));
    }

    @Test
    void actionsOfUnknownMenusAreRejected() {
        PeakMenuSession session = new PeakMenuSession();

        assertEquals(UNKNOWN_MENU, session.checkAction("shop", "buy-button", "buy", NO_ARGS, 0));
    }

    @Test
    void removedMenusAreNoLongerAccepted() {
        PeakMenuSession session = new PeakMenuSession();
        session.markKnown("shop");
        session.markRemoved("shop");

        assertEquals(UNKNOWN_MENU, session.checkAction("shop", "e", "buy", NO_ARGS, 0));
    }

    @Test
    void oversizedFieldsAreInvalid() {
        PeakMenuSession session = new PeakMenuSession();
        session.markKnown("shop");
        String tooLong = "x".repeat(PeakMenuSession.MAX_ID_LENGTH + 1);
        Map<String, String> tooManyArguments = new LinkedHashMap<>();
        for (int i = 0; i <= PeakMenuSession.MAX_ARGUMENTS; i++) tooManyArguments.put("k" + i, "v");

        assertEquals(INVALID, session.checkAction("shop", "e", tooLong, NO_ARGS, 0));
        assertEquals(INVALID, session.checkAction("shop", tooLong, "buy", NO_ARGS, 0));
        assertEquals(INVALID, session.checkAction("shop", "e", "", NO_ARGS, 0));
        assertEquals(INVALID, session.checkAction("shop", "e", "buy", tooManyArguments, 0));
        assertEquals(INVALID, session.checkAction("shop", "e", "buy",
                Map.of("k", "v".repeat(PeakMenuSession.MAX_ARGUMENT_LENGTH + 1)), 0));
    }

    @Test
    void tooManyActionsPerSecondAreRateLimited() {
        PeakMenuSession session = new PeakMenuSession();
        session.markKnown("shop");

        for (int i = 0; i < PeakMenuSession.MAX_EVENTS_PER_SECOND; i++) {
            assertEquals(ACCEPTED, session.checkAction("shop", "e", "buy", NO_ARGS, 1000));
        }
        assertEquals(RATE_LIMITED, session.checkAction("shop", "e", "buy", NO_ARGS, 1500));
        assertEquals(ACCEPTED, session.checkAction("shop", "e", "buy", NO_ARGS, 2100));
    }

    @Test
    void navigationOnlyKeepsKnownMenus() {
        PeakMenuSession session = new PeakMenuSession();
        session.markKnown("a");
        session.markKnown("b");

        session.navigated("b", List.of("a", "ghost", "b"));

        assertEquals("b", session.current());
        assertEquals(List.of("a", "b"), session.history());

        session.navigated("", List.of());
        assertNull(session.current());
        assertTrue(session.history().isEmpty());
    }

    @Test
    void removingTheCurrentMenuFallsBackToTheHistory() {
        PeakMenuSession session = new PeakMenuSession();
        session.markKnown("a");
        session.markKnown("b");
        session.navigated("b", List.of("a", "b"));

        session.markRemoved("b");

        assertEquals("a", session.current());
        assertEquals(List.of("a"), session.history());
    }
}
