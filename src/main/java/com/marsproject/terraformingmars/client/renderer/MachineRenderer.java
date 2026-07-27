package com.marsproject.terraformingmars.client.renderer;

import com.marsproject.terraformingmars.block.entity.MachineBlockEntity;
import com.marsproject.terraformingmars.client.model.MachineModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class MachineRenderer extends GeoBlockRenderer<MachineBlockEntity> {
    public MachineRenderer(BlockEntityRendererProvider.Context context) {
        super(new MachineModel());
    }
}
