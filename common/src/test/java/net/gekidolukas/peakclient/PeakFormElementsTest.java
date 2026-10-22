package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakCheckboxLook;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakNumberFieldLayout;
import net.gekidolukas.peakclient.ui.PeakOption;
import net.gekidolukas.peakclient.ui.PeakRadioGroup;
import net.gekidolukas.peakclient.ui.PeakRadioLayout;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.client.PeakInputValues;
import net.gekidolukas.peakclient.ui.client.PeakMenuInputState;
import net.gekidolukas.peakclient.ui.client.PeakTextEditor;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PeakFormElementsTest {
    private static PeakDocument roundTrip(PeakDocument document) {
        return PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
    }

    private static PeakRadioGroup sizes() {
        return PeakRadioGroup.create("ns:size")
                .option("s", "Small")
                .option("m", "Medium")
                .option("l", "Large", false)
                .option("xl", "Extra large")
                .selected("m");
    }

    @Test
    void theRadioGroupFindsItsSelectionAndMovesOverDisabledOptions() {
        PeakRadioGroup group = sizes();

        assertEquals(1, group.selectedIndex());
        assertEquals(3, group.nextEnabledIndex(1, 1));
        assertEquals(0, group.nextEnabledIndex(1, -1));
        assertEquals(0, group.nextEnabledIndex(3, 1));
        assertEquals(3, group.nextEnabledIndex(0, -1));
        assertEquals(0, group.firstEnabledIndex());
        assertEquals(3, group.lastEnabledIndex());
        assertEquals("m", group.inputValue());
    }

    @Test
    void anUnknownSelectionHasNoIndex() {
        PeakRadioGroup group = sizes().selected("nope");

        assertEquals(-1, group.selectedIndex());
        assertEquals("nope", group.inputValue());
    }

    @Test
    void aGroupWithoutEnabledOptionsMovesNowhere() {
        PeakRadioGroup group = PeakRadioGroup.create("ns:none").option("a", "A", false);

        assertEquals(-1, group.firstEnabledIndex());
        assertEquals(-1, group.nextEnabledIndex(0, 1));
    }

    @Test
    void theRadioGroupSurvivesTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(sizes().row().gap(8).onChange(PeakAction.send("size-changed")));

        PeakRadioGroup read = (PeakRadioGroup) roundTrip(menu).findById("ns:size");

        assertEquals(4, read.getOptions().size());
        assertEquals("Large", read.getOptions().get(2).getText().getString());
        assertFalse(read.getOptions().get(2).isEnabled());
        assertEquals("m", read.getSelected());
        assertEquals(PeakLayoutDirection.ROW, read.getDirection());
        assertEquals(8, read.getGap());
        assertEquals("size-changed", read.actionsFor(PeakEvent.CHANGE).get(0).target());
        assertNotNull(read.getInteraction());
    }

    @Test
    void theRadioGroupIsPatchable() {
        PeakRadioGroup group = sizes();

        group.applyPatch(PeakPatch.radioGroup().selected("xl").options(PeakOption.of("a", "A"), PeakOption.of("b", "B")).build());

        assertEquals("xl", group.getSelected());
        assertEquals(2, group.getOptions().size());
        assertEquals("b", group.getOptions().get(1).getValue());
        assertEquals("radio_group", group.elementType());
    }

    @Test
    void theRadioLayoutStacksOrLinesUpTheOptions() {
        int[] widths = { 30, 50, 0 };

        PeakRadioLayout column = PeakRadioLayout.of(widths, 9, false, 4);
        PeakRadioLayout row = PeakRadioLayout.of(widths, 9, true, 4);

        assertEquals(3, column.count());
        assertEquals(PeakRadioLayout.CIRCLE, column.rowHeight());
        assertEquals(0, column.cell(0).y());
        assertEquals(PeakRadioLayout.CIRCLE + 4, column.cell(1).y());
        assertEquals(CIRCLE_PLUS_TEXT(50), column.width());
        assertEquals(3 * PeakRadioLayout.CIRCLE + 2 * 4, column.height());

        assertEquals(0, row.cell(0).x());
        assertEquals(CIRCLE_PLUS_TEXT(30) + 4, row.cell(1).x());
        assertEquals(PeakRadioLayout.CIRCLE, row.height());
    }

    private static int CIRCLE_PLUS_TEXT(int text) {
        return PeakRadioLayout.CIRCLE + PeakRadioLayout.TEXT_GAP + text;
    }

    @Test
    void theRadioLayoutFindsTheOptionUnderAPoint() {
        PeakRadioLayout layout = PeakRadioLayout.of(new int[]{ 30, 50 }, 9, false, 4);

        assertEquals(0, layout.optionAt(2, 2));
        assertEquals(1, layout.optionAt(20, PeakRadioLayout.CIRCLE + 6));
        assertEquals(-1, layout.optionAt(5, PeakRadioLayout.CIRCLE + 1));
        assertEquals(-1, layout.optionAt(500, 2));
        assertEquals(-1, PeakRadioLayout.of(new int[0], 9, false, 4).optionAt(0, 0));
    }

    @Test
    void theNumberFieldClampsAndSnapsToTheStep() {
        PeakNumberField field = PeakNumberField.create("ns:n", 0, 10, 3.4).step(0.5);

        assertEquals(3.5, field.getValue());
        assertEquals(10.0, field.value(99).getValue());
        assertEquals(0.0, field.value(-5).getValue());
        assertEquals(0.0, field.value(Double.NaN).getValue());
    }

    @Test
    void steppingMovesByTheStepAndKeepsWithinTheRange() {
        PeakNumberField field = PeakNumberField.create("ns:n", 0, 10, 5).step(2);

        assertEquals(6.0, field.stepped(4, 1));
        assertEquals(2.0, field.stepped(4, -1));
        assertEquals(10.0, field.stepped(8, 10));
        assertEquals(0.0, field.stepped(2, -10));
    }

    @Test
    void numbersAreFormattedAndParsed() {
        PeakNumberField auto = PeakNumberField.create("ns:a", 0, 100, 2.5).step(0);
        PeakNumberField fixed = PeakNumberField.create("ns:b", 0, 100, 2.5).step(0.5).decimals(2);

        assertEquals("2.5", auto.inputValue());
        assertEquals("2.50", fixed.inputValue());
        assertEquals("7", PeakNumberField.create("ns:c", 0, 100, 7).inputValue());
        assertEquals(12.5, PeakNumberField.parse("12.5"));
        assertEquals(12.5, PeakNumberField.parse(" 12,5 "));
        assertEquals(-3.0, PeakNumberField.parse("-3"));
        assertNull(PeakNumberField.parse("-"));
        assertNull(PeakNumberField.parse(""));
        assertNull(PeakNumberField.parse("abc"));
    }

    @Test
    void theNumberFieldSurvivesTheWireAndIsCollected() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakNumberField.create("ns:amount", 1, 64, 16).step(1).showButtons(false).onChange(PeakAction.send("amount")));

        PeakNumberField read = (PeakNumberField) roundTrip(menu).findById("ns:amount");

        assertEquals(16.0, read.getValue());
        assertEquals(1.0, read.getMin());
        assertEquals(64.0, read.getMax());
        assertFalse(read.isShowButtons());
        assertEquals("amount", read.actionsFor(PeakEvent.CHANGE).get(0).target());

        Map<String, String> arguments = PeakInputValues.arguments(new PeakDocumentModel(menu),
                PeakAction.send("save").collectAll(), Map.of());
        assertEquals("16", arguments.get("ns:amount"));
    }

    @Test
    void theNumberFieldIsPatchable() {
        PeakNumberField field = PeakNumberField.create("ns:n");

        field.applyPatch(PeakPatch.numberField().min(5).max(20).step(5).value(12).decimals(1).build());

        assertEquals(10.0, field.getValue());
        assertEquals("10.0", field.inputValue());
        assertEquals("number_field", field.elementType());
    }

    @Test
    void theNumberEditorFollowsTheValueOnlyWhileNotFocused() {
        PeakNumberField field = PeakNumberField.create("ns:n", 0, 100, 5);
        PeakMenuInputState input = new PeakMenuInputState();

        PeakTextEditor editor = input.numberEditor(field);
        assertEquals("5", editor.value());

        field.value(8);
        assertEquals("8", input.numberEditor(field).value());

        input.focus("ns:n");
        field.value(9);
        assertEquals("8", input.numberEditor(field).value());

        input.resetNumberEditor(field);
        assertEquals("9", input.numberEditor(field).value());
    }

    @Test
    void theNumberFieldLayoutSplitsButtonsAndText() {
        PeakNumberFieldLayout layout = PeakNumberFieldLayout.of(100, 16, true);

        assertTrue(layout.hasButtons());
        assertEquals(PeakNumberFieldLayout.Part.MINUS, layout.partAt(2, 5));
        assertEquals(PeakNumberFieldLayout.Part.PLUS, layout.partAt(97, 5));
        assertEquals(PeakNumberFieldLayout.Part.TEXT, layout.partAt(50, 5));
        assertEquals(PeakNumberFieldLayout.Part.NONE, layout.partAt(120, 5));
        assertEquals(layout.width() - layout.buttonWidth(), layout.plusX());
        assertEquals(layout.width() - 2 * layout.buttonWidth(), layout.textWidth());

        PeakNumberFieldLayout plain = PeakNumberFieldLayout.of(100, 16, false);
        assertFalse(plain.hasButtons());
        assertEquals(PeakNumberFieldLayout.Part.TEXT, plain.partAt(2, 5));
        assertEquals(100, plain.textWidth());
    }

    @Test
    void theCheckboxLookSurvivesTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakCheckbox.create("ns:flag", "Flag").asSwitch().checked(true));
        menu.add(PeakCheckbox.create("ns:plain", "Plain"));

        PeakDocument back = roundTrip(menu);

        assertEquals(PeakCheckboxLook.SWITCH, ((PeakCheckbox) back.findById("ns:flag")).getLook());
        assertEquals(PeakCheckboxLook.BOX, ((PeakCheckbox) back.findById("ns:plain")).getLook());
    }

    @Test
    void theCheckboxLookIsPatchable() {
        PeakCheckbox box = PeakCheckbox.create("ns:flag", "Flag");

        box.applyPatch(PeakPatch.checkbox().look(PeakCheckboxLook.SWITCH).build());

        assertEquals(PeakCheckboxLook.SWITCH, box.getLook());
    }
}
