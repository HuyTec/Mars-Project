package com.marsproject.terraformingmars.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Snow-style layer block capped at seven because this mod has no layer-8 model. */
public class SevenLayerBlock extends SnowLayerBlock {
    public static final int MAX_LAYERS = 7;

    public SevenLayerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this) && existing.getValue(LAYERS) >= MAX_LAYERS) {
            return null;
        }

        BlockState placed = super.getStateForPlacement(context);
        if (placed != null && placed.is(this) && placed.getValue(LAYERS) > MAX_LAYERS) {
            return placed.setValue(LAYERS, MAX_LAYERS);
        }
        return placed;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return state.getValue(LAYERS) < MAX_LAYERS && super.canBeReplaced(state, context);
    }
}
