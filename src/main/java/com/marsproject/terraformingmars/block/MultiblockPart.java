package com.marsproject.terraformingmars.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One occupied cell, expressed in controller-local space for FACING north. */
public record MultiblockPart(BlockPos offset, VoxelShape shape, boolean blocksPlacement) {
    public MultiblockPart(BlockPos offset, VoxelShape shape) {
        this(offset, shape, true);
    }
}
