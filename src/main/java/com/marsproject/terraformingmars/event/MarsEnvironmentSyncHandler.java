package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;

import com.marsproject.terraformingmars.screen.TeleportHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsEnvironmentSyncHandler {

    private static final int SYNC_INTERVAL_TICKS = 20; // 1 giay/lan, tranh spam goi tin

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) return;
        if (player.tickCount % SYNC_INTERVAL_TICKS != 0) return;

        float progress = MarsTerraformProgress.get((ServerLevel) player.level()).getProgress();
        MarsEnvironmentStage stage = MarsEnvironmentManager.resolve(progress);
        double oxygen = stage.atmosphereComposition().getOrDefault("oxygen", 0.0);

        PacketDistributor.sendToPlayer(player, new MarsEnvironmentSyncPayload(
                stage.radiation(),
                stage.atmospherePressurePercent(),
                oxygen,
                stage.temperatureCelsius(),
                stage.waterPercent(),
                stage.biologyPercent(),
                MarsEnvironmentManager.canLive(stage),
                stage.progress()
        ));
    }
}