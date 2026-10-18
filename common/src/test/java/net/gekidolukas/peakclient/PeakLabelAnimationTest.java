package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLabelAnimation;
import net.gekidolukas.peakclient.ui.PeakLabelText;
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
    @Test
    void theFirstNumberIsFoundWithPrefixAndSuffix() {
        PeakLabelText.NumberSpan span = PeakLabelText.findNumber("Score: 120 pts");

        assertNotNull(span);
        assertEquals(120.0, span.value());
        assertEquals(7, span.start());
        assertEquals(10, span.end());
        assertEquals(0, span.decimals());
    }

    @Test
    void formatCodesAreNotReadAsNumbers() {
        PeakLabelText.NumberSpan span = PeakLabelText.findNumber("§6Score: §f42");
        assertEquals(42.0, span.value());

        String hex = "§x§F§F§0§0§0§0Level 7";
        assertEquals(7.0, PeakLabelText.findNumber(hex).value());
        assertNull(PeakLabelText.findNumber("§6Nothing here"));
        assertNull(PeakLabelText.findNumber(""));
    }

    @Test
    void signsAndDecimalsAreKept() {
        PeakLabelText.NumberSpan negative = PeakLabelText.findNumber("Delta -30 now");
        assertEquals(-30.0, negative.value());
        assertEquals("Delta -12 now", PeakLabelText.replaceNumber("Delta -30 now", negative, -12.0));

        PeakLabelText.NumberSpan decimal = PeakLabelText.findNumber("Ratio 12.50x");
        assertEquals(2, decimal.decimals());
        assertEquals("Ratio 3.14x", PeakLabelText.replaceNumber("Ratio 12.50x", decimal, 3.14159));

        PeakLabelText.NumberSpan plus = PeakLabelText.findNumber("Bonus +5");
        assertEquals("Bonus +9", PeakLabelText.replaceNumber("Bonus +5", plus, 9.0));
        assertEquals("Bonus -2", PeakLabelText.replaceNumber("Bonus +5", plus, -2.0));
    }

    @Test
    void skeletonsMatchWhenOnlyTheNumberChanged() {
        String first = "Score: 120 pts";
        String second = "Score: 150 pts";
        assertEquals(PeakLabelText.skeleton(first, PeakLabelText.findNumber(first)),
                PeakLabelText.skeleton(second, PeakLabelText.findNumber(second)));
        String other = "Points: 150 pts";
        assertNotEquals(PeakLabelText.skeleton(first, PeakLabelText.findNumber(first)),
                PeakLabelText.skeleton(other, PeakLabelText.findNumber(other)));
    }

    @Test
    void visibleTextIgnoresFormatCodes() {
        assertEquals(5, PeakLabelText.visibleLength("§6Hello"));
        assertEquals(5, PeakLabelText.visibleLength("§x§F§F§0§0§0§0Hello"));
        assertEquals("§6He", PeakLabelText.truncateVisible("§6Hello", 2));
        assertEquals("", PeakLabelText.truncateVisible("§6Hello", 0));
        assertEquals("§6Hello", PeakLabelText.truncateVisible("§6Hello", 9));
        assertEquals("§aab", PeakLabelText.truncateVisible("§aab§cc", 2));
    }

    @Test
    void theCharacterBudgetIsSpreadOverTheLines() {
        List<String> lines = List.of("Hello", "big", "world");

        assertEquals(List.of("Hel", "", ""), PeakLabelText.truncateLines(lines, 3));
        assertEquals(List.of("Hello", "bi", ""), PeakLabelText.truncateLines(lines, 7));
        assertEquals(lines, PeakLabelText.truncateLines(lines, 13));
        assertEquals(List.of("", "", ""), PeakLabelText.truncateLines(lines, 0));
    }

    @Test
    void theFirstTextIsShownAtOnceAndANumberChangeCountsUp() {
        PeakLabelAnimation animation = new PeakLabelAnimation();

        assertEquals("Score: 120", animation.displayText("Score: 120", 20, 0.0));
        assertEquals("Score: 120", animation.displayText("Score: 120", 20, 5.0));

        assertEquals("Score: 120", animation.displayText("Score: 220", 20, 10.0));
        String middle = animation.displayText("Score: 220", 20, 20.0);
        int value = Integer.parseInt(middle.substring("Score: ".length()));
        assertTrue(value > 120 && value < 220, middle);
        assertEquals("Score: 220", animation.displayText("Score: 220", 20, 40.0));
    }

    @Test
    void countingEasesOutAndEndsOnTheExactTarget() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.displayText("N 0", 10, 0.0);
        animation.displayText("N 100", 10, 1.0);

        int halfway = Integer.parseInt(animation.displayText("N 100", 10, 6.0).substring(2));
        int early = Integer.parseInt(animation.displayText("N 100", 10, 2.0).substring(2));

        assertTrue(halfway > 50, "eased out, halfway at " + halfway);
        assertTrue(early < halfway);
        assertEquals("N 100", animation.displayText("N 100", 10, 11.0));
    }

    @Test
    void aNewValueDuringTheCountStartsFromTheCurrentDisplay() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.displayText("N 0", 10, 0.0);
        animation.displayText("N 100", 10, 1.0);
        int shown = Integer.parseInt(animation.displayText("N 100", 10, 5.0).substring(2));

        String restarted = animation.displayText("N 200", 10, 5.0);

        assertEquals("N " + shown, restarted);
        assertEquals("N 200", animation.displayText("N 200", 10, 15.0));
    }

    @Test
    void aChangeBeyondTheNumberJumpsAndTextWithoutNumbersIsUntouched() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.displayText("Score: 10", 20, 0.0);

        assertEquals("Points: 99", animation.displayText("Points: 99", 20, 1.0));
        assertEquals("No number", animation.displayText("No number", 20, 2.0));
        assertEquals("Now 5", animation.displayText("Now 5", 20, 3.0));
    }

    @Test
    void decimalsAndFormatCodesSurviveTheCount() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.displayText("§6Score: §f10.0", 20, 0.0);

        String middle = animation.displayText("§6Score: §f20.0", 20, 1.0 + 10.0);
        String early = animation.displayText("§6Score: §f20.0", 20, 1.0 + 10.0);

        assertTrue(middle.startsWith("§6Score: §f"), middle);
        assertTrue(early.matches("§6Score: §f\\d+\\.\\d"), early);
        assertEquals("§6Score: §f20.0", animation.displayText("§6Score: §f20.0", 20, 60.0));
    }

    @Test
    void withoutCountUpTicksTheTargetAppearsAtOnce() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.displayText("N 1", 0, 0.0);

        assertEquals("N 50", animation.displayText("N 50", 0, 1.0));
    }

    @Test
    void theTypewriterRevealsOverTimeAndThenShowsEverything() {
        PeakLabelAnimation animation = new PeakLabelAnimation();

        assertEquals(0, animation.revealBudget("Hello world", 20f, false, 0.0));
        assertEquals(5, animation.revealBudget("Hello world", 20f, false, 5.0));
        assertEquals(10, animation.revealBudget("Hello world", 20f, false, 10.0));
        assertEquals(-1, animation.revealBudget("Hello world", 20f, false, 11.0));
        assertEquals(-1, animation.revealBudget("Hello world", 20f, false, 100.0));
    }

    @Test
    void theTypewriterIsOffWithoutASpeed() {
        assertEquals(-1, new PeakLabelAnimation().revealBudget("abc", 0f, false, 0.0));
    }

    @Test
    void appendedTextContinuesAndOtherTextRestarts() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget("Hi", 20f, false, 0.0);
        assertEquals(-1, animation.revealBudget("Hi", 20f, false, 10.0));

        int appended = animation.revealBudget("Hi there", 20f, false, 10.0);
        assertEquals(2, appended);
        assertEquals(5, animation.revealBudget("Hi there", 20f, false, 13.0));

        assertEquals(0, animation.revealBudget("Bye", 20f, false, 14.0));
    }

    @Test
    void textThatWasNotFullyShownRestartsInsteadOfContinuing() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget("Hello", 20f, false, 0.0);
        animation.revealBudget("Hello", 20f, false, 2.0);

        assertEquals(0, animation.revealBudget("Hello world", 20f, false, 3.0));
    }

    @Test
    void aNumberOnlyChangeKeepsTheRevealWhenCountingUp() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget("Score: 10", 20f, true, 0.0);
        assertEquals(-1, animation.revealBudget("Score: 10", 20f, true, 10.0));

        assertEquals(-1, animation.revealBudget("Score: 25", 20f, true, 11.0));
    }

    @Test
    void restartingBeginsAgainFromNothing() {
        PeakLabelAnimation animation = new PeakLabelAnimation();
        animation.revealBudget("Hello", 20f, false, 0.0);
        assertEquals(-1, animation.revealBudget("Hello", 20f, false, 10.0));

        animation.restart();

        assertEquals(0, animation.revealBudget("Hello", 20f, false, 11.0));
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
        label.animation().revealBudget("Hello", 20f, false, 0.0);
        assertEquals(-1, label.animation().revealBudget("Hello", 20f, false, 10.0));

        label.applyPatch(PeakPatch.label().replay().build());

        assertTrue(label.isPatchKeyKnown("replay"));
        assertEquals(0, label.animation().revealBudget("Hello", 20f, false, 11.0));
        assertFalse(label.getText().isEmpty());
    }
}
