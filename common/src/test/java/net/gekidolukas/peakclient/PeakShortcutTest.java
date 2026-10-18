package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakShortcut;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakShortcutTest {
    @Test
    void lettersDigitsAndFunctionKeysResolve() {
        assertEquals(69, PeakShortcut.of("E").keyCode());
        assertEquals(69, PeakShortcut.of("e").keyCode());
        assertEquals(49, PeakShortcut.of("1").keyCode());
        assertEquals(290, PeakShortcut.of("F1").keyCode());
        assertEquals(301, PeakShortcut.of("F12").keyCode());
    }

    @Test
    void namedKeysResolve() {
        assertEquals(32, PeakShortcut.of("space").keyCode());
        assertEquals(257, PeakShortcut.of("enter").keyCode());
        assertEquals(258, PeakShortcut.of("TAB").keyCode());
        assertEquals(265, PeakShortcut.of("up").keyCode());
        assertEquals(267, PeakShortcut.of("page_down").keyCode());
    }

    @Test
    void modifiersAreParsedAndMustMatchExactly() {
        PeakShortcut shortcut = PeakShortcut.of("ctrl+shift+S", PeakAction.close());

        assertTrue(shortcut.isCtrl());
        assertTrue(shortcut.isShift());
        assertFalse(shortcut.isAlt());
        assertEquals("ctrl+shift+S", shortcut.spec());
        assertTrue(shortcut.matches(83, true, true, false));
        assertFalse(shortcut.matches(83, true, false, false));
        assertFalse(shortcut.matches(83, true, true, true));
        assertFalse(shortcut.matches(68, true, true, false));
    }

    @Test
    void aPlainShortcutRejectsHeldModifiers() {
        PeakShortcut shortcut = PeakShortcut.of("E");

        assertTrue(shortcut.matches(69, false, false, false));
        assertFalse(shortcut.matches(69, true, false, false));
    }

    @Test
    void invalidSpecsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PeakShortcut.of("ESCAPE"));
        assertThrows(IllegalArgumentException.class, () -> PeakShortcut.of("hyper+E"));
        assertThrows(IllegalArgumentException.class, () -> PeakShortcut.of("ctrl+"));
        assertThrows(IllegalArgumentException.class, () -> PeakShortcut.of("F13"));
        assertThrows(IllegalArgumentException.class, () -> PeakShortcut.of(""));
    }

    @Test
    void shortcutsSurviveTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu", settings -> settings
                .shortcut("E", PeakAction.close())
                .shortcut("alt+1", PeakAction.send("pick").arg("slot", "1"))
                .invalidColor(0xFFAA0000));

        PeakMenuSettings back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu)).getMenu();

        assertEquals(2, back.getShortcuts().size());
        assertEquals(69, back.getShortcuts().get(0).keyCode());
        assertEquals(1, back.getShortcuts().get(0).getActions().size());
        assertTrue(back.getShortcuts().get(1).isAlt());
        assertEquals("pick", back.getShortcuts().get(1).getActions().get(0).target());
        assertEquals(PeakPaint.solid(0xFFAA0000), back.getInvalidColor());
    }

    @Test
    void aMenuWithoutShortcutsHasNone() {
        PeakMenuSettings back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(PeakMenu.create("ns:menu"))).getMenu();

        assertTrue(back.getShortcuts().isEmpty());
        assertEquals(PeakPaint.solid(0xFFFF5555), back.getInvalidColor());
    }
}
