package net.gekidolukas.peakclient.mixin;

import net.gekidolukas.peakclient.render.PeakCameraState;
import net.gekidolukas.peakclient.render.PeakDepthCapture;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void peakclient$captureCamera(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
                                          GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f viewRotation,
                                          Matrix4f projection, CallbackInfo ci) {
        PeakCameraState.capture(camera, viewRotation, projection, deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void peakclient$captureDepth(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
                                         GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f viewRotation,
                                         Matrix4f projection, CallbackInfo ci) {
        PeakDepthCapture.capture(Minecraft.getInstance());
    }
}
