package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.atmosphere.RoomAtmosphereManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Invisible marker for a block-sized volume of pressurized breathable air. */
public final class BreathableAirBlock extends Block {
    public static final MapCodec<BreathableAirBlock> CODEC = simpleCodec(BreathableAirBlock::new);

    public BreathableAirBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BreathableAirBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   net.minecraft.world.level.block.Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        RoomAtmosphereManager.ventIfExposed(level, pos);
    }
}
