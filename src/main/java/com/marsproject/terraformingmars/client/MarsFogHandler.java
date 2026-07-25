package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import com.marsproject.terraformingmars.weather.MarsWeatherBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = TerraformingMarsMod.MODID, value = Dist.CLIENT)
public final class MarsFogHandler {
    private static final float DUST_FOG_RED = 0.63F;
    private static final float DUST_FOG_GREEN = 0.28F;
    private static final float DUST_FOG_BLUE = 0.12F;
    private static final float DRY_ICE_FOG_RED = 0.95F;
    private static final float DRY_ICE_FOG_GREEN = 0.98F;
    private static final float DRY_ICE_FOG_BLUE = 1.0F;

    private MarsFogHandler() {
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        var level = Minecraft.getInstance().level;
        if (level == null
                || !level.dimension().equals(TeleportHelper.MARS_LEVEL_KEY)
                || event.getType() != FogType.NONE) {
            return;
        }

        float storm = localStormIntensity(level);
        float originalFar = event.getFarPlaneDistance();
        event.setNearPlaneDistance(originalFar * Mth.lerp(storm, 0.55F, 0.04F));
        event.setFarPlaneDistance(originalFar * Mth.lerp(storm, 1.4F, 0.24F));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().equals(TeleportHelper.MARS_LEVEL_KEY)) {
            return;
        }

        boolean cryotic = isPlayerInCryotic(level);
        if (cryotic && !ClientMarsWeatherData.isDustStorm()) {
            event.setRed(Mth.lerp(0.68F, event.getRed(), DRY_ICE_FOG_RED));
            event.setGreen(Mth.lerp(0.68F, event.getGreen(), DRY_ICE_FOG_GREEN));
            event.setBlue(Mth.lerp(0.68F, event.getBlue(), DRY_ICE_FOG_BLUE));
        }

        float localStorm = localStormIntensity(level);
        if (localStorm <= 0.0F) {
            return;
        }

        boolean dryIce = ClientMarsWeatherData.isDryIceStorm();
        float blend = localStorm * 0.72F;
        event.setRed(Mth.lerp(blend, event.getRed(),
                dryIce ? DRY_ICE_FOG_RED : DUST_FOG_RED));
        event.setGreen(Mth.lerp(blend, event.getGreen(),
                dryIce ? DRY_ICE_FOG_GREEN : DUST_FOG_GREEN));
        event.setBlue(Mth.lerp(blend, event.getBlue(),
                dryIce ? DRY_ICE_FOG_BLUE : DUST_FOG_BLUE));
    }

    private static float localStormIntensity(net.minecraft.client.multiplayer.ClientLevel level) {
        if (ClientMarsWeatherData.isDustStorm()) {
            return ClientMarsWeatherData.visualIntensity() / 3.0F;
        }
        if (ClientMarsWeatherData.isDryIceStorm()) {
            if (isPlayerInCryotic(level)) {
                return ClientMarsWeatherData.visualIntensity() / 3.0F;
            }
        }
        return 0.0F;
    }

    private static boolean isPlayerInCryotic(
            net.minecraft.client.multiplayer.ClientLevel level) {
        var player = Minecraft.getInstance().player;
        return player != null && MarsWeatherBiomes.isCryotic(
                level, BlockPos.containing(player.position()));
    }
}
