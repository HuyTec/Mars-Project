package com.marsproject.terraformingmars.survival;

import com.marsproject.terraformingmars.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Server-authoritative space-suit sealing, oxygen and protection rules. */
public final class SpaceSuitService {
    public static final int MAX_OXYGEN = 600;
    public static final int OXYGEN_PER_CANISTER = 300;
    private static final String OXYGEN_KEY = "terraforming_mars_space_suit_oxygen";

    private SpaceSuitService() {
    }

    public static boolean isSealed(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SPACE_HELMET.get())
                && entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SPACE_CHESTPLATE.get())
                && entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SPACE_LEGGINGS.get())
                && entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SPACE_BOOTS.get());
    }

    public static ItemStack getChestplate(LivingEntity entity) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        return stack.is(ModItems.SPACE_CHESTPLATE.get()) ? stack : ItemStack.EMPTY;
    }

    public static int getOxygen(ItemStack chestplate) {
        if (!chestplate.is(ModItems.SPACE_CHESTPLATE.get())) {
            return 0;
        }
        CustomData data = chestplate.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return Math.max(0, Math.min(MAX_OXYGEN, data.copyTag().getInt(OXYGEN_KEY)));
    }

    public static int refill(ItemStack chestplate, int offered) {
        if (!chestplate.is(ModItems.SPACE_CHESTPLATE.get()) || offered <= 0) {
            return 0;
        }
        int current = getOxygen(chestplate);
        int accepted = Math.min(offered, MAX_OXYGEN - current);
        if (accepted > 0) {
            setOxygen(chestplate, current + accepted);
        }
        return accepted;
    }

    public static boolean trySupplyOxygen(LivingEntity entity) {
        if (!isSealed(entity)) {
            return false;
        }
        ItemStack chestplate = getChestplate(entity);
        int oxygen = getOxygen(chestplate);
        if (oxygen <= 0) {
            return false;
        }
        int maximumAir = Math.max(1, entity.getMaxAirSupply());
        if (entity.getAirSupply() <= maximumAir / 2) {
            setOxygen(chestplate, oxygen - 1);
            entity.setAirSupply(Math.min(maximumAir,
                    entity.getAirSupply() + Math.max(1, maximumAir / 2)));
        }
        return true;
    }

    public static double thermalInsulation(LivingEntity entity) {
        double protection = 0.0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SPACE_HELMET.get())) protection += 0.15;
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SPACE_CHESTPLATE.get())) protection += 0.35;
        if (entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SPACE_LEGGINGS.get())) protection += 0.25;
        if (entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SPACE_BOOTS.get())) protection += 0.10;
        return Math.min(0.85, protection);
    }

    public static double radiationProtection(LivingEntity entity) {
        int pieces = 0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SPACE_HELMET.get())) pieces++;
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SPACE_CHESTPLATE.get())) pieces++;
        if (entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SPACE_LEGGINGS.get())) pieces++;
        if (entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SPACE_BOOTS.get())) pieces++;
        return pieces * 0.25;
    }

    private static void setOxygen(ItemStack chestplate, int amount) {
        int clamped = Math.max(0, Math.min(MAX_OXYGEN, amount));
        CustomData.update(DataComponents.CUSTOM_DATA, chestplate,
                tag -> tag.putInt(OXYGEN_KEY, clamped));
    }
}
