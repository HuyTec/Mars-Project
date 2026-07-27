package com.marsproject.terraformingmars.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/** Implemented only by machines that expose an air-pipe port. */
public interface PipeConnectable {
    boolean canConnectPipe(LevelReader level, BlockPos machinePos,
                           BlockState machineState, BlockPos pipePos);
}
