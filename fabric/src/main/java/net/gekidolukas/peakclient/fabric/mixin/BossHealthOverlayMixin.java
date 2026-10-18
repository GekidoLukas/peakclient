package net.gekidolukas.peakclient.fabric.mixin;

import net.gekidolukas.peakclient.render.PeakVanillaHudRender;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void peakclient$begin(GuiGraphics graphics, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.BOSS_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void peakclient$end(GuiGraphics graphics, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.BOSS_BAR, graphics);
    }
}
