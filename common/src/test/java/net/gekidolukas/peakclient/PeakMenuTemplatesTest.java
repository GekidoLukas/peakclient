package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakConfirmDialog;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentValidator;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakMenuEscape;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.template.PeakChoiceList;
import net.gekidolukas.peakclient.ui.template.PeakPagedList;
import net.gekidolukas.peakclient.ui.template.PeakTabs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakMenuTemplatesTest {
    private static PeakPagedList list(int items, int perPage) {
        PeakPagedList list = PeakPagedList.create("list", perPage);
        for (int i = 1; i <= items; i++) list.add(PeakLabel.create("item-" + i, "Item " + i));
        return list;
    }

    @Test
    void pagedListSplitsItemsIntoPagesAndShowsTheFirst() {
        PeakPanel root = list(7, 3).build();

        assertEquals(3, root.getChildren().size());
        PeakPanel first = (PeakPanel) root.getChildren().get(0);
        PeakPanel second = (PeakPanel) root.getChildren().get(1);
        PeakPanel last = (PeakPanel) root.getChildren().get(2);
        assertFalse(first.isHidden());
        assertTrue(second.isHidden());
        assertTrue(last.isHidden());
        assertEquals(4, first.getChildren().size());
        assertEquals(4, second.getChildren().size());
        assertEquals(2, last.getChildren().size());
        assertEquals("list/page-0", first.getId());
    }

    @Test
    void pagedListNavigationPatchesTheNeighbouringPages() {
        PeakPanel root = list(7, 3).build();
        PeakPanel second = (PeakPanel) root.getChildren().get(1);
        PeakPanel navigation = (PeakPanel) second.getChildren().get(second.getChildren().size() - 1);
        PeakButton previous = (PeakButton) navigation.getChildren().get(0);
        PeakButton next = (PeakButton) navigation.getChildren().get(2);

        List<PeakAction> nextActions = next.actionsFor(PeakEvent.CLICK);
        assertEquals(2, nextActions.size());
        assertEquals(PeakActionType.PATCH, nextActions.get(0).type());
        assertEquals("list/page-1", nextActions.get(0).target());
        assertTrue(nextActions.get(0).patch().get("hidden").getAsBoolean());
        assertEquals("list/page-2", nextActions.get(1).target());
        assertFalse(nextActions.get(1).patch().get("hidden").getAsBoolean());
        assertEquals("list/page-0", previous.actionsFor(PeakEvent.CLICK).get(1).target());
    }

    @Test
    void firstAndLastPageDisableTheirOuterButtons() {
        PeakPanel root = list(7, 3).build();
        PeakPanel first = (PeakPanel) root.getChildren().get(0);
        PeakPanel last = (PeakPanel) root.getChildren().get(2);
        PeakPanel firstNavigation = (PeakPanel) first.getChildren().get(first.getChildren().size() - 1);
        PeakPanel lastNavigation = (PeakPanel) last.getChildren().get(last.getChildren().size() - 1);

        assertFalse(((PeakButton) firstNavigation.getChildren().get(0)).getInteraction().isEnabled());
        assertTrue(((PeakButton) firstNavigation.getChildren().get(2)).getInteraction().isEnabled());
        assertFalse(((PeakButton) lastNavigation.getChildren().get(2)).getInteraction().isEnabled());
    }

    @Test
    void aSinglePageHasNoNavigation() {
        PeakPanel root = list(2, 5).build();

        assertEquals(1, root.getChildren().size());
        assertEquals(2, ((PeakPanel) root.getChildren().get(0)).getChildren().size());
    }

    @Test
    void anEmptyListStillBuildsOnePage() {
        PeakPagedList empty = PeakPagedList.create("empty", 4);

        assertEquals(1, empty.pageCount());
        assertEquals(1, empty.build().getChildren().size());
    }

    @Test
    void tabsShowOnlyTheSelectedContent() {
        PeakPanel root = PeakTabs.create("tabs")
                .tab("One", PeakLabel.create("a", "A"))
                .tab("Two", PeakLabel.create("b", "B"))
                .tab("Three", PeakLabel.create("c", "C"))
                .selected(1)
                .build();

        assertEquals(4, root.getChildren().size());
        PeakPanel header = (PeakPanel) root.getChildren().get(0);
        assertEquals(3, header.getChildren().size());
        assertTrue(((PeakPanel) root.getChildren().get(1)).isHidden());
        assertFalse(((PeakPanel) root.getChildren().get(2)).isHidden());
        assertTrue(((PeakPanel) root.getChildren().get(3)).isHidden());
        assertEquals(PeakPaint.solid(PeakTabs.SELECTED_BACKGROUND), ((PeakButton) header.getChildren().get(1)).getStyle().getBackground());
        assertEquals(PeakPaint.solid(PeakButton.DEFAULT_BACKGROUND), ((PeakButton) header.getChildren().get(0)).getStyle().getBackground());
    }

    @Test
    void aTabClickPatchesEveryContentAndEveryTab() {
        PeakPanel root = PeakTabs.create("tabs")
                .tab("One", PeakLabel.create("a", "A"))
                .tab("Two", PeakLabel.create("b", "B"))
                .build();
        PeakPanel header = (PeakPanel) root.getChildren().get(0);
        PeakButton second = (PeakButton) header.getChildren().get(1);

        List<PeakAction> actions = second.actionsFor(PeakEvent.CLICK);

        assertEquals(4, actions.size());
        assertEquals("tabs/content-0", actions.get(0).target());
        assertTrue(actions.get(0).patch().get("hidden").getAsBoolean());
        assertEquals("tabs/content-1", actions.get(2).target());
        assertFalse(actions.get(2).patch().get("hidden").getAsBoolean());
        assertEquals(PeakTabs.SELECTED_BACKGROUND, actions.get(3).patch().get("style.background").getAsInt());
    }

    @Test
    void aChoiceListSendsTheValueAndMovesTheHighlight() {
        PeakPanel root = PeakChoiceList.create("difficulty", "pick")
                .option("easy", "Easy").option("hard", "Hard").selected("easy").build();
        PeakButton hard = (PeakButton) root.getChildren().get(1);

        assertEquals(PeakPaint.solid(PeakChoiceList.SELECTED_BACKGROUND), ((PeakButton) root.getChildren().get(0)).getStyle().getBackground());
        assertEquals(PeakPaint.solid(PeakButton.DEFAULT_BACKGROUND), hard.getStyle().getBackground());

        List<PeakAction> actions = hard.actionsFor(PeakEvent.CLICK);
        assertEquals(3, actions.size());
        assertEquals(PeakButton.DEFAULT_BACKGROUND, actions.get(0).patch().get("style.background").getAsInt());
        assertEquals(PeakChoiceList.SELECTED_BACKGROUND, actions.get(1).patch().get("style.background").getAsInt());
        assertEquals(PeakActionType.SEND, actions.get(2).type());
        assertEquals("pick", actions.get(2).target());
        assertEquals("hard", actions.get(2).args().get("value"));
    }

    @Test
    void aConfirmDialogIsAMenuThatGoesBack() {
        PeakDocument dialog = PeakConfirmDialog.create("buy")
                .title("Buy").message("Buy the sword for 10 coins?").arg("item", "sword").build();

        assertTrue(dialog.isMenu());
        assertEquals(PeakMenuEscape.BACK, dialog.getMenu().getEscape());
        PeakButton confirm = (PeakButton) dialog.findById("buy/confirm");
        List<PeakAction> actions = confirm.actionsFor(PeakEvent.CLICK);
        assertEquals(2, actions.size());
        assertEquals(PeakActionType.SEND, actions.get(0).type());
        assertEquals("confirm", actions.get(0).target());
        assertEquals("sword", actions.get(0).args().get("item"));
        assertEquals(PeakActionType.BACK, actions.get(1).type());
        assertNotNull(dialog.findById("buy/message"));
    }

    @Test
    void tabsUseCustomColorsInTheStartStateAndInThePatches() {
        PeakPanel root = PeakTabs.create("tabs")
                .tab("One", PeakLabel.create("a", "A"))
                .tab("Two", PeakLabel.create("b", "B"))
                .selectedBackground(0xFF00AA00)
                .normalBackground(0xFF222222)
                .selectedColor(0xFFFF00)
                .normalColor(0xAAAAAA)
                .tabButton(button -> button.width(60))
                .build();
        PeakPanel header = (PeakPanel) root.getChildren().get(0);
        PeakButton first = (PeakButton) header.getChildren().get(0);
        PeakButton second = (PeakButton) header.getChildren().get(1);

        assertEquals(PeakPaint.solid(0xFF00AA00), first.getStyle().getBackground());
        assertEquals(PeakPaint.solid(0xFF222222), second.getStyle().getBackground());
        assertEquals(PeakPaint.solid(0xFFFF00), ((PeakLabel) first.getChildren().get(0)).getColor());
        assertEquals(PeakPaint.solid(0xAAAAAA), ((PeakLabel) second.getChildren().get(0)).getColor());
        assertEquals(60, first.getSize().getWidth().value());

        List<PeakAction> actions = second.actionsFor(PeakEvent.CLICK);
        assertEquals(6, actions.size());
        assertEquals(0xFF222222, actions.get(1).patch().get("style.background").getAsInt());
        assertEquals("tabs/tab-0/label", actions.get(2).target());
        assertEquals(0xFF00AA00, actions.get(4).patch().get("style.background").getAsInt());
        assertEquals("tabs/tab-1/label", actions.get(5).target());
    }

    @Test
    void aChoiceListUsesCustomColors() {
        PeakPanel root = PeakChoiceList.create("difficulty", "pick")
                .option("easy", "Easy").option("hard", "Hard").selected("hard")
                .selectedBackground(0xFFAA0000).normalBackground(0xFF111111)
                .optionButton(button -> button.padding(8)).build();
        PeakButton easy = (PeakButton) root.getChildren().get(0);
        PeakButton hard = (PeakButton) root.getChildren().get(1);

        assertEquals(PeakPaint.solid(0xFF111111), easy.getStyle().getBackground());
        assertEquals(PeakPaint.solid(0xFFAA0000), hard.getStyle().getBackground());
        assertEquals(0xFFAA0000, easy.actionsFor(PeakEvent.CLICK).get(0).patch().get("style.background").getAsInt());
    }

    @Test
    void aPagedListUsesTheNavigationHooksAndFormat() {
        PeakPanel root = PeakPagedList.create("list", 2)
                .add(PeakLabel.create("a", "A"), PeakLabel.create("b", "B"), PeakLabel.create("c", "C"))
                .labelFormat("%d of %d").buttonTexts("Back", "More")
                .navButton(button -> button.width(50)).build();
        PeakPanel first = (PeakPanel) root.getChildren().get(0);
        PeakPanel navigation = (PeakPanel) first.getChildren().get(first.getChildren().size() - 1);
        PeakButton previous = (PeakButton) navigation.getChildren().get(0);
        PeakLabel label = (PeakLabel) navigation.getChildren().get(1);

        assertEquals(50, previous.getSize().getWidth().value());
        assertEquals("1 of 2", label.getText());
    }

    @Test
    void aConfirmDialogRunsTheStyleHooks() {
        PeakDocument dialog = PeakConfirmDialog.create("buy")
                .panel(panel -> panel.background(0xFF101010))
                .confirmButton(button -> button.width(70))
                .cancelButton(button -> button.width(80)).build();

        assertEquals(PeakPaint.solid(0xFF101010), dialog.findById("buy/panel").getStyle().getBackground());
        assertEquals(70, dialog.findById("buy/confirm").getSize().getWidth().value());
        assertEquals(80, dialog.findById("buy/cancel").getSize().getWidth().value());
    }

    @Test
    void theValidatorWarnsAboutMenuFieldsThatHaveNoEffect() {
        PeakDocument menu = PeakMenu.create("m").hideVanilla(PeakVanillaElement.HOTBAR).documentTimeToLiveTicks(100);
        PeakDocument clean = PeakMenu.create("clean");

        assertEquals(2, PeakDocumentValidator.validate(menu, 0, 0).issues().size());
        assertTrue(PeakDocumentValidator.validate(clean, 0, 0).isValid());
    }

    @Test
    void aConfirmDialogCanStayOpenAndRunClientActions() {
        PeakDocument dialog = PeakConfirmDialog.create("buy").backAfterAnswer(false)
                .onCancelClient(PeakAction.close()).build();

        List<PeakAction> cancel = ((PeakButton) dialog.findById("buy/cancel")).actionsFor(PeakEvent.CLICK);
        assertEquals(2, cancel.size());
        assertEquals(PeakActionType.SEND, cancel.get(0).type());
        assertEquals(PeakActionType.CLOSE_MENU, cancel.get(1).type());
    }
}
