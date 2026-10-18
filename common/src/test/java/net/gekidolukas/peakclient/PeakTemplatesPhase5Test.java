package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakSpinner;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.client.PeakFormValidation;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.gekidolukas.peakclient.ui.template.PeakCheckList;
import net.gekidolukas.peakclient.ui.template.PeakCollapsible;
import net.gekidolukas.peakclient.ui.template.PeakFormField;
import net.gekidolukas.peakclient.ui.template.PeakPagedList;
import net.gekidolukas.peakclient.ui.template.PeakSeparator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakTemplatesPhase5Test {
    private static PeakDocument menuWith(PeakPanel panel) {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(panel);
        return menu;
    }

    @Test
    void aClosedCollapsibleHidesItsContentAndTheOpenArrow() {
        PeakPanel panel = PeakCollapsible.create("ns:box", "Advanced")
                .add(PeakLabel.create("ns:one", "One"), PeakLabel.create("ns:two", "Two"))
                .build();
        PeakDocument menu = menuWith(panel);

        assertTrue(menu.findById("ns:box/content").isHidden());
        assertTrue(menu.findById("ns:box/arrow-open").isHidden());
        assertFalse(menu.findById("ns:box/arrow-closed").isHidden());
        assertNotNull(menu.findById("ns:one"));

        PeakButton header = (PeakButton) menu.findById("ns:box/header");
        List<PeakAction> actions = header.actionsFor(PeakEvent.CLICK);
        assertEquals(3, actions.size());
        assertTrue(actions.stream().allMatch(a -> a.type() == PeakActionType.TOGGLE));
        assertEquals("ns:box/content", actions.get(0).target());
        assertEquals("ns:box/arrow-open", actions.get(1).target());
        assertEquals("ns:box/arrow-closed", actions.get(2).target());
    }

    @Test
    void anOpenCollapsibleStartsExpanded() {
        PeakDocument menu = menuWith(PeakCollapsible.create("ns:box", "Advanced").open(true).build());

        assertFalse(menu.findById("ns:box/content").isHidden());
        assertFalse(menu.findById("ns:box/arrow-open").isHidden());
        assertTrue(menu.findById("ns:box/arrow-closed").isHidden());
    }

    @Test
    void aSeparatorIsALineOrALabelWithALine() {
        PeakPanel plain = PeakSeparator.create("ns:sep").width(120).thickness(2).color(0xFF888888).build();
        PeakPanel labeled = PeakSeparator.create("ns:head").label("Section").width(150).build();

        assertEquals("ns:sep", plain.getId());
        assertEquals(120f, plain.getSize().getWidth().value());
        assertEquals(2f, plain.getSize().getHeight().value());
        assertEquals(PeakPaint.solid(0xFF888888), plain.getStyle().getBackground());

        PeakDocument menu = menuWith(labeled);
        assertNotNull(menu.findById("ns:head/label"));
        PeakPanel line = (PeakPanel) menu.findById("ns:head/line");
        assertEquals(150f, line.getSize().getWidth().value());
    }

    @Test
    void aCheckListSendsTheOptionAndTheCheckedStateSeparately() {
        PeakPanel list = PeakCheckList.create("ns:perks", "perk")
                .option("speed", "Speed", true)
                .option("jump", "Jump")
                .build();
        PeakDocument menu = menuWith(list);

        PeakCheckbox first = (PeakCheckbox) menu.findById("ns:perks/option-0");
        PeakCheckbox second = (PeakCheckbox) menu.findById("ns:perks/option-1");

        assertTrue(first.isChecked());
        assertFalse(second.isChecked());
        assertEquals("Speed", first.getText());
        PeakAction action = first.actionsFor(PeakEvent.CHANGE).get(0);
        assertEquals("perk", action.target());
        assertEquals("speed", action.args().get(PeakCheckList.OPTION_ARG));
        assertEquals("ns:perks/option-1", PeakCheckList.optionId("ns:perks", 1));
    }

    @Test
    void aFormFieldWrapsTheInputAndLinksTheError() {
        PeakTextBox input = PeakTextBox.create("ns:name-input", "");
        PeakPanel field = PeakFormField.create("ns:name", "Name").input(input).required(true).hint("3 to 16 letters").build();
        PeakDocument menu = menuWith(field);

        PeakLabel label = (PeakLabel) menu.findById("ns:name/label");
        PeakLabel error = (PeakLabel) menu.findById("ns:name/error");

        assertTrue(label.getText().startsWith("Name"));
        assertTrue(label.getText().contains("*"));
        assertEquals("3 to 16 letters", ((PeakLabel) menu.findById("ns:name/hint")).getText());
        assertTrue(error.isHidden());
        assertEquals(PeakFormField.REQUIRED_MESSAGE, error.getText());
        assertTrue(input.getInteraction().isRequired());
        assertEquals("ns:name/error", input.getInteraction().getErrorElement());
    }

    @Test
    void aFormFieldWithoutAnInputIsRejected() {
        assertThrows(IllegalStateException.class, () -> PeakFormField.create("ns:x", "X").build());
    }

    @Test
    void serverErrorsPatchTheInputAndTheErrorLabel() {
        List<PeakUiOperation> shown = PeakFormField.errorOperations("ns:name", "ns:name-input", "Too short");
        List<PeakUiOperation> cleared = PeakFormField.errorOperations("ns:name", "ns:name-input", "");

        PeakUiOperation.Update input = (PeakUiOperation.Update) shown.get(0);
        PeakUiOperation.Update error = (PeakUiOperation.Update) shown.get(1);
        assertEquals("ns:name-input", input.elementId());
        assertTrue(input.patch().get("interaction.invalid").getAsBoolean());
        assertEquals("ns:name/error", error.elementId());
        assertEquals("Too short", error.patch().get("text").getAsString());
        assertFalse(error.patch().get("hidden").getAsBoolean());

        PeakUiOperation.Update clearedInput = (PeakUiOperation.Update) cleared.get(0);
        PeakUiOperation.Update clearedError = (PeakUiOperation.Update) cleared.get(1);
        assertFalse(clearedInput.patch().get("interaction.invalid").getAsBoolean());
        assertTrue(clearedError.patch().get("hidden").getAsBoolean());
        assertFalse(clearedError.patch().has("text"));
    }

    @Test
    void theValidateFlagSurvivesTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakButton.text("ns:save", "Save").onClick(PeakAction.send("save").collectAll().validate()));
        menu.add(PeakButton.text("ns:plain", "Plain").onClick(PeakAction.send("plain")));

        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu));
        PeakAction save = ((PeakButton) back.findById("ns:save")).actionsFor(PeakEvent.CLICK).get(0);
        PeakAction plain = ((PeakButton) back.findById("ns:plain")).actionsFor(PeakEvent.CLICK).get(0);

        assertTrue(save.validates());
        assertTrue(save.collectsAll());
        assertFalse(plain.validates());
    }

    @Test
    void validationFlagsMissingRequiredInputs() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakTextBox.create("ns:name", "  ").required(true));
        menu.add(PeakTextBox.create("ns:city", "Berlin").required(true));
        menu.add(PeakTextBox.create("ns:note", "").required(false));
        menu.add(PeakNumberField.create("ns:amount", 1, 10, 3).required(true));
        menu.add(PeakDropdown.create("ns:country").option("de", "Germany").required(true));
        menu.add(PeakCheckbox.create("ns:terms", "Terms").required(true));
        PeakDocumentModel model = new PeakDocumentModel(menu);

        PeakFormValidation.Result result = PeakFormValidation.validate(model, PeakAction.send("save").collectAll().validate());

        assertEquals(List.of("ns:name", "ns:country", "ns:terms").stream().sorted().toList(),
                result.failed().stream().sorted().toList());
        assertTrue(result.passed().contains("ns:city"));
        assertTrue(result.passed().contains("ns:amount"));
        assertFalse(result.passed().contains("ns:note"));
    }

    @Test
    void validationOnlyLooksAtCollectedInputsWhenListed() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakTextBox.create("ns:name", "").required(true));
        menu.add(PeakTextBox.create("ns:city", "").required(true));
        PeakDocumentModel model = new PeakDocumentModel(menu);

        PeakFormValidation.Result result = PeakFormValidation.validate(model, PeakAction.send("save").collect("ns:city").validate());

        assertEquals(List.of("ns:city"), result.failed());
        assertTrue(result.passed().isEmpty());
    }

    @Test
    void aFilledCheckboxAndRadioPass() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakCheckbox.create("ns:terms", "Terms").required(true).checked(true));
        PeakDocumentModel model = new PeakDocumentModel(menu);

        PeakFormValidation.Result result = PeakFormValidation.validate(model, PeakAction.send("save").collectAll().validate());

        assertTrue(result.failed().isEmpty());
        assertEquals(List.of("ns:terms"), result.passed());
    }

    @Test
    void theSpinnerHasDefaultsAndSurvivesTheWire() {
        PeakSpinner spinner = PeakSpinner.create("ns:spin");
        assertEquals(16f, spinner.getSize().getWidth().value());
        assertEquals(8, spinner.getDots());
        assertEquals(16, spinner.getPeriodTicks());
        assertEquals("spinner", spinner.elementType());

        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakSpinner.create("ns:spin2").color(0x55FF55).dots(12).periodTicks(24));
        PeakSpinner read = (PeakSpinner) PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu)).findById("ns:spin2");

        assertEquals(PeakPaint.solid(0xFF55FF55), read.getColor());
        assertEquals(12, read.getDots());
        assertEquals(24, read.getPeriodTicks());
    }

    @Test
    void theSpinnerIsPatchable() {
        PeakSpinner spinner = PeakSpinner.create("ns:spin");

        spinner.applyPatch(PeakPatch.spinner().dots(4).color(0xFF0000).build());

        assertEquals(4, spinner.getDots());
        assertEquals(PeakPaint.solid(0xFFFF0000), spinner.getColor());
    }

    @Test
    void aRemoteListCanShowASpinnerWhileLoading() {
        PeakPagedList list = PeakPagedList.remote("ns:list", 2).loadingSpinner();
        PeakDocument menu = menuWith(list.build());

        assertInstanceOf(PeakSpinner.class, menu.findById("ns:list/page-0/loading"));
        assertInstanceOf(PeakSpinner.class, list.loadingElement(1));
        assertEquals("ns:list/page-1/loading", list.loadingElement(1).getId());
    }

    @Test
    void aRemoteListKeepsItsLabelByDefault() {
        PeakPagedList list = PeakPagedList.remote("ns:list", 2).loadingText("Wait");

        assertInstanceOf(PeakLabel.class, list.loadingElement(0));
        assertEquals("Wait", ((PeakLabel) list.loadingElement(0)).getText());
    }

    @Test
    void errorOperationsUseOnlyKnownPatchKeys() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        PeakTextBox input = PeakTextBox.create("ns:name-input", "");
        menu.add(PeakFormField.create("ns:name", "Name").input(input).build());

        for (PeakUiOperation operation : PeakFormField.errorOperations("ns:name", "ns:name-input", "Bad")) {
            PeakUiOperation.Update update = (PeakUiOperation.Update) operation;
            var target = menu.findById(update.elementId());
            for (String key : update.patch().keySet()) {
                assertTrue(target.isPatchKeyKnown(key), key + " on " + update.elementId());
            }
        }
    }
}
