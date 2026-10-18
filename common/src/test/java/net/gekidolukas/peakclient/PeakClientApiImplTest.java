package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakMenuCommand;
import net.gekidolukas.peakclient.server.PeakClientApiImpl;
import net.gekidolukas.peakclient.server.PeakPlayerStates;
import net.gekidolukas.peakclient.server.PeakSentDocuments;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakClientApiImplTest {
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();
    private final Queue<Runnable> queue = new ArrayDeque<>();
    private boolean onServerThread;
    private PeakClientApiImpl api;

    private void drain() {
        boolean before = onServerThread;
        onServerThread = true;
        try {
            while (!queue.isEmpty()) queue.poll().run();
        } finally {
            onServerThread = before;
        }
    }

    @BeforeEach
    void install() {
        api = new PeakClientApiImpl();
        onServerThread = true;
        PeakServerThread.install(() -> onServerThread, queue::add);
    }

    @AfterEach
    void restore() {
        PeakServerThread.reset();
        PeakSentDocuments.clearAll();
        PeakPlayerStates.forget(first);
        PeakPlayerStates.forget(second);
    }

    private static String customValue(UUID playerId, String namespace, String key) {
        Map<String, String> values = PeakPlayerStates.of(playerId).customValues().get(namespace);
        return values == null ? null : values.get(key);
    }

    @Test
    void recordsStateEvenWithoutReachablePlayers() {
        api.setCustomValue("MyMod", "score", "5", List.of(first));

        assertEquals("5", customValue(first, "mymod", "score"));
    }

    @Test
    void reservedNamespacesAreRefused() {
        for (String namespace : List.of("minecraft", "MC", "peakclient", "Pc")) {
            api.setCustomValue(namespace, "key", "value", List.of(first));
        }

        assertTrue(PeakPlayerStates.of(first).customValues().isEmpty());
    }

    @Test
    void callsFromAnotherThreadWaitForTheServerThread() {
        onServerThread = false;
        api.setCustomValue("mod", "a", "1", List.of(first));
        api.setCustomValue("mod", "a", "2", List.of(first));

        assertNull(customValue(first, "mod", "a"));
        drain();
        assertEquals("2", customValue(first, "mod", "a"));
    }

    @Test
    void playerIdsAreCopiedWhenTheCallIsMade() {
        onServerThread = false;
        List<UUID> ids = new ArrayList<>(List.of(first));
        api.setCustomValue("mod", "a", "1", ids);
        ids.add(second);

        drain();

        assertEquals("1", customValue(first, "mod", "a"));
        assertNull(customValue(second, "mod", "a"));
    }

    @Test
    void vanillaHudEntriesAreCopiedWhenTheCallIsMade() {
        onServerThread = false;
        Map<PeakVanillaElement, PeakVanillaHudEntry> entries = new HashMap<>();
        entries.put(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.disabled());
        api.setVanillaHudElements(entries, List.of(first));
        entries.put(PeakVanillaElement.HEALTH_BAR, PeakVanillaHudEntry.disabled());

        drain();

        Map<PeakVanillaElement, PeakVanillaHudEntry> stored = PeakPlayerStates.of(first).vanillaHud();
        assertTrue(stored.containsKey(PeakVanillaElement.HOTBAR));
        assertFalse(stored.containsKey(PeakVanillaElement.HEALTH_BAR));
    }

    @Test
    void borderIsRecordedPerPlayer() {
        api.setCustomWorldBorder(0, 0, 10, 10, List.of(first, second));
        api.clearCustomWorldBorder(List.of(second));

        assertTrue(PeakPlayerStates.of(first).border().isActive());
        assertFalse(PeakPlayerStates.of(second).border().isActive());
    }

    @Test
    void documentIsCachedForEveryRequestedPlayer() {
        PeakDocument document = PeakDocument.create("test:hud");

        api.sendUiToClient(document, List.of(first, second));

        assertSame(document, PeakSentDocuments.knownDocument(List.of(first), "test:hud"));
        assertSame(document, PeakSentDocuments.knownDocument(List.of(second), "test:hud"));
    }

    @Test
    void menuIsNotCachedWithoutReachablePlayers() {
        PeakDocument menu = PeakMenu.create("test:menu");

        api.sendMenuToClient(menu, List.of(first));

        assertNull(PeakSentDocuments.knownMenu(List.of(first), "test:menu"));
    }

    @Test
    void menuCommandsWithoutReachablePlayersLeaveTheCacheAlone() {
        PeakDocument menu = PeakMenu.create("test:menu");
        PeakSentDocuments.rememberMenu(first, "test:menu", menu);

        api.menuCommand(PeakMenuCommand.REMOVE, "test:menu", 0, List.of(first));
        api.menuCommand(PeakMenuCommand.CLEAR, "test:menu", 0, List.of(first));

        assertSame(menu, PeakSentDocuments.knownMenu(List.of(first), "test:menu"));
    }

    @Test
    void forgetPlayerGoesThroughTheServerThread() {
        api.setCustomValue("mod", "a", "1", List.of(first));
        onServerThread = false;

        api.forgetPlayer(first);
        assertEquals("1", customValue(first, "mod", "a"));

        drain();
        assertNull(PeakPlayerStates.peek(first));
    }
}
