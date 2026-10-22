package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLabelAnimation;
import net.gekidolukas.peakclient.ui.PeakLabelText;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakLabelAnimationTest {
    private static PeakStyledText t(String text) {
        return PeakStyledText.ofLegacy(text);
    }

    private static String display(PeakLabelAnimation animation, String text, int ticks, double now) {
        return animation.displayText(t(text), ticks, now).plain();
    }

    private static List<String> plains(List<PeakStyledText> lines) {
        return lines.stream().map(PeakStyledText::plain).toList();
    }

    @Test
    void theFirstNumberIsFoundWithPrefixAndSuffix() {
        PeakLabelText.NumberSpan span = PeakLabelText.findNumber(t("Score: 120 pts"));

        assertNotNull(span);
        assertEquals(120.0, span.value());
        assertEquals(7, span.start());
        assertEquals(10, span.end());
        assertEquals(0, span.decimals());
    }

    @Test
    void formatCodesAreNotReadAsNumbers() {
        PeakLabelText.NumberSpan span = PeakLabelText.findNumber(t("§6Score: §f42"));
        assertEquals(42.0, span.value());

        String hex = "§x§F§F§0§0§0§0Level 7";
        assertEquals(7.0, PeakLabelText.findNumber(t(hex)).value());
        assertNull(PeakLabelText.findNumber(t("§6Nothing here")));
        assertNull(PeakLabelText.findNumber(t("")));
    }

    @Test
    void signsAndDecimalsAreKept() {
        PeakLabelText.NumberSpan negative = PeakLabelText.findNumber(t("Delta -30 now"));
        assertEquals(-30.0, negative.value());
        assertEquals("Delta -12 now", PeakLabelText.replaceNumber(t("Delta -30 now"), negative, -12.0).plain());

        PeakLabelText.NumberSpan decimal = PeakLabelText.findNumber(t("Ratio 12.50x"));
        assertEquals(2, decimal.decimals());
        assertEquals("Ratio 3.14x", PeakLabelText.replaceNumber(t("Ratio 12.50x"), decimal, 3.14159).plain());

        PeakLabelText.NumberSpan plus = PeakLabelText.findNumber(t("Bonus +5"));
        assertEquals("Bonus +9", PeakLabelText.replaceNumber(t("Bonus +5"), plus, 9.0).plain());
        assertEquals("Bonus -2", PeakLabelText.replaceNumber(t("Bonus +5"), plus, -2.0).plain());
    }

    @Test
    void skeletonsMatchWhenOnlyTheNumberChanged() {
        String first = "Score: 120 pts";
        String second = "Score: 150 pts";
        assertEquals(PeakLabelText.skeleton(t(first), PeakLabelText.findNumber(t(first))),
                PeakLabelText.skeleton(t(second), PeakLabelText.findNumber(t(second))));
        String other = "Points: 150 pts";
        assertNotEquals(PeakLabelText.skeleton(t(first), PeakLabelText.findNumber(t(first))),
                PeakLabelText.skeleton(t(other), PeakLabelText.findNumber(t(other))));
    }

    @Test
    void visibleTextIgnoresFormatCodes() {
        assertEquals(5, PeakLabelText.visibleLength(t("§6Hello")));
        assertEquals(5, PeakLabelText.visibleLength(t("§x§F§F§0§0§0§0Hello")));
        assertEquals("He", PeakLabelText.truncateVisible(t("§6Hello"), 2).plain());
        assertEquals(t("§6He"), PeakLabelText.truncateVisible(t("§6Hello"), 2));
        assertEquals("", PeakLabelText.truncateVisible(t("§6Hello"), 0).plain());
        assertEquals(t("§6Hello"), PeakLabelText.truncateVisible(t("§6Hello"), 9));
        assertEquals(t("§aab"), PeakLabelText.truncateVisible(t("§aab§cc"), 2));
    }

    @Test
    void theCharacterBudgetIsSpreadOverTheLines() {
        List<PeakStyledText> lines = List.of(t("Hello"), t("big"), t("world"));

        assertEquals(List.of("Hel", "", ""), plains(PeakLabelText.truncateLines(lines, 3)));
        assertEquals(List.of("Hello", "bi", ""), plains(PeakLabelText.truncateLines(lines, 7)));
        assertEquals(lines, PeakLabelText.truncateLines(lines, 13));
        assertEquals(List.of("", "", ""), plains(PeakLabelText.truncateLines(lines, 0)));
    }

    @Test
    void theFirstTextIsShownAtOnceAndANumberChangeCountsUp() {
        PeakLabelAnimation animation = new PeakLabelAnimation();

        assertEquals("Score: 120", display(animation, "Score: 120", 20, 0.0));
        assertEquals("Score: 120", display(animation, "Score: 120", 20, 5.0));

        assertEquals("Score: 120", display(animation, "Score: 220", 20, 10.0));
        String middle = display(animation, "Score: 220", 20, 20.0);
        int value = Integer.parseInt(middle.substring("Score: ".length()));
        assertTrue(value > 120 && value < 220, middle);
        assertEquals("Score: 220", display(animation, "Score: 220", 20, 40.0));
    }

    @Test
    void countingEasesOutAndEndsOnTheExactTarget() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        display(animation, "N 0", 10, 0.0);
        display(animation, "N 100", 10, 1.0);

        int halfway = Integer.parseInt(display(animation, "N 100", 10, 6.0).substring(2));
        int early = Integer.parseInt(display(animation, "N 100", 10, 2.0).substring(2));

        assertTrue(halfway > 50, "eased out, halfway at " + halfway);
        assertTrue(early < halfway);
        assertEquals("N 100", display(animation, "N 100", 10, 11.0));
    }

    @Test
    void aNewValueDuringTheCountStartsFromTheCurrentDisplay() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        display(animation, "N 0", 10, 0.0);
        display(animation, "N 100", 10, 1.0);
        int shown = Integer.parseInt(display(animation, "N 100", 10, 5.0).substring(2));

        String restarted = display(animation, "N 200", 10, 5.0);

        assertEquals("N " + shown, restarted);
        assertEquals("N 200", display(animation, "N 200", 10, 15.0));
    }

    @Test
    void aChangeBeyondTheNumberJumpsAndTextWithoutNumbersIsUntouched() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        display(animation, "Score: 10", 20, 0.0);

        assertEquals("Points: 99", display(animation, "Points: 99", 20, 1.0));
        assertEquals("No number", display(animation, "No number", 20, 2.0));
        assertEquals("Now 5", display(animation, "Now 5", 20, 3.0));
    }

    @Test
    void decimalsAndFormatCodesSurviveTheCount() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.displayText(t("§6Score: §f10.0"), 20, 0.0);

        PeakStyledText middle = animation.displayText(t("§6Score: §f20.0"), 20, 1.0 + 10.0);

        assertTrue(middle.plain().startsWith("Score: "), middle.plain());
        assertTrue(middle.plain().matches("Score: \\d+\\.\\d"), middle.plain());
        assertEquals(t("§6Score: ").styleAt(0), middle.styleAt(0));
        assertEquals(t("§6Score: §f1").styleAt(7), middle.styleAt(7));
        assertEquals(t("§6Score: §f20.0"), animation.displayText(t("§6Score: §f20.0"), 20, 60.0));
    }

    @Test
    void withoutCountUpTicksTheTargetAppearsAtOnce() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        display(animation, "N 1", 0, 0.0);

        assertEquals("N 50", display(animation, "N 50", 0, 1.0));
    }

    @Test
    void theTypewriterRevealsOverTimeAndThenShowsEverything() {
        PeakLabelAnimation animation = new PeakLabelAnimation();

        assertEquals(0, animation.revealBudget(t("Hello world"), 20f, false, 0.0));
        assertEquals(5, animation.revealBudget(t("Hello world"), 20f, false, 5.0));
        assertEquals(10, animation.revealBudget(t("Hello world"), 20f, false, 10.0));
        assertEquals(-1, animation.revealBudget(t("Hello world"), 20f, false, 11.0));
        assertEquals(-1, animation.revealBudget(t("Hello world"), 20f, false, 100.0));
    }

    @Test
    void theTypewriterIsOffWithoutASpeed() {
        assertEquals(-1, new PeakLabelAnimation().revealBudget(t("abc"), 0f, false, 0.0));
    }

    @Test
    void appendedTextContinuesAndOtherTextRestarts() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget(t("Hi"), 20f, false, 0.0);
        assertEquals(-1, animation.revealBudget(t("Hi"), 20f, false, 10.0));

        int appended = animation.revealBudget(t("Hi there"), 20f, false, 10.0);
        assertEquals(2, appended);
        assertEquals(5, animation.revealBudget(t("Hi there"), 20f, false, 13.0));

        assertEquals(0, animation.revealBudget(t("Bye"), 20f, false, 14.0));
    }

    @Test
    void textThatWasNotFullyShownRestartsInsteadOfContinuing() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget(t("Hello"), 20f, false, 0.0);
        animation.revealBudget(t("Hello"), 20f, false, 2.0);

        assertEquals(0, animation.revealBudget(t("Hello world"), 20f, false, 3.0));
    }

    @Test
    void aNumberOnlyChangeKeepsTheRevealWhenCountingUp() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget(t("Score: 10"), 20f, true, 0.0);
        assertEquals(-1, animation.revealBudget(t("Score: 10"), 20f, true, 10.0));

        assertEquals(-1, animation.revealBudget(t("Score: 25"), 20f, true, 11.0));
    }

    @Test
    void restartingBeginsAgainFromNothing() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget(t("Hello"), 20f, false, 0.0);
        assertEquals(-1, animation.revealBudget(t("Hello"), 20f, false, 10.0));

        animation.restart();

        assertEquals(0, animation.revealBudget(t("Hello"), 20f, false, 11.0));
    }

    @Test
    void theLabelFieldsHaveDefaultsAndSurviveTheWireAndPatches() {
        PeakLabel plain = PeakLabel.create("test:plain", "x");
        assertEquals(0, plain.getCountUpTicks());
        assertEquals(0f, plain.getTypewriterCharsPerSecond());

        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakLabel.create("test:animated", "Score: 1").countUpSeconds(1.5).typewriter(30f));
        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
        PeakLabel label = assertInstanceOf(PeakLabel.class, back.findById("test:animated"));
        assertEquals(30, label.getCountUpTicks());
        assertEquals(30f, label.getTypewriterCharsPerSecond());

        label.applyPatch(PeakPatch.label().countUp(10).typewriter(5f).build());
        assertEquals(10, label.getCountUpTicks());
        assertEquals(5f, label.getTypewriterCharsPerSecond());
    }

    @Test
    void theReplayPatchRestartsTheTypewriter() {
        PeakLabel label = PeakLabel.create("test:label", "Hello").typewriter(20f);
        label.animation().revealBudget(t("Hello"), 20f, false, 0.0);
        assertEquals(-1, label.animation().revealBudget(t("Hello"), 20f, false, 10.0));

        label.applyPatch(PeakPatch.label().replay().build());

        assertTrue(label.isPatchKeyKnown("replay"));
        assertEquals(0, label.animation().revealBudget(t("Hello"), 20f, false, 11.0));
        assertFalse(label.getText().getString().isEmpty());
    }
}
