package net.gekidolukas.peakclienttest.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.gekidolukas.peakclienttest.common.PeakElementTestSuite;
import net.gekidolukas.peakclienttest.common.PeakMenuTestSuite;
import net.gekidolukas.peakclienttest.common.PeakQuickTestRegistry;
import net.gekidolukas.peakclienttest.common.PeakTestSuite;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PeakTestCommand {

    private static final SuggestionProvider<CommandSourceStack> ELEMENT_NAMES = (context, builder) -> {
        for (var name : PeakElementTestSuite.availableTests()) builder.suggest(name);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> QUICK_NAMES = (context, builder) -> {
        for (var name : PeakQuickTestRegistry.availableTests()) builder.suggest(name);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> MENU_NAMES = (context, builder) -> {
        for (var name : PeakMenuTestSuite.availableTests()) builder.suggest(name);
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(literal("peaktest")
                .then(literal("run")
                        .then(literal("element")
                                .then(argument("name", word()).suggests(ELEMENT_NAMES)
                                        .executes(ctx -> runElement(ctx, -1))
                                        .then(argument("step", integer(1))
                                                .executes(ctx -> runElement(ctx, getInteger(ctx, "step") - 1)))))
                        .then(literal("menu")
                                .then(argument("name", word()).suggests(MENU_NAMES)
                                        .executes(ctx -> runMenu(ctx, -1))
                                        .then(argument("step", integer(1))
                                                .executes(ctx -> runMenu(ctx, getInteger(ctx, "step") - 1)))))
                        .then(literal("quick")
                                .then(argument("name", word()).suggests(QUICK_NAMES)
                                        .executes(ctx -> {
                                            String name = getString(ctx, "name");

                                            boolean found = PeakQuickTestRegistry.run(name, ctx.getSource().getPlayerOrException());
                                            if (!found) ctx.getSource().sendFailure(Component.literal("Unknown quick test: " + name));
                                            return 1;
                                        }))))
                .then(literal("run-all")
                        .then(literal("element").executes(ctx -> {
                            PeakElementTestSuite.runAll(ctx.getSource().getPlayerOrException());
                            return 1;
                        }))
                        .then(literal("menu").executes(ctx -> {
                            PeakMenuTestSuite.runAll(ctx.getSource().getPlayerOrException());
                            return 1;
                        })))
                .then(literal("clear-all").executes(ctx -> {
                    PeakTestSuite.clearAll(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(literal("list")
                        .then(literal("element").executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§7Elements (steps): ").append(ComponentUtils.formatList(PeakElementTestSuite.describeTests())), false);
                            return 1;
                        }))
                        .then(literal("menu").executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§7Menu tests: ").append(ComponentUtils.formatList(PeakMenuTestSuite.availableTests())), false);
                            return 1;
                        }))
                        .then(literal("quick").executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§7Quick tests: ").append(ComponentUtils.formatList(PeakQuickTestRegistry.availableTests())), false);
                            return 1;
                        }))
                ));
    }

    private static int runElement(CommandContext<CommandSourceStack> ctx, int step) throws CommandSyntaxException {
        String name = getString(ctx, "name");
        boolean found = PeakElementTestSuite.runSingle(name, ctx.getSource().getPlayerOrException(), step);
        if (!found) ctx.getSource().sendFailure(Component.literal("Unknown element test: " + name));
        return 1;
    }

    private static int runMenu(CommandContext<CommandSourceStack> ctx, int step) throws CommandSyntaxException {
        String name = getString(ctx, "name");
        boolean found = PeakMenuTestSuite.runSingle(name, ctx.getSource().getPlayerOrException(), step);
        if (!found) ctx.getSource().sendFailure(Component.literal("Unknown menu test: " + name));
        return 1;
    }
}
