package net.gekidolukas.peakclient.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.debug.PeakDebugOverlayRenderer;
import net.gekidolukas.peakclient.debug.PeakDebugState;
import net.gekidolukas.peakclient.render.PeakUiRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class PeakDebugRenderMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void peakclient$renderOnTop(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);

        Minecraft mc = Minecraft.getInstance();
        PeakUiRenderer.renderAll(graphics, mc);
        if (PeakDebugState.isEnabled()) {
            PeakDebugOverlayRenderer.render(graphics, mc);
        }
    }
}
