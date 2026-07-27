package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import com.marsproject.terraformingmars.registry.ModBlocks;
import com.marsproject.terraformingmars.weather.MarsWeatherData;
import com.marsproject.terraformingmars.weather.MarsWeatherBiomes;
import com.marsproject.terraformingmars.block.SevenLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public final class MarsWeatherHandler {
    private static final int PERIODIC_SYNC_TICKS = 100;
    private static final int ACCUMULATION_INTERVAL_TICKS = 20;
    private static final int ACCUMULATION_RADIUS = 24;

    private MarsWeatherHandler() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!level.dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) {
            return;
        }

        MarsWeatherData weather = MarsWeatherData.get(level);
        boolean transitioned = weather.tick(level);
        if (level.getGameTime() % ACCUMULATION_INTERVAL_TICKS == 0) {
            if (weather.isDustStorm()) {
                accumulateLayer(level, weather.intensity(), ModBlocks.DUST_LAYER.get(), false);
            } else if (weather.isDryIceStorm()) {
                accumulateLayer(level, weather.intensity(), ModBlocks.DRY_ICE_LAYER.get(), true);
            }
        }
        if (transitioned || level.getGameTime() % PERIODIC_SYNC_TICKS == 0) {
            PacketDistributor.sendToPlayersInDimension(level, weather.toPayload());
        }
    }

    private static void accumulateLayer(ServerLevel level, int intensity,
                                        Block layerBlock, boolean cryoticOnly) {
        int attemptsPerPlayer = switch (intensity) {
            case 1 -> 2;
            case 2 -> 5;
            default -> 9;
        };

        for (ServerPlayer player : level.players()) {
            BlockPos origin = player.blockPosition();
            for (int attempt = 0; attempt < attemptsPerPlayer; attempt++) {
                int x = origin.getX() + level.getRandom().nextIntBetweenInclusive(
                        -ACCUMULATION_RADIUS, ACCUMULATION_RADIUS);
                int z = origin.getZ() + level.getRandom().nextIntBetweenInclusive(
                        -ACCUMULATION_RADIUS, ACCUMULATION_RADIUS);
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                BlockPos target = new BlockPos(x, y, z);

                if (!level.canSeeSky(target)) {
                    continue;
                }
                if (cryoticOnly && !MarsWeatherBiomes.isCryotic(level, target)) {
                    continue;
                }

                BlockState current = level.getBlockState(target);
                BlockPos existingLayerPos = current.is(layerBlock)
                        ? target
                        : target.below();
                BlockState existingLayer = level.getBlockState(existingLayerPos);
                if (existingLayer.is(layerBlock)) {
                    int layers = existingLayer.getValue(SnowLayerBlock.LAYERS);
                    if (layers > SevenLayerBlock.MAX_LAYERS) {
                        // Repair layer-8 states left in worlds created before
                        // the seven-layer cap was introduced.
                        level.setBlock(existingLayerPos,
                                existingLayer.setValue(
                                        SnowLayerBlock.LAYERS,
                                        SevenLayerBlock.MAX_LAYERS
                                ),
                                Block.UPDATE_ALL);
                    } else if (layers < SevenLayerBlock.MAX_LAYERS) {
                        level.setBlock(existingLayerPos,
                                existingLayer.setValue(SnowLayerBlock.LAYERS, layers + 1),
                                Block.UPDATE_ALL);
                    }
                    continue;
                }

                BlockState layer = layerBlock.defaultBlockState();
                if (current.isAir() && layer.canSurvive(level, target)) {
                    level.setBlock(target, layer, Block.UPDATE_ALL);
                }
            }
        }
    }
}
