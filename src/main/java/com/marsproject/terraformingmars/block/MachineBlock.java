package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.block.entity.MachineBlockEntity;
import com.marsproject.terraformingmars.machine.MachineType;
import com.marsproject.terraformingmars.pipe.PipeConnectable;
import com.marsproject.terraformingmars.power.CableConnectable;
import com.marsproject.terraformingmars.power.PowerGenerator;
import com.marsproject.terraformingmars.pipe.PipeType;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import com.marsproject.terraformingmars.registry.ModItems;
import com.marsproject.terraformingmars.gas.GasType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Data-driven controller shared by all standard processing machines. */
public final class MachineBlock extends BaseEntityBlock
        implements MultiblockController, CableConnectable, PipeConnectable, PowerGenerator {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final MachineType machineType;

    public MachineBlock(BlockBehaviour.Properties properties, MachineType machineType) {
        super(properties);
        this.machineType = machineType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    public MachineType getMachineType() {
        return machineType;
    }

    @Override
    public List<MultiblockPart> getMultiblockParts() {
        return machineType.parts();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(
                type, ModBlockEntities.MACHINE.get(), MachineBlockEntity::serverTick);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placed = defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            MachineType type = machine.getMachineType();
            serverPlayer.displayClientMessage(Component.translatable(
                    machine.getStatusTranslationKey(),
                    machine.getDisplayName(),
                    type.energyPerOperation(),
                    type.operationIntervalTicks()
            ), false);
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.terraforming_mars.machine_diagnostics",
                    machine.getStoredOutputGas(), machine.getGasCapacity(),
                    machine.getOutputGasType().name(), type.energyPerOperation(),
                    type.operationIntervalTicks()), false);
            serverPlayer.displayClientMessage(Component.literal(
                    "Resources: " + machine.getResourceSummary()), false);
            String portKey = switch (type.operation()) {
                case WATER_EXTRACTION, ELECTROLYSIS ->
                        "message.terraforming_mars.machine_ports_water";
                case SABATIER_REACTION -> "message.terraforming_mars.machine_ports_fuel";
                case METHANE_HEATING, METHANE_POWER ->
                        "message.terraforming_mars.machine_ports_heat";
                default -> type.operation().isAirCreator()
                        ? "message.terraforming_mars.machine_ports_air_creator"
                        : "message.terraforming_mars.machine_ports_generator";
            };
            serverPlayer.displayClientMessage(Component.translatable(
                    portKey), false);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.45F, 1.0F);
            serverPlayer.openMenu(machine, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (!stack.is(ModItems.O2_CANISTER.get())
                || machineType.operation().outputGas() != GasType.OXYGEN) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof MachineBlockEntity machine)) {
            return ItemInteractionResult.FAIL;
        }

        int transferred = machine.refillSuitFromOxygenBuffer(
                player, com.marsproject.terraformingmars.survival.SpaceSuitService.OXYGEN_PER_CANISTER);
        serverPlayer.displayClientMessage(Component.translatable(
                transferred > 0
                        ? "message.terraforming_mars.generator_refilled_suit"
                        : "message.terraforming_mars.generator_refill_failed",
                transferred, machine.getStoredOutputGas()), true);
        if (transferred > 0) {
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.55F, 1.2F);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useMultiblockPart(BlockState controllerState, Level level,
                                               BlockPos controllerPos, Player player,
                                               BlockHitResult hitResult) {
        return useWithoutItem(controllerState, level, controllerPos, player, hitResult);
    }

    @Override
    public boolean canConnectCable(LevelReader level, BlockPos machinePos,
                                   BlockState machineState, BlockPos cablePos) {
        if (machineType.operation().isAirCreator()
                || machineType.operation().isDualInput()
                || machineType.operation() == com.marsproject.terraformingmars.machine.MachineOperation.ELECTROLYSIS) {
            return frontCablePos(machinePos, machineState).equals(cablePos)
                    || backCablePos(machinePos, machineState).equals(cablePos)
                    || topCablePos(machinePos).equals(cablePos)
                    || bottomCablePos(machinePos).equals(cablePos);
        }
        return leftCablePos(machinePos, machineState).equals(cablePos)
                || rightCablePos(machinePos, machineState).equals(cablePos);
    }

    /** @deprecated Machines now expose left and right cable ports. */
    @Deprecated
    public static BlockPos getCablePos(BlockPos pos, BlockState state) {
        return leftCablePos(pos, state);
    }

    public static BlockPos leftCablePos(BlockPos pos, BlockState state) {
        Direction front = state.getValue(FACING);
        return pos.relative(front.getCounterClockWise());
    }

    public static BlockPos rightCablePos(BlockPos pos, BlockState state) {
        Direction front = state.getValue(FACING);
        return pos.relative(front.getClockWise());
    }

    public static BlockPos frontCablePos(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING));
    }

    public static BlockPos backCablePos(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING).getOpposite());
    }

    public static BlockPos topCablePos(BlockPos pos) {
        return pos.above();
    }

    public static BlockPos bottomCablePos(BlockPos pos) {
        return pos.below();
    }

    public static BlockPos inputPipePos(BlockPos pos, BlockState state) {
        Direction front = state.getValue(FACING);
        return pos.relative(front.getOpposite());
    }

    public static BlockPos outputPipePos(BlockPos pos, BlockState state) {
        return pos.above();
    }

    public static BlockPos oxygenInputPipePos(BlockPos pos, BlockState state) {
        return rightCablePos(pos, state);
    }

    public static BlockPos nitrogenInputPipePos(BlockPos pos, BlockState state) {
        return leftCablePos(pos, state);
    }

    public static BlockPos secondaryOutputPipePos(BlockPos pos, BlockState state) {
        return frontCablePos(pos, state);
    }

    public static BlockPos fluidPortPos(BlockPos pos, BlockState state) {
        return backCablePos(pos, state);
    }

    @Override
    public boolean canConnectPipe(LevelReader level, BlockPos machinePos,
                                  BlockState machineState, BlockPos pipePos) {
        if (!(level.getBlockState(pipePos).getBlock() instanceof PipeBlock pipe)) {
            return false;
        }
        PipeType pipeType = pipe.getPipeType();
        var operation = machineType.operation();
        if (operation.isAirCreator()) {
            return (oxygenInputPipePos(machinePos, machineState).equals(pipePos)
                    || nitrogenInputPipePos(machinePos, machineState).equals(pipePos))
                    && pipeType == PipeType.GAS;
        }
        if (operation == com.marsproject.terraformingmars.machine.MachineOperation.WATER_EXTRACTION) {
            return outputPipePos(machinePos, machineState).equals(pipePos)
                    && pipeType == PipeType.FLUID;
        }
        if (operation == com.marsproject.terraformingmars.machine.MachineOperation.ELECTROLYSIS) {
            return fluidPortPos(machinePos, machineState).equals(pipePos)
                    && pipeType == PipeType.FLUID
                    || (outputPipePos(machinePos, machineState).equals(pipePos)
                    || secondaryOutputPipePos(machinePos, machineState).equals(pipePos))
                    && pipeType == PipeType.GAS;
        }
        if (operation == com.marsproject.terraformingmars.machine.MachineOperation.SABATIER_REACTION) {
            return (oxygenInputPipePos(machinePos, machineState).equals(pipePos)
                    || nitrogenInputPipePos(machinePos, machineState).equals(pipePos)
                    || outputPipePos(machinePos, machineState).equals(pipePos))
                    && pipeType == PipeType.GAS
                    || fluidPortPos(machinePos, machineState).equals(pipePos)
                    && pipeType == PipeType.FLUID;
        }
        if (operation == com.marsproject.terraformingmars.machine.MachineOperation.METHANE_HEATING) {
            return (oxygenInputPipePos(machinePos, machineState).equals(pipePos)
                    || nitrogenInputPipePos(machinePos, machineState).equals(pipePos))
                    && pipeType == PipeType.GAS
                    || outputPipePos(machinePos, machineState).equals(pipePos)
                    && pipeType == PipeType.HEAT;
        }
        if (operation == com.marsproject.terraformingmars.machine.MachineOperation.METHANE_POWER) {
            return (oxygenInputPipePos(machinePos, machineState).equals(pipePos)
                    || nitrogenInputPipePos(machinePos, machineState).equals(pipePos)
                    || outputPipePos(machinePos, machineState).equals(pipePos))
                    && pipeType == PipeType.GAS
                    || fluidPortPos(machinePos, machineState).equals(pipePos)
                    && pipeType == PipeType.FLUID
                    || secondaryOutputPipePos(machinePos, machineState).equals(pipePos)
                    && pipeType == PipeType.HEAT;
        }
        return (inputPipePos(machinePos, machineState).equals(pipePos)
                || outputPipePos(machinePos, machineState).equals(pipePos))
                && pipeType == PipeType.GAS;
    }

    public List<BlockPos> cablePortPositions(BlockPos pos, BlockState state) {
        if (machineType.operation().isAirCreator()
                || machineType.operation().isDualInput()
                || machineType.operation() == com.marsproject.terraformingmars.machine.MachineOperation.ELECTROLYSIS) {
            return List.of(
                    frontCablePos(pos, state),
                    backCablePos(pos, state),
                    topCablePos(pos),
                    bottomCablePos(pos)
            );
        }
        return List.of(leftCablePos(pos, state), rightCablePos(pos, state));
    }

    @Override
    public ResourceLocation generatorType() {
        return machineType.machineTypeId();
    }

    @Override
    public int generatedWatts(Level level, BlockPos pos, BlockState state) {
        if (!machineType.operation().isPowerGenerator()) return 0;
        return level.getBlockEntity(pos) instanceof MachineBlockEntity machine
                && machine.isPowerProducing() ? 12_000 : 0;
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
        builder.add(FACING);
    }
}
