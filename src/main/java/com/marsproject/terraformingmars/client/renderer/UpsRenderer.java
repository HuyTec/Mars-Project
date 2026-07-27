package com.marsproject.terraformingmars.client.renderer;

import com.marsproject.terraformingmars.block.entity.UpsBlockEntity;
import com.marsproject.terraformingmars.client.model.UpsModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class UpsRenderer extends GeoBlockRenderer<UpsBlockEntity> {
    public UpsRenderer(BlockEntityRendererProvider.Context context) {
        super(new UpsModel());
    }
}
