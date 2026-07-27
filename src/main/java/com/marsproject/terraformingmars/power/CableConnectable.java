package com.marsproject.terraformingmars.power;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implemented by machines that expose one or more physical cable ports.
 * This avoids teaching CableBlock about every future generator/consumer type.
 */
public interface CableConnectable {
    boolean canConnectCable(LevelReader level, BlockPos machinePos,
                            BlockState machineState, BlockPos cablePos);
}
