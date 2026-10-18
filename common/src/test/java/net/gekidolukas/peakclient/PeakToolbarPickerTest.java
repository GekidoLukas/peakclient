package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakColorPickerLayout;
import net.gekidolukas.peakclient.ui.PeakFormat;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.client.PeakMenuInputState;
import net.gekidolukas.peakclient.ui.client.PeakRichTextEditor;
import net.gekidolukas.peakclient.ui.template.PeakRichTextToolbar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakToolbarPickerTest {
    @Test
    void withoutThePickerTheToolbarKeepsItsStructure() {
        PeakPanel toolbar = PeakRichTextToolbar.create("ns:tb", "ns:notes").build();

        assertEquals("ns:tb", toolbar.getId());
        assertEquals(PeakLayoutDirection.ROW, toolbar.getDirection());
        assertEquals(7, toolbar.getChildren().size());
        assertEquals("ns:tb/bold", toolbar.getChildren().get(0).getId());
    }

    @Test
    void thePickerOptionWrapsTheButtonsAndAddsAHiddenPicker() {
        PeakPanel toolbar = PeakRichTextToolbar.create("ns:tb", "ns:notes").colorPicker().build();

        assertEquals("ns:tb", toolbar.getId());
        assertEquals(PeakLayoutDirection.COLUMN, toolbar.getDirection());
        assertEquals(2, toolbar.getChildren().size());

        PeakPanel buttons = (PeakPanel) toolbar.getChildren().get(0);
        assertEquals("ns:tb/buttons", buttons.getId());
        assertEquals(8, buttons.getChildren().size());
        assertEquals("ns:tb/bold", buttons.getChildren().get(0).getId());

        PeakButton color = (PeakButton) buttons.getChildren().get(7);
        assertEquals("ns:tb/color", color.getId());
        assertTrue(color.getInteraction().isKeepFocus());
        assertEquals(PeakActionType.TOGGLE, color.actionsFor(PeakEvent.CLICK).get(0).type());
        assertEquals("ns:tb/picker", color.actionsFor(PeakEvent.CLICK).get(0).target());

        PeakColorPicker picker = (PeakColorPicker) toolbar.getChildren().get(1);
        assertEquals("ns:tb/picker", picker.getId());
        assertTrue(picker.isHidden());
        assertEquals("ns:notes", picker.getTarget());
    }

    @Test
    void thePickerCanBeCustomizedAndTheButtonsStayVertical() {
        PeakPanel toolbar = PeakRichTextToolbar.create("ns:tb", "ns:notes")
                .buttons(PeakFormat.BOLD, PeakFormat.COLOR)
                .vertical()
                .colorPicker(picker -> picker.alpha(true).size(180, 120).hexField("ns:hex"))
                .build();

        PeakPanel buttons = (PeakPanel) toolbar.getChildren().get(0);
        PeakColorPicker picker = (PeakColorPicker) toolbar.getChildren().get(1);

        assertEquals(PeakLayoutDirection.COLUMN, buttons.getDirection());
        assertEquals(2, buttons.getChildren().size());
        assertEquals("ns:tb/color", buttons.getChildren().get(1).getId());
        assertTrue(picker.isAlpha());
        assertEquals("ns:hex", picker.getHexField());
        assertEquals(180f, picker.getSize().getWidth().value());
        assertTrue(PeakColorPickerLayout.of(180, 120, true).field().width() > 0);
    }

    @Test
    void theColorButtonIsSkippedWithoutAPicker() {
        PeakPanel toolbar = PeakRichTextToolbar.create("ns:tb", "ns:notes")
                .buttons(PeakFormat.BOLD, PeakFormat.COLOR)
                .build();

        assertEquals(1, toolbar.getChildren().size());
    }

    private static PeakMenuInputState focusedBox(PeakRichTextBox box) {
        PeakMenuInputState input = new PeakMenuInputState();
        input.richEditor(box);
        input.focus(box.getId());
        return input;
    }

    @Test
    void thePickerShowsTheColorAtTheCaret() {
        PeakRichTextBox box = PeakRichTextBox.create("ns:notes", "§cred plain");
        PeakColorPicker picker = PeakColorPicker.create("ns:picker", 0xFF336699).target("ns:notes");
        PeakMenuInputState input = focusedBox(box);
        PeakRichTextEditor editor = input.richEditor(box);

        editor.setCaret(2, false);

        assertTrue(input.followTarget(picker));
        assertEquals(0xFFFF5555, picker.getValue());
        assertFalse(input.followTarget(picker));
    }

    @Test
    void aManualPickerChoiceIsNotOverwritten() {
        PeakRichTextBox box = PeakRichTextBox.create("ns:notes", "§cred plain");
        PeakColorPicker picker = PeakColorPicker.create("ns:picker").target("ns:notes");
        PeakMenuInputState input = focusedBox(box);
        input.richEditor(box).setCaret(2, false);
        input.followTarget(picker);

        picker.value(0xFF00FF00);

        assertFalse(input.followTarget(picker));
        assertEquals(0xFF00FF00, picker.getValue());
    }

    @Test
    void textWithoutAColorLeavesThePickerAlone() {
        PeakRichTextBox box = PeakRichTextBox.create("ns:notes", "plain text");
        PeakColorPicker picker = PeakColorPicker.create("ns:picker", 0xFF336699).target("ns:notes");
        PeakMenuInputState input = focusedBox(box);
        input.richEditor(box).setCaret(3, false);

        assertFalse(input.followTarget(picker));
        assertEquals(0xFF336699, picker.getValue());
    }

    @Test
    void anUnfocusedBoxIsNotFollowed() {
        PeakRichTextBox box = PeakRichTextBox.create("ns:notes", "§cred");
        PeakColorPicker picker = PeakColorPicker.create("ns:picker", 0xFF336699).target("ns:notes");
        PeakMenuInputState input = focusedBox(box);
        input.richEditor(box).setCaret(2, false);
        input.blur();

        assertFalse(input.followTarget(picker));
        assertEquals(0xFF336699, picker.getValue());
    }

    @Test
    void followingKeepsTheAlphaOfAnAlphaPicker() {
        PeakRichTextBox box = PeakRichTextBox.create("ns:notes", "§ared");
        PeakColorPicker picker = PeakColorPicker.create("ns:picker", 0x80336699).alpha(true).target("ns:notes");
        PeakMenuInputState input = focusedBox(box);
        input.richEditor(box).setCaret(2, false);

        assertTrue(input.followTarget(picker));
        assertEquals(0x8055FF55, picker.getValue());
    }

    @Test
    void aPickerWithoutATargetFollowsNothing() {
        PeakRichTextBox box = PeakRichTextBox.create("ns:notes", "§cred");
        PeakColorPicker picker = PeakColorPicker.create("ns:picker", 0xFF336699);
        PeakMenuInputState input = focusedBox(box);
        input.richEditor(box).setCaret(2, false);

        assertFalse(input.followTarget(picker));
    }
}
