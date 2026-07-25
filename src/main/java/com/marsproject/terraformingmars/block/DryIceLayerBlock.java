package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class DryIceLayerBlock extends SnowLayerBlock {
    private static final double SUBLIMATION_TEMPERATURE_CELSIUS = -45.0;

    public DryIceLayerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double temperature = MarsEnvironmentManager.resolve(
                MarsTerraformProgress.get(level).getProgress()
        ).temperatureCelsius();
        if (temperature <= SUBLIMATION_TEMPERATURE_CELSIUS || random.nextInt(3) != 0) {
            return;
        }

        int layers = state.getValue(LAYERS);
        if (layers <= 1) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(LAYERS, layers - 1), Block.UPDATE_ALL);
        }
    }
}
