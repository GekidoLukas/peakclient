package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.template.PeakRichTextToolbar;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class ColorPickerMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/colorpicker";
    private static final String RICH_MENU = "peaktest:menu/colorpicker-rich";
    private static final String ALPHA_MENU = "peaktest:menu/colorpicker-alpha";
    private static final String TOOLBAR_MENU = "peaktest:menu/colorpicker-toolbar";

    @Override public String name() { return "colorpicker"; }
    @Override public String description() { return "Color picker: field, hue bar, alpha, hex field, swatches, keyboard and rich text link"; }

    @Override
    public void registerHandlers() {
        for (String menu : List.of(MENU, RICH_MENU, ALPHA_MENU)) {
            PeakClient.menu(menu).on("picked", event -> say(event.player(), "picked " + event.arg("value")));
        }
    }

    private static PeakPanel swatches(String pickerId) {
        PeakPanel row = PeakPanel.create("colorpicker:swatches").row().gap(3).padding(0).background(0);
        row.add(
                swatch("red", "Red", pickerId, 0xFF5555),
                swatch("green", "Green", pickerId, 0x55FF55),
                swatch("blue", "Blue", pickerId, 0x5555FF),
                swatch("gold", "Gold", pickerId, 0xFFAA00));
        return row;
    }

    private static PeakButton swatch(String key, String text, String pickerId, int rgb) {
        return PeakButton.text("colorpicker:swatch-" + key, text).padding(2, 4)
                .onClick(PeakAction.setColor(pickerId, rgb));
    }

    private static void open(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Color picker"));
        PeakColorPicker picker = PeakColorPicker.create("colorpicker:picker", 0xFF8800)
                .hexField("colorpicker:hex")
                .onChange(PeakAction.send("picked"));
        menu.add(frame("Color picker",
                picker,
                PeakTextBox.create("colorpicker:hex", "#FF8800").maxLength(9).width(150),
                swatches("colorpicker:picker"),
                button("colorpicker:save", "Send the value", PeakAction.send("colorpicker-save").collectAll()),
                button("colorpicker:close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openRich(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(RICH_MENU, settings -> settings.title("Color picker and rich text"));
        PeakRichTextBox notes = PeakRichTextBox.create("colorpicker:notes", "Select some of this text and pick a color")
                .size(180, 56);
        PeakColorPicker picker = PeakColorPicker.create("colorpicker:rich-picker", 0xFF5555)
                .target("colorpicker:notes")
                .size(180, 100)
                .onChange(PeakAction.send("picked"));
        menu.add(frame("Rich text colors",
                PeakRichTextToolbar.create("colorpicker:toolbar", "colorpicker:notes").build(),
                notes,
                picker,
                button("colorpicker:rich-print", "Send the text",
                        PeakAction.send("colorpicker-text").collect("colorpicker:notes")),
                button("colorpicker:rich-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openToolbar(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(TOOLBAR_MENU, settings -> settings.title("Toolbar with picker"));
        PeakRichTextBox notes = PeakRichTextBox.create("colorpicker:toolbar-notes",
                        "§cRed§r, §aGreen§r and plain text")
                .size(180, 48);
        menu.add(frame("Toolbar with picker",
                PeakRichTextToolbar.create("colorpicker:tb", "colorpicker:toolbar-notes").colorPicker().build(),
                notes,
                button("colorpicker:toolbar-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openAlpha(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(ALPHA_MENU, settings -> settings.title("Color picker with alpha"));
        PeakColorPicker picker = PeakColorPicker.create("colorpicker:alpha-picker", 0x8055AAFF)
                .alpha(true)
                .size(180, 110)
                .onChange(PeakAction.send("picked"));
        menu.add(frame("Alpha",
                PeakLabel.create("colorpicker:alpha-label", "The right bar sets the transparency").color(0xAAAAAA),
                picker,
                button("colorpicker:alpha-close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("Dragging the field and the hue bar changes the color")
                        .setup(ColorPickerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The square shows white to orange left to right and bright to dark top to bottom, the bar on the right shows all hues, the small box shows orange with '#FF8800'"))
                        .check(PeakMenuChecks.action("Press in the square, drag around and release", "picked"))
                        .check(PeakMenuChecks.manual("While dragging the marker followed the pointer (also outside the square), the preview and the hex text changed with it and the hex field below shows the same value"))
                        .check(PeakMenuChecks.action("Drag the hue bar and release", "picked"))
                        .check(PeakMenuChecks.manual("The square changed its hue, the marker kept its position, and a grey stays at the same hue when you drag the marker to the left edge and then move the hue bar")),

                PeakMenuTestStep.of("The hex field drives the picker")
                        .setup(ColorPickerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the hex field, select everything and type 00FF00", "picked",
                                event -> "#00FF00".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The picker turned green while you typed the last digit and the hex field text was left alone"))
                        .check(PeakMenuChecks.manual("Typing an invalid value (for example 12) does not change the picker")),

                PeakMenuTestStep.of("Swatches set the color through an action")
                        .setup(ColorPickerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Red'", "picked", event -> "#FF5555".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The picker, its hex text and the hex field show #FF5555"))
                        .check(PeakMenuChecks.action("Click 'Blue'", "picked", event -> "#5555FF".equals(event.arg("value")))),

                PeakMenuTestStep.of("The server receives the value")
                        .setup(ColorPickerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Gold', then 'Send the value'", "colorpicker-save",
                                event -> "#FFAA00".equals(event.arg("colorpicker:picker")))),

                PeakMenuTestStep.of("Keyboard control")
                        .setup(ColorPickerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Press Tab until the picker has the focus frame, then use Left/Right (saturation), Up/Down (brightness) and Page Up/Page Down (hue); Shift makes the steps ten times larger"))
                        .check(PeakMenuChecks.manual("Each key press changes the marker, the preview and the hex field; the menu does not scroll or move the focus")),

                PeakMenuTestStep.of("A picker colors the selection of a rich text box")
                        .setup(ColorPickerMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The menu opens", RICH_MENU))
                        .check(PeakMenuChecks.manual("Select a word in the text box and drag in the picker: the word changes color live and stays selected (the box keeps its focus)"))
                        .check(PeakMenuChecks.manual("Click Bold in the toolbar, then pick another color without a selection and type: the new text has the color and is bold"))
                        .check(PeakMenuChecks.action("Click 'Send the text'", "colorpicker-text",
                                event -> event.arg("colorpicker:notes") != null)),

                PeakMenuTestStep.of("The toolbar shows the picker on demand and follows the caret color")
                        .setup(ColorPickerMenuTest::openToolbar)
                        .check(PeakMenuChecks.navigated("The menu opens", TOOLBAR_MENU))
                        .check(PeakMenuChecks.manual("The toolbar ends with a 'C' button and no picker is visible"))
                        .check(PeakMenuChecks.manual("Click 'C': the picker appears below the buttons, click it again: it disappears; the text box keeps its focus"))
                        .check(PeakMenuChecks.manual("With the picker open, click into the red word: the picker and its hex text show #FF5555; click into the green word: #55FF55; click into plain text: the picker keeps the last color"))
                        .check(PeakMenuChecks.manual("Select the plain text and drag in the picker: the selection takes the color live")),

                PeakMenuTestStep.of("Alpha bar")
                        .setup(ColorPickerMenuTest::openAlpha)
                        .check(PeakMenuChecks.navigated("The alpha menu opens", ALPHA_MENU))
                        .check(PeakMenuChecks.manual("A second bar with a checkerboard sits right of the hue bar, the preview shows a half transparent blue over a checkerboard and the hex text has eight digits"))
                        .check(PeakMenuChecks.action("Drag the alpha bar and release", "picked",
                                event -> event.arg("value") != null && event.arg("value").length() == 9))
        );
    }
}
