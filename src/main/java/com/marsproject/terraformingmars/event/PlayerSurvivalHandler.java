package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.network.SurvivalSyncPayload;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import com.marsproject.terraformingmars.survival.PlayerSurvivalData;
import com.marsproject.terraformingmars.survival.SpaceSuitService;
import com.marsproject.terraformingmars.weather.MarsWeatherBiomes;
import com.marsproject.terraformingmars.weather.MarsWeatherData;
import com.marsproject.terraformingmars.atmosphere.RoomClimateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public final class PlayerSurvivalHandler {
    private static final int UPDATE_INTERVAL_TICKS = 20;
    private static final float BASE_THIRST_LOSS = 0.025F;
    private static final double MIN_TEMPERATURE_STEP = 0.005;
    private static final double MAX_TEMPERATURE_STEP = 0.35;
    private static final double MAX_HEATING_STEP = 0.35;
    private static final double TEMPERATURE_RESPONSE = 0.001;
    private static final int LAVA_HEAT_RADIUS = 4;
    private static final double COLD_DAMAGE_TEMPERATURE = 30.0;
    private static final double HOT_DAMAGE_TEMPERATURE = 40.0;
    private static final int TEMPERATURE_REFERENCE_HEIGHT = 64;
    private static final double ALTITUDE_COOLING_PER_BLOCK = 0.06;

    private PlayerSurvivalHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % UPDATE_INTERVAL_TICKS != 0) return;

        if (!player.isCreative() && !player.isSpectator()) {
            updateThirst(player);
            updateBodyTemperature(player);
            applySurvivalDamage(player);
        } else {
            PlayerSurvivalData.setThirst(player, PlayerSurvivalData.MAX_THIRST);
            PlayerSurvivalData.setBodyTemperature(
                    player, PlayerSurvivalData.NORMAL_BODY_TEMPERATURE);
        }

        sync(player);
    }

    @SubscribeEvent
    public static void onItemUsed(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PotionContents potion = event.getItem().get(DataComponents.POTION_CONTENTS);
        if (potion != null && potion.is(Potions.WATER)) {
            PlayerSurvivalData.restoreThirst(player, 6.0F);
            sync(player);
        } else if (event.getItem().is(Items.MILK_BUCKET)) {
            PlayerSurvivalData.restoreThirst(player, 4.0F);
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            PlayerSurvivalData.reset(event.getEntity());
        } else {
            PlayerSurvivalData.copy(event.getOriginal(), event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    private static void updateThirst(ServerPlayer player) {
        double temperature = PlayerSurvivalData.getBodyTemperature(player);
        double heatStress = Math.max(0.0, temperature - PlayerSurvivalData.NORMAL_BODY_TEMPERATURE);
        double coldStress = Math.max(0.0, PlayerSurvivalData.NORMAL_BODY_TEMPERATURE - temperature);

        float loss = BASE_THIRST_LOSS
                + (float) (heatStress * 0.04)
                + (float) (coldStress * 0.01);
        if (player.isSprinting()) {
            loss += 0.025F;
        }
        PlayerSurvivalData.setThirst(player, PlayerSurvivalData.getThirst(player) - loss);
    }

    private static void updateBodyTemperature(ServerPlayer player) {
        double current = PlayerSurvivalData.getBodyTemperature(player);
        ThermalEnvironment environment = resolveThermalEnvironment(player);
        double targetDifference = environment.targetBodyTemperature() - current;
        if (Math.abs(targetDifference) < 0.001) {
            return;
        }

        // A larger gap between ambient and body temperature transfers heat faster.
        // This is the hook where Space Suit insulation can reduce the step later.
        double ambientDifference = Math.abs(environment.ambientTemperature() - current);
        double insulationMultiplier = 1.0 - SpaceSuitService.thermalInsulation(player);
        double step = Mth.clamp(
                ambientDifference * TEMPERATURE_RESPONSE
                        * environment.exposureMultiplier() * insulationMultiplier,
                MIN_TEMPERATURE_STEP,
                MAX_TEMPERATURE_STEP);
        if (targetDifference > 0.0) {
            step = Math.min(MAX_HEATING_STEP, step * 2.0);
        }
        double change = Mth.clamp(targetDifference, -step, step);
        PlayerSurvivalData.setBodyTemperature(player, current + change);
    }

    private static ThermalEnvironment resolveThermalEnvironment(ServerPlayer player) {
        if (player.isOnFire()) {
            return new ThermalEnvironment(600.0, 42.5, 4.0);
        }

        double lavaProximity = nearestLavaProximity(player);
        if (lavaProximity > 0.0) {
            double ambient = 180.0 + 270.0 * lavaProximity;
            double target = 39.0 + 3.0 * lavaProximity;
            return new ThermalEnvironment(ambient, target, 1.5 + 2.5 * lavaProximity);
        }

        if (player.isInWaterRainOrBubble()) {
            return new ThermalEnvironment(10.0, 34.0, 1.5);
        }
        ServerLevel serverLevel = (ServerLevel) player.level();
        java.util.OptionalDouble roomTemperature = RoomClimateManager.temperatureAt(
                serverLevel, player.blockPosition());
        if (roomTemperature.isPresent()) {
            double indoor = roomTemperature.getAsDouble();
            double target = Mth.clamp(37.0 + (indoor - 20.0) * 0.04, 35.0, 38.5);
            return new ThermalEnvironment(indoor, target, 1.35);
        }
        double ambient = ambientTemperature(serverLevel, player.blockPosition());
        double target = Mth.clamp(37.0 + (ambient - 20.0) * 0.08, 29.0, 40.5);
        double exposure = coldExposureMultiplier(serverLevel, player.blockPosition(), ambient);
        return new ThermalEnvironment(ambient, target, exposure);
    }

    public static double ambientTemperature(ServerLevel level, BlockPos pos) {
        if (level.dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) {
            float progress = MarsTerraformProgress.get(level).getProgress();
            double stageTemperature = MarsEnvironmentManager.resolve(progress).temperatureCelsius();
            double biomeOffset = (level.getBiome(pos).value().getBaseTemperature() - 0.4) * 12.0;
            double dryIceStormCooling = 0.0;
            MarsWeatherData weather = MarsWeatherData.get(level);
            if (weather.isDryIceStorm() && level.canSeeSky(pos.above())) {
                dryIceStormCooling = weather.intensity() * 5.0;
            }
            return stageTemperature + biomeOffset + altitudeCooling(pos) - dryIceStormCooling;
        }
        if (level.dimension().equals(Level.NETHER)) return 450.0;
        if (level.dimension().equals(Level.END)) return -20.0 + altitudeCooling(pos);
        return 15.0
                + (level.getBiome(pos).value().getBaseTemperature() - 0.8) * 20.0
                + altitudeCooling(pos);
    }

    private static double altitudeCooling(BlockPos pos) {
        return -(pos.getY() - TEMPERATURE_REFERENCE_HEIGHT) * ALTITUDE_COOLING_PER_BLOCK;
    }

    private static double coldExposureMultiplier(ServerLevel level, BlockPos pos, double ambient) {
        if (level.dimension().equals(Level.NETHER)) {
            return 1.5;
        }

        double exposure = ambient < 0.0 ? 1.35 : 1.0;
        double biomeTemperature = level.getBiome(pos).value().getBaseTemperature();
        if (biomeTemperature <= 0.15 || MarsWeatherBiomes.isCryotic(level, pos)) {
            exposure += 0.5;
        }
        if (pos.getY() > 96) {
            exposure += Math.min(0.75, (pos.getY() - 96) / 128.0);
        }
        if (level.dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) {
            MarsWeatherData weather = MarsWeatherData.get(level);
            if (weather.isDryIceStorm() && level.canSeeSky(pos.above())) {
                exposure += weather.intensity() * 0.4;
            }
        }
        return exposure;
    }

    private static double nearestLavaProximity(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-LAVA_HEAT_RADIUS, -LAVA_HEAT_RADIUS, -LAVA_HEAT_RADIUS),
                center.offset(LAVA_HEAT_RADIUS, LAVA_HEAT_RADIUS, LAVA_HEAT_RADIUS))) {
            if (player.level().getFluidState(pos).is(FluidTags.LAVA)) {
                nearestDistanceSquared = Math.min(nearestDistanceSquared, center.distSqr(pos));
            }
        }
        if (nearestDistanceSquared == Double.MAX_VALUE) {
            return 0.0;
        }
        double distance = Math.sqrt(nearestDistanceSquared);
        return Mth.clamp(1.0 - distance / (LAVA_HEAT_RADIUS + 1.0), 0.0, 1.0);
    }

    private static void applySurvivalDamage(ServerPlayer player) {
        float thirst = PlayerSurvivalData.getThirst(player);
        double temperature = PlayerSurvivalData.getBodyTemperature(player);

        if (thirst <= 0.0F && player.tickCount % 80 == 0) {
            player.hurt(player.damageSources().starve(), 1.0F);
        }
        if (temperature < COLD_DAMAGE_TEMPERATURE && player.tickCount % 100 == 0) {
            player.hurt(player.damageSources().freeze(), 1.0F);
        } else if (temperature >= HOT_DAMAGE_TEMPERATURE && player.tickCount % 100 == 0) {
            player.hurt(player.damageSources().onFire(), 1.0F);
        }
    }

    private record ThermalEnvironment(
            double ambientTemperature,
            double targetBodyTemperature,
            double exposureMultiplier
    ) {
    }

    private static void sync(ServerPlayer player) {
        ItemStack suitChestplate = SpaceSuitService.getChestplate(player);
        PacketDistributor.sendToPlayer(player, new SurvivalSyncPayload(
                PlayerSurvivalData.getThirst(player),
                PlayerSurvivalData.getBodyTemperature(player),
                SpaceSuitService.getOxygen(suitChestplate),
                suitChestplate.isEmpty() ? 0 : SpaceSuitService.MAX_OXYGEN,
                SpaceSuitService.isSealed(player)));
    }
}
