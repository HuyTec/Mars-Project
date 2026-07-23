package com.marsproject.terraformingmars.command;

import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public class MarsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mars")
                .then(Commands.literal("setprogress")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                                .executes(context -> {
                                    float value = FloatArgumentType.getFloat(context, "value");
                                    ServerLevel level = context.getSource().getLevel();

                                    MarsTerraformProgress.get(level).setProgress(value);

                                    PacketDistributor.sendToPlayersInDimension(level, new MarsEnvironmentSyncPayload(
                                            0, 0, 0, 0, 0, 0, false, value
                                    ));

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Da dat Mars terraform progress = " + value), true);
                                    return 1;
                                }))));
    }
}