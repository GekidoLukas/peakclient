package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;

public final class ScrollMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/scroll";
    private static final String STYLED_MENU = "peaktest:menu/scroll-styled";
    private static final String KEYS_MENU = "peaktest:menu/scroll-keys";
    private static final String THEME_MENU = "peaktest:menu/scroll-theme";

    @Override public String name() { return "scroll"; }
    @Override public String description() { return "Scroll containers: wheel, draggable and styled scrollbars, nested lists, horizontal strips and clipped clicks"; }

    private static void open(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Scroll"));

        PeakPanel list = PeakPanel.create("scroll:list").column().gap(2).padding(4, 10, 4, 4)
                .width(180).height(110).overflow(PeakOverflow.SCROLL).background(0xFF101018);
        for (int i = 1; i <= 12; i++) {
            list.add(PeakButton.text("scroll:item-" + i, "Item " + i).width(155)
                    .onClick(PeakAction.send("pick").arg("n", String.valueOf(i))));
        }

        PeakPanel inner = PeakPanel.create("scroll:inner").column().gap(2).padding(2, 8, 2, 2)
                .width(150).height(44).overflow(PeakOverflow.SCROLL).background(0xFF202838);
        for (int i = 1; i <= 8; i++) {
            inner.add(PeakButton.text("scroll:inner-" + i, "Inner " + i).width(120)
                    .onClick(PeakAction.send("pick").arg("n", "inner-" + i)));
        }
        list.add(PeakLabel.create("scroll:inner-title", "Nested list: scrolls first"));
        list.add(inner);
        for (int i = 13; i <= 18; i++) {
            list.add(PeakButton.text("scroll:item-" + i, "Item " + i).width(155)
                    .onClick(PeakAction.send("pick").arg("n", String.valueOf(i))));
        }

        PeakPanel strip = PeakPanel.create("scroll:strip").row().gap(4).padding(4, 4, 8, 4)
                .width(180).height(34).overflow(PeakOverflow.SCROLL).background(0xFF101018);
        for (int i = 1; i <= 10; i++) {
            strip.add(PeakButton.text("scroll:tab-" + i, "Tab " + i)
                    .onClick(PeakAction.send("pick").arg("n", "tab-" + i)));
        }

        menu.add(frame("Scrolling lists",
                PeakLabel.create("scroll:header", "Mouse wheel over a list"),
                list,
                PeakLabel.create("scroll:strip-title", "Horizontal strip"),
                strip,
                button("scroll:close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openStyled(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(STYLED_MENU, settings -> settings.title("Scrollbar styles"));

        PeakPanel styled = PeakPanel.create("scroll:styled").column().gap(2).padding(4, 12, 4, 4)
                .width(180).height(70).overflow(PeakOverflow.SCROLL).background(0xFF101018);
        styled.scrollbar(bar -> bar.thickness(6).margin(3).radius(3)
                .trackColor(0x40FFFFFF).thumbColor(0x3C78C8)
                .thumbHoverColor(0x78B4FF).thumbDragColor(0xFFA040));
        for (int i = 1; i <= 10; i++) styled.add(PeakLabel.create("scroll:styled-" + i, "Styled row " + i));

        PeakPanel hidden = PeakPanel.create("scroll:hidden").column().gap(2).padding(4)
                .width(180).height(70).overflow(PeakOverflow.SCROLL).background(0xFF181010);
        hidden.hideScrollbar();
        for (int i = 1; i <= 10; i++) hidden.add(PeakLabel.create("scroll:hidden-" + i, "Hidden bar row " + i));

        PeakPanel horizontal = PeakPanel.create("scroll:wide").row().gap(4).padding(4, 4, 10, 4)
                .width(180).height(36).overflow(PeakOverflow.SCROLL).background(0xFF101818);
        horizontal.scrollbar(bar -> bar.radius(2).thickness(4).thumbColor(0x55FF55));
        for (int i = 1; i <= 10; i++) horizontal.add(PeakButton.text("scroll:wide-" + i, "Wide " + i));

        menu.add(frame("Scrollbar styles",
                styled,
                hidden,
                horizontal,
                button("scroll:styled-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openKeys(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(KEYS_MENU, settings -> settings.title("Scroll keys"));

        PeakPanel list = PeakPanel.create("scroll:keys-list").column().gap(2).padding(4, 10, 4, 4)
                .width(180).height(110).overflow(PeakOverflow.SCROLL).background(0xFF101018);
        for (int i = 1; i <= 8; i++) {
            list.add(PeakButton.text("scroll:keys-" + i, "Entry " + i).width(155)
                    .onClick(PeakAction.send("pick").arg("n", String.valueOf(i))));
        }
        list.add(PeakSlider.create("scroll:keys-slider", 0, 10, 5).step(1).width(155)
                .onChange(PeakAction.send("slider")));
        for (int i = 9; i <= 16; i++) {
            list.add(PeakButton.text("scroll:keys-" + i, "Entry " + i).width(155)
                    .onClick(PeakAction.send("pick").arg("n", String.valueOf(i))));
        }

        PeakPanel strip = PeakPanel.create("scroll:keys-strip").row().gap(4).padding(4, 4, 8, 4)
                .width(180).height(34).overflow(PeakOverflow.SCROLL).background(0xFF101018);
        for (int i = 1; i <= 10; i++) {
            strip.add(PeakButton.text("scroll:keys-tab-" + i, "Tab " + i)
                    .onClick(PeakAction.send("pick").arg("n", "tab-" + i)));
        }

        menu.add(frame("Scrolling with the keyboard",
                list,
                strip,
                button("scroll:keys-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openTheme(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(THEME_MENU, settings -> settings.title("Scrollbar theme")
                .scrollbar(bar -> bar.thickness(6).margin(3).radius(3).trackColor(0x40FFFFFF)
                        .thumbColor(0x55FF55).thumbHoverColor(0xAAFFAA)));

        PeakPanel inherit = PeakPanel.create("scroll:theme-inherit").column().gap(2).padding(4, 12, 4, 4)
                .width(180).height(60).overflow(PeakOverflow.SCROLL).background(0xFF101018);
        for (int i = 1; i <= 8; i++) inherit.add(PeakLabel.create("scroll:theme-inherit-" + i, "Inherits the theme " + i));

        PeakPanel own = PeakPanel.create("scroll:theme-own").column().gap(2).padding(4, 12, 4, 4)
                .width(180).height(60).overflow(PeakOverflow.SCROLL).background(0xFF181010);
        own.scrollbar(bar -> bar.thumbColor(0xFF8800));
        for (int i = 1; i <= 8; i++) own.add(PeakLabel.create("scroll:theme-own-" + i, "Own thumb color " + i));

        PeakPanel hidden = PeakPanel.create("scroll:theme-hidden").column().gap(2).padding(4)
                .width(180).height(60).overflow(PeakOverflow.SCROLL).background(0xFF101810);
        hidden.hideScrollbar();
        for (int i = 1; i <= 8; i++) hidden.add(PeakLabel.create("scroll:theme-hidden-" + i, "No scrollbar " + i));

        menu.add(frame("Scrollbar theme", inherit, own, hidden,
                button("scroll:theme-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("The list scrolls and shows a scrollbar")
                        .setup(ScrollMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Only the first items are visible, a thin scrollbar sits at the right edge of the list and moves while you scroll with the mouse wheel; items are cut off cleanly at the list borders")),

                PeakMenuTestStep.of("Scrolled items can be clicked")
                        .setup(ScrollMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Scroll the outer list to the end and click 'Item 18'", "pick",
                                event -> "18".equals(event.arg("n")))),

                PeakMenuTestStep.of("A nested list scrolls first")
                        .setup(ScrollMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Scroll to the nested list, scroll it to its end with the wheel and click 'Inner 8'", "pick",
                                event -> "inner-8".equals(event.arg("n"))))
                        .check(PeakMenuChecks.manual("While the pointer was over the nested list, the wheel moved it first and the outer list only after the nested one was at its end")),

                PeakMenuTestStep.of("A horizontal strip scrolls sideways")
                        .setup(ScrollMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Scroll the strip to the right and click 'Tab 10'", "pick",
                                event -> "tab-10".equals(event.arg("n")))),

                PeakMenuTestStep.of("The scrollbar can be dragged")
                        .setup(ScrollMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Press the scrollbar thumb of the outer list and drag it up and down: the list follows, it keeps following when the pointer leaves the list sideways, no button gets clicked on release"))
                        .check(PeakMenuChecks.manual("Click the scrollbar track above or below the thumb: the thumb jumps there and can be dragged on at once"))
                        .check(PeakMenuChecks.manual("Drag the scrollbar of the horizontal strip sideways: the tabs scroll"))
                        .check(PeakMenuChecks.manual("Drag the thumb of the nested list (scroll the outer list until it is visible): only the nested list moves")),

                PeakMenuTestStep.of("Tab scrolls the focused element into view")
                        .setup(ScrollMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Press Tab repeatedly: the white focus frame moves from button to button and the outer list scrolls along, so the focused item is always fully visible; Shift+Tab walks back and scrolls up again"))
                        .check(PeakMenuChecks.action("Tab until 'Item 18' is focused (the list scrolls to the end) and press Enter", "pick",
                                event -> "18".equals(event.arg("n"))))
                        .check(PeakMenuChecks.action("Press Shift+Tab until 'Inner 8' is focused: the nested list scrolls inside, the outer list follows; press Enter", "pick",
                                event -> "inner-8".equals(event.arg("n")))),

                PeakMenuTestStep.of("Clicking does not scroll")
                        .setup(ScrollMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Scroll the outer list so that 'Item 6' is only half visible at the bottom edge and click it: the list stays where it is and the click is not lost")),

                PeakMenuTestStep.of("Scrollbar styles: thickness, radius, colors, hover and drag color")
                        .setup(ScrollMenuTest::openStyled)
                        .check(PeakMenuChecks.navigated("The styled menu opens", STYLED_MENU))
                        .check(PeakMenuChecks.manual("The first list has a thick, rounded, blue scrollbar with a light track; the thumb turns light blue when the pointer is over it and orange while you drag it"))
                        .check(PeakMenuChecks.manual("The horizontal strip at the bottom has a green rounded bar and drags like the others")),

                PeakMenuTestStep.of("A hidden scrollbar still scrolls")
                        .setup(ScrollMenuTest::openStyled)
                        .check(PeakMenuChecks.navigated("The styled menu opens", STYLED_MENU))
                        .check(PeakMenuChecks.manual("The second list (reddish) shows no scrollbar but scrolls with the mouse wheel, and nothing happens when you click at its right edge")),

                PeakMenuTestStep.of("Arrow keys, Page Up/Down, Home and End scroll the list")
                        .setup(ScrollMenuTest::openKeys)
                        .check(PeakMenuChecks.navigated("The menu opens", KEYS_MENU))
                        .check(PeakMenuChecks.manual("Press Tab once (the first entry is focused), then Down and Up: the list moves line by line and the thumb follows; holding the key keeps scrolling"))
                        .check(PeakMenuChecks.manual("Press Page Down and Page Up: the list moves by almost a full box; Home jumps to the first entry, End to the last"))
                        .check(PeakMenuChecks.manual("Click an empty area of the menu frame so that nothing is focused and move the pointer over the list: Down still scrolls the list under the pointer"))
                        .check(PeakMenuChecks.action("Scroll to the end with End and click 'Entry 16'", "pick",
                                event -> "16".equals(event.arg("n")))),

                PeakMenuTestStep.of("Left and Right scroll the horizontal strip")
                        .setup(ScrollMenuTest::openKeys)
                        .check(PeakMenuChecks.navigated("The menu opens", KEYS_MENU))
                        .check(PeakMenuChecks.manual("Move the pointer over the strip and press Right repeatedly: the strip scrolls sideways; Left scrolls back; End jumps to the last tab"))
                        .check(PeakMenuChecks.action("Click 'Tab 10'", "pick",
                                event -> "tab-10".equals(event.arg("n")))),

                PeakMenuTestStep.of("A slider keeps its keys inside a scrolling list")
                        .setup(ScrollMenuTest::openKeys)
                        .check(PeakMenuChecks.navigated("The menu opens", KEYS_MENU))
                        .check(PeakMenuChecks.manual("Tab to the slider between 'Entry 8' and 'Entry 9': Left and Right change its value and the list does not move; Down and Up scroll the list"))
                        .check(PeakMenuChecks.action("Press Right on the focused slider", "slider")),

                PeakMenuTestStep.of("A menu theme styles all scrollbars")
                        .setup(ScrollMenuTest::openTheme)
                        .check(PeakMenuChecks.navigated("The theme menu opens", THEME_MENU))
                        .check(PeakMenuChecks.manual("The first list has a thick, rounded, green scrollbar on a light track and a pale green thumb on hover"))
                        .check(PeakMenuChecks.manual("The second list uses the same thickness and radius but an orange thumb: only the thumb color was overridden"))
                        .check(PeakMenuChecks.manual("The third list shows no scrollbar but scrolls with the mouse wheel"))
        );
    }
}
