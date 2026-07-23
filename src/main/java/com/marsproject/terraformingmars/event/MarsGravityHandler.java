package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.screen.TeleportHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import com.marsproject.terraformingmars.TerraformingMarsMod;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsGravityHandler {

    private static final double VANILLA_GRAVITY = 0.08D;
    private static final double MARS_GRAVITY_FACTOR = 0.38D;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        // Chỉ áp dụng cho LivingEntity (player, mob) — loại trừ item, projectile, falling block, v.v.
        if (!(entity instanceof LivingEntity living)) return;

        if (!living.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) return;
        if (living.isNoGravity()) return;
        if (living.onGround()) return;

        double compensation = VANILLA_GRAVITY * (1.0D - MARS_GRAVITY_FACTOR);
        Vec3 motion = living.getDeltaMovement();
        living.setDeltaMovement(motion.x, motion.y + compensation, motion.z);
    }
}