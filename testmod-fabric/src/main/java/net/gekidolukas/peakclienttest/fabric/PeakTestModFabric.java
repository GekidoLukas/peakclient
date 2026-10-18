package net.gekidolukas.peakclienttest.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.fabric.PeakClientFabricBootstrap;
import net.gekidolukas.peakclienttest.common.PeakTestCommon;
import net.gekidolukas.peakclienttest.common.PeakTestSuite;
import net.gekidolukas.peakclienttest.common.command.PeakTestCommand;
import net.minecraft.network.chat.Component;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class PeakTestModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PeakClientMod.init();
        PeakClientFabricBootstrap.registerCommon();
        PeakTestCommon.registerQuickTests();
        CommandRegistrationCallback.EVENT.register(PeakTestCommand::register);
    }
}