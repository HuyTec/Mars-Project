package com.marsproject.terraformingmars.item;

import com.marsproject.terraformingmars.survival.SpaceSuitService;
import com.marsproject.terraformingmars.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A portable refill consumed directly into an equipped space-suit chestplate. */
public final class OxygenCanisterItem extends Item {
    public OxygenCanisterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack canister = player.getItemInHand(hand);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (level.isClientSide()) {
            boolean canRefill = chestplate.is(ModItems.SPACE_CHESTPLATE.get())
                    && SpaceSuitService.getOxygen(chestplate) < SpaceSuitService.MAX_OXYGEN;
            return canRefill
                    ? InteractionResultHolder.success(canister)
                    : InteractionResultHolder.fail(canister);
        }
        int accepted = SpaceSuitService.refill(chestplate, SpaceSuitService.OXYGEN_PER_CANISTER);
        if (accepted <= 0) {
            player.displayClientMessage(Component.translatable(
                    "message.terraforming_mars.space_suit_refill_failed"), true);
            return InteractionResultHolder.fail(canister);
        }

        if (!player.getAbilities().instabuild) {
            canister.shrink(1);
        }
        player.displayClientMessage(Component.translatable(
                "message.terraforming_mars.space_suit_refilled", accepted), true);
        return InteractionResultHolder.sidedSuccess(canister, level.isClientSide());
    }
}
