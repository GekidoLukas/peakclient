package net.gekidolukas.peakclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.gekidolukas.peakclient.render.PeakEntityNameTagState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void peakclient$suppressNameTag(Entity entity, Component component, PoseStack poseStack,
                                            MultiBufferSource bufferSource, int packedLight, float partialTick,
                                            CallbackInfo ci) {
        if (PeakEntityNameTagState.isSuppressed()) ci.cancel();
    }
}
