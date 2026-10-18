package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.client.PeakRichText;
import net.gekidolukas.peakclient.ui.client.PeakTextLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakRichTextTest {
    private static final PeakTextLayout.Measure MONO = (from, to) -> (to - from) * 6;

    private static PeakTextLayout layout(String plain, int maxWidth) {
        return PeakTextLayout.build(PeakRichText.parse(plain), maxWidth, MONO);
    }

    @Test
    void plainTextRoundTrips() {
        PeakRichText text = PeakRichText.parse("Hello world");

        assertEquals("Hello world", text.plain());
        assertEquals("Hello world", text.serialize());
        assertEquals(PeakRichText.Style.NONE, text.styleAt(3));
    }

    @Test
    void colorsAndFlagsAreParsedPerCharacter() {
        PeakRichText text = PeakRichText.parse("§cRed §lBold§r plain");

        assertEquals("Red Bold plain", text.plain());
        assertEquals(0xFF5555, text.styleAt(0).rgb());
        assertEquals(0, text.styleAt(0).flags());
        assertTrue(text.styleAt(4).has(PeakRichText.BOLD));
        assertEquals(0xFF5555, text.styleAt(4).rgb());
        assertEquals(PeakRichText.Style.NONE, text.styleAt(9));
    }

    @Test
    void aColorCodeClearsEarlierFlags() {
        PeakRichText text = PeakRichText.parse("§l§cA");

        assertEquals(0, text.styleAt(0).flags());
        assertEquals(0xFF5555, text.styleAt(0).rgb());
    }

    @Test
    void serializeAndParseAreStable() {
        String source = "§cRed §lbold §nand underlined§r then §ogreen§r§a text";
        PeakRichText first = PeakRichText.parse(source);
        PeakRichText second = PeakRichText.parse(first.serialize());

        assertEquals(first.plain(), second.plain());
        for (int i = 0; i < first.length(); i++) assertEquals(first.styleAt(i), second.styleAt(i), "at " + i);
        assertEquals(first.serialize(), second.serialize());
    }

    @Test
    void hexColorsSurviveTheRoundTrip() {
        PeakRichText text = PeakRichText.parse("§x§1§2§3§4§5§6Hi");

        assertEquals("Hi", text.plain());
        assertEquals(0x123456, text.styleAt(0).rgb());
        assertEquals("§x§1§2§3§4§5§6Hi", text.serialize());
    }

    @Test
    void legacyColorsAreWrittenAsShortCodes() {
        PeakRichText text = PeakRichText.parse("§x§F§F§5§5§5§5R");

        assertEquals("§cR", text.serialize());
    }

    @Test
    void unknownCodesAreDropped() {
        assertEquals("AB", PeakRichText.parse("§zA§qB").plain());
        assertEquals("trailing", PeakRichText.parse("trailing§").plain().replace("§", ""));
    }

    @Test
    void serializingARangeStartsFromTheResetState() {
        PeakRichText text = PeakRichText.parse("§cabc");

        assertEquals("§cbc", text.serialize(1, 3));
        assertEquals("b", PeakRichText.parse("§lab").plain(1, 2));
    }

    @Test
    void runsMergeEqualStyles() {
        PeakRichText text = PeakRichText.parse("ab§lcd§refg");

        List<PeakRichText.Run> runs = text.runs(0, text.length());

        assertEquals(3, runs.size());
        assertEquals(0, runs.get(0).start());
        assertEquals(2, runs.get(0).end());
        assertTrue(runs.get(1).style().has(PeakRichText.BOLD));
        assertEquals(7, runs.get(2).end());
    }

    @Test
    void textWrapsAtSpacesAndKeepsTheSpaceOnTheLine() {
        PeakTextLayout layout = layout("hello world foo", 30);

        assertEquals(3, layout.lineCount());
        assertEquals(new PeakTextLayout.Line(0, 6), layout.line(0));
        assertEquals(new PeakTextLayout.Line(6, 12), layout.line(1));
        assertEquals(new PeakTextLayout.Line(12, 15), layout.line(2));
    }

    @Test
    void longWordsBreakByCharacters() {
        PeakTextLayout layout = layout("abcdefghij", 30);

        assertEquals(2, layout.lineCount());
        assertEquals(new PeakTextLayout.Line(0, 5), layout.line(0));
        assertEquals(new PeakTextLayout.Line(5, 10), layout.line(1));
    }

    @Test
    void hardNewlinesStartNewLines() {
        PeakTextLayout layout = layout("ab\ncd\n", 0);

        assertEquals(3, layout.lineCount());
        assertEquals(new PeakTextLayout.Line(0, 2), layout.line(0));
        assertEquals(new PeakTextLayout.Line(3, 5), layout.line(1));
        assertEquals(new PeakTextLayout.Line(6, 6), layout.line(2));
    }

    @Test
    void withoutAWrapWidthLongLinesStayWhole() {
        PeakTextLayout layout = layout("a very long line without any break at all", 0);

        assertEquals(1, layout.lineCount());
    }

    @Test
    void emptyTextHasOneEmptyLine() {
        PeakTextLayout layout = layout("", 30);

        assertEquals(1, layout.lineCount());
        assertEquals(new PeakTextLayout.Line(0, 0), layout.line(0));
        assertEquals(0, layout.lineOf(0));
    }

    @Test
    void indicesMapToLinesAndPixels() {
        PeakTextLayout layout = layout("hello world foo", 30);

        assertEquals(0, layout.lineOf(0));
        assertEquals(0, layout.lineOf(5));
        assertEquals(1, layout.lineOf(6));
        assertEquals(2, layout.lineOf(15));
        assertEquals(12, layout.xOf(8, MONO));
        assertEquals(0, layout.xOf(6, MONO));
    }

    @Test
    void pixelsMapToTheClosestBoundary() {
        PeakTextLayout layout = layout("hello world foo", 30);

        assertEquals(0, layout.indexAt(0, -5, MONO));
        assertEquals(2, layout.indexAt(0, 14, MONO));
        assertEquals(3, layout.indexAt(0, 16, MONO));
        assertEquals(8, layout.indexAt(1, 12, MONO));
        assertEquals(12, layout.indexAt(1, 999, MONO));
    }
}
