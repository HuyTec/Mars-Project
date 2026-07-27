package com.marsproject.terraformingmars.block.entity;

import com.marsproject.terraformingmars.block.SolarArrayBlock;
import com.marsproject.terraformingmars.block.SolarArrayType;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class SolarArrayBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean tracking;
    private float dustPenalty;
    private int currentWatts;
    private float renderedYaw;
    private float renderedPitch;

    public SolarArrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_ARRAY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  SolarArrayBlockEntity solar) {
        if (!(state.getBlock() instanceof SolarArrayBlock block)) {
            return;
        }

        SolarArrayType type = block.getArrayType();
        int previousDustStage = solar.getDustStage();
        int previousWatts = solar.currentWatts;

        if (level.canSeeSky(pos.above())) {
            solar.dustPenalty = Math.min(
                    type.maxDustPenalty(),
                    solar.dustPenalty + type.dustAccumulationRate()
            );
        }

        float dayFactor = level.isDay() && level.canSeeSky(pos.above()) ? 1.0F : 0.0F;
        solar.currentWatts = Math.max(0, Math.round(
                type.baseWattage() * dayFactor * (1.0F - solar.dustPenalty)
        ));

        int currentDustStage = solar.getDustStage();
        if (currentDustStage != previousDustStage) {
            solar.setChanged();
            solar.syncToClient();
        } else if (solar.currentWatts != previousWatts) {
            solar.setChanged();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "solar_controller", 10,
                state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean toggleTracking() {
        tracking = !tracking;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return tracking;
    }

    public boolean cleanDust() {
        if (dustPenalty <= 0.0F) {
            return false;
        }
        dustPenalty = 0.0F;
        setChanged();
        syncToClient();
        return true;
    }

    public int getCurrentWatts() {
        return currentWatts;
    }

    public float getDustPenalty() {
        return dustPenalty;
    }

    public SolarArrayType getArrayType() {
        if (getBlockState().getBlock() instanceof SolarArrayBlock block) {
            return block.getArrayType();
        }
        throw new IllegalStateException("SolarArrayBlockEntity is not attached to a SolarArrayBlock");
    }

    public boolean isTracking() {
        return tracking;
    }

    public float getRenderedYaw() {
        return renderedYaw;
    }

    public void setRenderedYaw(float renderedYaw) {
        this.renderedYaw = renderedYaw;
    }

    public float getRenderedPitch() {
        return renderedPitch;
    }

    public void setRenderedPitch(float renderedPitch) {
        this.renderedPitch = renderedPitch;
    }

    public int getDustStage() {
        SolarArrayType type = getArrayType();

        if (type.maxDustPenalty() <= 0.0F) {
            return 0;
        }

        float normalizedDust = dustPenalty / type.maxDustPenalty();

        if (normalizedDust < 0.25F) return 0;
        if (normalizedDust < 0.50F) return 1;
        if (normalizedDust < 0.75F) return 2;
        return 3;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("TrackingSun", tracking);
        tag.putFloat("DustPenalty", dustPenalty);
        tag.putInt("CurrentWatts", currentWatts);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tracking = tag.getBoolean("TrackingSun");
        SolarArrayType type = getArrayType();
        dustPenalty = Mth.clamp(tag.getFloat("DustPenalty"), 0.0F, type.maxDustPenalty());
        currentWatts = Math.max(0, tag.getInt("CurrentWatts"));
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
