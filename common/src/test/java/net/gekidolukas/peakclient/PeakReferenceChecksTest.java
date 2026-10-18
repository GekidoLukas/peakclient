package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentValidator;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakReferenceChecksTest {
    private static List<String> issues(PeakDocument document) {
        return PeakDocumentValidator.validate(document, 10, 10).issues();
    }

    private static PeakDocument validMenu() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakLabel.create("ns:label", "Text"));
        menu.add(PeakRichTextBox.create("ns:notes", ""));
        menu.add(PeakTextBox.create("ns:hex", ""));
        menu.add(PeakColorPicker.create("ns:picker").target("ns:notes").hexField("ns:hex"));
        menu.add(PeakButton.text("ns:patch", "Patch").onClick(PeakAction.patch("ns:label", PeakPatch.label().text("x"))));
        menu.add(PeakButton.text("ns:toggle", "Toggle").onClick(PeakAction.toggle("ns:picker")));
        menu.add(PeakButton.text("ns:format", "Bold").onClick(PeakAction.format("ns:notes", net.gekidolukas.peakclient.ui.PeakFormat.BOLD)));
        menu.add(PeakButton.text("ns:color", "Red").onClick(PeakAction.setColor("ns:picker", 0xFF0000)));
        return menu;
    }

    @Test
    void validReferencesProduceNoIssues() {
        assertEquals(List.of(), issues(validMenu()));
    }

    @Test
    void aPickerTargetMustBeARichTextBoxAndTheHexFieldATextBox() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakTextBox.create("ns:plain", ""));
        menu.add(PeakRichTextBox.create("ns:rich", ""));
        menu.add(PeakColorPicker.create("ns:one").target("ns:plain").hexField("ns:rich"));
        menu.add(PeakColorPicker.create("ns:two").target("ns:missing").hexField("ns:gone"));

        List<String> found = issues(menu);

        assertEquals(4, found.size());
        assertTrue(found.stream().anyMatch(i -> i.contains("'ns:one'") && i.contains("target 'ns:plain'")));
        assertTrue(found.stream().anyMatch(i -> i.contains("'ns:one'") && i.contains("hex field 'ns:rich'")));
        assertTrue(found.stream().anyMatch(i -> i.contains("'ns:two'") && i.contains("target 'ns:missing'")));
        assertTrue(found.stream().anyMatch(i -> i.contains("'ns:two'") && i.contains("hex field 'ns:gone'")));
    }

    @Test
    void actionTargetsMustExistAndHaveTheRightType() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakLabel.create("ns:label", "Text"));
        menu.add(PeakSlider.create("ns:slider", 0, 10, 5));
        menu.add(PeakButton.text("ns:a", "A").onClick(PeakAction.patch("ns:nowhere", PeakPatch.label().text("x"))));
        menu.add(PeakButton.text("ns:b", "B").onClick(PeakAction.toggle("ns:nowhere")));
        menu.add(PeakButton.text("ns:c", "C").onClick(PeakAction.format("ns:label", net.gekidolukas.peakclient.ui.PeakFormat.BOLD)));
        menu.add(PeakButton.text("ns:d", "D").onClick(PeakAction.setColor("ns:slider", 0)));

        List<String> found = issues(menu);

        assertEquals(4, found.size());
        assertTrue(found.stream().anyMatch(i -> i.contains("PATCH") && i.contains("'ns:a'") && i.contains("does not exist")));
        assertTrue(found.stream().anyMatch(i -> i.contains("TOGGLE") && i.contains("'ns:b'") && i.contains("does not exist")));
        assertTrue(found.stream().anyMatch(i -> i.contains("FORMAT") && i.contains("'ns:c'") && i.contains("not a rich text box")));
        assertTrue(found.stream().anyMatch(i -> i.contains("SET_COLOR") && i.contains("'ns:d'") && i.contains("not a color picker")));
    }

    @Test
    void changeAndSubmitActionsAreCheckedToo() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakSlider.create("ns:slider", 0, 10, 5).onChange(PeakAction.toggle("ns:a")));
        menu.add(PeakCheckbox.create("ns:check", "Check").onChange(PeakAction.toggle("ns:b")));
        menu.add(PeakColorPicker.create("ns:picker").onChange(PeakAction.toggle("ns:c")));
        menu.add(PeakTextBox.create("ns:text", "").onSubmit(PeakAction.toggle("ns:d")));
        menu.add(PeakRichTextBox.create("ns:rich", "").onSubmit(PeakAction.toggle("ns:e")));

        assertEquals(5, issues(menu).size());
    }

    @Test
    void actionsThatNeedNoElementAreNotChecked() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakButton.text("ns:send", "Send").onClick(PeakAction.send("ns:hello"), PeakAction.close(),
                PeakAction.open("ns:other"), PeakAction.copyText("text")));

        assertEquals(List.of(), issues(menu));
    }

    @Test
    void hudDocumentsAreCheckedForReferencesAndMenuOnlyEvents() {
        PeakDocument hud = PeakDocument.create("hud");
        hud.add(PeakButton.text("ns:button", "Button").onClick(PeakAction.toggle("ns:nowhere")));

        List<String> issues = issues(hud);

        assertEquals(2, issues.size());
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("only fires in menus")));
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("ns:nowhere")));
    }

    @Test
    void theToggleActionCarriesItsTargetOverTheWire() {
        PeakAction action = PeakAction.toggle("ns:panel");
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakButton.text("ns:button", "Toggle").onClick(action));

        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu));
        PeakAction read = ((PeakButton) back.findById("ns:button")).actionsFor(PeakEvent.CLICK).get(0);

        assertEquals(PeakActionType.TOGGLE, read.type());
        assertEquals("ns:panel", read.target());
        assertThrows(IllegalArgumentException.class, () -> PeakAction.toggle("a:b:c"));
    }
}
