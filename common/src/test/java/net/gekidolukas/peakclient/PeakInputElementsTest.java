package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTextFilter;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.client.PeakClientMenuState;
import net.gekidolukas.peakclient.ui.client.PeakInputValues;
import net.gekidolukas.peakclient.ui.client.PeakMenuInputState;
import net.gekidolukas.peakclient.ui.client.PeakTextEditor;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PeakInputElementsTest {
    private static PeakDocument form() {
        PeakDocument menu = PeakMenu.create("form");
        menu.add(PeakTextBox.create("name", "Alex"));
        menu.add(PeakTextBox.create("amount", "12").filter(PeakTextFilter.INTEGER));
        menu.add(PeakCheckbox.create("agree", "I agree").checked(true));
        menu.add(PeakSlider.create("volume", 0, 10, 7).step(1));
        menu.add(PeakLabel.create("label", "not an input"));
        return menu;
    }

    @Test
    void sliderSnapsClampsAndFormats() {
        PeakSlider slider = PeakSlider.create("s", 0, 10, 0).step(2);

        assertEquals(6, slider.valueForFraction(0.55), 1e-9);
        assertEquals(10, slider.valueForFraction(5), 1e-9);
        assertEquals(0, slider.valueForFraction(-1), 1e-9);

        PeakSlider continuous = PeakSlider.create("c", 0, 1, 0.25);
        assertEquals("0.25", continuous.inputValue());
        assertEquals("7", PeakSlider.create("i", 0, 10, 7).inputValue());
        assertEquals("100%", PeakSlider.create("p", 0, 100, 100).format("%.0f%%").displayText());
    }

    @Test
    void sliderValuesOutsideTheRangeAreClamped() {
        PeakSlider slider = PeakSlider.create("s", 5, 15, 100);

        assertEquals(15, slider.getValue(), 1e-9);
        assertEquals(1.0, slider.fraction(), 1e-9);
    }

    @Test
    void textBoxLimitsAreClamped() {
        PeakTextBox field = PeakTextBox.create("f").maxLength(100000);

        assertEquals(PeakTextBox.MAX_LENGTH_LIMIT, field.getMaxLength());
        assertEquals(1, field.maxLength(0).getMaxLength());
    }

    @Test
    void collectAllGathersEveryInputOfTheMenu() {
        PeakDocumentModel model = new PeakDocumentModel(form());

        Map<String, String> arguments = PeakInputValues.arguments(model, PeakAction.send("save").collectAll(), Map.of());

        assertEquals(Map.of("name", "Alex", "amount", "12", "agree", "true", "volume", "7"), arguments);
    }

    @Test
    void collectGathersOnlyTheNamedInputs() {
        PeakDocumentModel model = new PeakDocumentModel(form());

        Map<String, String> arguments = PeakInputValues.arguments(model,
                PeakAction.send("save").collect("name", "label", "missing"), Map.of());

        assertEquals(Map.of("name", "Alex"), arguments);
    }

    @Test
    void explicitArgumentsWinOverCollectedAndExtraValues() {
        PeakDocumentModel model = new PeakDocumentModel(form());
        PeakAction action = PeakAction.send("save").collect("name").arg("name", "fixed");

        Map<String, String> arguments = PeakInputValues.arguments(model, action, Map.of("value", "v", "name", "extra"));

        assertEquals("fixed", arguments.get("name"));
        assertEquals("v", arguments.get("value"));
    }

    @Test
    void collectionWorksWithoutAModel() {
        Map<String, String> arguments = PeakInputValues.arguments(null, PeakAction.send("x").collectAll().arg("k", "v"), Map.of());

        assertEquals(Map.of("k", "v"), arguments);
    }

    @Test
    void inputsSurviveTheWireFormat() {
        PeakDocument menu = form();
        ((PeakTextBox) menu.findById("name")).onSubmit(PeakAction.send("submit"));
        ((PeakCheckbox) menu.findById("agree")).onChange(PeakAction.send("agree").collect("name"));

        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu));

        PeakTextBox name = (PeakTextBox) back.findById("name");
        assertEquals("Alex", name.getValue());
        assertEquals(1, name.actionsFor(PeakEvent.SUBMIT).size());
        assertEquals(PeakTextFilter.INTEGER, ((PeakTextBox) back.findById("amount")).getFilter());
        PeakCheckbox agree = (PeakCheckbox) back.findById("agree");
        assertTrue(agree.isChecked());
        assertEquals(List.of("name"), agree.actionsFor(PeakEvent.CHANGE).get(0).collected());
        PeakSlider volume = (PeakSlider) back.findById("volume");
        assertEquals(7, volume.getValue(), 1e-9);
        assertEquals(1, volume.getStep(), 1e-9);
        assertNotNull(name.getInteraction());
    }

    @Test
    void serverPatchesChangeTheCachedValues() {
        PeakClientMenuState state = new PeakClientMenuState();
        state.put(form(), Set.of());

        state.applyOperations("form", List.of(
                new PeakUiOperation.Update("name", PeakPatch.textBox().value("Sam").build()),
                new PeakUiOperation.Update("agree", PeakPatch.checkbox().checked(false).build()),
                new PeakUiOperation.Update("volume", PeakPatch.slider().value(3).build())));

        PeakDocument menu = state.get("form");
        assertEquals("Sam", ((PeakTextBox) menu.findById("name")).getValue());
        assertFalse(((PeakCheckbox) menu.findById("agree")).isChecked());
        assertEquals(3, ((PeakSlider) menu.findById("volume")).getValue(), 1e-9);
    }

    @Test
    void theEditorFollowsServerChangesButNotItsOwnEdits() {
        PeakTextBox field = PeakTextBox.create("f", "start");
        PeakMenuInputState input = new PeakMenuInputState();

        PeakTextEditor editor = input.editor(field);
        assertEquals("start", editor.value());

        editor.insert("!");
        input.commit(field, editor);
        assertEquals("start!", field.getValue());
        assertEquals(6, input.editor(field).caret());

        field.value("from server");
        assertEquals("from server", input.editor(field).value());
    }

    @Test
    void focusIsTrackedByElementId() {
        PeakMenuInputState input = new PeakMenuInputState();
        PeakElement<?> field = PeakTextBox.create("f");

        input.focus(field.getId());
        assertTrue(input.isFocused("f"));
        input.forget("f");
        assertFalse(input.isFocused("f"));
        assertNull(input.focusedId());
    }
}
