package net.gekidolukas.peakclient.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.PeakClientNetworking;
import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.debug.PeakDebugKeybindHandler;
import net.gekidolukas.peakclient.debug.PeakDebugOverlayRenderer;
import net.gekidolukas.peakclient.debug.PeakDebugState;
import net.gekidolukas.peakclient.render.PeakUiRenderer;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;
import net.gekidolukas.peakclient.ui.client.PeakClientMenus;
import net.gekidolukas.peakclient.ui.client.PeakClientUiState;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import net.gekidolukas.peakclient.ui.client.PeakClientWaypointRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid = PeakClientMod.MOD_ID, value = Dist.CLIENT)
public final class PeakClientNetworkingNeoForgeClient {



    @SubscribeEvent
    static void onLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        PeakClientVanillaHudState.registerSupported(PeakVanillaHudNeoForgeOverlays.supportedElements());
        PeakClientNetworking.sendHello();
    }

    @SubscribeEvent
    static void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PeakClientUiState.clearAll();
        PeakClientVanillaHudState.clearAll();
        PeakClientCustomValues.clearAll();
        PeakClientWaypointRegistry.clearAll();
        PeakClientMenus.clearAll();
    }

    @SubscribeEvent
    static void onRenderGui(RenderGuiEvent.Post event) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);

        Minecraft mc = Minecraft.getInstance();
        PeakUiRenderer.renderAll(event.getGuiGraphics(), mc);
        if (PeakDebugState.isEnabled()) {
            PeakDebugOverlayRenderer.render(event.getGuiGraphics(), mc);
        }
    }
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        PeakClientTickCounter.tick();
    }
}
