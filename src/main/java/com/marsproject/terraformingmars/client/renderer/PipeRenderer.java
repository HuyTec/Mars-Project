package com.marsproject.terraformingmars.client.renderer;

import com.marsproject.terraformingmars.block.entity.PipeBlockEntity;
import com.marsproject.terraformingmars.client.model.PipeModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class PipeRenderer extends GeoBlockRenderer<PipeBlockEntity> {
    public PipeRenderer(BlockEntityRendererProvider.Context context) {
        super(new PipeModel());
    }
}
