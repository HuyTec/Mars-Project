package com.marsproject.terraformingmars.item;

import com.marsproject.terraformingmars.survival.SpaceSuitService;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class SpaceSuitItem extends ArmorItem {
    public SpaceSuitItem(Holder<ArmorMaterial> material, Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (getType() == Type.CHESTPLATE) {
            tooltip.add(Component.translatable(
                    "tooltip.terraforming_mars.space_suit_oxygen",
                    SpaceSuitService.getOxygen(stack), SpaceSuitService.MAX_OXYGEN));
        }
    }
}
