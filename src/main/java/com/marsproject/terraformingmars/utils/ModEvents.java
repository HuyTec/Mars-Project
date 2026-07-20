package com.marsproject.terraformingmars.utils;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.network.OpenIntroPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class ModEvents {

    private static final String NBT_KEY = "terraforming_mars_first_join_done";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CompoundTag persistentData = serverPlayer.getPersistentData();

            if (!persistentData.getBoolean(NBT_KEY)) {
                persistentData.putBoolean(NBT_KEY, true);

                PacketDistributor.sendToPlayer(serverPlayer, new OpenIntroPayload());
            }
        }
    }
}