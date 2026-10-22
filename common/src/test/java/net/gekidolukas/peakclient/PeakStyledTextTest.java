package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakPlaceholderResolver;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakTextWrap;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakStyledTextTest {

    @AfterEach
    void clearValues() {
        PeakClientCustomValues.clearAll();
    }

    private static boolean explicitlyNotBold(Style style) {
        return !style.isBold() && !style.applyTo(Style.EMPTY.withBold(true)).isBold();
    }

    @Test
    void legacyColorCodesBecomeStylesAndAreNotPartOfThePlainText() {
        PeakStyledText text = PeakStyledText.ofLegacy("a§cb§lc§rd");

        assertEquals("abcd", text.plain());
        assertNull(text.styleAt(0).getColor());
        assertEquals(ChatFormatting.RED.getColor(), text.styleAt(1).getColor().getValue());
        assertFalse(text.styleAt(1).isBold());
        assertTrue(text.styleAt(2).isBold());
        assertEquals(ChatFormatting.RED.getColor(), text.styleAt(2).getColor().getValue());
        assertEquals(Style.EMPTY, text.styleAt(3));
    }

    @Test
    void aColorCodeResetsEarlierFormats() {
        PeakStyledText text = PeakStyledText.ofLegacy("§l§nA§aB");

        assertTrue(text.styleAt(0).isBold());
        assertTrue(text.styleAt(0).isUnderlined());
        assertFalse(text.styleAt(1).isBold());
        assertFalse(text.styleAt(1).isUnderlined());
        assertEquals(ChatFormatting.GREEN.getColor(), text.styleAt(1).getColor().getValue());
    }

    @Test
    void hexSequencesAreRead() {
        PeakStyledText text = PeakStyledText.ofLegacy("§x§3§3§6§6§9§9Hi");

        assertEquals("Hi", text.plain());
        assertEquals(0x336699, text.styleAt(0).getColor().getValue());
        assertEquals("", PeakStyledText.ofLegacy("§x§3").plain());
    }

    @Test
    void unknownCodesAreDropped() {
        assertEquals("ab", PeakStyledText.ofLegacy("a§zb").plain());
    }

    @Test
    void aComponentTreeKeepsItsStyles() {
        Component component = Component.literal("red ").withStyle(ChatFormatting.RED)
                .append(Component.literal("bold").withStyle(ChatFormatting.BOLD));

        PeakStyledText text = PeakStyledText.of(component);

        assertEquals("red bold", text.plain());
        assertEquals(ChatFormatting.RED.getColor(), text.styleAt(0).getColor().getValue());
        assertTrue(text.styleAt(4).isBold());
        assertEquals(ChatFormatting.RED.getColor(), text.styleAt(4).getColor().getValue());
    }

    @Test
    void anExplicitFalseOverridesTheParent() {
        Component component = Component.literal("a").withStyle(ChatFormatting.BOLD)
                .append(Component.literal("b").withStyle(Style.EMPTY.withBold(false)));

        PeakStyledText text = PeakStyledText.of(component);

        assertTrue(text.styleAt(0).isBold());
        assertTrue(explicitlyNotBold(text.styleAt(1)));
    }

    @Test
    void legacyCodesInsideAComponentStartFromTheComponentStyle() {
        Component component = Component.literal("§lX§rY").withStyle(ChatFormatting.GOLD);

        PeakStyledText text = PeakStyledText.of(component);

        assertEquals("XY", text.plain());
        assertTrue(text.styleAt(0).isBold());
        assertEquals(ChatFormatting.GOLD.getColor(), text.styleAt(0).getColor().getValue());
        assertFalse(text.styleAt(1).isBold());
        assertEquals(ChatFormatting.GOLD.getColor(), text.styleAt(1).getColor().getValue());
    }

    @Test
    void theFontIsKept() {
        ResourceLocation font = ResourceLocation.parse("minecraft:alt");
        PeakStyledText text = PeakStyledText.of(Component.literal("x").withStyle(Style.EMPTY.withFont(font)));

        assertEquals(font, text.styleAt(0).getFont());
    }

    @Test
    void clickEventsAndInsertionsAreRemoved() {
        Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://example.org"))
                .withInsertion("ins").withColor(ChatFormatting.AQUA);

        PeakStyledText text = PeakStyledText.of(Component.literal("x").withStyle(style));

        assertNull(text.styleAt(0).getClickEvent());
        assertNull(text.styleAt(0).getInsertion());
        assertEquals(ChatFormatting.AQUA.getColor(), text.styleAt(0).getColor().getValue());
    }

    @Test
    void slicingAndConcatenatingKeepTheStylesPerCharacter() {
        PeakStyledText text = PeakStyledText.ofLegacy("§cab§9cd");

        PeakStyledText head = text.slice(0, 2);
        PeakStyledText tail = text.slice(2, 4);

        assertEquals("ab", head.plain());
        assertEquals(text, head.concat(tail));
        assertSame(text, text.slice(0, 4));
        assertEquals(PeakStyledText.EMPTY, text.slice(3, 1));
    }

    @Test
    void linesAreSplitAtNewlines() {
        List<PeakStyledText> lines = PeakStyledText.ofLegacy("a\n§cb\n").splitLines();

        assertEquals(3, lines.size());
        assertEquals("a", lines.get(0).plain());
        assertEquals(ChatFormatting.RED.getColor(), lines.get(1).styleAt(0).getColor().getValue());
        assertEquals("", lines.get(2).plain());
    }

    @Test
    void placeholdersTakeTheStyleOfTheirFirstCharacter() {
        PeakClientCustomValues.set("test", "name", "Bob");
        PeakStyledText text = PeakStyledText.ofLegacy("Hi §a{test:name}!");

        PeakStyledText resolved = PeakPlaceholderResolver.resolve(text);

        assertEquals("Hi Bob!", resolved.plain());
        assertEquals(ChatFormatting.GREEN.getColor(), resolved.styleAt(3).getColor().getValue());
        assertEquals(ChatFormatting.GREEN.getColor(), resolved.styleAt(5).getColor().getValue());
        assertEquals(ChatFormatting.GREEN.getColor(), resolved.styleAt(6).getColor().getValue());
        assertNull(resolved.styleAt(0).getColor());
    }

    @Test
    void aPlaceholderResultMayCarryItsOwnColors() {
        PeakClientCustomValues.set("test", "team", "§cRed§rGray");

        PeakStyledText resolved = PeakPlaceholderResolver.resolve(PeakStyledText.ofLegacy("{test:team}"));

        assertEquals("RedGray", resolved.plain());
        assertEquals(ChatFormatting.RED.getColor(), resolved.styleAt(0).getColor().getValue());
        assertNull(resolved.styleAt(3).getColor());
    }

    @Test
    void textWithoutPlaceholdersIsReturnedAsIs() {
        PeakStyledText text = PeakStyledText.ofLegacy("plain");
        assertSame(text, PeakPlaceholderResolver.resolve(text));
    }

    @Test
    void wrappingKeepsTheStyleOverLineBreaks() {
        PeakStyledText text = PeakStyledText.ofLegacy("§cone two three");

        List<PeakStyledText> lines = PeakTextWrap.layout(line -> line.length(), text, 8);

        assertEquals(List.of("one two", "three"), lines.stream().map(PeakStyledText::plain).toList());
        assertEquals(ChatFormatting.RED.getColor(), lines.get(1).styleAt(0).getColor().getValue());
    }

    @Test
    void wrappingSplitsParagraphsAndKeepsLongWordsWhole() {
        PeakStyledText text = PeakStyledText.ofLegacy("a\nsupercalifragilistic b");

        List<PeakStyledText> lines = PeakTextWrap.layout(line -> line.length(), text, 5);

        assertEquals(List.of("a", "supercalifragilistic", "b"), lines.stream().map(PeakStyledText::plain).toList());
    }

    @Test
    void withoutAWrapWidthOnlyNewlinesBreak() {
        List<PeakStyledText> lines = PeakTextWrap.layout(line -> line.length(), PeakStyledText.ofLegacy("a b\nc"), 0);
        assertEquals(List.of("a b", "c"), lines.stream().map(PeakStyledText::plain).toList());
    }
}
