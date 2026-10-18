package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakRef;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class ServerMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/server";
    private static final String COUNTER = "peaktest:menu/server/counter";

    private record Session(PeakAudience audience, PeakView view, AtomicInteger counter) {}

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    @Override public String name() { return "server"; }
    @Override public String description() { return "A PeakView drives the menu from the server: updates, close and removal"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(MENU)
                .on("inc", event -> {
                    Session session = SESSIONS.get(event.player().getUUID());
                    if (session == null) return;
                    int count = session.counter().incrementAndGet();
                    session.view().update(PeakRef.label(COUNTER), patch -> patch.text("Counter: " + count));
                })
                .on("finish", event -> {
                    say(event.player(), "the server closes the menu and the view");
                    PeakClient.to(event.player()).menus().close();
                    cleanup(event.player());
                });
    }

    private static void open(ServerPlayer player) {
        cleanupSession(player);
        PeakAudience audience = PeakClient.audience(List.of(player));
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Server"));
        menu.add(frame("Server driven menu",
                PeakLabel.create(COUNTER, "Counter: 0"),
                button("server-inc", "Count up on the server", PeakAction.send("inc")),
                button("server-finish", "Finish (server closes)", PeakAction.send("finish")),
                button("server-close", "Close", PeakAction.close())));
        PeakView view = audience.menuView(menu).persistent(true);
        SESSIONS.put(player.getUUID(), new Session(audience, view, new AtomicInteger()));
        audience.menus().open(MENU);
    }

    private static void cleanupSession(ServerPlayer player) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session != null) session.audience().close();
    }

    @Override
    public void cleanup(ServerPlayer player) {
        cleanupSession(player);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("The server updates a label through a view")
                        .setup(ServerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.actionCount("Click 'Count up on the server' three times", "inc", 3))
                        .check(PeakMenuChecks.manual("The counter reads 'Counter: 3'")),

                PeakMenuTestStep.of("The server closes the menu")
                        .setup(ServerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Finish (server closes)'", "finish"))
                        .check(PeakMenuChecks.navigatedBy("The server closes the menu", null, "server")),

                PeakMenuTestStep.of("Closing the view removes the cached menu")
                        .setup(ServerMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Finish (server closes)'", "finish"))
                        .check(PeakMenuChecks.navigatedBy("The menu closes", null, "server"))
                        .check(PeakMenuChecks.poll("The server tries to open the removed menu again", context -> {
                            PeakClient.to(context.player()).menus().open(MENU);
                            return true;
                        }))
                        .check(PeakMenuChecks.noNavigationFor("The menu does not reappear", 40))
        );
    }
}
