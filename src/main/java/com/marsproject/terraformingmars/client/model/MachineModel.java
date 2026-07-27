package com.marsproject.terraformingmars.client.model;

import com.marsproject.terraformingmars.block.entity.MachineBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MachineModel extends GeoModel<MachineBlockEntity> {
    @Override
    public ResourceLocation getModelResource(MachineBlockEntity animatable) {
        return animatable.getMachineType().model();
    }

    @Override
    public ResourceLocation getTextureResource(MachineBlockEntity animatable) {
        return animatable.getMachineType().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(MachineBlockEntity animatable) {
        return animatable.getMachineType().animation();
    }
}
