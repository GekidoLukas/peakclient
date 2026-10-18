package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTextFilter;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.template.PeakRichTextToolbar;
import net.gekidolukas.peakclienttest.common.PeakMenuCheck;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class InputMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/input";
    private static final String RICH_MENU = "peaktest:menu/input-rich";
    private static final String RICH_FILLED = "§aServer §lwrote§r this\nsecond line";

    @Override public String name() { return "input"; }
    @Override public String description() { return "Text field, checkbox, slider, collected values, server patches and keyboard focus"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(MENU).on("fill", event -> {
            say(event.player(), "the server fills the inputs");
            PeakClient.to(event.player()).menus()
                    .update(MENU, "input:name", PeakPatch.textBox().value("Filled by the server"))
                    .update(MENU, "input:amount", PeakPatch.textBox().value("42"))
                    .update(MENU, "input:agree", PeakPatch.checkbox().checked(true))
                    .update(MENU, "input:volume", PeakPatch.slider().value(9));
        });
        PeakClient.menu(RICH_MENU).on("rich-fill", event -> {
            say(event.player(), "the server rewrites the rich text box");
            PeakClient.to(event.player()).menus()
                    .update(RICH_MENU, "rich:notes", PeakPatch.richTextBox().value(RICH_FILLED));
        });
    }

    private static void open(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Input"));
        PeakTextBox name = PeakTextBox.create("input:name").placeholder("Your name")
                .onSubmit(PeakAction.send("submit"))
                .onChange(PeakAction.send("typing"));
        PeakTextBox amount = PeakTextBox.create("input:amount", "10").filter(PeakTextFilter.INTEGER).maxLength(6)
                .onSubmit(PeakAction.send("submit"));
        PeakCheckbox agree = PeakCheckbox.create("input:agree", "I agree to everything")
                .onChange(PeakAction.send("toggle"));
        PeakSlider volume = PeakSlider.create("input:volume", 0, 10, 5).step(1)
                .onChange(PeakAction.send("volume"));
        PeakCheckbox locked = PeakCheckbox.create("input:locked", "Disabled checkbox").enabled(false);

        menu.add(frame("Input elements",
                PeakLabel.create("input:l1", "Name (Enter submits)").color(0xAAAAAA), name,
                PeakLabel.create("input:l2", "Amount (digits only)").color(0xAAAAAA), amount,
                agree, locked,
                PeakLabel.create("input:l3", "Volume (arrows, Home/End)").color(0xAAAAAA), volume,
                button("input:save", "Send all values", PeakAction.send("save").collectAll()),
                button("input:fill", "Server fills values", PeakAction.send("fill")),
                button("input:close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static void openRich(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(RICH_MENU, settings -> settings.title("Rich text"));
        PeakRichTextBox notes = PeakRichTextBox.create("rich:notes", "§lWelcome§r to the §crich§r text box")
                .placeholder("Write something")
                .size(180, 64)
                .onSubmit(PeakAction.send("rich-submit"));
        PeakRichTextBox limited = PeakRichTextBox.create("rich:limited").maxLines(2).wrap(false).maxLength(80)
                .placeholder("Two lines at most, no wrapping")
                .size(180, 34);
        menu.add(frame("Rich text boxes",
                PeakLabel.create("rich:l1", "Notes (Ctrl+Enter submits)").color(0xAAAAAA),
                PeakRichTextToolbar.create("rich:toolbar", "rich:notes").build(),
                notes,
                PeakLabel.create("rich:l2", "Limited: 2 lines, no wrap").color(0xAAAAAA), limited,
                button("rich:save", "Send all values", PeakAction.send("rich-save").collectAll()),
                button("rich:fill", "Server rewrites notes", PeakAction.send("rich-fill")),
                button("rich:close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private static boolean from(PeakMenuActionEvent event, String elementId) {
        return event.elementId().equals(elementId);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        PeakMenuCheck digitsOnly = new PeakMenuCheck("Type 'abc123' into the amount field and press Enter", false, (context, event) -> {
            if (!(event instanceof PeakMenuActionEvent action) || !action.actionId().equals("submit")
                    || !from(action, "input:amount")) {
                return PeakMenuCheck.Outcome.NO;
            }
            String value = action.arg("value", "");
            return value.matches("\\d+") && value.endsWith("123") ? PeakMenuCheck.Outcome.YES : PeakMenuCheck.Outcome.FAIL;
        });

        return List.of(
                PeakMenuTestStep.of("A text field submits its value")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The name field shows the placeholder 'Your name' in gray"))
                        .check(PeakMenuChecks.action("Click the name field, type a name and press Enter", "submit",
                                event -> from(event, "input:name") && !event.arg("value", "").isEmpty())),

                PeakMenuTestStep.of("A text field reports changes while typing (throttled)")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the name field and type 'abc' quickly", "typing",
                                event -> from(event, "input:name") && event.arg("value", "").contains("abc")))
                        .check(PeakMenuChecks.manual("Typing a long word fast sent a few 'typing' messages (at most one every quarter second), not one per letter")),

                PeakMenuTestStep.of("The integer filter drops everything but digits")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(digitsOnly),

                PeakMenuTestStep.of("Selection, clipboard and editing keys")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("In the name field: type text, Shift+Left selects, Ctrl+A selects all, Ctrl+C / Ctrl+V copy and paste, Ctrl+Backspace deletes a word, Home / End move the caret, the caret blinks and long text scrolls inside the field")),

                PeakMenuTestStep.of("A checkbox toggles and reports its state")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the checkbox to check it", "toggle",
                                event -> "true".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Click it again to uncheck it", "toggle",
                                event -> "false".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The disabled checkbox below cannot be toggled and looks dimmed")),

                PeakMenuTestStep.of("A slider reports its value")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Drag the slider to the far right and release", "volume",
                                event -> "10".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Press Home with the slider focused", "volume",
                                event -> "0".equals(event.arg("value")))),

                PeakMenuTestStep.of("A button collects every input")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Type a name, then click 'Send all values'", "save", event ->
                                event.args().containsKey("input:name") && event.args().containsKey("input:amount")
                                        && event.args().containsKey("input:agree") && event.args().containsKey("input:volume")
                                        && !event.arg("input:name", "").isEmpty())),

                PeakMenuTestStep.of("Server patches replace the typed values")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Server fills values'", "fill"))
                        .check(PeakMenuChecks.action("Click 'Send all values': the client holds the server's values", "save", event ->
                                "Filled by the server".equals(event.arg("input:name"))
                                        && "42".equals(event.arg("input:amount"))
                                        && "true".equals(event.arg("input:agree"))
                                        && "9".equals(event.arg("input:volume")))),

                PeakMenuTestStep.of("Keyboard focus")
                        .setup(InputMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Tab and Shift+Tab move a white focus ring through the fields, checkbox, slider and buttons (not the disabled ones); Space toggles the focused checkbox; Enter or Space presses the focused button; ESC first leaves a text field")),

                PeakMenuTestStep.of("A rich text box edits several lines")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.manual("The notes box shows 'Welcome' in bold and 'rich' in red; click into it, press Enter for new lines and type until the text wraps at the box width"))
                        .check(PeakMenuChecks.manual("When the text is taller than the box a scrollbar appears; the mouse wheel and dragging the scrollbar scroll it, and typing keeps the caret in view")),

                PeakMenuTestStep.of("Rich text formatting shortcuts")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.manual("Select a word with Shift+Arrows and press Ctrl+B, Ctrl+I, Ctrl+U and Ctrl+D (strikethrough): the selection toggles bold, italic, underline and strikethrough; Ctrl+B must not switch the Minecraft narrator on or off (no narrator message, no toast)"))
                        .check(PeakMenuChecks.manual("Ctrl+K cycles the selection through the palette colors, Shift+Ctrl+K goes backwards, Ctrl+0 clears the formatting"))
                        .check(PeakMenuChecks.manual("Press Ctrl+B without a selection and type: only the new characters are bold"))
                        .check(PeakMenuChecks.action("Make some text bold with Ctrl+B and click 'Send all values': the value carries formatting codes", "rich-save",
                                event -> event.arg("rich:notes", "").contains("§l"))),

                PeakMenuTestStep.of("Double click selects a word, triple click a line")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.manual("Type two lines in the notes box, double click a word: only the word is selected; triple click: the whole line is selected (not the other line)"))
                        .check(PeakMenuChecks.manual("In the limited box a double click selects a word too; a triple click selects the line")),

                PeakMenuTestStep.of("Selected text can be dragged and dropped")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.manual("Select 'rich' in the notes box and drag it a few words to the left: a cyan drop marker follows the pointer, a small label shows the text, and releasing moves it with its red color"))
                        .check(PeakMenuChecks.manual("Select a word and hold Ctrl while releasing over another place: the word is copied, the original stays"))
                        .check(PeakMenuChecks.manual("Drag a selection from the notes box into the limited box: the text arrives there; ESC while dragging cancels; dropping inside the selection changes nothing")),

                PeakMenuTestStep.of("The formatting toolbar styles the selection and keeps the focus")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.manual("Select a word and click B, I, U and S: the selection changes, the box keeps its focus and selection, the buttons of the active styles are highlighted blue; « and » cycle the color, Tx clears"))
                        .check(PeakMenuChecks.action("Make a word bold with the toolbar and click 'Send all values'", "rich-save",
                                event -> event.arg("rich:notes", "").contains("§l"))),

                PeakMenuTestStep.of("Ctrl+Enter submits the rich text")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.action("Click into the notes box and press Ctrl+Enter (plain Enter only adds a line)", "rich-submit",
                                event -> from(event, "rich:notes") && !event.arg("value", "").isEmpty())),

                PeakMenuTestStep.of("The limited rich text box")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.manual("In the limited box press Enter twice: the second Enter adds nothing (2 lines at most); long text does not wrap but scrolls sideways with the caret")),

                PeakMenuTestStep.of("Server patches rewrite the rich text")
                        .setup(InputMenuTest::openRich)
                        .check(PeakMenuChecks.navigated("The rich menu opens", RICH_MENU))
                        .check(PeakMenuChecks.action("Click 'Server rewrites notes'", "rich-fill"))
                        .check(PeakMenuChecks.action("Click 'Send all values': the client holds the server's text with its formatting", "rich-save",
                                event -> RICH_FILLED.equals(event.arg("rich:notes"))))
        );
    }
}
