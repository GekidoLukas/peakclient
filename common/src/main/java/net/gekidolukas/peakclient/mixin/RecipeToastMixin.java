package net.gekidolukas.peakclient.mixin;

import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeToast.class)
public abstract class RecipeToastMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void peakclient$render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l, CallbackInfoReturnable<Toast.Visibility> cir) {
        if (!PeakClientVanillaHudState.isEnabled(PeakVanillaElement.RECIPE_TOAST)) {
            cir.setReturnValue(Toast.Visibility.HIDE);
        }
    }
}
