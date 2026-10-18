package net.gekidolukas.peakclient.neoforge;


import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.helper.PeakHud;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;
import net.gekidolukas.peakclient.network.*;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.client.*;
import net.gekidolukas.peakclient.ui.serialize.PeakGzip;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = PeakClientMod.MOD_ID)
public final class PeakClientNetworkingNeoForge {

    @SubscribeEvent
    static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PeakClientNeoForgeBootstrap.registerPayloads(event.registrar("1"));
    }

    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) { PeakClientNeoForgeBootstrap.onServerStarted(event.getServer()); }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) { PeakClientNeoForgeBootstrap.onServerStopped(); }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {PeakTaskRegistry.tickAll(); }

    @SubscribeEvent
    static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            PeakClientNeoForgeBootstrap.onPlayerLoggedOut(player);
        }
    }
}
