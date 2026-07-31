package com.marsproject.terraformingmars.survival;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Persistent thirst and body-temperature values owned by the server player. */
public final class PlayerSurvivalData {
    public static final float MAX_THIRST = 20.0F;
    public static final double NORMAL_BODY_TEMPERATURE = 37.0;

    private static final String THIRST_KEY = "terraforming_mars_thirst";
    private static final String TEMPERATURE_KEY = "terraforming_mars_body_temperature";

    private PlayerSurvivalData() {
    }

    public static float getThirst(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(THIRST_KEY)) {
            data.putFloat(THIRST_KEY, MAX_THIRST);
        }
        return Mth.clamp(data.getFloat(THIRST_KEY), 0.0F, MAX_THIRST);
    }

    public static void setThirst(Player player, float thirst) {
        player.getPersistentData().putFloat(THIRST_KEY, Mth.clamp(thirst, 0.0F, MAX_THIRST));
    }

    public static void restoreThirst(Player player, float amount) {
        if (amount > 0.0F) {
            setThirst(player, getThirst(player) + amount);
        }
    }

    public static double getBodyTemperature(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(TEMPERATURE_KEY)) {
            data.putDouble(TEMPERATURE_KEY, NORMAL_BODY_TEMPERATURE);
        }
        return Mth.clamp(data.getDouble(TEMPERATURE_KEY), 25.0, 45.0);
    }

    public static void setBodyTemperature(Player player, double temperature) {
        player.getPersistentData().putDouble(
                TEMPERATURE_KEY, Mth.clamp(temperature, 25.0, 45.0));
    }

    public static void reset(Player player) {
        setThirst(player, MAX_THIRST);
        setBodyTemperature(player, NORMAL_BODY_TEMPERATURE);
    }
    public static void copy(Player original, Player replacement) {
        setThirst(replacement, getThirst(original));
        setBodyTemperature(replacement, getBodyTemperature(original));
    }
}