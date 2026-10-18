package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakTextFilter;
import net.gekidolukas.peakclient.ui.client.PeakTextEditor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakTextEditorTest {
    private static PeakTextEditor editor(String value) {
        PeakTextEditor editor = new PeakTextEditor();
        editor.setValue(value);
        return editor;
    }

    @Test
    void typingInsertsAtTheCaret() {
        PeakTextEditor editor = editor("ab");
        editor.setCaret(1, false);

        assertTrue(editor.insert("X"));

        assertEquals("aXb", editor.value());
        assertEquals(2, editor.caret());
    }

    @Test
    void typingReplacesTheSelection() {
        PeakTextEditor editor = editor("hello world");
        editor.selectAll();

        editor.insert("hi");

        assertEquals("hi", editor.value());
        assertFalse(editor.hasSelection());
    }

    @Test
    void backspaceAndDeleteWorkOnCharactersAndWords() {
        PeakTextEditor editor = editor("one two three");
        assertTrue(editor.backspace(false));
        assertEquals("one two thre", editor.value());

        assertTrue(editor.backspace(true));
        assertEquals("one two ", editor.value());

        editor.home(false);
        assertTrue(editor.delete(false));
        assertEquals("ne two ", editor.value());
        assertTrue(editor.delete(true));
        assertEquals(" two ", editor.value());
    }

    @Test
    void nothingChangesAtTheBoundaries() {
        PeakTextEditor editor = editor("a");
        editor.home(false);

        assertFalse(editor.backspace(false));
        editor.end(false);
        assertFalse(editor.delete(false));
    }

    @Test
    void shiftMovesExtendTheSelection() {
        PeakTextEditor editor = editor("abcdef");
        editor.home(false);
        editor.moveRight(false, true);
        editor.moveRight(false, true);

        assertEquals("ab", editor.selectedText());
        editor.moveRight(false, false);
        assertFalse(editor.hasSelection());
        assertEquals(2, editor.caret());
    }

    @Test
    void theMaximumLengthIsRespected() {
        PeakTextEditor editor = new PeakTextEditor();
        editor.configure(5, PeakTextFilter.ANY);

        editor.insert("abcdefgh");

        assertEquals("abcde", editor.value());
    }

    @Test
    void filtersRejectInvalidCharacters() {
        PeakTextEditor editor = new PeakTextEditor();
        editor.configure(10, PeakTextFilter.INTEGER);

        editor.insert("12a-3");

        assertEquals("123", editor.value());
        editor.home(false);
        editor.insert("-");
        assertEquals("-123", editor.value());
    }

    @Test
    void decimalFilterAllowsOneSeparator() {
        PeakTextEditor editor = new PeakTextEditor();
        editor.configure(10, PeakTextFilter.DECIMAL);

        editor.insert("1.5.2");

        assertEquals("1.52", editor.value());
    }

    @Test
    void controlAndFormattingCharactersAreRejected() {
        PeakTextEditor editor = new PeakTextEditor();

        editor.insert("a\n§cb\u007f");

        assertEquals("acb", editor.value());
    }

    @Test
    void settingTheValueMovesTheCaretToTheEnd() {
        PeakTextEditor editor = editor("start");
        editor.setCaret(2, false);

        editor.setValue("replaced");

        assertEquals(8, editor.caret());
        assertFalse(editor.hasSelection());
    }

    @Test
    void selectionAndCaretClampToTheText() {
        PeakTextEditor editor = editor("abc");

        editor.setCaret(99, false);
        assertEquals(3, editor.caret());
        editor.setCaret(-5, false);
        assertEquals(0, editor.caret());
    }

    @Test
    void syncTrackingOnlyTriggersForOutsideChanges() {
        PeakTextEditor editor = new PeakTextEditor();

        assertTrue(editor.needsSync("a"));
        editor.synced("a");
        assertFalse(editor.needsSync("a"));
        assertTrue(editor.needsSync("b"));
    }
}
