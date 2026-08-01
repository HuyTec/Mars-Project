package com.marsproject.terraformingmars.client.model;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.PipeBlock;
import com.marsproject.terraformingmars.block.entity.PipeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public final class PipeModel extends GeoModel<PipeBlockEntity> {
    @Override
    public ResourceLocation getModelResource(PipeBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "geo/pipe.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PipeBlockEntity animatable) {
        PipeBlock block = (PipeBlock) animatable.getBlockState().getBlock();
        String texture = switch (block.getPipeType()) {
            case FLUID -> "textures/block/fluid_pipe.png";
            case HEAT -> "textures/block/heat_pipe.png";
            default -> "textures/block/pipe.png";
        };
        return new ResourceLocation(TerraformingMarsMod.MODID, texture);
    }

    @Override
    public ResourceLocation getAnimationResource(PipeBlockEntity animatable) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "animations/pipe.animation.json");
    }

    @Override
    public void setCustomAnimations(PipeBlockEntity pipe, long instanceId,
                                    AnimationState<PipeBlockEntity> state) {
        super.setCustomAnimations(pipe, instanceId, state);

        setHidden("north", !pipe.getBlockState().getValue(PipeBlock.NORTH));
        setHidden("south", !pipe.getBlockState().getValue(PipeBlock.SOUTH));
        setHidden("east", !pipe.getBlockState().getValue(PipeBlock.EAST));
        setHidden("west", !pipe.getBlockState().getValue(PipeBlock.WEST));
        setHidden("up", !pipe.getBlockState().getValue(PipeBlock.UP));
        setHidden("down", !pipe.getBlockState().getValue(PipeBlock.DOWN));
    }

    private void setHidden(String name, boolean hidden) {
        GeoBone bone = getAnimationProcessor().getBone(name);
        if (bone != null) {
            bone.setHidden(hidden);
        }
    }
}
