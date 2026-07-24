package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.screen.TeleportHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsAirSupplyHandler {
    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        LivingEntity entity = event.getEntity();
        // Chỉ chạy server
        if (entity.level().isClientSide())
            return;
        // Chỉ chạy trên Mars
        if (!entity.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY))
            return;
        ServerLevel level = (ServerLevel) entity.level();
        float progress = MarsTerraformProgress
                .get(level)
                .getProgress();
        MarsEnvironmentStage stage =
                MarsEnvironmentManager.resolve(progress);
        // Mars chưa có khí quyển -> không thể thở
        if (!MarsEnvironmentManager.canLive(stage)) {
            event.setCanBreathe(false);
            // Có thể tăng tốc độ mất oxy:
            // event.setConsumeAirAmount(2);
        }
    }
}