package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakItemElement;
import net.gekidolukas.peakclient.ui.PeakItemSource;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class PeakItemElementRenderer implements PeakElementRenderer<PeakItemElement> {
    @Override
    public boolean writesDepth() { return true; }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakItemElement element, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack stack = resolveItemStack(mc, element.getSource());
        if (stack == null || stack.isEmpty()) return;
        int size = Math.min(width, height);
        float itemScale = size / 16f;
        ctx.graphics().pose().pushPose();
        ctx.graphics().pose().translate((width - size) / 2f, (height - size) / 2f, 0f);
        ctx.graphics().pose().scale(itemScale, itemScale, 1f);
        ctx.graphics().renderItem(stack, 0, 0);
        ctx.graphics().renderItemDecorations(ctx.font(), stack, 0, 0);
        ctx.graphics().pose().popPose();
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakItemElement element) {
        return PeakPixelSize.ZERO;
    }

    public static ItemStack resolveItemStack(Minecraft mc, PeakItemSource source) {
        return switch (source.kind()) {
            case FIXED -> source.itemStack();
            case EQUIPMENT_SLOT -> mc.player.getItemBySlot(source.equipmentSlot());
            case INVENTORY_SLOT -> mc.player.getInventory().getItem(source.inventorySlot());
            case TARGETED_BLOCK -> {
                if (!(mc.hitResult instanceof BlockHitResult blockHit)
                        || mc.hitResult.getType() == HitResult.Type.MISS) {
                    yield ItemStack.EMPTY;
                }
                var state = mc.level.getBlockState(blockHit.getBlockPos());
                var item = Item.byBlock(state.getBlock());
                yield item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
            }
        };
    }
}
