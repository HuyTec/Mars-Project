package com.marsproject.terraformingmars.weather;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

public final class MarsWeatherBiomes {
    public static final ResourceKey<Biome> CRYOTIC_WASTES = ResourceKey.create(
            Registries.BIOME,
            new ResourceLocation(TerraformingMarsMod.MODID, "cryotic_wastes")
    );

    private MarsWeatherBiomes() {
    }

    public static boolean isCryotic(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(CRYOTIC_WASTES);
    }
}
