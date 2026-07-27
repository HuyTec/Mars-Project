package com.marsproject.terraformingmars.client.renderer;

import com.marsproject.terraformingmars.block.entity.CableBlockEntity;
import com.marsproject.terraformingmars.client.model.CableModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class CableRenderer extends GeoBlockRenderer<CableBlockEntity> {
    public CableRenderer(BlockEntityRendererProvider.Context context) {
        super(new CableModel());
    }
}
