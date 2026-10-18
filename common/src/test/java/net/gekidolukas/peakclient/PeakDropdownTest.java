package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakDropdownLayout;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakOption;
import net.gekidolukas.peakclient.ui.client.PeakMenuInputState;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakDropdownTest {
    private static PeakDropdown countries() {
        return PeakDropdown.create("ns:country")
                .option("de", "Germany")
                .option("fr", "France")
                .option("xx", "Hidden", false)
                .option("it", "Italy")
                .selected("fr")
                .placeholder("Pick one")
                .maxVisible(3);
    }

    @Test
    void theDropdownKnowsItsSelection() {
        PeakDropdown dropdown = countries();

        assertEquals(1, dropdown.selectedIndex());
        assertEquals("France", dropdown.selectedText());
        assertEquals("fr", dropdown.inputValue());
        assertEquals(3, dropdown.nextEnabledIndex(1, 1));
        assertEquals(0, dropdown.nextEnabledIndex(1, -1));
        assertEquals(0, dropdown.firstEnabledIndex());
        assertEquals(3, dropdown.lastEnabledIndex());
    }

    @Test
    void anUnknownSelectionShowsNoText() {
        PeakDropdown dropdown = countries().selected("zz");

        assertEquals(-1, dropdown.selectedIndex());
        assertEquals("", dropdown.selectedText());
    }

    @Test
    void theDropdownSurvivesTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(countries().onChange(PeakAction.send("country")));

        PeakDropdown read = (PeakDropdown) PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu)).findById("ns:country");

        assertEquals(4, read.getOptions().size());
        assertFalse(read.getOptions().get(2).isEnabled());
        assertEquals("fr", read.getSelected());
        assertEquals("Pick one", read.getPlaceholder());
        assertEquals(3, read.getMaxVisible());
        assertEquals("country", read.actionsFor(PeakEvent.CHANGE).get(0).target());
        assertNotNull(read.getInteraction());
    }

    @Test
    void theDropdownIsPatchable() {
        PeakDropdown dropdown = countries();

        dropdown.applyPatch(PeakPatch.dropdown().selected("it").maxVisible(10)
                .options(PeakOption.of("a", "A"), PeakOption.of("it", "Italy")).build());

        assertEquals(2, dropdown.getOptions().size());
        assertEquals("it", dropdown.getSelected());
        assertEquals(10, dropdown.getMaxVisible());
        assertEquals("dropdown", dropdown.elementType());
    }

    @Test
    void theOverlayOpensBelowTheFieldWhenThereIsRoom() {
        PeakDropdownLayout layout = PeakDropdownLayout.of(20, 30, 100, 16, 400, 300, 3, 6, 12, 50, 0);

        assertFalse(layout.above());
        assertEquals(20, layout.x());
        assertEquals(46, layout.y());
        assertEquals(100, layout.width());
        assertEquals(3, layout.visibleRows());
        assertEquals(3 * 12 + 2, layout.height());
    }

    @Test
    void theOverlayOpensAboveNearTheBottomEdge() {
        PeakDropdownLayout layout = PeakDropdownLayout.of(20, 250, 100, 16, 400, 300, 6, 6, 12, 50, 0);

        assertTrue(layout.above());
        assertEquals(250 - layout.height(), layout.y());
        assertEquals(6, layout.visibleRows());
    }

    @Test
    void theOverlayShrinksToTheRoomThatIsLeft() {
        PeakDropdownLayout layout = PeakDropdownLayout.of(20, 50, 100, 16, 400, 150, 20, 8, 12, 50, 0);

        assertFalse(layout.above());
        assertEquals(6, layout.visibleRows());
        assertEquals(66, layout.y());
        assertTrue(layout.y() + layout.height() <= 150);
    }

    @Test
    void longTextsWidenTheOverlayAndStayOnScreen() {
        PeakDropdownLayout wide = PeakDropdownLayout.of(300, 30, 100, 16, 400, 300, 3, 6, 12, 150, 0);

        assertTrue(wide.width() > 100);
        assertTrue(wide.x() + wide.width() <= 400);
    }

    @Test
    void scrollingIsClampedAndRowsAreFound() {
        PeakDropdownLayout layout = PeakDropdownLayout.of(0, 0, 100, 16, 400, 300, 10, 4, 12, 40, 99);

        assertTrue(layout.scrolls());
        assertEquals(6, layout.firstRow());
        assertEquals(6, layout.maxFirstRow());
        assertEquals(6, layout.rowAt(10, layout.y() + 2));
        assertEquals(9, layout.rowAt(10, layout.y() + 1 + 3 * 12 + 2));
        assertEquals(-1, layout.rowAt(-5, layout.y() + 2));
        assertEquals(-1, layout.rowAt(10, layout.y() + layout.height() + 5));
    }

    @Test
    void scrollToShowKeepsTheIndexVisible() {
        PeakDropdownLayout layout = PeakDropdownLayout.of(0, 0, 100, 16, 400, 300, 10, 4, 12, 40, 3);

        assertEquals(3, layout.scrollToShow(4));
        assertEquals(1, layout.scrollToShow(1));
        assertEquals(6, layout.scrollToShow(9));
        assertEquals(3, layout.scrollToShow(-1));
    }

    @Test
    void theOpenStateIsPerElementAndClears() {
        PeakMenuInputState input = new PeakMenuInputState();

        assertFalse(input.isDropdownOpen("ns:country"));
        input.openDropdown("ns:country", 2, 1);
        assertTrue(input.isDropdownOpen("ns:country"));
        assertFalse(input.isDropdownOpen("ns:other"));
        assertEquals(2, input.dropdownScroll());
        assertEquals(1, input.dropdownCursor());

        input.forget("ns:country");
        assertNull(input.openDropdownId());

        input.openDropdown("ns:country", 0, 0);
        input.closeDropdown();
        assertNull(input.openDropdownId());
        assertEquals(-1, input.dropdownCursor());
    }
}
