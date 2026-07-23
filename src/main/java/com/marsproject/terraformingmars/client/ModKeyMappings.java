package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = TerraformingMarsMod.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModKeyMappings {

    public static final KeyMapping TOGGLE_MARS_HUD = new KeyMapping(
            "key.terraforming_mars.toggle_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.terraforming_mars"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MARS_HUD);
    }
}