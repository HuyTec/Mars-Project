package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.registry.ModEffects;
import com.marsproject.terraformingmars.screen.TeleportHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;


@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsEnvironmentEffectHandler {

    private static final int CHECK_INTERVAL_TICKS = 40; // 2 giây

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // Chỉ xử lý sinh vật sống
        if (!(event.getEntity() instanceof LivingEntity entity))
            return;
        // Chỉ chạy server
        if (entity.level().isClientSide())
            return;
        // Chỉ chạy trên Mars
        if (!entity.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY))
            return;
        // Giảm tải: mỗi 2 giây kiểm tra 1 lần
        if (entity.tickCount % CHECK_INTERVAL_TICKS != 0)
            return;
        ServerLevel level = (ServerLevel) entity.level();
        float progress = MarsTerraformProgress
                .get(level)
                .getProgress();
        MarsEnvironmentStage stage =
                MarsEnvironmentManager.resolve(progress);
        // Nếu môi trường Mars chưa sống được
        if (!MarsEnvironmentManager.canLive(stage)) {
            entity.addEffect(new MobEffectInstance(
                    ModEffects.RADIATION,
                    60, // thời gian effect
                    0,  // cấp độ
                    false,
                    true,
                    true
            ));
        }
    }
}