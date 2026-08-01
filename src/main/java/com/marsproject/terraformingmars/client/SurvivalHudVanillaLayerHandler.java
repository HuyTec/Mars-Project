package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Replaces selected vanilla icon layers with the unified survival bars. */
@EventBusSubscriber(modid = TerraformingMarsMod.MODID, value = Dist.CLIENT)
public final class SurvivalHudVanillaLayerHandler {
    private SurvivalHudVanillaLayerHandler() {
    }

    @SubscribeEvent
    public static void beforeVanillaLayer(RenderGuiLayerEvent.Pre event) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }

        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)
                || event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)
                || event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)
                || event.getName().equals(VanillaGuiLayers.AIR_LEVEL)) {
            event.setCanceled(true);
        }
    }
}
