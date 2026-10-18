package net.gekidolukas.peakclienttest.neoforge;

import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.neoforge.PeakClientNeoForgeBootstrap;
import net.gekidolukas.peakclienttest.common.PeakTestCommon;
import net.gekidolukas.peakclienttest.common.PeakTestSuite;
import net.gekidolukas.peakclienttest.common.command.PeakTestCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod("peaktest")
public final class PeakTestModNeoForge {
    public PeakTestModNeoForge() {
        PeakClientMod.init();

        PeakTestCommon.registerQuickTests();
    }

    @EventBusSubscriber(modid = "peaktest")
    public static final class Handlers {
        @SubscribeEvent
        static void registerPayloads(RegisterPayloadHandlersEvent event) {
            PeakClientNeoForgeBootstrap.registerPayloads(event.registrar("1"));
        }

        @SubscribeEvent
        static void onServerStarted(ServerStartedEvent event) { PeakClientNeoForgeBootstrap.onServerStarted(event.getServer()); }

        @SubscribeEvent
        static void onServerStopped(ServerStoppedEvent event) { PeakClientNeoForgeBootstrap.onServerStopped(); }

        @SubscribeEvent
        static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                PeakClientNeoForgeBootstrap.onPlayerLoggedOut(player);
            }
        }

        @SubscribeEvent
        static void onRegisterCommands(RegisterCommandsEvent event) {
            PeakTestCommand.register(event.getDispatcher(),event.getBuildContext(),event.getCommandSelection());
        }
    }
}