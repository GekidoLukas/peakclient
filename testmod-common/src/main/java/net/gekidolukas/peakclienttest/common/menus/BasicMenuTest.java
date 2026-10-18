package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestKit;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;

public final class BasicMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/basic";

    @Override public String name() { return "basic"; }
    @Override public String description() { return "Open, hover, click, disabled buttons, copy text, close and ESC"; }

    private static void open(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Basic")
                .shortcut("1", PeakAction.send("shortcut").arg("key", "1"))
                .shortcut("ctrl+K", PeakAction.send("shortcut").arg("key", "ctrl+K")));
        menu.add(frame("Basic menu",
                button("basic-send", "Send to server", PeakAction.send("hello").arg("from", "basic"))
                        .tooltip("Sends the action 'hello' with one argument")
                        .onHoverEnter(PeakAction.send("hover-enter"))
                        .onHoverLeave(PeakAction.send("hover-leave")),
                button("basic-slot", "Slot (click, right, double)", PeakAction.send("slot-click"))
                        .onRightClick(PeakAction.send("slot-right"))
                        .onDoubleClick(PeakAction.send("slot-double")),
                button("basic-copy", "Copy \"peakclient\"", PeakAction.copyText("peakclient")),
                button("basic-disabled", "Disabled button", PeakAction.send("hello")).enabled(false)
                        .tooltip("Disabled buttons do nothing"),
                button("basic-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("The menu opens above the HUD")
                        .setup(player -> {
                            PeakMenuTestKit.showHudLabel(player, "HUD label: it stays visible below the menu");
                            open(player);
                        })
                        .check(PeakMenuChecks.navigated("The menu opens", MENU, MENU))
                        .check(PeakMenuChecks.manual("The yellow HUD label is visible below the dark backdrop and the panel is centered")),

                PeakMenuTestStep.of("Hover and tooltip")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Hover 'Send to server': the button turns lighter and a tooltip appears; the disabled button shows its own tooltip and stays dark")),

                PeakMenuTestStep.of("Hover events send their own actions")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Move the mouse onto 'Send to server'", "hover-enter", "basic-send"))
                        .check(PeakMenuChecks.action("Move the mouse off the button again", "hover-leave", "basic-send")),

                PeakMenuTestStep.of("A button sends an action to the server")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Send to server'", "hello", event ->
                                event.elementId().equals("basic-send") && "basic".equals(event.arg("from")))),

                PeakMenuTestStep.of("A disabled button does nothing")
                        .setup(BasicMenuTest::open)
                        .forbid(event -> event instanceof PeakMenuActionEvent action && action.elementId().equals("basic-disabled"))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the disabled button first, then 'Send to server'", "hello", "basic-send")),

                PeakMenuTestStep.of("Copy text to the clipboard")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Click the copy button, paste with Ctrl+V into a text field or the chat and confirm that it reads 'peakclient'")),

                PeakMenuTestStep.of("A button closes the menu")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.navigatedBy("Click 'Close'", null, "action")),

                PeakMenuTestStep.of("Right click and double click send their own actions")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Slot' once", "slot-click"))
                        .check(PeakMenuChecks.action("Right-click 'Slot'", "slot-right"))
                        .check(PeakMenuChecks.action("Double-click 'Slot'", "slot-double")),

                PeakMenuTestStep.of("Menu shortcuts run actions")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Press the key 1", "shortcut", event -> "1".equals(event.arg("key"))))
                        .check(PeakMenuChecks.action("Press Ctrl+K", "shortcut", event -> "ctrl+K".equals(event.arg("key"))))
                        .check(PeakMenuChecks.manual("Holding a shortcut key does not send the action repeatedly (chat is not flooded)")),

                PeakMenuTestStep.of("The menu title is announced by the narrator")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("With the menu open, turn the narrator on (Ctrl+B): it announces 'Basic', the title set in the menu settings (skip if no narrator is available)")),

                PeakMenuTestStep.of("ESC closes the menu")
                        .setup(BasicMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.navigatedBy("Press ESC", null, "escape"))
        );
    }
}
