package com.marsproject.terraformingmars.item;

import com.marsproject.terraformingmars.screen.TeleportHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class MarsBeaconItem extends Item {

    public MarsBeaconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<net.minecraft.world.item.ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            TeleportHelper.teleportToMars(serverPlayer);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}