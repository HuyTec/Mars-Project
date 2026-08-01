package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.atmosphere.RoomClimateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/** Freezes exposed water in cold environments and lets nearby heat keep it liquid. */
@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public final class WaterThermalHandler {
    private static final int UPDATE_INTERVAL_TICKS = 40;
    private static final int SCAN_RADIUS = 4;
    private static final double HEAT_RADIUS_SQUARED = 16.0;

    private WaterThermalHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % UPDATE_INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        BlockPos center = player.blockPosition();
        List<BlockPos> heatSources = collectHeatSources(level, center);

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            BlockState state = level.getBlockState(pos);
            boolean heated = isNearHeat(pos, heatSources);
            double ambient = RoomClimateManager.temperatureAt(level, pos)
                    .orElseGet(() -> PlayerSurvivalHandler.ambientTemperature(level, pos));

            if (state.is(Blocks.WATER)
                    && level.getBlockState(pos.above()).isAir()
                    && ambient < 0.0 && !heated) {
                level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
            } else if (state.is(Blocks.ICE) && (ambient >= 0.0 || heated)) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            }
        }
    }

    private static List<BlockPos> collectHeatSources(ServerLevel level, BlockPos center) {
        int radius = SCAN_RADIUS * 2;
        List<BlockPos> sources = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(pos);
            boolean litCampfire = (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
                    && state.hasProperty(BlockStateProperties.LIT)
                    && state.getValue(BlockStateProperties.LIT);
            if (state.getFluidState().is(FluidTags.LAVA)
                    || state.is(Blocks.TORCH)
                    || state.is(Blocks.WALL_TORCH)
                    || state.is(Blocks.SOUL_TORCH)
                    || state.is(Blocks.SOUL_WALL_TORCH)
                    || litCampfire) {
                sources.add(pos.immutable());
            }
        }
        return sources;
    }

    private static boolean isNearHeat(BlockPos pos, List<BlockPos> heatSources) {
        return heatSources.stream().anyMatch(heat -> heat.distSqr(pos) <= HEAT_RADIUS_SQUARED);
    }

}
