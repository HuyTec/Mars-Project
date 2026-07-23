package com.marsproject.terraformingmars.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
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

        if (level.dimension().location().equals(new
                ResourceLocation("terraforming_mars:mars"))) {

            // Đẩy fog ra xa hơn thay vì tắt hẳn -> fog "nhạt" hơn, ít che tầm nhìn
            event.setNearPlaneDistance(event.getFarPlaneDistance() * 0.6F);
            event.setFarPlaneDistance(event.getFarPlaneDistance() * 1.8F);
        }
    }
}