package net.gekidolukas.peakclient.fabric;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;
import net.gekidolukas.peakclient.network.PeakPayloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public final class PeakClientFabricBootstrap {
    private static boolean initialized = false;

    private PeakClientFabricBootstrap() {}

    public static synchronized void registerCommon() {
        if (initialized) return;
        initialized = true;

        PeakPayloads.forEachClientbound(new PeakPayloads.ClientboundVisitor() {
            @Override
            public <T extends CustomPacketPayload> void visit(CustomPacketPayload.Type<T> type,
                                                              StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                              Consumer<T> handler) {
                PayloadTypeRegistry.playS2C().register(type, codec);
            }
        });

        PeakPayloads.forEachServerbound(new PeakPayloads.ServerboundVisitor() {
            @Override
            public <T extends CustomPacketPayload> void visit(CustomPacketPayload.Type<T> type,
                                                              StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                              BiConsumer<ServerPlayer, T> handler) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(context.player(), payload));
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(PeakClientServer::set);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PeakClientServer.clear());
        ServerTickEvents.END_SERVER_TICK.register(server -> PeakTaskRegistry.tickAll());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PeakClientMod.onPlayerDisconnected(handler.getPlayer().getUUID()));
    }
}
