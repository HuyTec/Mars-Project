package com.marsproject.terraformingmars.block.entity;

import com.marsproject.terraformingmars.block.CableBlock;
import com.marsproject.terraformingmars.block.UpsBlock;
import com.marsproject.terraformingmars.power.PowerNetworkScanner;
import com.marsproject.terraformingmars.power.PowerNetworkSnapshot;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class UpsBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final int NETWORK_SCAN_INTERVAL = 10;
    private static final int ENERGY_CAPACITY = 500_000;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private PowerNetworkSnapshot network = PowerNetworkSnapshot.empty();
    private int storedEnergy;

    public UpsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPS.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos,
                                  BlockState state, UpsBlockEntity ups) {
        if (!(level instanceof ServerLevel serverLevel)
                || serverLevel.getGameTime() % NETWORK_SCAN_INTERVAL != 0) {
            return;
        }
        ups.refreshNetwork(serverLevel, state);
    }

    private void refreshNetwork(ServerLevel level, BlockState state) {
        BlockPos inputCable = UpsBlock.inputCablePos(worldPosition, state);
        if (level.getBlockState(inputCable).getBlock() instanceof CableBlock) {
            network = PowerNetworkScanner.scan(level, inputCable);
        } else {
            network = PowerNetworkSnapshot.empty();
        }

        if (network.totalWatts() > 0) {
            int generatedEnergy = Math.max(
                    1,
                    network.totalWatts() * NETWORK_SCAN_INTERVAL / 20
            );
            storedEnergy = Math.min(ENERGY_CAPACITY, storedEnergy + generatedEnergy);
        }

        boolean online = storedEnergy > 0;
        if (state.getValue(UpsBlock.POWERED) != online) {
            level.setBlock(worldPosition, state.setValue(UpsBlock.POWERED, online),
                    Block.UPDATE_CLIENTS);
        }
        setChanged();
    }

    public boolean isOnline() {
        return getBlockState().getValue(UpsBlock.POWERED);
    }

    public int getTotalWatts() {
        return network.totalWatts();
    }

    public int getGeneratorCount() {
        return network.generators().size();
    }

    public int getStoredEnergy() {
        return storedEnergy;
    }

    public int getEnergyCapacity() {
        return ENERGY_CAPACITY;
    }

    /**
     * Extracts energy for an output-side consumer and returns the amount supplied.
     * Consumers added to the power network should call this method for their load.
     */
    public int consumeEnergy(int requestedEnergy) {
        int supplied = Math.min(Math.max(0, requestedEnergy), storedEnergy);
        if (supplied == 0) {
            return 0;
        }

        storedEnergy -= supplied;
        setChanged();
        updatePoweredState();
        return supplied;
    }

    /**
     * Atomically extracts the complete request or leaves stored energy unchanged.
     */
    public boolean tryConsumeEnergy(int requestedEnergy) {
        if (requestedEnergy <= 0) {
            return true;
        }
        if (storedEnergy < requestedEnergy) {
            return false;
        }

        storedEnergy -= requestedEnergy;
        setChanged();
        updatePoweredState();
        return true;
    }

    public PowerNetworkSnapshot getNetworkSnapshot() {
        return network;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("StoredEnergy", storedEnergy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedEnergy = Mth.clamp(tag.getInt("StoredEnergy"), 0, ENERGY_CAPACITY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void updatePoweredState() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        boolean powered = storedEnergy > 0;
        if (state.getValue(UpsBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(UpsBlock.POWERED, powered),
                    Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
