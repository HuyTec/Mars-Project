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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsEnvironmentEffectHandler {

    private static final int CHECK_INTERVAL_TICKS = 40; // 2 giay/lan
    private static final float UNSAFE_DAMAGE = 1.0f;    // 0.5 tim moi lan

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!player.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) return;
        // Dùng đúng cách check dimension Mars mà bạn đã sửa ở MarsEnvironmentSyncHandler —
        // copy y hệt điều kiện đó vào đây (mình không đoán lại tên class/field nữa).
        // if (!player.level().dimension().equals(<your Mars level key>)) return;

        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        float progress = MarsTerraformProgress.get((ServerLevel) player.level()).getProgress();
        MarsEnvironmentStage stage = MarsEnvironmentManager.resolve(progress);

        if (!MarsEnvironmentManager.canLive(stage)) {
            player.addEffect(new MobEffectInstance(
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