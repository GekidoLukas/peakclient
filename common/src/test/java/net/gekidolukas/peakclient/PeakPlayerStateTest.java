package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.server.PeakPlayerState;
import net.gekidolukas.peakclient.server.PeakServerBorderState;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PeakPlayerStateTest {

    @Test
    void customValuesUseLowerCaseNamespaces() {
        PeakPlayerState state = new PeakPlayerState();
        state.setCustomValue("MyMod", "score", "5");
        assertEquals("5", state.customValues().get("mymod").get("score"));
    }

    @Test
    void clearingTheLastValueRemovesTheNamespace() {
        PeakPlayerState state = new PeakPlayerState();
        state.setCustomValue("mod", "a", "1");
        state.clearCustomValue("MOD", "a");
        assertTrue(state.customValues().isEmpty());
        assertTrue(state.isEmpty());
    }

    @Test
    void clearingANamespaceRemovesAllItsValues() {
        PeakPlayerState state = new PeakPlayerState();
        state.setCustomValue("mod", "a", "1");
        state.setCustomValue("mod", "b", "2");
        state.setCustomValue("other", "c", "3");
        state.clearCustomNamespace("mod");
        assertFalse(state.customValues().containsKey("mod"));
        assertTrue(state.customValues().containsKey("other"));
    }

    @Test
    void waypointPositionSurvivesAnUpsert() {
        PeakPlayerState state = new PeakPlayerState();
        state.upsertWaypoint("wp", "{\"id\":\"wp\"}");
        state.syncWaypointPosition("wp", 1, 2, 3);
        state.upsertWaypoint("wp", "{\"id\":\"wp\",\"label\":\"x\"}");
        assertEquals(new PeakPlayerState.Position(1, 2, 3), state.waypoints().get("wp").position());
        assertEquals("{\"id\":\"wp\",\"label\":\"x\"}", state.waypoints().get("wp").json());
    }

    @Test
    void positionSyncForUnknownWaypointIsIgnored() {
        PeakPlayerState state = new PeakPlayerState();
        state.syncWaypointPosition("missing", 1, 2, 3);
        assertTrue(state.waypoints().isEmpty());
    }

    @Test
    void waypointsCanBeRemovedAndCleared() {
        PeakPlayerState state = new PeakPlayerState();
        state.upsertWaypoint("a", "{}");
        state.upsertWaypoint("b", "{}");
        state.removeWaypoint("a");
        assertEquals(1, state.waypoints().size());
        state.clearWaypoints();
        assertTrue(state.waypoints().isEmpty());
    }

    @Test
    void vanillaHudEntriesAreMergedAndOverwritten() {
        PeakPlayerState state = new PeakPlayerState();
        state.mergeVanillaHud(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.disabled()));
        state.mergeVanillaHud(Map.of(PeakVanillaElement.HEALTH_BAR, PeakVanillaHudEntry.disabled()));
        assertEquals(2, state.vanillaHud().size());
        state.mergeVanillaHud(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.create()));
        assertEquals(1, state.vanillaHud().size());
        assertFalse(state.isEmpty());
        state.mergeVanillaHud(Map.of(PeakVanillaElement.HEALTH_BAR, PeakVanillaHudEntry.create()));
        assertTrue(state.isEmpty());
    }

    @Test
    void vanillaHudPatchesKeepTheFieldsTheyDoNotName() {
        PeakPlayerState state = new PeakPlayerState();
        state.mergeVanillaHud(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.moved(0, -20)));
        state.mergeVanillaHud(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.disabled()));
        PeakVanillaHudEntry entry = state.vanillaHud().get(PeakVanillaElement.HOTBAR);
        assertFalse(entry.isEnabled());
        assertEquals(-20, entry.getOffsetY());

        state.mergeVanillaHud(Map.of(PeakVanillaElement.HOTBAR, PeakVanillaHudEntry.shown()));
        entry = state.vanillaHud().get(PeakVanillaElement.HOTBAR);
        assertTrue(entry.isEnabled());
        assertEquals(-20, entry.getOffsetY());
    }

    @Test
    void offlineMarkerCanBeSetAndReset() {
        PeakPlayerState state = new PeakPlayerState();
        assertEquals(-1, state.offlineSinceTick());
        state.markOffline(100);
        assertEquals(100, state.offlineSinceTick());
        state.markOnline();
        assertEquals(-1, state.offlineSinceTick());
    }

    @Test
    void borderInterpolatesTowardsTheTarget() {
        PeakServerBorderState border = new PeakServerBorderState();
        border.setCurrent(0, 0, 100, 100, 10);
        border.setTarget(20, 20, 80, 80, 100, 10);
        assertBounds(10, 10, 90, 90, border.currentBounds(60));
        assertEquals(50, border.remainingTicks(60));
        assertEquals(0, border.remainingTicks(200));
        assertBounds(20, 20, 80, 80, border.currentBounds(200));
    }

    @Test
    void newTargetStartsFromTheCurrentInterpolation() {
        PeakServerBorderState border = new PeakServerBorderState();
        border.setCurrent(0, 0, 100, 100, 0);
        border.setTarget(50, 50, 50, 50, 100, 0);
        border.setTarget(0, 0, 100, 100, 100, 50);
        assertBounds(25, 25, 75, 75, border.currentBounds(50));
    }

    @Test
    void borderWithoutTransitionIsAtTheTargetImmediately() {
        PeakServerBorderState border = new PeakServerBorderState();
        border.setCurrent(1, 2, 3, 4, 0);
        assertTrue(border.isActive());
        assertEquals(0, border.remainingTicks(5));
        assertBounds(1, 2, 3, 4, border.currentBounds(5));
        border.clear();
        assertFalse(border.isActive());
    }

    private static void assertBounds(double minX, double minZ, double maxX, double maxZ, PeakServerBorderState.Bounds actual) {
        assertEquals(minX, actual.minX(), 0.0001);
        assertEquals(minZ, actual.minZ(), 0.0001);
        assertEquals(maxX, actual.maxX(), 0.0001);
        assertEquals(maxZ, actual.maxZ(), 0.0001);
    }
}
