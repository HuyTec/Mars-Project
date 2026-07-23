package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;

import com.marsproject.terraformingmars.registry.ModEffects;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsEnvironmentEffectHandler {

    private static final int CHECK_INTERVAL_TICKS = 40; // 2 giay/lan
    private static final float UNSAFE_DAMAGE = 1.0f;    // 0.5 tim moi lan

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        if (!serverLevel.dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) return;

        // Bỏ qua Creative/Spectator
        if (entity instanceof ServerPlayer player) {
            if (player.isCreative() || player.isSpectator()) return;
        }

        if (entity.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        float progress = MarsTerraformProgress.get(serverLevel).getProgress();
        MarsEnvironmentStage stage = MarsEnvironmentManager.resolve(progress);

        if (!MarsEnvironmentManager.canLive(stage)) {
            entity.addEffect(new MobEffectInstance(
                    ModEffects.RADIATION,
                    60,
                    0,
                    false,
                    true,
                    true
            ));
        }
    }
}