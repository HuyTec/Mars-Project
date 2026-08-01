package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.network.SurvivalSyncPayload;
import com.marsproject.terraformingmars.survival.PlayerSurvivalData;
import net.minecraft.util.Mth;

public final class ClientSurvivalData {
    private static float thirst = PlayerSurvivalData.MAX_THIRST;
    private static double bodyTemperature = PlayerSurvivalData.NORMAL_BODY_TEMPERATURE;
    private static int suitOxygen;
    private static int suitOxygenCapacity;
    private static boolean suitSealed;

    private ClientSurvivalData() {
    }

    public static void update(SurvivalSyncPayload payload) {
        thirst = Mth.clamp(payload.thirst(), 0.0F, PlayerSurvivalData.MAX_THIRST);
        bodyTemperature = Mth.clamp(payload.bodyTemperature(), 25.0, 45.0);
        suitOxygenCapacity = Math.max(0, payload.suitOxygenCapacity());
        suitOxygen = Mth.clamp(payload.suitOxygen(), 0, suitOxygenCapacity);
        suitSealed = payload.suitSealed();
    }

    public static float thirst() {
        return thirst;
    }

    public static double bodyTemperature() {
        return bodyTemperature;
    }

    public static int suitOxygen() { return suitOxygen; }
    public static int suitOxygenCapacity() { return suitOxygenCapacity; }
    public static boolean suitSealed() { return suitSealed; }
}
