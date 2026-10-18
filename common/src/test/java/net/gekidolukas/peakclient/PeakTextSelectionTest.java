package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakFormat;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.client.PeakMenuInputState;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.gekidolukas.peakclient.ui.client.PeakMenuScrollState;
import net.gekidolukas.peakclient.ui.client.PeakRichText;
import net.gekidolukas.peakclient.ui.client.PeakRichTextEditor;
import net.gekidolukas.peakclient.ui.client.PeakTextEditor;
import net.gekidolukas.peakclient.ui.client.PeakTextLayout;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.gekidolukas.peakclient.ui.template.PeakRichTextToolbar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakTextSelectionTest {
    private static final PeakTextLayout.Measure MONO = (from, to) -> (to - from) * 6;

    private static PeakTextEditor textEditor(String value) {
        PeakTextEditor editor = new PeakTextEditor();
        editor.setValue(value);
        return editor;
    }

    private static PeakRichTextEditor richEditor(String value) {
        PeakRichTextEditor editor = new PeakRichTextEditor();
        editor.setValue(value);
        return editor;
    }

    @Test
    void doubleClickSelectsTheWordUnderTheIndex() {
        PeakTextEditor editor = textEditor("hello big world");

        editor.selectWordAt(7);

        assertEquals("big", editor.selectedText());
    }

    @Test
    void aWordSelectionOnASpaceTakesTheSpaces() {
        PeakTextEditor editor = textEditor("hello   world");

        editor.selectWordAt(6);

        assertEquals("   ", editor.selectedText());
    }

    @Test
    void aWordSelectionPastTheEndTakesTheLastWord() {
        PeakTextEditor editor = textEditor("hello world");

        editor.selectWordAt(99);

        assertEquals("world", editor.selectedText());
    }

    @Test
    void rangesCanBeSelectedAndDeleted() {
        PeakTextEditor editor = textEditor("hello world");

        editor.selectRange(2, 5);
        assertEquals("llo", editor.selectedText());

        assertTrue(editor.deleteRange(5, 11));
        assertEquals("hello", editor.value());
        assertEquals(5, editor.caret());
        assertFalse(editor.deleteRange(3, 3));
    }

    @Test
    void richWordAndParagraphSelection() {
        PeakRichTextEditor editor = richEditor("ab cd\nef gh");

        editor.selectWordAt(7);
        assertEquals("ef", editor.selectedPlain());

        editor.selectParagraphAt(8);
        assertEquals("ef gh", editor.selectedPlain());

        editor.selectParagraphAt(1);
        assertEquals("ab cd", editor.selectedPlain());
    }

    @Test
    void aSliceKeepsTheFormattingAndCanBeInsertedElsewhere() {
        PeakRichTextEditor source = richEditor("§lab§rcd");
        source.selectRange(0, 2);
        PeakRichText fragment = source.selectedFragment();

        PeakRichTextEditor target = richEditor("xy");
        target.setCaret(1, false);
        assertTrue(target.insertRich(fragment));

        assertEquals("x§lab§ry", target.value());
        assertEquals(3, target.caret());
    }

    @Test
    void insertingAFragmentRespectsTheLimits() {
        PeakRichTextEditor target = new PeakRichTextEditor();
        target.configure(3, 0);
        target.setValue("xy");

        target.insertRich(PeakRichText.parse("abc"));

        assertEquals("xyc".length(), target.value().length());
        assertEquals("xya", target.value());
    }

    @Test
    void richRangesCanBeDeleted() {
        PeakRichTextEditor editor = richEditor("§chello§r world");

        assertTrue(editor.deleteRange(0, 5));

        assertEquals(" world", editor.value());
        assertFalse(editor.deleteRange(2, 2));
    }

    @Test
    void theLayoutFindsTheCharacterUnderAPixel() {
        PeakTextLayout layout = PeakTextLayout.build(PeakRichText.parse("hello world foo"), 30, MONO);

        assertEquals(1, layout.charIndexAt(0, 7, MONO));
        assertEquals(0, layout.charIndexAt(0, 0, MONO));
        assertEquals(-1, layout.charIndexAt(0, -3, MONO));
        assertEquals(-1, layout.charIndexAt(0, 500, MONO));
        assertEquals(6, layout.charIndexAt(1, 2, MONO));
    }

    @Test
    void formatActionsCarryTheirKindAndColor() {
        PeakAction bold = PeakAction.format("ns:notes", PeakFormat.BOLD);
        PeakAction color = PeakAction.formatColor("ns:notes", 0x55FF55);

        assertEquals(PeakActionType.FORMAT, bold.type());
        assertEquals("ns:notes", bold.target());
        assertEquals(PeakFormat.BOLD, bold.formatKind());
        assertEquals(PeakFormat.COLOR, color.formatKind());
        assertEquals(0x55FF55, color.formatColorValue());
        assertThrows(IllegalArgumentException.class, () -> PeakAction.format("a:b:c", PeakFormat.BOLD));
    }

    @Test
    void theToolbarBuildsFocusKeepingFormatButtons() {
        PeakPanel toolbar = PeakRichTextToolbar.create("ns:tb", "ns:notes").gap(3).build();

        assertEquals("ns:tb", toolbar.getId());
        assertEquals(7, toolbar.getChildren().size());
        PeakButton bold = (PeakButton) toolbar.getChildren().get(0);
        assertEquals("ns:tb/bold", bold.getId());
        assertTrue(bold.getInteraction().isKeepFocus());
        assertNotNull(bold.getInteraction().getActiveStyle());
        assertEquals(PeakActionType.FORMAT, bold.actionsFor(PeakEvent.CLICK).get(0).type());
        assertEquals("ns:notes", bold.actionsFor(PeakEvent.CLICK).get(0).target());

        PeakButton clear = (PeakButton) toolbar.getChildren().get(6);
        assertEquals("ns:tb/clear", clear.getId());
        assertNull(clear.getInteraction().getActiveStyle());
    }

    @Test
    void theToolbarCanBeReducedAndStyled() {
        PeakPanel toolbar = PeakRichTextToolbar.create("tb", "notes")
                .buttons(PeakFormat.BOLD, PeakFormat.CLEAR)
                .buttonStyle(button -> button.width(30))
                .build();

        assertEquals(2, toolbar.getChildren().size());
        assertEquals(30f, toolbar.getChildren().get(0).getSize().getWidth().value());
        assertThrows(IllegalArgumentException.class, () -> PeakRichTextToolbar.create("a:b:c", "notes"));
    }

    @Test
    void keepFocusAndTheActiveStyleSurviveTheWireFormat() {
        PeakDocument menu = PeakMenu.create("form");
        menu.add(PeakButton.text("ns:b", "B").keepFocus(true)
                .activeStyle(PeakStyle.create().background(0xFF3C78C8))
                .onClick(PeakAction.format("ns:notes", PeakFormat.ITALIC)));

        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu));
        PeakButton read = (PeakButton) back.findById("ns:b");

        assertTrue(read.getInteraction().isKeepFocus());
        assertEquals(PeakPaint.solid(0xFF3C78C8), read.getInteraction().getActiveStyle().getBackground());
        assertEquals(PeakFormat.ITALIC, read.actionsFor(PeakEvent.CLICK).get(0).formatKind());
    }

    @Test
    void theActiveFormatFollowsTheCaretStyle() {
        PeakRichTextBox box = PeakRichTextBox.create("ns:notes", "§lbold");
        PeakMenuInputState input = new PeakMenuInputState();
        PeakRichTextEditor editor = input.richEditor(box);
        input.focus("ns:notes");

        PeakMenuInteractionState.begin(null, null, new PeakMenuScrollState(), input);
        try {
            assertTrue(PeakMenuInteractionState.formatActive(PeakAction.format("ns:notes", PeakFormat.BOLD)));
            assertFalse(PeakMenuInteractionState.formatActive(PeakAction.format("ns:notes", PeakFormat.ITALIC)));
            assertFalse(PeakMenuInteractionState.formatActive(PeakAction.format("ns:other", PeakFormat.BOLD)));

            editor.clearFormat();
            editor.selectAll();
            editor.clearFormat();
            assertFalse(PeakMenuInteractionState.formatActive(PeakAction.format("ns:notes", PeakFormat.BOLD)));

            input.blur();
            editor.setValue("§lbold");
            assertFalse(PeakMenuInteractionState.formatActive(PeakAction.format("ns:notes", PeakFormat.BOLD)));
        } finally {
            PeakMenuInteractionState.end();
        }
    }
}
