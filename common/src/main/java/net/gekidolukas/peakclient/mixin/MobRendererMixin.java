package net.gekidolukas.peakclient.mixin;

import net.gekidolukas.peakclient.render.PeakEntityNameTagState;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobRenderer.class)
public abstract class MobRendererMixin {

    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void peakclient$forceNameTag(Mob mob, CallbackInfoReturnable<Boolean> cir) {
        if (PeakEntityNameTagState.isForced()) cir.setReturnValue(true);
    }
}
