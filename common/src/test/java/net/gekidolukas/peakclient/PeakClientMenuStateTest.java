package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.client.PeakClientMenuState;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PeakClientMenuStateTest {
    private static PeakDocument menu(String id) {
        PeakDocument menu = PeakMenu.create(id);
        menu.add(PeakLabel.create(id + ":label", "start"));
        return menu;
    }

    @Test
    void operationsEditACachedMenuThatIsNotOpen() {
        PeakClientMenuState state = new PeakClientMenuState();
        state.put(menu("shop"), Set.of());

        boolean applied = state.applyOperations("shop", List.of(
                new PeakUiOperation.Update("shop:label", PeakPatch.label().text("edited").build())));

        assertTrue(applied);
        assertEquals("edited", ((PeakLabel) state.get("shop").findById("shop:label")).getText().getString());
    }

    @Test
    void operationsOnUnknownMenusAreIgnored() {
        PeakClientMenuState state = new PeakClientMenuState();

        assertFalse(state.applyOperations("missing", List.of(new PeakUiOperation.Delete("x"))));
    }

    @Test
    void sendingAgainReplacesTheMenu() {
        PeakClientMenuState state = new PeakClientMenuState();
        state.put(menu("shop"), Set.of());
        state.applyOperations("shop", List.of(new PeakUiOperation.Update("shop:label", PeakPatch.label().text("edited").build())));

        state.put(menu("shop"), Set.of());

        assertEquals("start", ((PeakLabel) state.get("shop").findById("shop:label")).getText().getString());
        assertEquals(1, state.size());
    }

    @Test
    void theOldestUnprotectedMenusAreEvicted() {
        PeakClientMenuState state = new PeakClientMenuState(3);
        state.put(menu("a"), Set.of());
        state.put(menu("b"), Set.of());
        state.put(menu("c"), Set.of());

        List<String> evicted = state.put(menu("d"), Set.of("a"));

        assertEquals(List.of("b"), evicted);
        assertTrue(state.contains("a"));
        assertFalse(state.contains("b"));
        assertTrue(state.contains("d"));
    }

    @Test
    void removeAndClearEmptyTheCache() {
        PeakClientMenuState state = new PeakClientMenuState();
        state.put(menu("a"), Set.of());
        state.put(menu("b"), Set.of());

        assertTrue(state.remove("a"));
        assertFalse(state.remove("a"));
        state.clear();

        assertEquals(0, state.size());
    }
}
