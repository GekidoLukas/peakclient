package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakClientApiHolder;
import net.gekidolukas.peakclient.api.PeakVanillaHud;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PeakVanillaHudTest {
    private RecordingPeakApi api;
    private PeakClientApi previous;
    private final UUID player = UUID.randomUUID();

    @BeforeEach
    void install() {
        previous = PeakClientApiHolder.INSTANCE;
        api = new RecordingPeakApi();
        PeakClientApiHolder.INSTANCE = api;
        PeakClientVanillaHudState.clearAll();
    }

    @AfterEach
    void restore() {
        PeakClientApiHolder.INSTANCE = previous;
        PeakClientVanillaHudState.clearAll();
    }

    @SuppressWarnings("unchecked")
    private Map<PeakVanillaElement, PeakVanillaHudEntry> entries(int call) {
        return (Map<PeakVanillaElement, PeakVanillaHudEntry>) api.calls.get(call).payload();
    }

    @Test
    void hideSendsOnePacketWithDisabledEntries() {
        PeakClient.to(player).vanillaHud().hide(PeakVanillaElement.HOTBAR, PeakVanillaElement.CHAT);

        assertEquals(1, api.calls.size());
        assertEquals(List.of(player), api.calls.get(0).ids());
        assertEquals(Set.of(PeakVanillaElement.HOTBAR, PeakVanillaElement.CHAT), entries(0).keySet());
        assertFalse(entries(0).get(PeakVanillaElement.HOTBAR).isEnabled());
    }

    @Test
    void groupsAreAcceptedAsCollections() {
        PeakClient.to(player).vanillaHud().hide(PeakVanillaElement.STATUS_BARS);

        assertEquals(PeakVanillaElement.STATUS_BARS, entries(0).keySet());
    }

    @Test
    void hideAllExceptKeepsTheNamedElementsVisible() {
        PeakClient.to(player).vanillaHud().hideAllExcept(PeakVanillaElement.CHAT, PeakVanillaElement.CROSSHAIR);

        Map<PeakVanillaElement, PeakVanillaHudEntry> entries = entries(0);
        assertEquals(PeakVanillaElement.ALL.size(), entries.size());
        assertTrue(entries.get(PeakVanillaElement.CHAT).isEnabled());
        assertTrue(entries.get(PeakVanillaElement.CROSSHAIR).isEnabled());
        assertFalse(entries.get(PeakVanillaElement.HOTBAR).isEnabled());
        assertFalse(entries.get(PeakVanillaElement.RECIPE_TOAST).isEnabled());
    }

    @Test
    void offsetOnlyChangesTheOffset() {
        PeakClient.to(player).vanillaHud().offset(PeakVanillaElement.HOTBAR, 4, -20);

        PeakVanillaHudEntry entry = entries(0).get(PeakVanillaElement.HOTBAR);
        assertEquals(4, entry.getOffsetX());
        assertEquals(-20, entry.getOffsetY());
        assertTrue(entry.isEnabled());
    }

    @Test
    void offsetOnAnElementThatCannotMoveIsIgnored() {
        PeakClient.to(player).vanillaHud().offset(PeakVanillaElement.CHAT, 0, -20);

        assertTrue(api.calls.isEmpty());
    }

    @Test
    void resetSendsDefaultEntriesForEveryElement() {
        PeakClient.to(player).vanillaHud().reset();

        Map<PeakVanillaElement, PeakVanillaHudEntry> entries = entries(0);
        assertEquals(PeakVanillaElement.ALL.size(), entries.size());
        for (PeakVanillaHudEntry entry : entries.values()) assertTrue(entry.isDefault());
    }

    @Test
    void trackedHudReplaysToJoinersAndRestoresLeaversAndEveryone() {
        UUID member = UUID.randomUUID();
        UUID joiner = UUID.randomUUID();
        PeakVanillaHud hud = PeakVanillaHud.tracked(() -> List.of(member));

        hud.hide(PeakVanillaElement.HOTBAR);
        assertEquals(List.of(member), api.calls.get(0).ids());

        hud.replayTo(joiner);
        assertEquals(List.of(joiner), api.calls.get(1).ids());
        assertFalse(entries(1).get(PeakVanillaElement.HOTBAR).isEnabled());

        hud.restoreFor(joiner);
        assertEquals(List.of(joiner), api.calls.get(2).ids());
        assertTrue(entries(2).get(PeakVanillaElement.HOTBAR).isDefault());

        hud.restoreAll();
        assertEquals(List.of(member), api.calls.get(3).ids());
        assertTrue(entries(3).get(PeakVanillaElement.HOTBAR).isDefault());

        hud.replayTo(UUID.randomUUID());
        assertEquals(4, api.calls.size());
    }

    @Test
    void untrackedHudRemembersNothing() {
        PeakVanillaHud hud = PeakClient.to(player).vanillaHud();
        hud.hide(PeakVanillaElement.HOTBAR);

        hud.replayTo(UUID.randomUUID());
        hud.restoreAll();

        assertEquals(1, api.calls.size());
    }

    @Test
    void clientStateMergesPatchesPerField() {
        PeakClientVanillaHudState.apply(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.moved(0, -20)));
        PeakClientVanillaHudState.apply(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.disabled()));

        assertFalse(PeakClientVanillaHudState.isEnabled(PeakVanillaElement.HOTBAR));
        assertEquals(-20, PeakClientVanillaHudState.offsetY(PeakVanillaElement.HOTBAR));

        PeakClientVanillaHudState.apply(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.create()));

        assertTrue(PeakClientVanillaHudState.isEnabled(PeakVanillaElement.HOTBAR));
        assertEquals(0, PeakClientVanillaHudState.offsetY(PeakVanillaElement.HOTBAR));
    }

    @Test
    void documentsHideElementsAndWinOverShow() {
        PeakClientVanillaHudState.apply(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.shown()));
        PeakClientVanillaHudState.setDocumentHides("a", Set.of(PeakVanillaElement.HOTBAR));
        PeakClientVanillaHudState.setDocumentHides("b", Set.of(PeakVanillaElement.CHAT));

        assertFalse(PeakClientVanillaHudState.isEnabled(PeakVanillaElement.HOTBAR));
        assertFalse(PeakClientVanillaHudState.isEnabled(PeakVanillaElement.CHAT));

        PeakClientVanillaHudState.removeDocument("a");

        assertTrue(PeakClientVanillaHudState.isEnabled(PeakVanillaElement.HOTBAR));
        assertFalse(PeakClientVanillaHudState.isEnabled(PeakVanillaElement.CHAT));

        PeakClientVanillaHudState.setDocumentHides("b", Set.of());

        assertTrue(PeakClientVanillaHudState.isEnabled(PeakVanillaElement.CHAT));
    }
}
