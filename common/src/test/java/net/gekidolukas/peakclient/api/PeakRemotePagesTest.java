package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.RecordingPeakApi;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.template.PeakPagedList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PeakRemotePagesTest {
    private static final String MENU = "shop:menu";
    private static final String LIST = "shop:list";

    private PeakClientApi previous;
    private RecordingPeakApi api;
    private final UUID player = UUID.randomUUID();
    private PeakRemotePages pages;
    private final AtomicInteger loads = new AtomicInteger();

    @BeforeEach
    void install() {
        previous = PeakClientApiHolder.INSTANCE;
        api = new RecordingPeakApi();
        PeakClientApiHolder.INSTANCE = api;
        PeakPagedList list = PeakPagedList.remote(LIST, 3).width(100);
        pages = PeakRemotePages.attach(MENU, list, (p, page) -> List.of());
    }

    @AfterEach
    void restore() {
        pages.detach();
        PeakClientApiHolder.INSTANCE = previous;
    }

    private List<PeakElement<?>> items(int page, int count) {
        loads.incrementAndGet();
        List<PeakElement<?>> items = new ArrayList<>();
        for (int i = 0; i < count; i++) items.add(PeakLabel.create("shop:item/" + page + "-" + i, "p" + page + " i" + i));
        return items;
    }

    private static PeakDocument menuOf(PeakPagedList list) {
        PeakDocument menu = PeakMenu.create(MENU);
        menu.add(list.build());
        return menu;
    }

    @Test
    void aRemoteListBuildsEmptyPagesWithLoadingLabels() {
        PeakDocument menu = menuOf(PeakPagedList.remote(LIST, 3).loadingText("Please wait"));

        PeakPanel root = (PeakPanel) menu.findById(LIST);
        assertEquals(3, root.getChildren().size());
        for (int page = 0; page < 3; page++) {
            assertNotNull(menu.findById(PeakPagedList.pageId(LIST, page)));
            assertNotNull(menu.findById(PeakPagedList.contentId(LIST, page)));
            PeakLabel loading = (PeakLabel) menu.findById(PeakPagedList.loadingId(LIST, page));
            assertEquals("Please wait", loading.getText().getString());
        }
        assertFalse(menu.findById(PeakPagedList.pageId(LIST, 0)).isHidden());
        assertTrue(menu.findById(PeakPagedList.pageId(LIST, 1)).isHidden());
    }

    @Test
    void theNavigationAsksTheServerForTheTargetPage() {
        PeakDocument menu = menuOf(PeakPagedList.remote(LIST, 3));

        PeakButton next = (PeakButton) menu.findById(LIST + "/next-0");
        List<PeakAction> actions = next.actionsFor(PeakEvent.CLICK);

        assertEquals(3, actions.size());
        assertEquals(PeakActionType.PATCH, actions.get(0).type());
        assertEquals(PeakActionType.PATCH, actions.get(1).type());
        assertEquals(PeakActionType.SEND, actions.get(2).type());
        assertEquals(PeakPagedList.loadActionId(LIST), actions.get(2).target());
        assertEquals("1", actions.get(2).args().get(PeakPagedList.LOAD_PAGE_ARG));

        PeakButton previousButton = (PeakButton) menu.findById(LIST + "/prev-2");
        assertEquals("1", previousButton.actionsFor(PeakEvent.CLICK).get(2).args().get("page"));
    }

    @Test
    void aLocalListKeepsItsPlainNavigation() {
        PeakDocument menu = menuOf(PeakPagedList.create(LIST, 1).add(PeakLabel.create("a", "A"), PeakLabel.create("b", "B")));

        PeakButton next = (PeakButton) menu.findById(LIST + "/next-0");

        assertEquals(2, next.actionsFor(PeakEvent.CLICK).size());
    }

    @Test
    void localAndRemoteModesRejectTheOtherModesCalls() {
        assertThrows(IllegalStateException.class, () -> PeakPagedList.remote(LIST, 2).add(PeakLabel.create("a", "A")));
        assertThrows(IllegalStateException.class, () -> PeakPagedList.create(LIST, 2).pageCount(4));
        assertThrows(IllegalArgumentException.class,
                () -> PeakRemotePages.attach(MENU, PeakPagedList.create(LIST, 2), (p, page) -> List.of()));
    }

    @Test
    void theRemotePageCountIsLive() {
        PeakPagedList list = PeakPagedList.remote(LIST, 2);

        assertEquals(2, list.pageCount());
        list.pageCount(5);

        assertEquals(5, list.pageCount());
        assertEquals(5, ((PeakPanel) menuOf(list).findById(LIST)).getChildren().size());
    }

    @Test
    void theFirstLoadReplacesTheLoadingLabelWithTheItems() {
        List<PeakUiOperation> operations = pages.loadOperations(player, 1, false, () -> items(1, 2));

        assertEquals(3, operations.size());
        assertEquals(new PeakUiOperation.Delete(PeakPagedList.loadingId(LIST, 1)), operations.get(0));
        PeakUiOperation.Add first = assertInstanceOf(PeakUiOperation.Add.class, operations.get(1));
        assertEquals(PeakPagedList.contentId(LIST, 1), first.parentId());
        assertEquals("shop:item/1-0", first.element().getId());
        assertTrue(pages.isLoaded(player, 1));
        assertFalse(pages.isLoaded(player, 0));
    }

    @Test
    void aLoadedPageIsNotLoadedAgainForTheSamePlayer() {
        pages.loadOperations(player, 0, false, () -> items(0, 1));

        List<PeakUiOperation> again = pages.loadOperations(player, 0, false, () -> items(0, 1));
        List<PeakUiOperation> other = pages.loadOperations(UUID.randomUUID(), 0, false, () -> items(0, 1));

        assertTrue(again.isEmpty());
        assertFalse(other.isEmpty());
        assertEquals(2, loads.get());
    }

    @Test
    void aFailingLoaderLeavesThePageUnloaded() {
        assertThrows(IllegalStateException.class, () -> pages.loadOperations(player, 0, false, () -> {
            throw new IllegalStateException("database down");
        }));

        assertFalse(pages.isLoaded(player, 0));
        assertFalse(pages.loadOperations(player, 0, false, () -> items(0, 1)).isEmpty());
    }

    @Test
    void refreshingReplacesThePreviousItems() {
        pages.loadOperations(player, 0, false, () -> items(0, 2));

        List<PeakUiOperation> operations = pages.loadOperations(player, 0, true, () -> items(0, 1));

        assertEquals(new PeakUiOperation.Delete("shop:item/0-0"), operations.get(0));
        assertEquals(new PeakUiOperation.Delete("shop:item/0-1"), operations.get(1));
        assertInstanceOf(PeakUiOperation.Add.class, operations.get(2));
        assertEquals(3, operations.size());
    }

    @Test
    void invalidatingPutsTheLoadingLabelBack() {
        pages.loadOperations(player, 2, false, () -> items(2, 2));

        List<PeakUiOperation> operations = pages.invalidateOperations(player, 2);

        assertEquals(3, operations.size());
        assertEquals(new PeakUiOperation.Delete("shop:item/2-0"), operations.get(0));
        PeakUiOperation.Add label = assertInstanceOf(PeakUiOperation.Add.class, operations.get(2));
        assertEquals(PeakPagedList.contentId(LIST, 2), label.parentId());
        assertEquals(PeakPagedList.loadingId(LIST, 2), label.element().getId());
        assertFalse(pages.isLoaded(player, 2));
        assertTrue(pages.invalidateOperations(player, 2).isEmpty());
        assertFalse(pages.loadOperations(player, 2, false, () -> items(2, 1)).isEmpty());
    }

    @Test
    void invalidatingEverythingResetsEveryLoadedPageInOrder() {
        pages.loadOperations(player, 2, false, () -> items(2, 1));
        pages.loadOperations(player, 0, false, () -> items(0, 1));

        List<PeakUiOperation> operations = pages.invalidateAllOperations(player);

        assertEquals(4, operations.size());
        assertEquals(new PeakUiOperation.Delete("shop:item/0-0"), operations.get(0));
        PeakUiOperation.Add firstLabel = assertInstanceOf(PeakUiOperation.Add.class, operations.get(1));
        assertEquals(PeakPagedList.loadingId(LIST, 0), firstLabel.element().getId());
        assertEquals(new PeakUiOperation.Delete("shop:item/2-0"), operations.get(2));
        assertFalse(pages.isLoaded(player, 0));
        assertFalse(pages.isLoaded(player, 2));
        assertTrue(pages.invalidateAllOperations(player).isEmpty());
    }

    @Test
    void pageNumbersAreClampedToTheList() {
        assertEquals(0, pages.clamp(-4));
        assertEquals(2, pages.clamp(2));
        assertEquals(2, pages.clamp(99));
    }

    @Test
    void sendingTheMenuAgainForgetsWhatWasLoaded() {
        pages.loadOperations(player, 0, false, () -> items(0, 1));
        UUID other = UUID.randomUUID();
        pages.loadOperations(other, 0, false, () -> items(0, 1));

        PeakClient.to(player).menus().send(PeakMenu.create(MENU));

        assertFalse(pages.isLoaded(player, 0));
        assertTrue(pages.isLoaded(other, 0));
    }

    @Test
    void sendingAnotherMenuKeepsTheState() {
        pages.loadOperations(player, 0, false, () -> items(0, 1));

        PeakClient.to(player).menus().send(PeakMenu.create("shop:other"));

        assertTrue(pages.isLoaded(player, 0));
    }

    @Test
    void forgettingAPlayerDropsTheirState() {
        pages.loadOperations(player, 1, false, () -> items(1, 1));

        PeakRemotePages.forgetPlayer(player);

        assertFalse(pages.isLoaded(player, 1));
    }

    @Test
    void attachingTheSameListAgainReplacesTheEarlierController() {
        pages.loadOperations(player, 0, false, () -> items(0, 1));

        PeakRemotePages second = PeakRemotePages.attach(MENU, PeakPagedList.remote(LIST, 3), (p, page) -> List.of());

        assertFalse(pages.isLoaded(player, 0));
        second.detach();
    }
}
