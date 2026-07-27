package com.marsproject.terraformingmars.client.model;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.CableBlock;
import com.marsproject.terraformingmars.block.entity.CableBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public final class CableModel extends GeoModel<CableBlockEntity> {
    @Override
    public ResourceLocation getModelResource(CableBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "geo/cable.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CableBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "textures/block/cable.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CableBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "animations/cable.animation.json");
    }

    @Override
    public void setCustomAnimations(CableBlockEntity cable, long instanceId,
                                    AnimationState<CableBlockEntity> state) {
        super.setCustomAnimations(cable, instanceId, state);

        setHidden("north", !cable.getBlockState().getValue(CableBlock.NORTH));
        setHidden("south", !cable.getBlockState().getValue(CableBlock.SOUTH));
        setHidden("east", !cable.getBlockState().getValue(CableBlock.EAST));
        setHidden("west", !cable.getBlockState().getValue(CableBlock.WEST));
        setHidden("up", !cable.getBlockState().getValue(CableBlock.UP));
        setHidden("down", !cable.getBlockState().getValue(CableBlock.DOWN));
    }

    private void setHidden(String name, boolean hidden) {
        GeoBone bone = getAnimationProcessor().getBone(name);
        if (bone != null) bone.setHidden(hidden);
    }
}
