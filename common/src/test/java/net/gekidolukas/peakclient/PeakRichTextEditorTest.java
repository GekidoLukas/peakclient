package net.gekidolukas.peakclient;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.client.PeakInputValues;
import net.gekidolukas.peakclient.ui.client.PeakMenuInputState;
import net.gekidolukas.peakclient.ui.client.PeakRichText;
import net.gekidolukas.peakclient.ui.client.PeakRichTextEditor;
import net.gekidolukas.peakclient.ui.client.PeakTextLayout;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakRichTextEditorTest {
    private static final PeakTextLayout.Measure MONO_FOR_NONE = (from, to) -> (to - from) * 6;

    private static PeakRichTextEditor editor(String value) {
        PeakRichTextEditor editor = new PeakRichTextEditor();
        editor.setValue(value);
        return editor;
    }

    @Test
    void typingAppendsAtTheCaretAndInheritsTheStyleBefore() {
        PeakRichTextEditor editor = editor("§lab");

        assertTrue(editor.insertTyped('c'));

        assertEquals("§labc", editor.value());
        assertEquals(3, editor.caret());
    }

    @Test
    void controlCharactersAndSectionSignsAreRejected() {
        PeakRichTextEditor editor = editor("");

        assertFalse(editor.insertTyped('§'));
        assertFalse(editor.insertTyped('\n'));
        assertFalse(editor.insertTyped((char) 7));
        assertEquals("", editor.value());
    }

    @Test
    void togglingBoldOnASelectionStylesJustThatRange() {
        PeakRichTextEditor editor = editor("abc");
        editor.setCaret(0, false);
        editor.setCaret(2, true);

        assertTrue(editor.toggleFlag(PeakRichText.BOLD));
        assertEquals("§lab§rc", editor.value());

        assertTrue(editor.toggleFlag(PeakRichText.BOLD));
        assertEquals("abc", editor.value());
    }

    @Test
    void togglingWithoutASelectionStylesTheNextTypedCharacter() {
        PeakRichTextEditor editor = editor("abc");

        assertFalse(editor.toggleFlag(PeakRichText.BOLD));
        assertTrue(editor.insertTyped('x'));
        assertTrue(editor.insertTyped('y'));

        assertEquals("abc§lxy", editor.value());
    }

    @Test
    void movingTheCaretDropsThePendingStyle() {
        PeakRichTextEditor editor = editor("abc");
        editor.toggleFlag(PeakRichText.ITALIC);

        editor.moveLeft(false, false);
        editor.insertTyped('x');

        assertEquals("abxc", editor.value());
    }

    @Test
    void maxLengthCountsTheFormattingCodes() {
        PeakRichTextEditor editor = new PeakRichTextEditor();
        editor.configure(6, 0);
        editor.setValue("ab");
        editor.selectAll();
        editor.toggleFlag(PeakRichText.BOLD);
        editor.documentEnd(false);

        assertTrue(editor.insertTyped('c'));
        assertTrue(editor.insertTyped('d'));
        assertFalse(editor.insertTyped('e'));

        assertEquals("§labcd", editor.value());
    }

    @Test
    void maxLinesLimitsNewlines() {
        PeakRichTextEditor editor = new PeakRichTextEditor();
        editor.configure(100, 2);
        editor.setValue("a");

        assertTrue(editor.insertNewline());
        assertFalse(editor.insertNewline());
        assertEquals("a\n", editor.value());
    }

    @Test
    void pasteNormalizesLineEndingsAndStripsSectionSigns() {
        PeakRichTextEditor editor = editor("");

        assertTrue(editor.insertText("a\r\nb§c", true));

        assertEquals("a\nbc", editor.text().plain());
    }

    @Test
    void pasteWithoutNewlinesDropsThem() {
        PeakRichTextEditor editor = editor("");

        editor.insertText("a\nb", false);

        assertEquals("ab", editor.text().plain());
    }

    @Test
    void backspaceAndDeleteWorkOnSelectionsAndWords() {
        PeakRichTextEditor editor = editor("hello big world");

        assertTrue(editor.backspace(true));
        assertEquals("hello big ", editor.text().plain());

        editor.setCaret(0, false);
        assertTrue(editor.delete(true));
        assertEquals(" big ", editor.text().plain());

        editor.selectAll();
        assertTrue(editor.backspace(false));
        assertEquals("", editor.text().plain());
        assertFalse(editor.backspace(false));
    }

    @Test
    void cyclingTheColorWalksThePalette() {
        PeakRichTextEditor editor = editor("");
        List<Integer> palette = List.of(0xFF5555, 0x55FF55, 0x5555FF);

        editor.cycleColor(palette, false);
        editor.insertTyped('a');
        editor.cycleColor(palette, false);
        editor.insertTyped('b');
        editor.cycleColor(palette, true);
        editor.insertTyped('c');

        assertEquals("§ca§ab§cc", editor.value());
    }

    @Test
    void clearFormatResetsASelection() {
        PeakRichTextEditor editor = editor("§c§lab");
        editor.selectAll();

        assertTrue(editor.clearFormat());

        assertEquals("ab", editor.value());
    }

    @Test
    void verticalMovesKeepTheColumn() {
        PeakRichTextEditor editor = editor("abcd\nef\nghijk");
        PeakTextLayout layout = editor.layout(0, MONO_FOR_NONE);
        editor.setCaret(3, false);

        editor.moveVertical(1, false, layout, MONO_FOR_NONE);
        assertEquals(7, editor.caret());

        editor.moveVertical(1, false, layout, MONO_FOR_NONE);
        assertEquals(11, editor.caret());

        editor.moveVertical(-1, false, layout, MONO_FOR_NONE);
        editor.moveVertical(-1, false, layout, MONO_FOR_NONE);
        assertEquals(3, editor.caret());

        editor.moveVertical(-1, false, layout, MONO_FOR_NONE);
        assertEquals(0, editor.caret());
    }

    @Test
    void homeAndEndUseTheCurrentLine() {
        PeakRichTextEditor editor = editor("abcd\nefgh");
        PeakTextLayout layout = editor.layout(0, MONO_FOR_NONE);
        editor.setCaret(7, false);

        editor.lineStart(layout, false);
        assertEquals(5, editor.caret());

        editor.lineEnd(layout, false);
        assertEquals(9, editor.caret());

        editor.setCaret(2, false);
        editor.lineEnd(layout, true);
        assertEquals(4, editor.caret());
        assertEquals("cd", editor.selectedPlain());
    }

    @Test
    void layoutIsRebuiltAfterEdits() {
        PeakRichTextEditor editor = editor("ab");
        PeakTextLayout before = editor.layout(0, MONO_FOR_NONE);

        editor.insertNewline();
        editor.insertTyped('c');
        PeakTextLayout after = editor.layout(0, MONO_FOR_NONE);

        assertEquals(1, before.lineCount());
        assertEquals(2, after.lineCount());
    }

    @Test
    void setValueTruncatesToTheMaximumLength() {
        PeakRichTextEditor editor = new PeakRichTextEditor();
        editor.configure(5, 0);

        editor.setValue("abcdefgh");

        assertEquals("abcde", editor.value());
    }

    @Test
    void caretMovementIsReportedOnce() {
        PeakRichTextEditor editor = editor("abc");

        assertTrue(editor.takeCaretMoved());
        assertFalse(editor.takeCaretMoved());
        editor.moveLeft(false, false);
        assertTrue(editor.takeCaretMoved());
    }

    @Test
    void richTextBoxesSurviveTheWireFormat() {
        PeakDocument menu = PeakMenu.create("form");
        menu.add(PeakRichTextBox.create("notes", "§lHello\nworld").maxLength(300).maxLines(4).wrap(false)
                .placeholder("Write...").colors(0xFF5555, 0x55FF55)
                .scrollbar(bar -> bar.radius(1).thumbColor(0x55AAFF)));

        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu));
        PeakRichTextBox box = (PeakRichTextBox) back.findById("notes");

        assertEquals("§lHello\nworld", box.getValue());
        assertEquals(300, box.getMaxLength());
        assertEquals(4, box.getMaxLines());
        assertFalse(box.isWrap());
        assertEquals("Write...", box.getPlaceholder());
        assertEquals(List.of(0xFF5555, 0x55FF55), box.getColors());
        assertEquals(PeakPaint.solid(0xFF55AAFF), box.getScrollbar().getThumbColor());
        assertTrue(box.getInteraction().isEnabled());
    }

    @Test
    void defaultsAreNotWrittenAndComeBack() {
        PeakDocument menu = PeakMenu.create("form");
        menu.add(PeakRichTextBox.create("plain"));

        PeakRichTextBox box = (PeakRichTextBox) PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu))
                .findById("plain");

        assertEquals(512, box.getMaxLength());
        assertTrue(box.isWrap());
        assertTrue(box.isFormatting());
        assertEquals(16, box.getColors().size());
        assertEquals(160, (int) box.getSize().getWidth().value());
    }

    @Test
    void patchesUpdateTheValueAndSettings() {
        PeakRichTextBox box = PeakRichTextBox.create("notes", "old");
        JsonObject patch = PeakPatch.richTextBox().value("§cnew").maxLines(2).wrap(false).colors(0x112233).build();

        box.applyPatch(patch);

        assertEquals("§cnew", box.getValue());
        assertEquals(2, box.getMaxLines());
        assertFalse(box.isWrap());
        assertEquals(List.of(0x112233), box.getColors());
    }

    @Test
    void theLimitIsClamped() {
        assertEquals(PeakRichTextBox.MAX_LENGTH_LIMIT, PeakRichTextBox.create("a").maxLength(99999).getMaxLength());
        assertEquals(1, PeakRichTextBox.create("a").maxLength(-4).getMaxLength());
    }

    @Test
    void inputStateKeepsOneEditorPerBoxAndResyncsOnServerPatches() {
        PeakMenuInputState state = new PeakMenuInputState();
        PeakRichTextBox box = PeakRichTextBox.create("notes", "first");

        PeakRichTextEditor editor = state.richEditor(box);
        assertSame(editor, state.richEditor(box));
        assertEquals("first", editor.value());

        editor.insertTyped('!');
        state.commit(box, editor);
        assertEquals("first!", box.getValue());
        assertEquals("first!", state.richEditor(box).value());

        box.value("server");
        assertEquals("server", state.richEditor(box).value());
    }

    @Test
    void richTextBoxesAreCollectedAsInputs() {
        PeakDocument menu = PeakMenu.create("form");
        menu.add(PeakRichTextBox.create("notes", "§lbold"));

        var model = new net.gekidolukas.peakclient.ui.PeakDocumentModel(menu);
        java.util.Map<String, String> values = PeakInputValues.arguments(model,
                net.gekidolukas.peakclient.ui.PeakAction.send("save").collectAll(), java.util.Map.of());

        assertEquals("§lbold", values.get("notes"));
    }
}
