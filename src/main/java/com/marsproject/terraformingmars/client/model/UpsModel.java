package com.marsproject.terraformingmars.client.model;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.UpsBlock;
import com.marsproject.terraformingmars.block.entity.UpsBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public final class UpsModel extends GeoModel<UpsBlockEntity> {
    @Override
    public ResourceLocation getModelResource(UpsBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "geo/ups.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UpsBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "textures/block/ups.png");
    }

    @Override
    public ResourceLocation getAnimationResource(UpsBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "animations/ups.animation.json");
    }

    @Override
    public void setCustomAnimations(UpsBlockEntity ups, long instanceId,
                                    AnimationState<UpsBlockEntity> state) {
        super.setCustomAnimations(ups, instanceId, state);
        boolean online = ups.getBlockState().getValue(UpsBlock.POWERED);
        setHidden("on", !online);
        setHidden("off", online);
    }

    private void setHidden(String name, boolean hidden) {
        GeoBone bone = getAnimationProcessor().getBone(name);
        if (bone != null) {
            bone.setHidden(hidden);
        }
    }
}
