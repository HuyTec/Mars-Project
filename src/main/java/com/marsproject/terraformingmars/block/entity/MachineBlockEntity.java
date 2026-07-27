package com.marsproject.terraformingmars.block.entity;

import com.marsproject.terraformingmars.block.CableBlock;
import com.marsproject.terraformingmars.block.MachineBlock;
import com.marsproject.terraformingmars.machine.MachineMenu;
import com.marsproject.terraformingmars.machine.MachineRecipe;
import com.marsproject.terraformingmars.machine.MachineRecipeInput;
import com.marsproject.terraformingmars.machine.MachineType;
import com.marsproject.terraformingmars.power.PowerNetworkScanner;
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

public final class MachineBlockEntity extends BlockEntity
        implements MenuProvider, GeoBlockEntity {
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_NO_POWER = 2;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ItemStackHandler items;
    private int progress;
    private int processingTime;
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
        Optional<RecipeHolder<MachineRecipe>> match = machine.findRecipe(level);
        if (match.isEmpty() || !machine.canAccept(match.get().value().output())) {
            machine.updateState(STATUS_IDLE, 0);
            return;
        }

        MachineRecipe recipe = match.get().value();
        machine.processingTime = recipe.processingTimeTicks();
        if (!machine.hasPower(level, state, recipe.powerCostWatts())) {
            machine.updateState(STATUS_NO_POWER, machine.processingTime);
            return;
        }

        machine.progress++;
        machine.updateState(STATUS_WORKING, machine.processingTime);
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

    private boolean hasPower(Level level, BlockState state, int watts) {
        if (watts == 0) {
            return true;
        }
        BlockPos cablePos = MachineBlock.getCablePos(worldPosition, state);
        return level.getBlockState(cablePos).getBlock() instanceof CableBlock
                && PowerNetworkScanner.scan(level, cablePos).totalWatts() >= watts;
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

    private void updateState(int newStatus, int newProcessingTime) {
        boolean changed = status != newStatus || processingTime != newProcessingTime;
        status = newStatus;
        processingTime = newProcessingTime;
        if (changed) {
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
        tag.putInt("Status", status);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Items"));
        progress = Math.max(0, tag.getInt("Progress"));
        processingTime = Math.max(0, tag.getInt("ProcessingTime"));
        status = tag.getInt("Status");
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
