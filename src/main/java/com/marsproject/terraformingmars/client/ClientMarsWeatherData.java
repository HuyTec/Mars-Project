package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.network.MarsWeatherSyncPayload;
import net.minecraft.util.Mth;
import com.marsproject.terraformingmars.weather.MarsWeatherType;

public final class ClientMarsWeatherData {
    private static MarsWeatherType weatherType = MarsWeatherType.CLEAR;
    private static int intensity;
    private static int ticksRemaining;
    private static double windX = 1.0;
    private static double windZ;
    private static float visualIntensity;

    private ClientMarsWeatherData() {
    }

    public static void update(MarsWeatherSyncPayload payload) {
        int weatherId = Mth.clamp(payload.weatherType(), 0, MarsWeatherType.values().length - 1);
        weatherType = MarsWeatherType.values()[weatherId];
        intensity = Mth.clamp(payload.intensity(), 0, 3);
        ticksRemaining = Math.max(0, payload.ticksRemaining());
        windX = payload.windX();
        windZ = payload.windZ();
    }

    public static void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
        float target = weatherType != MarsWeatherType.CLEAR ? intensity : 0.0F;
        visualIntensity += (target - visualIntensity) * 0.08F;
        if (Math.abs(target - visualIntensity) < 0.01F) {
            visualIntensity = target;
        }
    }

    public static boolean isDustStorm() {
        return weatherType == MarsWeatherType.DUST_STORM && intensity > 0;
    }

    public static boolean isDryIceStorm() {
        return weatherType == MarsWeatherType.DRY_ICE_STORM && intensity > 0;
    }

    public static int intensity() {
        return intensity;
    }

    public static float visualIntensity() {
        return visualIntensity;
    }

    public static double windX() {
        return windX;
    }

    public static double windZ() {
        return windZ;
    }
}
