package net.gekidolukas.peakclient.fabric.client;


import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.gekidolukas.peakclient.PeakClientNetworking;
import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.debug.PeakDebugKeybindHandler;
import net.gekidolukas.peakclient.network.PeakPayloads;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;
import net.gekidolukas.peakclient.ui.client.PeakClientMenus;
import net.gekidolukas.peakclient.ui.client.PeakClientUiState;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import net.gekidolukas.peakclient.ui.client.PeakClientWaypointRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public final class PeakClientModFabricClient implements ClientModInitializer {

    private static final KeyMapping DEBUG_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.peakclient.toggle_debug", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_0, "category.peakclient"));


    @Override
    public void onInitializeClient() {
        PeakClientVanillaHudState.registerSupported(PeakVanillaElement.ALL);

        PeakPayloads.forEachClientbound(new PeakPayloads.ClientboundVisitor() {
            @Override
            public <T extends CustomPacketPayload> void visit(CustomPacketPayload.Type<T> type,
                                                              StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                              Consumer<T> handler) {
                ClientPlayNetworking.registerGlobalReceiver(type,
                        (payload, context) -> context.client().execute(() -> handler.accept(payload)));
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> PeakClientNetworking.sendHello());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PeakClientUiState.clearAll();
            PeakClientVanillaHudState.clearAll();
            PeakClientCustomValues.clearAll();
            PeakClientWaypointRegistry.clearAll();
            PeakClientMenus.clearAll();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> PeakClientTickCounter.tick());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.screen == null && InputConstants.isKeyDown(client.getWindow().getWindow(), GLFW.GLFW_KEY_F3)
                    && DEBUG_KEY.consumeClick()) {
                PeakDebugKeybindHandler.onDebugKeyPressed();
            }
        });
    }
}
