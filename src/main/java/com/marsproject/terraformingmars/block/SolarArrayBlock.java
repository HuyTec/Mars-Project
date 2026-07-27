package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.entity.SolarArrayBlockEntity;
import com.marsproject.terraformingmars.power.PowerGenerator;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/** Stage 0 solar generator rendered and animated by GeckoLib. */
public final class SolarArrayBlock extends BaseEntityBlock implements PowerGenerator, MultiblockController {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final SolarArrayType arrayType;
    private final VoxelShape shape;

    public SolarArrayBlock(BlockBehaviour.Properties properties, SolarArrayType arrayType) {
        super(properties);
        this.arrayType = arrayType;
        this.shape = arrayType.shape();
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return shape;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarArrayBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(
                type,
                ModBlockEntities.SOLAR_ARRAY.get(),
                SolarArrayBlockEntity::serverTick
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placed = defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
        return multiblockStateForPlacement(context, placed);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable net.minecraft.world.entity.LivingEntity placer,
                            net.minecraft.world.item.ItemStack stack) {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SolarArrayBlockEntity solar) {
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown()) {
                    boolean tracking = solar.toggleTracking();
                    player.displayClientMessage(Component.translatable(
                            tracking
                                    ? "message.terraforming_mars.solar_tracking_on"
                                    : "message.terraforming_mars.solar_tracking_off"
                    ), true);
                } else {
                    solar.cleanDust();
                    player.displayClientMessage(Component.translatable(
                            "message.terraforming_mars.solar_cleaned"), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean canConnectCable(LevelReader level, BlockPos machinePos,
                                   BlockState machineState, BlockPos cablePos) {
        Direction rear = machineState.getValue(FACING).getOpposite();
        return machinePos.relative(rear).equals(cablePos);
    }

    @Override
    public ResourceLocation generatorType() {
        return new ResourceLocation(TerraformingMarsMod.MODID, "solar_array");
    }

    @Override
    public int generatedWatts(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SolarArrayBlockEntity solar) {
            return solar.getCurrentWatts();
        }
        return 0;
    }

    public SolarArrayType getArrayType() {
        return arrayType;
    }

    @Override
    public List<MultiblockPart> getMultiblockParts() {
        return arrayType.parts();
    }
}
