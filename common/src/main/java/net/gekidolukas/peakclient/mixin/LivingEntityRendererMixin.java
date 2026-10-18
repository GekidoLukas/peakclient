package net.gekidolukas.peakclient.mixin;

import net.gekidolukas.peakclient.render.PeakEntityNameTagState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void peakclient$forceNameTag(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (PeakEntityNameTagState.isForced()) cir.setReturnValue(true);
    }
}
