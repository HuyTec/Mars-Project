package com.marsproject.terraformingmars.weather;

import com.marsproject.terraformingmars.network.MarsWeatherSyncPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;

public final class MarsWeatherData extends SavedData {
    private static final String DATA_NAME = "terraforming_mars_weather";

    private MarsWeatherType type = MarsWeatherType.CLEAR;
    private int intensity;
    private int ticksRemaining = 2400;
    private double windX = 1.0;
    private double windZ;

    private static MarsWeatherData create() {
        return new MarsWeatherData();
    }

    private static MarsWeatherData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        MarsWeatherData data = create();
        int weatherId = Mth.clamp(tag.getInt("weather_type"), 0, MarsWeatherType.values().length - 1);
        data.type = MarsWeatherType.values()[weatherId];
        data.intensity = Mth.clamp(tag.getInt("intensity"), 0, 3);
        data.ticksRemaining = Math.max(0, tag.getInt("ticks_remaining"));
        data.windX = tag.getDouble("wind_x");
        data.windZ = tag.getDouble("wind_z");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        tag.putInt("weather_type", type.ordinal());
        tag.putInt("intensity", intensity);
        tag.putInt("ticks_remaining", ticksRemaining);
        tag.putDouble("wind_x", windX);
        tag.putDouble("wind_z", windZ);
        return tag;
    }

    public static MarsWeatherData get(ServerLevel marsLevel) {
        SavedData.Factory<MarsWeatherData> factory =
                new SavedData.Factory<>(MarsWeatherData::create, MarsWeatherData::load, null);
        return marsLevel.getDataStorage().computeIfAbsent(factory, DATA_NAME);
    }

    public boolean tick(ServerLevel level) {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            if (ticksRemaining % 20 == 0) {
                setDirty();
            }
            return false;
        }

        if (type == MarsWeatherType.CLEAR) {
            int intensity = 1 + level.getRandom().nextInt(3);
            if (level.getRandom().nextFloat() < 0.3F) {
                startDryIceStorm(level, intensity);
            } else {
                startDustStorm(level, intensity);
            }
        } else {
            clear(level);
        }
        return true;
    }

    public void startDustStorm(ServerLevel level, int requestedIntensity) {
        type = MarsWeatherType.DUST_STORM;
        intensity = Mth.clamp(requestedIntensity, 1, 3);
        ticksRemaining = 2400 + level.getRandom().nextInt(3601);

        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        windX = Math.cos(angle);
        windZ = Math.sin(angle);
        setDirty();
    }

    public void startDryIceStorm(ServerLevel level, int requestedIntensity) {
        type = MarsWeatherType.DRY_ICE_STORM;
        intensity = Mth.clamp(requestedIntensity, 1, 3);
        ticksRemaining = 2400 + level.getRandom().nextInt(3601);
        windX = 0.0;
        windZ = 0.0;
        setDirty();
    }

    public void clear(ServerLevel level) {
        type = MarsWeatherType.CLEAR;
        intensity = 0;
        ticksRemaining = 6000 + level.getRandom().nextInt(6001);
        setDirty();
    }

    public MarsWeatherSyncPayload toPayload() {
        return new MarsWeatherSyncPayload(
                type.ordinal(),
                intensity,
                ticksRemaining,
                windX,
                windZ
        );
    }

    public boolean isDustStorm() {
        return type == MarsWeatherType.DUST_STORM && intensity > 0;
    }

    public int intensity() {
        return intensity;
    }

    public boolean isDryIceStorm() {
        return type == MarsWeatherType.DRY_ICE_STORM && intensity > 0;
    }

}
