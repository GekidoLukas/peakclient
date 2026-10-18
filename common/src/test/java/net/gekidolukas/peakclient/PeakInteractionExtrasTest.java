package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.gekidolukas.peakclient.ui.client.PeakChangeDebouncer;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakInteractionExtrasTest {
    private static PeakDocument roundTrip(PeakDocument document) {
        return PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
    }

    @Test
    void rightAndDoubleClickActionsSurviveTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakButton.text("ns:slot", "Slot")
                .onClick(PeakAction.send("select"))
                .onRightClick(PeakAction.send("inspect"))
                .onDoubleClick(PeakAction.send("use")));

        PeakButton read = (PeakButton) roundTrip(menu).findById("ns:slot");

        assertEquals("select", read.actionsFor(PeakEvent.CLICK).get(0).target());
        assertEquals("inspect", read.actionsFor(PeakEvent.RIGHT_CLICK).get(0).target());
        assertEquals("use", read.actionsFor(PeakEvent.DOUBLE_CLICK).get(0).target());
    }

    @Test
    void anElementWithoutThemCarriesEmptyLists() {
        PeakButton read = (PeakButton) roundTrip(menuWith(PeakButton.text("ns:b", "B"))).findById("ns:b");

        assertTrue(read.actionsFor(PeakEvent.RIGHT_CLICK).isEmpty());
        assertTrue(read.actionsFor(PeakEvent.DOUBLE_CLICK).isEmpty());
        assertFalse(read.getInteraction().isInvalid());
        assertFalse(read.getInteraction().isRequired());
    }

    private static PeakDocument menuWith(PeakButton button) {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(button);
        return menu;
    }

    @Test
    void invalidAndRequiredSurviveTheWireAndPatches() {
        PeakTextBox field = PeakTextBox.create("ns:name", "").required(true).invalid(true).invalidColor(0xFFAA0000);
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(field);

        PeakInteraction read = ((PeakTextBox) roundTrip(menu).findById("ns:name")).getInteraction();
        assertTrue(read.isRequired());
        assertTrue(read.isInvalid());
        assertEquals(PeakPaint.solid(0xFFAA0000), read.getInvalidColor());

        field.applyPatch(PeakPatch.textBox().field("interaction.invalid", false).build());
        assertFalse(field.getInteraction().isInvalid());
    }

    @Test
    void theInvalidColorFollowsElementThenMenuThenDefault() {
        PeakMenuSettings settings = PeakMenuSettings.create().invalidColor(0xFF990000);

        assertEquals(PeakPaint.solid(0xFFFF5555), PeakTheme.invalidColor(null, null));
        assertEquals(PeakPaint.solid(0xFF990000), PeakTheme.invalidColor(settings, PeakInteraction.create()));
        assertEquals(PeakPaint.solid(0xFF112233), PeakTheme.invalidColor(settings, PeakInteraction.create().invalidColor(0x112233)));
    }

    @Test
    void textBoxesCarryOnChangeAndTheDelay() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakTextBox.create("ns:search", "").onChange(PeakAction.send("search")).changeDelayTicks(12));
        menu.add(PeakRichTextBox.create("ns:notes", "").onChange(PeakAction.send("notes")));

        PeakDocument back = roundTrip(menu);
        PeakTextBox search = (PeakTextBox) back.findById("ns:search");
        PeakRichTextBox notes = (PeakRichTextBox) back.findById("ns:notes");

        assertEquals("search", search.actionsFor(PeakEvent.CHANGE).get(0).target());
        assertEquals(12, search.getChangeDelayTicks());
        assertEquals("notes", notes.actionsFor(PeakEvent.CHANGE).get(0).target());
        assertEquals(5, notes.getChangeDelayTicks());
    }

    @Test
    void theDebouncerFiresOnceAfterTheDelayWithTheLastValue() {
        PeakChangeDebouncer debouncer = new PeakChangeDebouncer();

        debouncer.change("a", "h", 5, 100);
        debouncer.change("a", "he", 5, 101);
        debouncer.change("a", "hel", 5, 103);

        assertTrue(debouncer.due(104).isEmpty());
        List<PeakChangeDebouncer.Fire> fires = debouncer.due(105);
        assertEquals(1, fires.size());
        assertEquals("hel", fires.get(0).value());
        assertTrue(debouncer.due(200).isEmpty());
        assertTrue(debouncer.isEmpty());
    }

    @Test
    void aZeroDelayFiresImmediately() {
        PeakChangeDebouncer debouncer = new PeakChangeDebouncer();

        debouncer.change("a", "x", 0, 10);

        assertEquals(1, debouncer.due(10).size());
    }

    @Test
    void flushingDeliversThePendingValueEarly() {
        PeakChangeDebouncer debouncer = new PeakChangeDebouncer();
        debouncer.change("a", "one", 20, 0);
        debouncer.change("b", "two", 20, 0);

        PeakChangeDebouncer.Fire a = debouncer.flush("a");
        assertEquals("one", a.value());
        assertNull(debouncer.flush("a"));
        assertTrue(debouncer.hasPending("b"));

        List<PeakChangeDebouncer.Fire> rest = debouncer.flushAll();
        assertEquals(1, rest.size());
        assertEquals("b", rest.get(0).elementId());
        assertTrue(debouncer.isEmpty());
    }

    @Test
    void elementsKeepTheirOwnDeadlines() {
        PeakChangeDebouncer debouncer = new PeakChangeDebouncer();
        debouncer.change("a", "1", 5, 0);
        debouncer.change("b", "2", 10, 0);

        assertEquals(1, debouncer.due(5).size());
        assertEquals(1, debouncer.due(10).size());
    }
}
