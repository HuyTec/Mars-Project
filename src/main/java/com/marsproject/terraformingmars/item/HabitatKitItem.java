package com.marsproject.terraformingmars.item;

import com.marsproject.terraformingmars.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Single-use landing module that unfolds into the first pressurised shelter.
 * Machines are deliberately not placed automatically: wiring the base is the
 * player's first Stage 0 objective.
 */
public final class HabitatKitItem extends Item {
    public HabitatKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos().relative(context.getClickedFace());

        if (!hasRoom(level, origin)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.terraforming_mars.habitat_obstructed"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            buildHabitat(level, origin);
            if (context.getPlayer() instanceof ServerPlayer player && !player.isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static boolean hasRoom(Level level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-3, 0, -3), origin.offset(3, 4, 3))) {
            if (!level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    private static void buildHabitat(Level level, BlockPos origin) {
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 4; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean shell = x == -3 || x == 3 || y == 0 || y == 4 || z == -3 || z == 3;
                    if (!shell) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    } else if (y == 2 && (x == -3 || x == 3 || z == -3 || z == 3)
                            && Math.abs(x + z) <= 1) {
                        level.setBlockAndUpdate(pos, Blocks.TINTED_GLASS.defaultBlockState());
                    } else {
                        level.setBlockAndUpdate(pos, Blocks.IRON_BLOCK.defaultBlockState());
                    }
                }
            }
        }

        // Two-block airlock opening. Iron doors are supplied in the landing kit.
        level.setBlockAndUpdate(origin.offset(0, 1, -3), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(0, 2, -3), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(0, 1, 0), ModBlocks.LIFE_SUPPORT_UNIT.get().defaultBlockState());
    }
}
