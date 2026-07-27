package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.block.entity.PipeBlockEntity;
import com.marsproject.terraformingmars.pipe.PipeConnectable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Six-way air pipe kept completely separate from the power-cable network. */
public final class PipeBlock extends BaseEntityBlock {
    public static final MapCodec<PipeBlock> CODEC = simpleCodec(PipeBlock::new);
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape CENTER = box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH_ARM = box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SOUTH_ARM = box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape WEST_ARM = box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape EAST_ARM = box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape UP_ARM = box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape DOWN_ARM = box(6, 0, 6, 10, 6, 10);

    public PipeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return calculateConnections(context.getLevel(), context.getClickedPos(),
                defaultBlockState());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   net.minecraft.world.level.block.Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        BlockState updated = calculateConnections(level, pos, state);
        if (!updated.equals(state)) {
            level.setBlock(pos, updated, 2);
        }
    }

    public static BlockState calculateConnections(LevelReader level, BlockPos pos,
                                                  BlockState state) {
        return state
                .setValue(NORTH, connectsTo(level, pos, Direction.NORTH))
                .setValue(SOUTH, connectsTo(level, pos, Direction.SOUTH))
                .setValue(EAST, connectsTo(level, pos, Direction.EAST))
                .setValue(WEST, connectsTo(level, pos, Direction.WEST))
                .setValue(UP, connectsTo(level, pos, Direction.UP))
                .setValue(DOWN, connectsTo(level, pos, Direction.DOWN));
    }

    public static boolean connectsTo(LevelReader level, BlockPos pipePos,
                                     Direction direction) {
        BlockPos otherPos = pipePos.relative(direction);
        BlockState other = level.getBlockState(otherPos);
        if (other.getBlock() instanceof PipeBlock) {
            return true;
        }
        return other.getBlock() instanceof PipeConnectable connectable
                && connectable.canConnectPipe(level, otherPos, other, pipePos);
    }

    public static boolean isConnected(BlockState state, Direction direction) {
        if (!(state.getBlock() instanceof PipeBlock)) {
            return false;
        }
        return state.getValue(switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        });
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        VoxelShape shape = CENTER;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_ARM);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_ARM);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_ARM);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_ARM);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_ARM);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_ARM);
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }
}
