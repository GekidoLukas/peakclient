package net.gekidolukas.peakclient.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PeakDocumentEventsTest {
    @AfterEach
    void cleanUp() {
        PeakDocumentEvents.clearHandlers();
    }

    private static PeakDocumentActionEvent event(String documentId, String actionId) {
        return new PeakDocumentActionEvent(null, documentId, "ns:element", actionId, Map.of("old", "1", "new", "2"));
    }

    @Test
    void handlersRunForTheirDocumentAndActionOnly() {
        List<String> seen = new ArrayList<>();
        PeakDocumentEvents.on("ns:hud", "score-changed", e -> seen.add("handler " + e.arg("new")));
        PeakDocumentEvents.onAction(e -> seen.add("listener " + e.documentId() + "/" + e.actionId()));

        PeakDocumentEvents.dispatchAction(event("ns:hud", "score-changed"));
        PeakDocumentEvents.dispatchAction(event("ns:hud", "other"));
        PeakDocumentEvents.dispatchAction(event("ns:other", "score-changed"));

        assertEquals(List.of("handler 2", "listener ns:hud/score-changed", "listener ns:hud/other",
                "listener ns:other/score-changed"), seen);
    }

    @Test
    void aFailingHandlerDoesNotStopTheOthers() {
        List<String> seen = new ArrayList<>();
        PeakDocumentEvents.on("ns:hud", "go", e -> { throw new IllegalStateException("boom"); });
        PeakDocumentEvents.on("ns:hud", "go", e -> seen.add("second"));
        PeakDocumentEvents.onAction(e -> seen.add("listener"));

        PeakDocumentEvents.dispatchAction(event("ns:hud", "go"));

        assertEquals(List.of("second", "listener"), seen);
    }

    @Test
    void idsFollowTheConvention() {
        assertThrows(IllegalArgumentException.class, () -> PeakDocumentEvents.on("a:b:c", "go", e -> { }));
        assertThrows(IllegalArgumentException.class, () -> PeakDocumentEvents.on("ns:hud", "a:b:c", e -> { }));
    }

    @Test
    void theSessionAcceptsOnlyDocumentsSentToThePlayer() {
        PeakMenuSession session = new PeakMenuSession();
        Map<String, String> args = Map.of("key", "value");

        assertEquals(PeakMenuSession.Result.ACCEPTED, session.checkDocumentAction("ns:hud", "ns:e", "go", args, 1000L, true));
        assertEquals(PeakMenuSession.Result.UNKNOWN_MENU, session.checkDocumentAction("ns:hud", "ns:e", "go", args, 1000L, false));
        assertEquals(PeakMenuSession.Result.INVALID, session.checkDocumentAction("ns:hud", "ns:e", "", args, 1000L, true));
    }

    @Test
    void documentActionsShareTheRateLimitWithMenuActions() {
        PeakMenuSession session = new PeakMenuSession();
        session.markKnown("ns:menu");
        Map<String, String> args = Map.of();

        for (int i = 0; i < PeakMenuSession.MAX_EVENTS_PER_SECOND; i++) {
            assertEquals(PeakMenuSession.Result.ACCEPTED, session.checkDocumentAction("ns:hud", "ns:e", "go", args, 5000L, true));
        }

        assertEquals(PeakMenuSession.Result.RATE_LIMITED, session.checkAction("ns:menu", "ns:e", "go", args, 5500L));
        assertEquals(PeakMenuSession.Result.ACCEPTED, session.checkAction("ns:menu", "ns:e", "go", args, 6100L));
    }
}
