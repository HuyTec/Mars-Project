package com.marsproject.terraformingmars.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.marsproject.terraformingmars.block.entity.SolarArrayBlockEntity;
import com.marsproject.terraformingmars.client.model.SolarArrayModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class SolarArrayRenderer extends GeoBlockRenderer<SolarArrayBlockEntity> {
    public SolarArrayRenderer(BlockEntityRendererProvider.Context context) {
        super(new SolarArrayModel());
    }

    @Override
    public void preRender(PoseStack poseStack, SolarArrayBlockEntity animatable,
                          BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        float scale = animatable.getArrayType().modelScale();
        poseStack.scale(scale, scale, scale);
    }
}
