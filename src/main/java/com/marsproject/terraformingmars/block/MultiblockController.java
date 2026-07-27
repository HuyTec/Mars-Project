package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.block.entity.MultiblockPartBlockEntity;
import com.marsproject.terraformingmars.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Shared placement, cleanup and lookup behavior for arbitrary multiblocks. */
public interface MultiblockController {
    List<MultiblockPart> getMultiblockParts();

    default BlockState multiblockStateForPlacement(BlockPlaceContext context, BlockState state) {
        BlockPos anchor = context.getClickedPos();
        Direction facing = getMultiblockFacing(state);
        for (MultiblockPart part : getMultiblockParts()) {
            if (part.blocksPlacement()
                    && !context.getLevel().getBlockState(
                            anchor.offset(rotateOffset(part.offset(), facing))).canBeReplaced()) {
                return null;
            }
        }
        return state;
    }

    default void placeMultiblockParts(Level level, BlockPos anchor, BlockState state,
                                      @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = getMultiblockFacing(state);
        List<MultiblockPart> parts = getMultiblockParts();
        for (int index = 0; index < parts.size(); index++) {
            MultiblockPart part = parts.get(index);
            BlockPos partPos = anchor.offset(rotateOffset(part.offset(), facing));
            if (!part.blocksPlacement() && !level.getBlockState(partPos).canBeReplaced()) {
                continue;
            }
            level.setBlock(partPos, ModBlocks.MULTIBLOCK_PART.get().defaultBlockState(),
                    Block.UPDATE_ALL);
            if (level.getBlockEntity(partPos) instanceof MultiblockPartBlockEntity partEntity) {
                partEntity.configure(anchor.subtract(partPos), index, state.getBlock());
            }
        }
    }

    default void removeMultiblockParts(Level level, BlockPos anchor, BlockState state) {
        Direction facing = getMultiblockFacing(state);
        for (MultiblockPart part : getMultiblockParts()) {
            BlockPos partPos = anchor.offset(rotateOffset(part.offset(), facing));
            if (level.getBlockEntity(partPos) instanceof MultiblockPartBlockEntity partEntity
                    && partEntity.belongsTo(anchor, state.getBlock())) {
                level.setBlock(partPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    default Direction getMultiblockFacing(BlockState state) {
        return state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
    }

    default InteractionResult useMultiblockPart(BlockState controllerState, Level level,
                                                BlockPos controllerPos, Player player,
                                                BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    static BlockPos rotateOffset(BlockPos localOffset, Direction facing) {
        Direction left = facing.getCounterClockWise();
        Direction back = facing.getOpposite();
        return new BlockPos(
                left.getStepX() * localOffset.getX() + back.getStepX() * localOffset.getZ(),
                localOffset.getY(),
                left.getStepZ() * localOffset.getX() + back.getStepZ() * localOffset.getZ()
        );
    }
}
