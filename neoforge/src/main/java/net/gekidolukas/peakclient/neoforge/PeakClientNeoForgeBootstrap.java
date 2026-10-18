package net.gekidolukas.peakclient.neoforge;

import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.network.PeakPayloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PeakClientNeoForgeBootstrap {

    private PeakClientNeoForgeBootstrap() {}


    private static volatile boolean payloadsRegistered = false;
    private static final boolean OPTIONAL_CHANNELS = false;

    public static void registerPayloads(PayloadRegistrar registrar) {
        if (payloadsRegistered) return;
        payloadsRegistered = true;
        PayloadRegistrar target = OPTIONAL_CHANNELS ? registrar.optional() : registrar;

        PeakPayloads.forEachClientbound(new PeakPayloads.ClientboundVisitor() {
            @Override
            public <T extends CustomPacketPayload> void visit(CustomPacketPayload.Type<T> type,
                                                              StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                              Consumer<T> handler) {
                target.playToClient(type, codec,
                        (payload, context) -> context.enqueueWork(() -> handler.accept(payload)));
            }
        });

        PeakPayloads.forEachServerbound(new PeakPayloads.ServerboundVisitor() {
            @Override
            public <T extends CustomPacketPayload> void visit(CustomPacketPayload.Type<T> type,
                                                              StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                              BiConsumer<ServerPlayer, T> handler) {
                target.playToServer(type, codec, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) handler.accept(player, payload);
                }));
            }
        });
    }

    public static void onServerStarted(MinecraftServer server) { PeakClientServer.set(server); }
    public static void onServerStopped() { PeakClientServer.clear(); }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        PeakClientMod.onPlayerDisconnected(player.getUUID());
    }
}
