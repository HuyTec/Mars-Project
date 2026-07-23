package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.screen.TeleportHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsAirSupplyHandler {

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        if (entity instanceof ServerPlayer player) {
            if (player.isCreative() || player.isSpectator()) return;
        }

        if (!entity.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) return;

        if (event.canBreathe()) {
            float progress = MarsTerraformProgress.get(serverLevel).getProgress();
            MarsEnvironmentStage stage = MarsEnvironmentManager.resolve(progress);

            if (!MarsEnvironmentManager.canLive(stage)) {
                event.setCanBreathe(false);
            }
        }
    }
}