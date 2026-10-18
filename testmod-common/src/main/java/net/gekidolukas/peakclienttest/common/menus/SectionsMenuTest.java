package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakSpinner;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.template.PeakCheckList;
import net.gekidolukas.peakclient.ui.template.PeakCollapsible;
import net.gekidolukas.peakclient.ui.template.PeakFormField;
import net.gekidolukas.peakclient.ui.template.PeakSeparator;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class SectionsMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/sections";
    private static final String FIELD = "sections:name";
    private static final String INPUT = "sections:name-input";

    @Override public String name() { return "sections"; }
    @Override public String description() { return "Collapsible, separator, check list, spinner and form fields with validation"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(MENU)
                .on("perk", event -> say(event.player(), "perk " + event.arg("option") + " = " + event.arg("value")))
                .on("form-save", event -> {
                    String name = event.arg(INPUT, "").trim();
                    if (name.length() < 3) {
                        PeakClient.to(event.player()).menus().formError(MENU, FIELD, INPUT, "At least 3 characters");
                    } else {
                        PeakClient.to(event.player()).menus().clearFormError(MENU, FIELD, INPUT);
                        say(event.player(), "saved " + event.args());
                    }
                });
    }

    private static void open(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Sections"));

        PeakPanel options = PeakCollapsible.create("sections:advanced", "Advanced options")
                .width(180)
                .add(PeakCheckList.create("sections:perks", "perk")
                        .option("speed", "Speed", true)
                        .option("jump", "Jump boost")
                        .option("night", "Night vision")
                        .build())
                .build();

        PeakPanel nameField = PeakFormField.create(FIELD, "Name")
                .input(PeakTextBox.create(INPUT, "").width(180))
                .required(true)
                .hint("At least 3 characters")
                .build();
        PeakPanel amountField = PeakFormField.create("sections:amount", "Amount")
                .input(PeakNumberField.create("sections:amount-input", 1, 10, 3).width(180))
                .build();

        PeakPanel loading = PeakPanel.create("sections:loading").row().gap(6).padding(0).background(0);
        loading.add(PeakSpinner.create("sections:spinner"), PeakLabel.create("sections:loading-text", "Spinner").color(0xAAAAAA));

        menu.add(frame("Sections",
                PeakSeparator.create("sections:sep").label("Settings").width(180).build(),
                options,
                PeakSeparator.create("sections:sep2").width(180).build(),
                nameField,
                amountField,
                loading,
                button("sections:save", "Save", PeakAction.send("form-save").collectAll().validate()),
                button("sections:close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("Collapsible opens and closes")
                        .setup(SectionsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("'> Advanced options' is closed, a labeled line 'Settings' sits above it and a plain line below"))
                        .check(PeakMenuChecks.manual("Click the header: the content (three checkboxes, indented) appears and the arrow turns into 'v'"))
                        .check(PeakMenuChecks.manual("Click the header again: the content disappears and the layout below moves up")),

                PeakMenuTestStep.of("Check list reports the option and its state")
                        .setup(SectionsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Open the collapsible and click 'Jump boost'", "perk",
                                event -> "jump".equals(event.arg("option")) && "true".equals(event.arg("value"))))
                        .check(PeakMenuChecks.action("Click 'Speed' (it starts checked)", "perk",
                                event -> "speed".equals(event.arg("option")) && "false".equals(event.arg("value")))),

                PeakMenuTestStep.of("A required field blocks sending")
                        .setup(SectionsMenuTest::open)
                        .forbid(event -> event instanceof PeakMenuActionEvent action && action.actionId().equals("form-save"))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The label reads 'Name *' with a red star; the amount field has no star"))
                        .check(PeakMenuChecks.manual("Click 'Save' with the name empty: the name box turns red, 'This field is required' appears and nothing is sent (no chat message, the step does not fail)")),

                PeakMenuTestStep.of("Server error and success")
                        .setup(SectionsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Type 'Al' into the name box and click 'Save'", "form-save",
                                event -> "Al".equals(event.arg(INPUT))))
                        .check(PeakMenuChecks.manual("The server marked the box red and shows 'At least 3 characters' in red below it"))
                        .check(PeakMenuChecks.action("Change the name to 'Alex' and click 'Save'", "form-save",
                                event -> "Alex".equals(event.arg(INPUT))))
                        .check(PeakMenuChecks.manual("The red border and the error text are gone; the chat shows 'saved'")),

                PeakMenuTestStep.of("Spinner")
                        .setup(SectionsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("A small ring of dots turns smoothly next to the word 'Spinner', one dot bright with a fading tail"))
        );
    }
}
