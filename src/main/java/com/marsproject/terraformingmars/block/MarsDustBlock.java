package com.marsproject.terraformingmars.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FallingBlock;

public class MarsDustBlock extends FallingBlock {

    public static final MapCodec<MarsDustBlock> CODEC =
            simpleCodec(MarsDustBlock::new);

    public MarsDustBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MarsDustBlock> codec() {
        return CODEC;
    }
}