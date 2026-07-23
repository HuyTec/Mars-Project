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
        if (!(entity instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!player.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) return;

        // Vanilla mac dinh nghi la "tho duoc" vi khong o duoi nuoc.
        // Neu moi truong Mars khong an toan, ep canBreathe = false
        // de vanilla tu tru khi va tu gay damage drown khi het khi.
        if (event.canBreathe()) {
            float progress = MarsTerraformProgress.get((ServerLevel) player.level()).getProgress();
            MarsEnvironmentStage stage = MarsEnvironmentManager.resolve(progress);

            if (!MarsEnvironmentManager.canLive(stage)) {
                event.setCanBreathe(false);
                // Neu muon khi mat nhanh hon mac dinh (1/tick), co the tang:
                // event.setConsumeAirAmount(2);
            }
        }
    }
}