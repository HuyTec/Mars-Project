package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/** Mobility bonuses supplied specifically by the space-suit leggings. */
@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public final class SpaceSuitMobilityHandler {
    private static final double JUMP_MULTIPLIER = 1.10;
    private static final float FALL_DAMAGE_MULTIPLIER = 0.80F;

    private SpaceSuitMobilityHandler() {
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SPACE_LEGGINGS.get())) {
            return;
        }
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x, movement.y * JUMP_MULTIPLIER, movement.z);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity().getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SPACE_LEGGINGS.get())) {
            event.setDamageMultiplier(event.getDamageMultiplier() * FALL_DAMAGE_MULTIPLIER);
        }
    }
}
