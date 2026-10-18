package net.gekidolukas.peakclient.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.debug.PeakDebugKeybindHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = PeakClientMod.MOD_ID, value = Dist.CLIENT)
public final class PeakDebugKeybindNeoForge {

    @SubscribeEvent
    static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (event.getKey() != GLFW.GLFW_KEY_0) return;

        Minecraft mc = Minecraft.getInstance();
        if (!InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_F3)) return;

        PeakDebugKeybindHandler.onDebugKeyPressed();
    }
}
