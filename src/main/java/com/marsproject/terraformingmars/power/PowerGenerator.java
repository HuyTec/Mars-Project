package com.marsproject.terraformingmars.power;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** A machine that contributes instantaneous generation capacity to a cable network. */
public interface PowerGenerator extends CableConnectable {
    ResourceLocation generatorType();

    int generatedWatts(Level level, BlockPos pos, BlockState state);
}
