package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.block.entity.UpsBlockEntity;
import com.marsproject.terraformingmars.power.CableConnectable;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Power hub: one rear input and four output cable ports. */
public final class UpsBlock extends BaseEntityBlock implements CableConnectable, MultiblockController {
    public static final MapCodec<UpsBlock> CODEC = simpleCodec(UpsBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final List<MultiblockPart> PARTS = List.of(
            new MultiblockPart(new BlockPos(0, 0, 1), Block.box(0, 0, 0, 16, 16, 16)),
            new MultiblockPart(new BlockPos(0, 1, 0), Block.box(0, 0, 0, 16, 16, 16)),
            new MultiblockPart(new BlockPos(0, 1, 1), Block.box(0, 0, 0, 16, 16, 16)),
            new MultiblockPart(new BlockPos(1, 0, 0), Block.box(0, 0, 0, 16, 16, 16)),
            new MultiblockPart(new BlockPos(1, 0, 1), Block.box(0, 0, 0, 16, 16, 16)),
            new MultiblockPart(new BlockPos(1, 1, 0), Block.box(0, 0, 0, 16, 16, 16)),
            new MultiblockPart(new BlockPos(1, 1, 1), Block.box(0, 0, 0, 16, 16, 16))
    );

    public UpsBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
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
        return new UpsBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placed = defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, false);
        return multiblockStateForPlacement(context, placed);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        placeMultiblockParts(level, pos, state, placer, stack);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            removeMultiblockParts(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        type,
                        ModBlockEntities.UPS.get(),
                        UpsBlockEntity::serverTick
                );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        return interact(level, pos, player);
    }

    public static InteractionResult interact(Level level, BlockPos controllerPos, Player player) {
        if (!level.isClientSide()
                && level.getBlockEntity(controllerPos) instanceof UpsBlockEntity ups) {
            player.displayClientMessage(Component.translatable(
                    "message.terraforming_mars.ups_status",
                    ups.isOnline()
                            ? Component.translatable("message.terraforming_mars.ups_on")
                            : Component.translatable("message.terraforming_mars.ups_off"),
                    ups.getTotalWatts(),
                    ups.getGeneratorCount(),
                    ups.getStoredEnergy(),
                    ups.getEnergyCapacity()
            ), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult useMultiblockPart(BlockState controllerState, Level level,
                                               BlockPos controllerPos, Player player,
                                               BlockHitResult hitResult) {
        return interact(level, controllerPos, player);
    }

    @Override
    public List<MultiblockPart> getMultiblockParts() {
        return PARTS;
    }

    public static Direction inputDirection(BlockState state) {
        return state.getValue(FACING).getCounterClockWise();
    }

    public static BlockPos partPos(BlockPos anchor, BlockState state, int x, int y, int z) {
        Direction front = state.getValue(FACING);
        // The anchor is bottom-front-left as seen by a player looking at the front.
        return anchor.relative(front.getCounterClockWise(), x)
                .relative(front.getOpposite(), z)
                .above(y);
    }

    /** The red input is behind the bottom-left controller cell. */
    public static BlockPos inputCablePos(BlockPos anchor, BlockState state) {
        Direction right = state.getValue(FACING).getCounterClockWise().getOpposite();
            return partPos(anchor, state, 0, 0, 1).relative(right);
        }

    public static boolean isCablePort(BlockPos anchor, BlockState state, BlockPos cablePos) {
        Direction front = state.getValue(FACING);
        Direction back = front.getOpposite();

        if (cablePos.equals(inputCablePos(anchor, state))) return true;

        return cablePos.equals(partPos(anchor, state, 0, 0, 0).relative(front))   // output 1
                || cablePos.equals(partPos(anchor, state, 1, 1, 0).relative(front)) // output 2
                || cablePos.equals(partPos(anchor, state, 0, 0, 1).relative(back))  // output 3 (đã sửa: đúng block + đúng hướng)
                || cablePos.equals(partPos(anchor, state, 1, 1, 1).relative(back)); // output 4
    }

    @Override
    public boolean canConnectCable(LevelReader level, BlockPos machinePos,
                                   BlockState machineState, BlockPos cablePos) {
        return isCablePort(machinePos, machineState, cablePos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }
}
