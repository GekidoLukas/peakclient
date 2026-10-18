package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakRadioGroup;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class FormsMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/forms";
    private static final List<String> MARKED = List.of("forms:size", "forms:team", "forms:amount", "forms:price", "forms:sound");

    @Override public String name() { return "forms"; }
    @Override public String description() { return "Radio groups, number fields, switch checkbox and the invalid state"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(MENU)
                .on("mark-invalid", event -> setInvalid(event.player(), true))
                .on("clear-invalid", event -> setInvalid(event.player(), false))
                .on("forms-save", event -> say(event.player(), "form values " + event.args()));
    }

    private static void setInvalid(ServerPlayer player, boolean invalid) {
        for (String id : MARKED) {
            PeakClient.to(player).menus().update(MENU, id, PeakPatch.radioGroup().field("interaction.invalid", invalid));
        }
    }

    private static void open(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Forms"));
        PeakRadioGroup size = PeakRadioGroup.create("forms:size")
                .option("s", "Small")
                .option("m", "Medium")
                .option("l", "Large (disabled)", false)
                .option("xl", "Extra large")
                .selected("m")
                .onChange(PeakAction.send("size"));
        PeakRadioGroup team = PeakRadioGroup.create("forms:team").row().gap(8)
                .option("red", "Red").option("blue", "Blue").option("green", "Green")
                .selected("red")
                .onChange(PeakAction.send("team"));
        PeakNumberField amount = PeakNumberField.create("forms:amount", 1, 64, 16).step(1)
                .onChange(PeakAction.send("amount"));
        PeakNumberField price = PeakNumberField.create("forms:price", 0, 10, 2.5).step(0.25).decimals(2)
                .onChange(PeakAction.send("price"));
        PeakDropdown country = PeakDropdown.create("forms:country").placeholder("Choose a country").maxVisible(5)
                .option("de", "Germany").option("fr", "France").option("it", "Italy").option("es", "Spain")
                .option("pl", "Poland").option("nl", "Netherlands").option("se", "Sweden (disabled)", false)
                .option("no", "Norway")
                .onChange(PeakAction.send("country"));
        PeakCheckbox sound = PeakCheckbox.create("forms:sound", "Sound (switch)").asSwitch().checked(true)
                .onChange(PeakAction.send("sound"));
        PeakCheckbox plain = PeakCheckbox.create("forms:plain", "Notifications (box)").checked(true)
                .onChange(PeakAction.send("notify"));

        PeakPanel left = PeakPanel.create("forms:left").column().gap(5).padding(0).background(0).width(170);
        left.add(PeakLabel.create("forms:l1", "Size").color(0xAAAAAA), size,
                PeakLabel.create("forms:l2", "Team (row)").color(0xAAAAAA), team,
                PeakLabel.create("forms:l5", "Country (dropdown)").color(0xAAAAAA), country,
                sound, plain);
        PeakPanel right = PeakPanel.create("forms:right").column().gap(5).padding(0).background(0).width(170);
        right.add(PeakLabel.create("forms:l3", "Amount 1 to 64").color(0xAAAAAA), amount,
                PeakLabel.create("forms:l4", "Price 0 to 10, step 0.25").color(0xAAAAAA), price,
                button("forms:invalid", "Server marks invalid", PeakAction.send("mark-invalid")).width(170),
                button("forms:valid", "Server clears invalid", PeakAction.send("clear-invalid")).width(170),
                button("forms:save", "Send all values", PeakAction.send("forms-save").collectAll()).width(170),
                button("forms:close", "Close", PeakAction.close()).width(170));
        PeakPanel columns = PeakPanel.create("forms:columns").row().gap(12).padding(0).background(0);
        columns.add(left, right);

        PeakPanel panel = frame("Forms", columns);
        panel.width(380);
        menu.add(panel);
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("Radio group: click selects, disabled options do nothing")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("'Medium' shows a filled dot, 'Large (disabled)' is dimmed"))
                        .check(PeakMenuChecks.action("Click 'Extra large'", "size", event -> "xl".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The dot moved to 'Extra large'; clicking 'Large (disabled)' changes nothing and sends nothing"))
                        .check(PeakMenuChecks.action("Click 'Small'", "size", event -> "s".equals(event.arg("value")))),

                PeakMenuTestStep.of("Radio group: keyboard")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Tab to the size group and press Down (skips the disabled option)", "size",
                                event -> "xl".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Press Down again (wraps around)", "size", event -> "s".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Press End", "size", event -> "xl".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Press Home", "size", event -> "s".equals(event.arg("value")))),

                PeakMenuTestStep.of("Radio group in a row")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Blue'", "team", event -> "blue".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Press Right (the group keeps the focus after the click)", "team",
                                event -> "green".equals(event.arg("value")))),

                PeakMenuTestStep.of("Number field: buttons and keys")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the '+' button of the amount field", "amount",
                                event -> "17".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Click the '-' button twice", "amount",
                                event -> "15".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Click into the field and press Up with Shift held (steps of ten)", "amount",
                                event -> "25".equals(event.arg("value")))),

                PeakMenuTestStep.of("Number field: typing and clamping")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the amount field, type 200 and press Enter", "amount",
                                event -> "64".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The field shows 64 (clamped); letters you type are ignored; Esc during typing restores the shown value")),

                PeakMenuTestStep.of("Number field: mouse wheel and decimals")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Scroll the mouse wheel up over the price field", "price",
                                event -> "2.75".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The price field always shows two decimals (2.75) and the buttons dim at 0 and 10")),

                PeakMenuTestStep.of("Dropdown: open, scroll and choose")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The country field shows 'Choose a country' in gray with an arrow on the right"))
                        .check(PeakMenuChecks.manual("Click it: a list with five rows opens below (or above near the screen bottom) with a thin scrollbar; the mouse wheel scrolls it; 'Sweden (disabled)' is dim"))
                        .check(PeakMenuChecks.action("Choose 'Italy'", "country", event -> "it".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The list closed and the field shows 'Italy'; opening it again marks 'Italy' in green")),

                PeakMenuTestStep.of("Dropdown: closing without choosing")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Open the dropdown, then click the 'Close' button: only the list closes, nothing else is triggered"))
                        .check(PeakMenuChecks.noNavigationFor("The menu stays open", 40))
                        .check(PeakMenuChecks.manual("Open the dropdown and press Esc: only the list closes; click 'Sweden (disabled)': the list stays open")),

                PeakMenuTestStep.of("Dropdown: keyboard")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Tab to the dropdown (third control) and press Down: the next entry is chosen without opening", "country",
                                event -> "de".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Press Enter to open, Down twice, Enter to choose", "country",
                                event -> "it".equals(event.arg("value")))),

                PeakMenuTestStep.of("Switch checkbox and box checkbox")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the switch 'Sound'", "sound", event -> "false".equals(event.arg("value"))))
                        .check(PeakMenuChecks.manual("The switch knob moved to the left and the track is dark; the box checkbox below still looks like a box"))
                        .check(PeakMenuChecks.action("Click the switch again", "sound", event -> "true".equals(event.arg("value")))),

                PeakMenuTestStep.of("Invalid state set by the server")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Server marks fields invalid'", "mark-invalid"))
                        .check(PeakMenuChecks.manual("The radio circles, both number fields and the switch have a red border"))
                        .check(PeakMenuChecks.action("Click 'Server clears invalid'", "clear-invalid"))
                        .check(PeakMenuChecks.manual("All borders are back to grey")),

                PeakMenuTestStep.of("All values are collected")
                        .setup(FormsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Send all values'", "forms-save",
                                event -> "m".equals(event.arg("forms:size")) && "16".equals(event.arg("forms:amount"))
                                        && "2.50".equals(event.arg("forms:price")) && "true".equals(event.arg("forms:sound"))))
        );
    }
}
