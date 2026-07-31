package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.network.SurvivalSyncPayload;
import com.marsproject.terraformingmars.survival.PlayerSurvivalData;
import net.minecraft.util.Mth;

public final class ClientSurvivalData {
    private static float thirst = PlayerSurvivalData.MAX_THIRST;
    private static double bodyTemperature = PlayerSurvivalData.NORMAL_BODY_TEMPERATURE;

    private ClientSurvivalData() {
    }

    public static void update(SurvivalSyncPayload payload) {
        thirst = Mth.clamp(payload.thirst(), 0.0F, PlayerSurvivalData.MAX_THIRST);
        bodyTemperature = Mth.clamp(payload.bodyTemperature(), 25.0, 45.0);
    }

    public static float thirst() {
        return thirst;
    }

    public static double bodyTemperature() {
        return bodyTemperature;
    }
}