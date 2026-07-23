package com.marsproject.terraformingmars.environment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;

public class MarsTerraformProgress extends SavedData {

    private static final String DATA_NAME = "terraforming_mars_progress";

    private float progress; // 0.0f = Mars nguyên thủy, 1.0f = terraform hoàn tất

    private MarsTerraformProgress(float progress) {
        this.progress = progress;
    }

    private static MarsTerraformProgress create() {
        return new MarsTerraformProgress(0.0f);
    }

    private static MarsTerraformProgress load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        MarsTerraformProgress data = create();
        data.progress = tag.getFloat("progress");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        tag.putFloat("progress", this.progress);
        return tag;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float newProgress) {
        this.progress = Mth.clamp(newProgress, 0.0f, 1.0f);
        this.setDirty();
    }

    /** Lấy (hoặc tạo mới nếu chưa có) instance gắn với level Mars truyền vào. */
    public static MarsTerraformProgress get(ServerLevel marsLevel) {
        SavedData.Factory<MarsTerraformProgress> factory =
                new SavedData.Factory<>(MarsTerraformProgress::create, MarsTerraformProgress::load, null);
        return marsLevel.getDataStorage().computeIfAbsent(factory, DATA_NAME);
    }
}