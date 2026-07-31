package com.marsproject.terraformingmars.block.entity;

import com.marsproject.terraformingmars.block.CableBlock;
import com.marsproject.terraformingmars.block.AirVentBlock;
import com.marsproject.terraformingmars.block.MachineBlock;
import com.marsproject.terraformingmars.block.PipeBlock;
import com.marsproject.terraformingmars.gas.GasType;
import com.marsproject.terraformingmars.machine.MachineMenu;
import com.marsproject.terraformingmars.machine.MachineRecipe;
import com.marsproject.terraformingmars.machine.MachineRecipeInput;
import com.marsproject.terraformingmars.machine.MachineType;
import com.marsproject.terraformingmars.power.PowerNetworkScanner;
import com.marsproject.terraformingmars.pipe.PipeNetworkScanner;
import com.marsproject.terraformingmars.pipe.PipeNetworkSnapshot;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import com.marsproject.terraformingmars.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MachineBlockEntity extends BlockEntity
        implements MenuProvider, GeoBlockEntity {
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_NO_POWER = 2;
    public static final int STATUS_NO_INPUT = 3;
    public static final int STATUS_OUTPUT_FULL = 4;
    public static final int GAS_CAPACITY = 10_000;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ItemStackHandler items;
    private int progress;
    private int processingTime;
    private int operationTicks;
    private int storedOutputGas;
    private int status = STATUS_IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> processingTime;
                case 2 -> status;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> processingTime = value;
                case 2 -> status = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE.get(), pos, state);
        MachineType type = machineType(state);
        int inputCount = type == null ? 1 : type.inputSlotCount();
        int outputCount = type == null ? 1 : type.outputSlotCount();
        items = new ItemStackHandler(inputCount + outputCount) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot < inputCount;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MachineBlockEntity machine) {
        MachineType type = machine.getMachineType();
        machine.operationTicks++;
        if (machine.operationTicks >= type.operationIntervalTicks()) {
            machine.operationTicks = 0;
            machine.setStatus(machine.tryOperate());
            machine.setChanged();
        }

        Optional<RecipeHolder<MachineRecipe>> match = machine.findRecipe(level);
        if (match.isEmpty() || !machine.canAccept(match.get().value().output())) {
            machine.resetRecipeProgress();
            return;
        }

        MachineRecipe recipe = match.get().value();
        machine.processingTime = recipe.processingTimeTicks();
        if (!machine.isActive()) {
            return;
        }

        machine.progress++;
        if (machine.progress >= recipe.processingTimeTicks()) {
            machine.finishRecipe(recipe);
            machine.progress = 0;
            machine.setChanged();
            machine.syncToClient();
        }
    }

    private Optional<RecipeHolder<MachineRecipe>> findRecipe(Level level) {
        MachineType type = getMachineType();
        ItemStack[] inputs = new ItemStack[type.inputSlotCount()];
        for (int slot = 0; slot < inputs.length; slot++) {
            inputs[slot] = items.getStackInSlot(slot);
        }
        return level.getRecipeManager().getRecipeFor(
                ModRecipeTypes.MACHINE_TYPE.get(),
                new MachineRecipeInput(type.machineTypeId(), inputs),
                level
        );
    }

    public boolean isActive() {
        return status == STATUS_WORKING;
    }

    public String getStatusTranslationKey() {
        return switch (status) {
            case STATUS_WORKING -> "message.terraforming_mars.machine_active";
            case STATUS_NO_INPUT -> "message.terraforming_mars.machine_no_input";
            case STATUS_OUTPUT_FULL -> "message.terraforming_mars.machine_output_full";
            default -> "message.terraforming_mars.machine_inactive";
        };
    }

    public int getEnergyPerOperation() {
        return getMachineType().energyPerOperation();
    }

    public int getStoredOutputGas() {
        return storedOutputGas;
    }

    public int getGasCapacity() {
        return GAS_CAPACITY;
    }

    public GasType getOutputGasType() {
        return getMachineType().operation().outputGas();
    }

    public boolean hasGas(GasType gasType, int amount) {
        return amount >= 0 && getOutputGasType() == gasType && storedOutputGas >= amount;
    }

    public boolean tryConsumeGas(GasType gasType, int amount) {
        if (amount <= 0) {
            return true;
        }
        if (!hasGas(gasType, amount)) {
            return false;
        }
        storedOutputGas -= amount;
        setChanged();
        syncToClient();
        return true;
    }

    private int tryOperate() {
        MachineType type = getMachineType();
        int produced = type.operation().outputAmount();
        if (storedOutputGas > GAS_CAPACITY - produced) {
            return STATUS_OUTPUT_FULL;
        }

        if (type.operation().isAirCreator()) {
            return tryCreateAir(type);
        }
        if (type.operation().requiresAirVent() && !hasAirVentInput()) {
            return STATUS_NO_INPUT;
        }
        if (!tryConsumePower()) {
            return STATUS_NO_POWER;
        }

        storedOutputGas += produced;
        syncToClient();
        return STATUS_WORKING;
    }

    private int tryCreateAir(MachineType type) {
        BlockState state = getBlockState();
        MachineBlockEntity oxygenSource = findGasSource(
                MachineBlock.oxygenInputPipePos(worldPosition, state),
                GasType.OXYGEN,
                type.operation().oxygenInput()
        );
        MachineBlockEntity nitrogenSource = findGasSource(
                MachineBlock.nitrogenInputPipePos(worldPosition, state),
                GasType.NITROGEN,
                type.operation().nitrogenInput()
        );
        if (oxygenSource == null || nitrogenSource == null) {
            return STATUS_NO_INPUT;
        }
        if (!tryConsumePower()) {
            return STATUS_NO_POWER;
        }

        // Sources were checked on the server thread immediately before this transaction.
        if (!oxygenSource.tryConsumeGas(GasType.OXYGEN, type.operation().oxygenInput())
                || !nitrogenSource.tryConsumeGas(
                        GasType.NITROGEN, type.operation().nitrogenInput())) {
            return STATUS_NO_INPUT;
        }
        storedOutputGas += type.operation().outputAmount();
        syncToClient();
        return STATUS_WORKING;
    }

    private boolean hasAirVentInput() {
        if (level == null) {
            return false;
        }
        BlockPos pipePos = MachineBlock.inputPipePos(worldPosition, getBlockState());
        if (!(level.getBlockState(pipePos).getBlock() instanceof PipeBlock)) {
            return false;
        }
        return PipeNetworkScanner.scan(level, pipePos).connectedMachines().stream()
                .anyMatch(pos -> level.getBlockState(pos).getBlock() instanceof AirVentBlock);
    }

    private @Nullable MachineBlockEntity findGasSource(BlockPos pipePos, GasType gasType,
                                                        int requiredAmount) {
        if (level == null || !(level.getBlockState(pipePos).getBlock() instanceof PipeBlock)) {
            return null;
        }
        PipeNetworkSnapshot network = PipeNetworkScanner.scan(level, pipePos);
        List<MachineBlockEntity> outputSources = network.connectedMachines().stream()
                .sorted(java.util.Comparator.comparingLong(BlockPos::asLong))
                .map(level::getBlockEntity)
                .filter(MachineBlockEntity.class::isInstance)
                .map(MachineBlockEntity.class::cast)
                .filter(source -> source.getMachineType().operation().requiresAirVent())
                .filter(source -> network.pipes().contains(MachineBlock.outputPipePos(
                        source.getBlockPos(), source.getBlockState())))
                .toList();
        if (outputSources.stream().anyMatch(source -> source.getOutputGasType() != gasType)) {
            return null;
        }
        return outputSources.stream()
                .filter(source -> source.hasGas(gasType, requiredAmount))
                .findFirst()
                .orElse(null);
    }

    private boolean tryConsumePower() {
        int requestedEnergy = getEnergyPerOperation();
        if (requestedEnergy == 0) {
            return true;
        }
        if (level == null || level.isClientSide()) {
            return false;
        }

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof MachineBlock block)) {
            return false;
        }
        Set<BlockPos> checkedUps = new HashSet<>();
        for (BlockPos cablePos : block.cablePortPositions(worldPosition, state)) {
            if (!(level.getBlockState(cablePos).getBlock() instanceof CableBlock)) {
                continue;
            }
            for (UpsBlockEntity ups : PowerNetworkScanner.findOutputUps(level, cablePos)) {
                if (checkedUps.add(ups.getBlockPos()) && ups.tryConsumeEnergy(requestedEnergy)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAccept(ItemStack output) {
        MachineType type = getMachineType();
        for (int slot = type.inputSlotCount(); slot < items.getSlots(); slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()
                    || ItemStack.isSameItemSameComponents(existing, output)
                    && existing.getCount() + output.getCount() <= existing.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void finishRecipe(MachineRecipe recipe) {
        for (int slot = 0; slot < recipe.inputs().size(); slot++) {
            items.extractItem(slot, 1, false);
        }
        MachineType type = getMachineType();
        for (int slot = type.inputSlotCount(); slot < items.getSlots(); slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()) {
                items.setStackInSlot(slot, recipe.output().copy());
                break;
            }
            if (ItemStack.isSameItemSameComponents(existing, recipe.output())) {
                existing.grow(recipe.output().getCount());
                items.setStackInSlot(slot, existing);
                break;
            }
        }
    }

    private void setActive(boolean active) {
        setStatus(active ? STATUS_WORKING : STATUS_NO_POWER);
    }

    private void setStatus(int newStatus) {
        if (status != newStatus) {
            status = newStatus;
            setChanged();
            syncToClient();
        }
    }

    private void resetRecipeProgress() {
        if (progress != 0 || processingTime != 0) {
            progress = 0;
            processingTime = 0;
            setChanged();
            syncToClient();
        }
    }

    public MachineType getMachineType() {
        MachineType type = machineType(getBlockState());
        if (type == null) {
            throw new IllegalStateException("MachineBlockEntity is not attached to MachineBlock");
        }
        return type;
    }

    private static @Nullable MachineType machineType(BlockState state) {
        return state.getBlock() instanceof MachineBlock block ? block.getMachineType() : null;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId,
                                                      Inventory inventory, Player player) {
        return new MachineMenu(containerId, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("ProcessingTime", processingTime);
        tag.putInt("OperationTicks", operationTicks);
        tag.putInt("StoredOutputGas", storedOutputGas);
        tag.putInt("Status", status);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Items"));
        progress = Math.max(0, tag.getInt("Progress"));
        processingTime = Math.max(0, tag.getInt("ProcessingTime"));
        operationTicks = Math.max(0, tag.getInt("OperationTicks"));
        storedOutputGas = Math.max(0, Math.min(GAS_CAPACITY, tag.getInt("StoredOutputGas")));
        status = switch (tag.getInt("Status")) {
            case STATUS_WORKING -> STATUS_WORKING;
            case STATUS_NO_POWER -> STATUS_NO_POWER;
            case STATUS_NO_INPUT -> STATUS_NO_INPUT;
            case STATUS_OUTPUT_FULL -> STATUS_OUTPUT_FULL;
            default -> STATUS_IDLE;
        };
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "machine_controller", 5, animation -> {
            MachineType type = getMachineType();
            String name = switch (status) {
                case STATUS_WORKING -> type.workingAnimation();
                case STATUS_NO_POWER -> type.noPowerAnimation();
                default -> type.idleAnimation();
            };
            animation.setAnimation(RawAnimation.begin().thenLoop(name));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
