package net.gekidolukas.peakclient.neoforge.mixin;

import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class OffhandMixin {

    @Redirect(method = "renderItemHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack peakclient$maybeHide(Player player) {
        if (!PeakClientVanillaHudState.isEnabled(PeakVanillaElement.OFFHAND)) {
            return ItemStack.EMPTY;
        }
        return player.getOffhandItem();
    }
}
