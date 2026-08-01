package com.marsproject.terraformingmars.block.entity;

import com.marsproject.terraformingmars.block.PipeBlock;
import com.marsproject.terraformingmars.block.ResourceTankBlock;
import com.marsproject.terraformingmars.gas.GasType;
import com.marsproject.terraformingmars.machine.ResourceStorage;
import com.marsproject.terraformingmars.pipe.PipeNetworkScanner;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ResourceTankBlockEntity extends BlockEntity implements ResourceStorage {
    public static final int CAPACITY = 64_000;
    private GasType storedType;
    private int storedAmount;

    public ResourceTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESOURCE_TANK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ResourceTankBlockEntity tank) {
        if (level.getGameTime() % 20 != 0 || !(state.getBlock() instanceof ResourceTankBlock block)) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos pipePos = pos.relative(direction);
            if (!(level.getBlockState(pipePos).getBlock() instanceof PipeBlock pipe)
                    || pipe.getPipeType() != block.getPipeType()) {
                continue;
            }
            var network = PipeNetworkScanner.scan(level, pipePos);
            for (BlockPos devicePos : network.connectedMachines()) {
                if (!(level.getBlockEntity(devicePos) instanceof MachineBlockEntity machine)) {
                    continue;
                }
                for (GasType resource : GasType.values()) {
                    if (resource.pipeType() != block.getPipeType()
                            || tank.getResourceCapacity(resource) <= 0
                            || !machine.isOutputNetwork(resource, network.pipes())) {
                        continue;
                    }
                    int moved = machine.extractResource(resource,
                            Math.min(1_000, CAPACITY - tank.storedAmount));
                    if (moved > 0) {
                        tank.insertResource(resource, moved);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public int getStoredResource(GasType resource) {
        return storedType == resource ? storedAmount : 0;
    }

    @Override
    public int getResourceCapacity(GasType resource) {
        return storedType == null || storedType == resource ? CAPACITY : 0;
    }

    @Override
    public int insertResource(GasType resource, int amount) {
        if (amount <= 0 || (storedType != null && storedType != resource)
                || resource.pipeType() != ((ResourceTankBlock) getBlockState().getBlock()).getPipeType()) {
            return 0;
        }
        int accepted = Math.min(amount, CAPACITY - storedAmount);
        if (accepted > 0) {
            storedType = resource;
            storedAmount += accepted;
            setChanged();
            syncToClient();
        }
        return accepted;
    }

    @Override
    public int extractResource(GasType resource, int amount) {
        if (amount <= 0 || storedType != resource) return 0;
        int extracted = Math.min(amount, storedAmount);
        storedAmount -= extracted;
        if (storedAmount == 0) storedType = null;
        if (extracted > 0) {
            setChanged();
            syncToClient();
        }
        return extracted;
    }

    public int insertWater(int amount, boolean simulate) {
        if (storedType != null && storedType != GasType.WATER) return 0;
        int accepted = Math.min(amount, CAPACITY - storedAmount);
        return simulate ? accepted : insertResource(GasType.WATER, accepted);
    }

    public int extractWater(int amount, boolean simulate) {
        int extracted = storedType == GasType.WATER ? Math.min(amount, storedAmount) : 0;
        return simulate ? extracted : extractResource(GasType.WATER, extracted);
    }

    public String getStoredTypeName() {
        return storedType == null ? "EMPTY" : storedType.name();
    }

    public int getStoredAmount() {
        return storedAmount;
    }

    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (storedType != null) tag.putString("ResourceType", storedType.name());
        tag.putInt("StoredAmount", storedAmount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        try {
            storedType = tag.contains("ResourceType")
                    ? GasType.valueOf(tag.getString("ResourceType")) : null;
        } catch (IllegalArgumentException ignored) {
            storedType = null;
        }
        storedAmount = Math.max(0, Math.min(CAPACITY, tag.getInt("StoredAmount")));
        if (storedAmount == 0) storedType = null;
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
}
