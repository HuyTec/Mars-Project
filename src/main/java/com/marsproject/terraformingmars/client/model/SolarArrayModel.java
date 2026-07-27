package com.marsproject.terraformingmars.client.model;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.entity.SolarArrayBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public final class SolarArrayModel extends GeoModel<SolarArrayBlockEntity> {
    @Override
    public ResourceLocation getModelResource(SolarArrayBlockEntity animatable) {
        return animatable.getArrayType().model();
    }

   @Override
    public ResourceLocation getTextureResource(SolarArrayBlockEntity solar) {
        ResourceLocation baseTexture = solar.getArrayType().texture();

        String suffix = switch (solar.getDustStage()) {
            case 1 -> "_dust_light";
            case 2 -> "_dust_medium";
            case 3 -> "_dust_heavy";
            default -> "";
        };

        if (suffix.isEmpty()) {
            return baseTexture;
        }

        String basePath = baseTexture.getPath();
        String dustyPath = basePath.endsWith(".png")
                ? basePath.substring(0, basePath.length() - 4) + suffix + ".png"
                : basePath + suffix;

        return new ResourceLocation(
                baseTexture.getNamespace(),
                dustyPath
        );
    }

    @Override
    public ResourceLocation getAnimationResource(SolarArrayBlockEntity animatable) {
        return animatable.getArrayType().animation();
    }

    @Override
    public void setCustomAnimations(SolarArrayBlockEntity solar, long instanceId,
                                    AnimationState<SolarArrayBlockEntity> state) {
        super.setCustomAnimations(solar, instanceId, state);
        GeoBone panel = getAnimationProcessor().getBone("panel");
        if (panel == null || solar.getLevel() == null) {
            return;
        }

        float targetYaw = 0.0F;
        float targetPitch = 0.0F;
        if (solar.isTracking()) {
            float sunAngle = solar.getLevel().getSunAngle(state.getPartialTick());
            float sunHeight = Mth.cos(sunAngle);

            // Below the horizon means night: keep both targets at zero so the
            // motor slowly parks the panel at its original placement pose.
            if (sunHeight > 0.0F) {
                float absoluteSunYaw =
                        (float) Math.toDegrees(Math.atan2(-Math.sin(sunAngle), 0.001)) + 0.0F;
                float placedYaw = solar.getBlockState()
                        .getValue(com.marsproject.terraformingmars.block.SolarArrayBlock.FACING)
                        .toYRot();
                Direction placedFacing = solar.getBlockState()
                        .getValue(com.marsproject.terraformingmars.block.SolarArrayBlock.FACING);
                float northSouthCorrection =
                        placedFacing.getAxis() == Direction.Axis.Z ? 180.0F : 0.0F;
                targetYaw = Mth.wrapDegrees(
                        absoluteSunYaw - placedYaw + northSouthCorrection
                );
                targetPitch = Mth.clamp(
                        (float) Math.toDegrees(Math.atan2(sunHeight, 1.0)),
                        -30.0F,
                        30.0F
                );
            }
        }

        // Slow motor movement. wrapDegrees always chooses the shortest turn.
        float yaw = solar.getRenderedYaw()
                + Mth.wrapDegrees(targetYaw - solar.getRenderedYaw()) * 0.025F;
        float pitch = Mth.lerp(0.025F, solar.getRenderedPitch(), targetPitch);
        solar.setRenderedYaw(yaw);
        solar.setRenderedPitch(pitch);

        panel.setRotY((float) Math.toRadians(yaw));
        panel.setRotX((float) Math.toRadians(pitch));
    }
}
