package com.marsproject.terraformingmars.block.entity;

import com.marsproject.terraformingmars.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MultiblockPartBlockEntity extends BlockEntity {
    private BlockPos anchorRelative = BlockPos.ZERO;
    private int partIndex = -1;
    private ResourceLocation controllerId;

    public MultiblockPartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIBLOCK_PART.get(), pos, state);
    }

    public void configure(BlockPos anchorRelative, int partIndex, Block controller) {
        this.anchorRelative = anchorRelative.immutable();
        this.partIndex = partIndex;
        this.controllerId = BuiltInRegistries.BLOCK.getKey(controller);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getControllerPos() {
        return worldPosition.offset(anchorRelative);
    }

    public int getPartIndex() {
        return partIndex;
    }

    public boolean belongsTo(BlockPos anchor, Block controller) {
        return getControllerPos().equals(anchor)
                && BuiltInRegistries.BLOCK.getKey(controller).equals(controllerId);
    }

    public boolean matchesController(Block controller) {
        return BuiltInRegistries.BLOCK.getKey(controller).equals(controllerId);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("AnchorRelative", anchorRelative.asLong());
        tag.putInt("PartIndex", partIndex);
        if (controllerId != null) {
            tag.putString("Controller", controllerId.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        anchorRelative = BlockPos.of(tag.getLong("AnchorRelative"));
        partIndex = tag.getInt("PartIndex");
        controllerId = ResourceLocation.tryParse(tag.getString("Controller"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
