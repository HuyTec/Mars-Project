package com.marsproject.terraformingmars.command;

import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import com.marsproject.terraformingmars.weather.MarsWeatherData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MarsCommands {
    private MarsCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mars")
                .then(Commands.literal("setprogress")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 1.0F))
                                .executes(context -> setProgress(
                                        context.getSource(),
                                        FloatArgumentType.getFloat(context, "value")
                                ))))
                .then(Commands.literal("weather")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("dust")
                                .executes(context -> startDustStorm(context.getSource(), 2))
                                .then(Commands.argument("intensity", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> startDustStorm(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "intensity")
                                        ))))
                        .then(Commands.literal("dry_ice")
                                .executes(context -> startDryIceStorm(context.getSource(), 2))
                                .then(Commands.argument("intensity", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> startDryIceStorm(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "intensity")
                                        ))))
                        .then(Commands.literal("clear")
                                .executes(context -> clearWeather(context.getSource())))));
    }

    private static int setProgress(CommandSourceStack source, float value) {
        ServerLevel level = source.getLevel();
        MarsTerraformProgress.get(level).setProgress(value);
        MarsEnvironmentStage stage = MarsEnvironmentManager.resolve(value);
        double oxygen = stage.atmosphereComposition().getOrDefault("oxygen", 0.0);

        PacketDistributor.sendToAllPlayers(new MarsEnvironmentSyncPayload(
                stage.radiation(),
                stage.atmospherePressurePercent(),
                oxygen,
                stage.temperatureCelsius(),
                stage.waterPercent(),
                stage.biologyPercent(),
                MarsEnvironmentManager.canLive(stage),
                stage.progress()
        ));

        source.sendSuccess(() ->
                Component.literal("Da dat Mars terraform progress = " + value), true);
        return 1;
    }

    private static int startDustStorm(CommandSourceStack source, int intensity) {
        ServerLevel marsLevel = source.getServer().getLevel(TeleportHelper.MARS_LEVEL_KEY);
        if (marsLevel == null) {
            source.sendFailure(Component.literal("Mars dimension chua duoc nap"));
            return 0;
        }

        MarsWeatherData weather = MarsWeatherData.get(marsLevel);
        weather.startDustStorm(marsLevel, intensity);
        PacketDistributor.sendToPlayersInDimension(marsLevel, weather.toPayload());
        source.sendSuccess(() ->
                Component.literal("Da bat bao bui Mars cap " + intensity), true);
        return 1;
    }

    private static int clearWeather(CommandSourceStack source) {
        ServerLevel marsLevel = source.getServer().getLevel(TeleportHelper.MARS_LEVEL_KEY);
        if (marsLevel == null) {
            source.sendFailure(Component.literal("Mars dimension chua duoc nap"));
            return 0;
        }

        MarsWeatherData weather = MarsWeatherData.get(marsLevel);
        weather.clear(marsLevel);
        PacketDistributor.sendToPlayersInDimension(marsLevel, weather.toPayload());
        source.sendSuccess(() -> Component.literal("Da tat thoi tiet bao Mars"), true);
        return 1;
    }

    private static int startDryIceStorm(CommandSourceStack source, int intensity) {
        ServerLevel marsLevel = source.getServer().getLevel(TeleportHelper.MARS_LEVEL_KEY);
        if (marsLevel == null) {
            source.sendFailure(Component.literal("Mars dimension chua duoc nap"));
            return 0;
        }

        MarsWeatherData weather = MarsWeatherData.get(marsLevel);
        weather.startDryIceStorm(marsLevel, intensity);
        PacketDistributor.sendToPlayersInDimension(marsLevel, weather.toPayload());
        source.sendSuccess(() ->
                Component.literal("Da bat bao tuyet CO2 cap " + intensity
                        + " trong cryotic_wastes"), true);
        return 1;
    }
}
