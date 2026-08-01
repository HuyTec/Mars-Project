package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.atmosphere.RoomAtmosphereManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import com.marsproject.terraformingmars.survival.SpaceSuitService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public final class MarsAirSupplyHandler {
    private MarsAirSupplyHandler() {
    }

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()
                || !entity.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) {
            return;
        }

        ServerLevel level = (ServerLevel) entity.level();
        float progress = MarsTerraformProgress.get(level).getProgress();
        MarsEnvironmentStage stage = MarsEnvironmentManager.resolve(progress);

        if (MarsEnvironmentManager.canLive(stage)
                || RoomAtmosphereManager.hasBreathableAir(level, entity.blockPosition())
                || SpaceSuitService.trySupplyOxygen(entity)) {
            event.setCanBreathe(true);
            return;
        }
        event.setCanBreathe(false);
    }
}
