package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.block.entity.MultiblockPartBlockEntity;
import com.marsproject.terraformingmars.power.CableConnectable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Invisible occupancy cell shared by every multiblock controller. */
public final class MultiblockPartBlock extends BaseEntityBlock implements CableConnectable {
    public static final MapCodec<MultiblockPartBlock> CODEC = simpleCodec(MultiblockPartBlock::new);

    public MultiblockPartBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiblockPartBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        ControllerData data = controllerData(level, pos);
        if (data == null) {
            return Shapes.empty();
        }
        MultiblockPart part = data.controller().getMultiblockParts().get(data.partIndex());
        return rotateShape(part.shape(), data.state().getValue(
                net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING));
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        ControllerData data = controllerData(context.getLevel(), context.getClickedPos());
        return data != null
                && !data.controller().getMultiblockParts().get(data.partIndex()).blocksPlacement();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        ControllerData data = controllerData(level, pos);
        return data == null ? InteractionResult.PASS
                : data.controller().useMultiblockPart(
                        data.state(), level, data.pos(), player, hitResult);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        destroyController(level, pos, !player.isCreative(), player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            ControllerData data = controllerData(level, pos);
            if (data != null
                    && data.controller().getMultiblockParts().get(data.partIndex()).blocksPlacement()) {
                level.destroyBlock(data.pos(), true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void destroyController(Level level, BlockPos pos, boolean drops,
                                          @Nullable Player player) {
        ControllerData data = controllerData(level, pos);
        if (data != null) {
            level.destroyBlock(data.pos(), drops, player);
        }
    }

    @Override
    public boolean canConnectCable(LevelReader level, BlockPos machinePos,
                                   BlockState machineState, BlockPos cablePos) {
        ControllerData data = controllerData(level, machinePos);
        return data != null
                && data.state().getBlock() instanceof CableConnectable connectable
                && connectable.canConnectCable(level, data.pos(), data.state(), cablePos);
    }

    private static @Nullable ControllerData controllerData(BlockGetter level, BlockPos partPos) {
        if (!(level.getBlockEntity(partPos) instanceof MultiblockPartBlockEntity partEntity)) {
            return null;
        }
        BlockPos controllerPos = partEntity.getControllerPos();
        BlockState controllerState = level.getBlockState(controllerPos);
        if (!(controllerState.getBlock() instanceof MultiblockController controller)
                || !partEntity.matchesController(controllerState.getBlock())
                || partEntity.getPartIndex() < 0
                || partEntity.getPartIndex() >= controller.getMultiblockParts().size()) {
            return null;
        }
        return new ControllerData(controllerPos, controllerState, controller,
                partEntity.getPartIndex());
    }

    private static VoxelShape rotateShape(VoxelShape shape, Direction facing) {
        if (facing == Direction.NORTH) {
            return shape;
        }
        VoxelShape rotated = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            AABB transformed = switch (facing) {
                case EAST -> new AABB(1.0 - box.maxZ, box.minY, box.minX,
                        1.0 - box.minZ, box.maxY, box.maxX);
                case SOUTH -> new AABB(1.0 - box.maxX, box.minY, 1.0 - box.maxZ,
                        1.0 - box.minX, box.maxY, 1.0 - box.minZ);
                case WEST -> new AABB(box.minZ, box.minY, 1.0 - box.maxX,
                        box.maxZ, box.maxY, 1.0 - box.minX);
                default -> box;
            };
            rotated = Shapes.or(rotated, Shapes.create(transformed));
        }
        return rotated.optimize();
    }

    private record ControllerData(BlockPos pos, BlockState state,
                                  MultiblockController controller, int partIndex) {
    }
}
