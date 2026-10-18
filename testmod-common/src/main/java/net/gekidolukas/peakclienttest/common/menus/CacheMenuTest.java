package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakMenuNavigationEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class CacheMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/cache";
    private static final String LABEL = "peaktest:menu/cache/label";

    @Override public String name() { return "cache"; }
    @Override public String description() { return "Menus are cached: edit while closed, edit while open, send again, remove"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(MENU).on("edit", event -> {
            say(event.player(), "the server edits the label of the open menu");
            PeakClient.to(event.player()).menus().update(MENU, LABEL, PeakPatch.label().text("Edited by the server while open"));
        });
    }

    private static PeakDocument menu(String labelText) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Cache"));
        menu.add(frame("Cached menu",
                PeakLabel.create(LABEL, labelText),
                button("cache-edit", "Let the server edit this label", PeakAction.send("edit")),
                button("cache-close", "Close", PeakAction.close())));
        return menu;
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("A closed menu can be edited and opens with the new content")
                        .setup(player -> {
                            PeakClient.to(player).menus().send(menu("Version 1 (as sent)"));
                            say(player, "the menu is cached but closed, it is edited in 2 seconds and opens in 4 seconds");
                            PeakTestTimeline.create(player)
                                    .atSeconds(2, () -> PeakClient.to(player).menus()
                                            .update(MENU, LABEL, PeakPatch.label().text("Version 2 (edited while closed)")))
                                    .atSeconds(4, () -> PeakClient.to(player).menus().open(MENU))
                                    .start();
                        })
                        .timeoutSeconds(60)
                        .check(PeakMenuChecks.navigated("The menu opens by itself after 4 seconds", MENU, MENU))
                        .check(PeakMenuChecks.manual("The label reads 'Version 2 (edited while closed)'")),

                PeakMenuTestStep.of("The server edits an open menu")
                        .setup(player -> PeakClient.to(player).menus().sendAndOpen(menu("Version 1 (as sent)")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Let the server edit this label'", "edit"))
                        .check(PeakMenuChecks.manual("The label changed to 'Edited by the server while open' without closing the menu")),

                PeakMenuTestStep.of("Sending a menu again replaces the cached one")
                        .setup(player -> {
                            PeakClient.to(player).menus().send(menu("Version 1 (as sent)"));
                            PeakClient.to(player).menus().send(menu("Version 3 (sent again)"));
                            PeakClient.to(player).menus().open(MENU);
                        })
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The label reads 'Version 3 (sent again)'")),

                PeakMenuTestStep.of("A removed menu cannot be opened")
                        .setup(player -> {
                            PeakClient.to(player).menus().send(menu("Version 1 (as sent)"));
                            PeakClient.to(player).menus().remove(MENU);
                            PeakClient.to(player).menus().open(MENU);
                            say(player, "the server logs one warning that the menu was never sent");
                        })
                        .forbid(event -> event instanceof PeakMenuNavigationEvent navigation
                                && navigation.menuId() != null)
                        .check(PeakMenuChecks.waitTicks("Nothing opens for 3 seconds", 60))
        );
    }
}
