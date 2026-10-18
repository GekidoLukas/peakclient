package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakPlaceholderResolver;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakAnimated;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentValidator;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakReaction;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakTimer;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.client.PeakActionScope;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;
import net.gekidolukas.peakclient.ui.client.PeakClientUiState;
import net.gekidolukas.peakclient.ui.client.PeakEventRuntime;
import net.gekidolukas.peakclient.ui.client.PeakWatchValues;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakEventsTest {
    private static final String NAMESPACE = "evt";

    @AfterEach
    void cleanUp() {
        PeakClientCustomValues.clearNamespace(NAMESPACE);
        PeakClientUiState.removeDocument("test:hud");
    }

    private static PeakAction mark(String labelId, String text) {
        return PeakAction.patch(labelId, PeakPatch.label().text(text));
    }

    private static PeakDocument hud(PeakPanel... children) {
        PeakDocument document = PeakDocument.create("test:hud");
        for (PeakPanel child : children) document.add(child);
        PeakClientUiState.applyDocument(document);
        return document;
    }

    private static String text(PeakDocument document, String id) {
        return ((PeakLabel) PeakClientUiState.model(document.getId()).find(id)).getText();
    }

    private static void run(PeakDocument document) {
        PeakEventRuntime.evaluate(document, PeakActionScope.document(document.getId()));
    }

    @Test
    void theConvenienceMethodsAddReactionsAndTheLookupMergesThem() {
        PeakPanel panel = PeakPanel.create("test:panel")
                .onClick(PeakAction.close())
                .onClick(PeakAction.back())
                .onIncrease("{evt:score}", PeakAction.close())
                .onAbove("{evt:score}", 5, PeakAction.back())
                .after(20, PeakAction.close())
                .every(10, PeakAction.close());

        assertEquals(6, panel.getEvents().size());
        assertEquals(2, panel.actionsFor(PeakEvent.CLICK).size());
        assertTrue(panel.hasEvent(PeakEvent.VALUE_INCREASE));
        assertFalse(panel.hasEvent(PeakEvent.VALUE_DECREASE));
        assertEquals(5.0, panel.getEvents().get(3).getThreshold());
        assertEquals(20, panel.getEvents().get(4).getTicks());
        assertEquals("{evt:score}", panel.getEvents().get(2).getWatch());
    }

    @Test
    void onlyInteractionEventsMakeAnElementInteractive() {
        assertNotNull(PeakPanel.create("test:a").onClick(PeakAction.close()).getInteraction());
        assertNotNull(PeakPanel.create("test:b").onHoverEnter(PeakAction.close()).getInteraction());
        assertNull(PeakPanel.create("test:c").onAppear(PeakAction.close()).getInteraction());
        assertNull(PeakPanel.create("test:d").onValueChange("{evt:x}", PeakAction.close()).getInteraction());
    }

    @Test
    void reactionsSurviveTheWireWithTheirSettings() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakLabel.create("test:label", "x")
                .on(PeakReaction.of(PeakEvent.VALUE_ABOVE).watch("{evt:hp}").threshold(10.5).cooldown(6).once().fireOnStart()
                        .then(PeakAction.sound("minecraft:entity.experience_orb.pickup", 0.5f, 1.5f)))
                .every(40, PeakAction.close()));

        PeakLabel back = assertInstanceOf(PeakLabel.class,
                PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document)).findById("test:label"));

        PeakReaction first = back.getEvents().get(0);
        assertEquals(PeakEvent.VALUE_ABOVE, first.getOn());
        assertEquals("{evt:hp}", first.getWatch());
        assertEquals(10.5, first.getThreshold());
        assertEquals(6, first.getCooldownTicks());
        assertTrue(first.isOnce());
        assertTrue(first.isFireOnStart());
        assertEquals(PeakActionType.SOUND, first.getActions().get(0).type());
        assertEquals(0.5f, first.getActions().get(0).soundSpec().getVolume(), 1e-6f);
        assertEquals(1.5f, first.getActions().get(0).soundSpec().getPitch(), 1e-6f);

        PeakReaction second = back.getEvents().get(1);
        assertEquals(PeakEvent.INTERVAL, second.getOn());
        assertEquals(40, second.getTicks());
        assertFalse(second.isOnce());
        assertEquals(0, second.getCooldownTicks());
        assertEquals("", second.getWatch());
    }

    @Test
    void thePatchReplacesTheWholeList() {
        PeakPanel panel = PeakPanel.create("test:panel").onClick(PeakAction.close());

        panel.applyPatch(PeakPatch.panel().onClick(PeakAction.back(), PeakAction.close()).build());
        assertEquals(2, panel.actionsFor(PeakEvent.CLICK).size());
        assertEquals(PeakActionType.BACK, panel.actionsFor(PeakEvent.CLICK).get(0).type());

        panel.applyPatch(PeakPatch.panel().clearEvents().build());
        assertTrue(panel.getEvents().isEmpty());

        panel.applyPatch(PeakPatch.panel().events(PeakReaction.of(PeakEvent.VALUE_CHANGE).watch("{evt:a}").then(PeakAction.close())).build());
        assertEquals(PeakEvent.VALUE_CHANGE, panel.getEvents().get(0).getOn());
    }

    @Test
    void aPatchedClickMakesTheElementInteractive() {
        PeakPanel panel = PeakPanel.create("test:panel");
        assertNull(panel.getInteraction());

        panel.applyPatch(PeakPatch.panel().onClick(PeakAction.close()).build());

        assertNotNull(panel.getInteraction());
    }

    @Test
    void valueChangesFireTheMatchingReactionsOnly() {
        PeakClientCustomValues.set(NAMESPACE, "score", "10");
        PeakPanel watcher = PeakPanel.create("test:watcher")
                .onValueChange("{evt:score}", mark("test:any", "changed"))
                .onIncrease("{evt:score}", mark("test:up", "up"))
                .onDecrease("{evt:score}", mark("test:down", "down"));
        PeakDocument document = hud(watcher, PeakPanel.create("test:targets")
                .add(PeakLabel.create("test:any", "-"), PeakLabel.create("test:up", "-"), PeakLabel.create("test:down", "-")));

        run(document);
        assertEquals("-", text(document, "test:any"));

        PeakClientCustomValues.set(NAMESPACE, "score", "14");
        run(document);
        assertEquals("changed", text(document, "test:any"));
        assertEquals("up", text(document, "test:up"));
        assertEquals("-", text(document, "test:down"));
    }

    @Test
    void aDecreaseFiresTheDecreaseReactionAndNotTheIncrease() {
        PeakClientCustomValues.set(NAMESPACE, "score", "10");
        PeakDocument document = hud(PeakPanel.create("test:watcher")
                        .onIncrease("{evt:score}", mark("test:up", "up"))
                        .onDecrease("{evt:score}", mark("test:down", "down")),
                PeakPanel.create("test:targets").add(PeakLabel.create("test:up", "-"), PeakLabel.create("test:down", "-")));

        run(document);
        PeakClientCustomValues.set(NAMESPACE, "score", "3");
        run(document);

        assertEquals("-", text(document, "test:up"));
        assertEquals("down", text(document, "test:down"));
    }

    @Test
    void thresholdsFireOnlyWhenCrossed() {
        PeakClientCustomValues.set(NAMESPACE, "hp", "20");
        PeakDocument document = hud(PeakPanel.create("test:watcher")
                        .onBelow("{evt:hp}", 8, mark("test:low", "low"))
                        .onAbove("{evt:hp}", 15, mark("test:high", "high")),
                PeakPanel.create("test:targets").add(PeakLabel.create("test:low", "-"), PeakLabel.create("test:high", "-")));

        run(document);
        PeakClientCustomValues.set(NAMESPACE, "hp", "12");
        run(document);
        assertEquals("-", text(document, "test:low"));
        assertEquals("-", text(document, "test:high"));

        PeakClientCustomValues.set(NAMESPACE, "hp", "6");
        run(document);
        assertEquals("low", text(document, "test:low"));
        assertEquals("-", text(document, "test:high"));
    }

    @Test
    void anUnresolvablePlaceholderNeverFiresAndNeverSetsTheBaseline() {
        PeakDocument document = hud(PeakPanel.create("test:watcher").onValueChange("{evt:late}", mark("test:target", "fired")),
                PeakPanel.create("test:targets").add(PeakLabel.create("test:target", "-")));

        run(document);
        PeakClientCustomValues.set(NAMESPACE, "late", "1");
        run(document);
        assertEquals("-", text(document, "test:target"));

        PeakClientCustomValues.set(NAMESPACE, "late", "2");
        run(document);
        assertEquals("fired", text(document, "test:target"));
    }

    @Test
    void fireOnStartFiresForTheFirstValue() {
        PeakClientCustomValues.set(NAMESPACE, "score", "10");
        PeakDocument document = hud(PeakPanel.create("test:watcher").on(PeakReaction.of(PeakEvent.VALUE_CHANGE)
                        .watch("{evt:score}").fireOnStart().then(mark("test:target", "start"))),
                PeakPanel.create("test:targets").add(PeakLabel.create("test:target", "-")));

        run(document);

        assertEquals("start", text(document, "test:target"));
    }

    @Test
    void onceLimitsAReactionToOneFiring() {
        PeakClientCustomValues.set(NAMESPACE, "score", "1");
        PeakPanel watcher = PeakPanel.create("test:watcher").on(PeakReaction.of(PeakEvent.VALUE_CHANGE).watch("{evt:score}").once()
                .then(PeakAction.patch("test:counter", PeakPatch.label().text("fired"))));
        PeakDocument document = hud(watcher, PeakPanel.create("test:targets").add(PeakLabel.create("test:counter", "-")));

        run(document);
        PeakClientCustomValues.set(NAMESPACE, "score", "2");
        run(document);
        assertEquals("fired", text(document, "test:counter"));

        PeakClientUiState.model("test:hud").applyOperation(new PeakUiOperation.Update(
                "test:counter", PeakPatch.label().text("reset").build()));
        PeakClientCustomValues.set(NAMESPACE, "score", "3");
        run(document);
        assertEquals("reset", text(document, "test:counter"));
    }

    @Test
    void appearFiresOnceAndShowHideFollowTheHiddenFlag() {
        PeakPanel watcher = PeakPanel.create("test:watcher")
                .onAppear(mark("test:appear", "appeared"))
                .onHide(mark("test:hide", "hidden"))
                .onShow(mark("test:show", "shown"));
        PeakDocument document = hud(watcher, PeakPanel.create("test:targets").add(PeakLabel.create("test:appear", "-"),
                PeakLabel.create("test:hide", "-"), PeakLabel.create("test:show", "-")));

        run(document);
        assertEquals("appeared", text(document, "test:appear"));
        assertEquals("-", text(document, "test:hide"));

        PeakClientUiState.model("test:hud").find("test:watcher").applyPatch(PeakPatch.panel().hidden(true).build());
        run(document);
        assertEquals("hidden", text(document, "test:hide"));
        assertEquals("-", text(document, "test:show"));

        PeakClientUiState.model("test:hud").find("test:watcher").applyPatch(PeakPatch.panel().hidden(false).build());
        run(document);
        assertEquals("shown", text(document, "test:show"));
    }

    @Test
    void aTimerThatAlreadyEndedFiresOnlyWithFireOnStart() {
        PeakTimer quiet = PeakTimer.countDownFrom("test:quiet", 0).onTimerEnd(mark("test:quiet-target", "ended"));
        PeakTimer eager = PeakTimer.countDownFrom("test:eager", 0).on(PeakReaction.of(PeakEvent.TIMER_END).fireOnStart()
                .then(mark("test:eager-target", "ended")));
        PeakDocument document = PeakDocument.create("test:hud");
        document.add(PeakPanel.create("test:holder").add(quiet, eager, PeakLabel.create("test:quiet-target", "-"),
                PeakLabel.create("test:eager-target", "-")));
        PeakClientUiState.applyDocument(document);

        run(document);

        assertEquals("-", text(document, "test:quiet-target"));
        assertEquals("ended", text(document, "test:eager-target"));
        assertTrue(quiet.isFinished());
    }

    @Test
    void timersReportTheirEndForBothModes() {
        assertTrue(PeakTimer.countDownFrom(0).isFinished());
        assertFalse(PeakTimer.countDownFrom(1000).isFinished());
        assertFalse(PeakTimer.countUpFrom(0).isFinished());
    }

    @Test
    void theHighlightSugarBuildsTriggersAimedAtTheContainer() {
        PeakAnimated animated = PeakAnimated.create("test:box").highlightOnTrend("{evt:score}")
                .highlightOnChange("{evt:name}");

        assertEquals(3, animated.getEvents().size());
        PeakReaction increase = animated.getEvents().get(0);
        assertEquals(PeakEvent.VALUE_INCREASE, increase.getOn());
        assertEquals("{evt:score}", increase.getWatch());
        PeakAction action = increase.getActions().get(0);
        assertEquals("test:box", action.target());
        assertEquals("FLASH", action.patch().getAsJsonObject("trigger").get("effect").getAsString());
        assertEquals(PeakAnimated.HIGHLIGHT_INCREASE, action.patch().getAsJsonObject("trigger").get("color").getAsInt());
        assertEquals(PeakAnimated.HIGHLIGHT_DECREASE,
                animated.getEvents().get(1).getActions().get(0).patch().getAsJsonObject("trigger").get("color").getAsInt());
        assertEquals(PeakEvent.VALUE_CHANGE, animated.getEvents().get(2).getOn());
        assertEquals("SHINE", animated.getEvents().get(2).getActions().get(0).patch().getAsJsonObject("trigger")
                .get("effect").getAsString());
        assertNull(animated.getInteraction());
    }

    @Test
    void placeholderKeysMayContainSlashesAndDashes() {
        PeakClientCustomValues.set(NAMESPACE, "events/score-box", "7");

        PeakPlaceholderResolver.Result result = PeakPlaceholderResolver.resolveStrict("Score {evt:events/score-box}/20");

        assertTrue(result.complete());
        assertEquals("Score 7/20", result.value());
    }

    @Test
    void watchValuesReadTheLeadingNumber() {
        assertEquals(12.0, PeakWatchValues.number("12/20"));
        assertEquals(5.0, PeakWatchValues.number("§c5 coins"));
        assertEquals(-3.5, PeakWatchValues.number("  -3.5"));
        assertNull(PeakWatchValues.number("abc"));
        assertNull(PeakWatchValues.number(null));
        assertEquals("4", PeakWatchValues.format(4.0));
        assertEquals("-2.5", PeakWatchValues.format(-2.5));
        assertEquals("0.333333", PeakWatchValues.format(1.0 / 3.0));
    }

    @Test
    void theValidatorFlagsBrokenReactions() {
        PeakDocument menu = PeakMenu.create("test:menu");
        menu.add(PeakPanel.create("test:p").onValueChange("{evt:x}", PeakAction.close()));
        menu.add(PeakPanel.create("test:q").on(PeakReaction.of(PeakEvent.VALUE_CHANGE).then(PeakAction.close())));
        menu.add(PeakLabel.create("test:l", "x").on(PeakReaction.of(PeakEvent.VALUE_CHANGE).watch("plain").then(PeakAction.close())));
        menu.add(PeakPanel.create("test:r").on(PeakReaction.of(PeakEvent.INTERVAL).then(PeakAction.close())));
        menu.add(PeakPanel.create("test:s").on(PeakReaction.of(PeakEvent.APPEAR)));

        List<String> issues = PeakDocumentValidator.validate(menu, 0, 0).issues();

        assertEquals(4, issues.size(), issues.toString());
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("'test:q'") && issue.contains("no default value")));
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("'test:l'") && issue.contains("no placeholder")));
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("'test:r'") && issue.contains("tick count")));
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("'test:s'") && issue.contains("no actions")));
    }

    @Test
    void inputElementsKeepTheirChangeAndSubmitReactions() {
        PeakSlider slider = PeakSlider.create("test:slider").onChange(PeakAction.send("volume"));
        PeakButton button = PeakButton.text("test:button", "OK").onClick(PeakAction.send("ok"));

        assertEquals("volume", slider.actionsFor(PeakEvent.CHANGE).get(0).target());
        assertNotNull(slider.getInteraction());
        assertNotNull(button.getInteraction());
    }
}
