package com.marsproject.terraformingmars.client;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(modid = "terraforming_mars", value = Dist.CLIENT)
public class MarsFogHandler {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        // Chỉ tắt fog khi ở dimension Mars
        if (level.dimension().location().equals(
               new net.minecraft.resources.ResourceLocation("terraforming_mars:mars"))) {
            event.setCanceled(true);
        }
    }
}