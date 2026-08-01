package com.marsproject.terraformingmars.block;

import com.marsproject.terraformingmars.block.entity.ResourceTankBlockEntity;
import com.marsproject.terraformingmars.pipe.PipeConnectable;
import com.marsproject.terraformingmars.pipe.PipeType;
import com.marsproject.terraformingmars.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ResourceTankBlock extends BaseEntityBlock implements PipeConnectable {
    private final PipeType pipeType;

    public ResourceTankBlock(BlockBehaviour.Properties properties, PipeType pipeType) {
        super(properties);
        this.pipeType = pipeType;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    public PipeType getPipeType() {
        return pipeType;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResourceTankBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(
                type, ModBlockEntities.RESOURCE_TANK.get(), ResourceTankBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ResourceTankBlockEntity tank) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.terraforming_mars.tank_status",
                    tank.getStoredTypeName(), tank.getStoredAmount(), tank.getCapacity()), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (pipeType != PipeType.FLUID
                || !(level.getBlockEntity(pos) instanceof ResourceTankBlockEntity tank)) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(Items.WATER_BUCKET) && tank.insertWater(1_000, level.isClientSide()) == 1_000) {
            if (!level.isClientSide() && !player.hasInfiniteMaterials()) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (stack.is(Items.BUCKET) && tank.extractWater(1_000, level.isClientSide()) == 1_000) {
            if (!level.isClientSide() && !player.hasInfiniteMaterials()) {
                stack.shrink(1);
                if (!player.getInventory().add(new ItemStack(Items.WATER_BUCKET))) {
                    player.drop(new ItemStack(Items.WATER_BUCKET), false);
                }
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean canConnectPipe(LevelReader level, BlockPos tankPos,
                                  BlockState tankState, BlockPos pipePos) {
        return level.getBlockState(pipePos).getBlock() instanceof PipeBlock pipe
                && pipe.getPipeType() == pipeType;
    }
}
