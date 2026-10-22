package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakItemSlot;
import net.gekidolukas.peakclient.ui.client.PeakComponentText;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakItemSlotTest {
    private static PeakItemSlot slot() {
        return new PeakItemSlot("ns:slot", null);
    }

    @Test
    void aSlotHasAnInventorySizedBoxAndAnInteraction() {
        PeakItemSlot slot = slot();

        assertEquals(18f, slot.getSize().getWidth().value());
        assertEquals(18f, slot.getSize().getHeight().value());
        assertNotNull(slot.getInteraction());
        assertTrue(slot.isShowCount());
        assertTrue(slot.isShowDurability());
        assertTrue(slot.isItemTooltip());
        assertFalse(slot.isSelected());
        assertEquals("", slot.getCountText().getString());
        assertEquals("item_slot", slot.elementType());
    }

    @Test
    void theBuildersAndShorthandsApply() {
        PeakItemSlot slot = slot().showCount(false).showDurability(false).countText("x3").selected(true).itemTooltip(false)
                .onClick(PeakAction.send("pick"))
                .onRightClick(PeakAction.send("inspect"))
                .onDoubleClick(PeakAction.send("buy"));

        assertFalse(slot.isShowCount());
        assertFalse(slot.isShowDurability());
        assertEquals("x3", slot.getCountText().getString());
        assertTrue(slot.isSelected());
        assertFalse(slot.isItemTooltip());
        assertEquals("pick", slot.actionsFor(PeakEvent.CLICK).get(0).target());
        assertEquals("inspect", slot.actionsFor(PeakEvent.RIGHT_CLICK).get(0).target());
        assertEquals("buy", slot.actionsFor(PeakEvent.DOUBLE_CLICK).get(0).target());
    }

    @Test
    void theSlotIsPatchable() {
        PeakItemSlot slot = slot();

        slot.applyPatch(PeakPatch.itemSlot().selected(true).countText("12g").showCount(false).build());

        assertTrue(slot.isSelected());
        assertEquals("12g", slot.getCountText().getString());
        assertFalse(slot.isShowCount());
        assertTrue(slot.isPatchKeyKnown("source"));
        assertTrue(slot.isPatchKeyKnown("itemTooltip"));
    }

    @Test
    void plainTextKeepsItsTextWithoutCodes() {
        assertEquals("Hello", PeakComponentText.toLegacy(Component.literal("Hello")));
    }

    @Test
    void namedColorsBecomeLegacyCodes() {
        assertEquals("§bDiamond", PeakComponentText.toLegacy(Component.literal("Diamond").withStyle(ChatFormatting.AQUA)));
        assertEquals("§cRed", PeakComponentText.toLegacy(Component.literal("Red").withStyle(ChatFormatting.RED)));
    }

    @Test
    void customColorsUseTheHexForm() {
        Component text = Component.literal("Custom").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x123ABC)));

        assertEquals("§x§1§2§3§a§b§cCustom", PeakComponentText.toLegacy(text));
    }

    @Test
    void formatsAreWrittenAfterTheColor() {
        Component text = Component.literal("Bold").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.ITALIC);

        assertEquals("§6§l§oBold", PeakComponentText.toLegacy(text));
    }

    @Test
    void segmentsAreSeparatedByAReset() {
        Component text = Component.literal("a").append(Component.literal("b").withStyle(ChatFormatting.RED));

        assertEquals("a§r§cb", PeakComponentText.toLegacy(text));
    }

    @Test
    void emptySegmentsAreSkipped() {
        Component text = Component.literal("").append(Component.literal("x").withStyle(ChatFormatting.GREEN));

        assertEquals("§ax", PeakComponentText.toLegacy(text));
    }
}
