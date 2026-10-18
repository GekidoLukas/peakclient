package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakMenuEscape;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class StackMenuTest implements PeakMenuTest {
    private static final String A = "peaktest:menu/stack-a";
    private static final String B = "peaktest:menu/stack-b";
    private static final String C = "peaktest:menu/stack-c";

    @Override public String name() { return "stack"; }
    @Override public String description() { return "Navigation history: forward, back, ESC per menu, replace, removal and close"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(C).on("remove-b", event -> {
            say(event.player(), "removing menu B from the client cache");
            PeakClient.to(event.player()).menus().remove(B);
        });
    }

    private static void send(ServerPlayer player) {
        PeakDocument menuA = PeakMenu.create(A, settings -> settings.title("A"));
        menuA.add(frame("Menu A (root)",
                button("a-open-b", "Open B", PeakAction.open(B)),
                button("a-close", "Close", PeakAction.close())));

        PeakDocument menuB = PeakMenu.create(B, settings -> settings.title("B"));
        menuB.add(frame("Menu B",
                button("b-open-c", "Open C", PeakAction.open(C)),
                button("b-back", "Back", PeakAction.back())));

        PeakDocument menuC = PeakMenu.create(C, settings -> settings.title("C").escape(PeakMenuEscape.BACK));
        menuC.add(frame("Menu C (ESC goes back)",
                button("c-back", "Back", PeakAction.back()),
                button("c-replace", "Replace C with A", PeakAction.replaceWith(A)),
                button("c-remove-b", "Server removes B", PeakAction.send("remove-b")),
                button("c-close", "Close all", PeakAction.close())));

        PeakClient.to(player).menus().send(menuA).send(menuB).send(menuC);
    }

    private static void openAtC(ServerPlayer player) {
        send(player);
        PeakClient.to(player).menus().open(A).open(B).open(C);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("Forward navigation builds the history")
                        .setup(player -> {
                            send(player);
                            PeakClient.to(player).menus().open(A);
                        })
                        .check(PeakMenuChecks.navigated("Menu A opens", A, A))
                        .check(PeakMenuChecks.navigated("Click 'Open B'", B, A, B))
                        .check(PeakMenuChecks.navigated("Click 'Open C'", C, A, B, C)),

                PeakMenuTestStep.of("Back buttons walk the history")
                        .setup(StackMenuTest::openAtC)
                        .check(PeakMenuChecks.navigated("Menu C is open with the full history", C, A, B, C))
                        .check(PeakMenuChecks.navigated("Click 'Back'", B, A, B))
                        .check(PeakMenuChecks.navigated("Click 'Back' again", A, A))
                        .check(PeakMenuChecks.navigatedBy("Click 'Close'", null, "action")),

                PeakMenuTestStep.of("ESC goes back in C and closes everything in B")
                        .setup(StackMenuTest::openAtC)
                        .check(PeakMenuChecks.navigated("Menu C is open", C, A, B, C))
                        .check(PeakMenuChecks.navigatedBy("Press ESC in C: it goes back to B", B, "escape"))
                        .check(PeakMenuChecks.navigatedBy("Press ESC in B: it closes everything", null, "escape")),

                PeakMenuTestStep.of("Replace keeps the depth")
                        .setup(StackMenuTest::openAtC)
                        .check(PeakMenuChecks.navigated("Menu C is open", C, A, B, C))
                        .check(PeakMenuChecks.navigated("Click 'Replace C with A': A is already in the history, so it goes back to it", A, A)),

                PeakMenuTestStep.of("The server removes a menu from the history")
                        .setup(StackMenuTest::openAtC)
                        .check(PeakMenuChecks.navigated("Menu C is open", C, A, B, C))
                        .check(PeakMenuChecks.action("Click 'Server removes B'", "remove-b"))
                        .check(PeakMenuChecks.navigated("The history lost B and C stays open", C, A, C))
                        .check(PeakMenuChecks.navigated("Click 'Back': it lands on A", A, A)),

                PeakMenuTestStep.of("Close all from the deepest menu")
                        .setup(StackMenuTest::openAtC)
                        .check(PeakMenuChecks.navigated("Menu C is open", C, A, B, C))
                        .check(PeakMenuChecks.navigatedBy("Click 'Close all'", null, "action"))
        );
    }
}
