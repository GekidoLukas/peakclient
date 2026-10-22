package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakItemSlot;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class PeakItemSlotRenderer implements PeakElementRenderer<PeakItemSlot> {
    @Override
    public boolean writesDepth() { return true; }

    @Override
    public PeakPixelSize measureContent(Font font, PeakItemSlot slot) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakItemSlot slot, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        boolean enabled = slot.getInteraction() == null || slot.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(slot);
        boolean hovered = enabled && PeakMenuInteractionState.isHovered(slot);
        int w = Math.max(width, 2);
        int h = Math.max(height, 2);

        boolean ring = focused && PeakMenuInteractionState.focusVisible(slot);
        PeakPaint border = ring ? PeakMenuInteractionState.focusColor(slot)
                : PeakMenuInteractionState.isInvalid(slot) ? PeakMenuInteractionState.invalidColor(slot)
                : PeakPaint.solid(slot.isSelected() ? 0xFFFFD700 : 0xFF555555);
        PeakPaintRenderer.fill(graphics, border, 0, 0, w, h);
        PeakImmediateRender.fill(graphics, 1, 1, w - 1, h - 1, 0xFF2B2B2B);
        if (slot.isSelected() && !ring) {
            PeakImmediateRender.fill(graphics, 1, 1, w - 1, h - 1, 0x30FFD700);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack stack = PeakItemElementRenderer.resolveItemStack(mc, slot.getSource());
            if (stack != null && !stack.isEmpty()) drawStack(mc, ctx, slot, stack, w, h);
        }

        if (hovered) PeakImmediateRender.fill(graphics, 1, 1, w - 1, h - 1, 0x40FFFFFF);
        if (!enabled) PeakImmediateRender.fill(graphics, 0, 0, w, h, 0x80202020);
    }

    private static void drawStack(Minecraft mc, PeakRenderContext ctx, PeakItemSlot slot, ItemStack stack, int w, int h) {
        GuiGraphics graphics = ctx.graphics();
        int size = Math.max(1, Math.min(w, h) - 2);
        float scale = size / 16f;

        ItemStack decorated = stack;
        if (!slot.isShowDurability() && stack.isDamageableItem()) {
            decorated = stack.copy();
            decorated.setDamageValue(0);
        }
        PeakStyledText customCount = PeakTextResolver.resolved(slot, slot.getCountText());
        boolean ownCount = slot.isShowCount() && !customCount.isEmpty();
        String countText = !slot.isShowCount() || ownCount ? "" : null;

        boolean offscreen = slot.getOpacity() < 0.999f;
        if (offscreen) PeakOffscreenOpacity.begin(mc, graphics);
        try {
            graphics.pose().pushPose();
            graphics.pose().translate((w - size) / 2f, (h - size) / 2f, 0f);
            graphics.pose().scale(scale, scale, 1f);
            graphics.renderItem(stack, 0, 0);
            graphics.renderItemDecorations(ctx.font(), decorated, 0, 0, countText);
            if (ownCount) {
                graphics.flush();
                graphics.pose().pushPose();
                graphics.pose().translate(0f, 0f, 200f);
                PeakImmediateRender.text(graphics, ctx.font(), customCount, 17 - customCount.width(ctx.font()), 9, 0xFFFFFFFF, true);
                graphics.pose().popPose();
            }
            graphics.pose().popPose();
        } finally {
            if (offscreen) PeakOffscreenOpacity.end(mc, graphics, slot.getOpacity());
        }
    }
}
