package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakConfirmDialog;
import net.gekidolukas.peakclient.api.PeakRemotePages;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.template.PeakChoiceList;
import net.gekidolukas.peakclient.ui.template.PeakPagedList;
import net.gekidolukas.peakclient.ui.template.PeakTabs;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class TemplateMenuTest implements PeakMenuTest {
    private static final String BASE = "peaktest:menu/templates-base";
    private static final String DIALOG = "peaktest:menu/templates-dialog";
    private static final String LIST = "peaktest:menu/templates-list";
    private static final String TABS = "peaktest:menu/templates-tabs";
    private static final String CHOICE = "peaktest:menu/templates-choice";
    private static final String STYLED = "peaktest:menu/templates-styled";
    private static final String REMOTE = "peaktest:menu/templates-remote";
    private static final PeakPagedList REMOTE_LIST = PeakPagedList.remote("peaktest:tpl-remote", 4)
            .width(180).loadingText("Loading from the server...");
    private static final AtomicInteger REMOTE_LOADS = new AtomicInteger();
    private static PeakRemotePages remotePages;

    @Override public String name() { return "templates"; }
    @Override public String description() { return "Menu templates: confirm dialog, paged list, tabs, choice list and styling"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(BASE).on("ask", event -> PeakConfirmDialog.create(DIALOG)
                .title("Delete the world?")
                .message("This cannot be undone. All chunks, players and entities are removed for good.")
                .confirmText("Delete")
                .cancelText("Keep")
                .arg("target", "world")
                .show(PeakClient.to(event.player())));
        PeakClient.menu(DIALOG)
                .on(PeakConfirmDialog.CONFIRM_ACTION, event ->
                        say(event.player(), "confirmed, target=" + event.arg("target")))
                .on(PeakConfirmDialog.CANCEL_ACTION, event ->
                        say(event.player(), "cancelled, target=" + event.arg("target")));
        PeakClient.menu(LIST).on("pick", event -> say(event.player(), "picked item " + event.arg("item")));
        PeakClient.menu(TABS).on("tab-pick", event -> say(event.player(), "picked the button in the second tab"));
        PeakClient.menu(STYLED).on("styled-pick", event -> say(event.player(), "picked the button in the blue tab"));
        PeakClient.menu(CHOICE).on("difficulty", event -> say(event.player(), "difficulty=" + event.arg("value")));
        remotePages = PeakRemotePages.attach(REMOTE, REMOTE_LIST, (player, page) -> {
            int load = REMOTE_LOADS.incrementAndGet();
            say(player, "the server loads page " + (page + 1) + " (load #" + load + ")");
            List<PeakElement<?>> items = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                items.add(button("tpl-remote-" + page + "-" + i, "Page " + (page + 1) + " item " + i + " (load #" + load + ")",
                        PeakAction.send("remote-pick").arg("item", page + "-" + i)));
            }
            return items;
        });
        PeakClient.menu(REMOTE)
                .on("remote-refresh", event -> remotePages.refresh(event.player(), 1))
                .on("remote-reset", event -> remotePages.invalidateAll(event.player()));
    }

    private static void openBase(ServerPlayer player) {
        PeakDocument base = PeakMenu.create(BASE, settings -> settings.title("Templates"));
        base.add(frame("Base menu",
                button("tpl-ask", "Delete world (asks)", PeakAction.send("ask")),
                button("tpl-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(base);
    }

    private static void openList(ServerPlayer player) {
        PeakPagedList list = PeakPagedList.create("peaktest:tpl-list", 3).width(180);
        for (int i = 1; i <= 7; i++) {
            list.add(button("tpl-item-" + i, "Item " + i, PeakAction.send("pick").arg("item", String.valueOf(i))));
        }
        PeakDocument menu = PeakMenu.create(LIST, settings -> settings.title("Paged list"));
        menu.add(frame("Paged list, 7 items", list.build(),
                button("tpl-list-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openRemote(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(REMOTE, settings -> settings.title("Remote pages"));
        menu.add(frame("Remote pages, 4 pages", REMOTE_LIST.build(),
                button("tpl-remote-refresh", "Server refreshes page 2", PeakAction.send("remote-refresh")),
                button("tpl-remote-reset", "Server invalidates all", PeakAction.send("remote-reset")),
                button("tpl-remote-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().send(menu);
        remotePages.sendFirstPage(player);
        PeakClient.to(player).menus().open(REMOTE);
    }

    private static void openTabs(ServerPlayer player) {
        PeakTabs tabs = PeakTabs.create("peaktest:tpl-tabs")
                .tab("One", PeakLabel.create("tpl-tab-one", "Content of tab one").color(0xFFFFFF))
                .tab("Two", button("tpl-tab-button", "Button in tab two", PeakAction.send("tab-pick")))
                .tab("Three", PeakLabel.create("tpl-tab-three", "Content of tab three").color(0xFFFFFF))
                .selected(0);
        PeakDocument menu = PeakMenu.create(TABS, settings -> settings.title("Tabs"));
        menu.add(frame("Tabs", tabs.build(), button("tpl-tabs-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openChoice(ServerPlayer player) {
        PeakChoiceList choice = PeakChoiceList.create("peaktest:tpl-choice", "difficulty")
                .option("easy", "Easy")
                .option("normal", "Normal")
                .option("hard", "Hard")
                .selected("normal")
                .width(180);
        PeakDocument menu = PeakMenu.create(CHOICE, settings -> settings.title("Choice"));
        menu.add(frame("Difficulty", choice.build(), button("tpl-choice-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openStyled(ServerPlayer player) {
        PeakTabs tabs = PeakTabs.create("peaktest:tpl-styled")
                .tab("Red", PeakLabel.create("tpl-styled-red", "The red tab").color(0xFF8888))
                .tab("Green", PeakLabel.create("tpl-styled-green", "The green tab").color(0x88FF88))
                .tab("Blue", button("tpl-styled-pick", "Pick in blue tab", PeakAction.send("styled-pick")))
                .selectedBackground(0xFF2E9E4F)
                .normalBackground(0xFF2A2A40)
                .selectedColor(0xFFFFFF)
                .normalColor(0xAAAAAA)
                .headerGap(6)
                .tabButton(button -> button.padding(6, 14).borderRadius(6));
        PeakDocument menu = PeakMenu.create(STYLED, settings -> settings.title("Styled").noBackdrop());
        menu.background(0x70102040).border(3, 0xFFFFD700);
        menu.add(frame("Styled tabs on a styled menu", tabs.build(), button("tpl-styled-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("Confirm dialog: confirm goes back to the base menu")
                        .setup(TemplateMenuTest::openBase)
                        .check(PeakMenuChecks.navigated("The base menu opens", BASE, BASE))
                        .check(PeakMenuChecks.action("Click 'Delete world (asks)'", "ask"))
                        .check(PeakMenuChecks.navigated("The dialog opens over the base menu", DIALOG, BASE, DIALOG))
                        .check(PeakMenuChecks.action("Click 'Delete'", PeakConfirmDialog.CONFIRM_ACTION,
                                event -> "world".equals(event.arg("target"))))
                        .check(PeakMenuChecks.navigated("The dialog goes back to the base menu", BASE, BASE)),

                PeakMenuTestStep.of("Confirm dialog: cancel goes back to the base menu")
                        .setup(TemplateMenuTest::openBase)
                        .check(PeakMenuChecks.navigated("The base menu opens", BASE, BASE))
                        .check(PeakMenuChecks.action("Click 'Delete world (asks)'", "ask"))
                        .check(PeakMenuChecks.navigated("The dialog opens over the base menu", DIALOG, BASE, DIALOG))
                        .check(PeakMenuChecks.action("Click 'Keep'", PeakConfirmDialog.CANCEL_ACTION))
                        .check(PeakMenuChecks.navigated("The dialog goes back to the base menu", BASE, BASE)),

                PeakMenuTestStep.of("Confirm dialog: ESC goes back without an answer")
                        .setup(TemplateMenuTest::openBase)
                        .check(PeakMenuChecks.navigated("The base menu opens", BASE, BASE))
                        .check(PeakMenuChecks.action("Click 'Delete world (asks)'", "ask"))
                        .check(PeakMenuChecks.navigated("The dialog opens over the base menu", DIALOG, BASE, DIALOG))
                        .check(PeakMenuChecks.navigatedBy("Press ESC: the dialog goes back", BASE, "escape")),

                PeakMenuTestStep.of("Paged list: paging and picking an item on the last page")
                        .setup(TemplateMenuTest::openList)
                        .check(PeakMenuChecks.navigated("The list opens", LIST, LIST))
                        .check(PeakMenuChecks.manual("Items 1 to 3 show, the page reads 'Page 1 / 3' and '<' is dimmed"))
                        .check(PeakMenuChecks.manual("Click '>' twice: only Item 7 shows, 'Page 3 / 3', '>' is dimmed"))
                        .check(PeakMenuChecks.manual("Click '<' once: Items 4 to 6 show"))
                        .check(PeakMenuChecks.manual("Click '>' once more so that Item 7 shows"))
                        .check(PeakMenuChecks.action("Click 'Item 7'", "pick", event -> "7".equals(event.arg("item")))),

                PeakMenuTestStep.of("Tabs: switching shows one content and highlights the tab")
                        .setup(TemplateMenuTest::openTabs)
                        .check(PeakMenuChecks.navigated("The tabs open", TABS, TABS))
                        .check(PeakMenuChecks.manual("Tab 'One' is blue and shows 'Content of tab one'"))
                        .check(PeakMenuChecks.manual("Click 'Three': the blue highlight moves and the content changes"))
                        .check(PeakMenuChecks.manual("Click 'Two': only the button in tab two shows"))
                        .check(PeakMenuChecks.action("Click 'Button in tab two'", "tab-pick")),

                PeakMenuTestStep.of("Choice list: the selection moves and the value is sent")
                        .setup(TemplateMenuTest::openChoice)
                        .check(PeakMenuChecks.navigated("The choice list opens", CHOICE, CHOICE))
                        .check(PeakMenuChecks.manual("'Normal' is blue, 'Easy' and 'Hard' are grey"))
                        .check(PeakMenuChecks.action("Click 'Hard'", "difficulty", event -> "hard".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("'Hard' is blue now, 'Normal' is grey")),

                PeakMenuTestStep.of("Styled tabs keep their custom colors on a menu with its own background")
                        .setup(TemplateMenuTest::openStyled)
                        .check(PeakMenuChecks.navigated("The styled menu opens", STYLED, STYLED))
                        .check(PeakMenuChecks.manual("The whole window has a blue tint and a gold border, no dark backdrop"))
                        .check(PeakMenuChecks.manual("The tabs are rounded, 'Red' is green with white text, the others are dark with grey text"))
                        .check(PeakMenuChecks.manual("Click 'Green', then 'Blue', then 'Red': the green highlight and text colors move with the selection"))
                        .check(PeakMenuChecks.action("Click 'Blue', then 'Pick in blue tab'", "styled-pick")),

                PeakMenuTestStep.of("Remote pages: loaded per page, cached on the client, refreshed by the server")
                        .setup(TemplateMenuTest::openRemote)
                        .check(PeakMenuChecks.navigated("The remote list opens", REMOTE, REMOTE))
                        .check(PeakMenuChecks.manual("Page 1 shows three buttons with 'load #1' (the chat says the server loaded page 1) and '<' is dimmed"))
                        .check(PeakMenuChecks.action("Click '>'", PeakPagedList.loadActionId("peaktest:tpl-remote"),
                                event -> "1".equals(event.arg("page"))))
                        .check(PeakMenuChecks.manual("Page 2 shows three buttons with the next load number and the chat says the server loaded page 2"))
                        .check(PeakMenuChecks.action("Click '<'", PeakPagedList.loadActionId("peaktest:tpl-remote"),
                                event -> "0".equals(event.arg("page"))))
                        .check(PeakMenuChecks.action("Click '>' again", PeakPagedList.loadActionId("peaktest:tpl-remote"),
                                event -> "1".equals(event.arg("page"))))
                        .check(PeakMenuChecks.manual("Page 2 shows the same load number as before and the chat shows no second load of page 2 (it comes from the client cache)"))
                        .check(PeakMenuChecks.action("Click 'Server refreshes page 2'", "remote-refresh"))
                        .check(PeakMenuChecks.manual("Page 2 shows a higher load number now, page 1 keeps its number"))
                        .check(PeakMenuChecks.action("Click 'Server invalidates all'", "remote-reset"))
                        .check(PeakMenuChecks.manual("The page you are on shows 'Loading from the server...'; go to another page and back and it loads again with new numbers"))
        );
    }
}
