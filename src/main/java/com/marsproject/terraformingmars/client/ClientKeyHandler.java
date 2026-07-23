package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = TerraformingMarsMod.MODID,
        bus = EventBusSubscriber.Bus.GAME,
        value = Dist.CLIENT
)
public class ClientKeyHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        while (ModKeyMappings.TOGGLE_MARS_HUD.consumeClick()) {

            MarsEnvironmentHud.visible = !MarsEnvironmentHud.visible;

        }

    }
}