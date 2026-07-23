package com.marsproject.terraformingmars.environment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record MarsEnvironmentStage(
        float progress,                              // 0.0 -> 1.0, mốc tiến trình terraform
        double radiation,                             // đơn vị tuỳ bạn định nghĩa, ví dụ mSv/h
        double magneticFieldPercent,                  // % so với từ trường Trái Đất
        double temperatureCelsius,
        double waterPercent,                          // % so với lượng nước Trái Đất
        double atmospherePressurePercent,              // % so với 1 atm
        Map<String, Double> atmosphereComposition,     // ví dụ {"oxygen": 21.0, "nitrogen": 78.0}
        double biologyPercent                          // % đa dạng sinh học so với Trái Đất
) {
    public static final Codec<MarsEnvironmentStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("progress").forGetter(MarsEnvironmentStage::progress),
            Codec.DOUBLE.fieldOf("radiation").forGetter(MarsEnvironmentStage::radiation),
            Codec.DOUBLE.fieldOf("magnetic_field_percent").forGetter(MarsEnvironmentStage::magneticFieldPercent),
            Codec.DOUBLE.fieldOf("temperature_celsius").forGetter(MarsEnvironmentStage::temperatureCelsius),
            Codec.DOUBLE.fieldOf("water_percent").forGetter(MarsEnvironmentStage::waterPercent),
            Codec.DOUBLE.fieldOf("atmosphere_pressure_percent").forGetter(MarsEnvironmentStage::atmospherePressurePercent),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                    .fieldOf("atmosphere_composition").forGetter(MarsEnvironmentStage::atmosphereComposition),
            Codec.DOUBLE.fieldOf("biology_percent").forGetter(MarsEnvironmentStage::biologyPercent)
    ).apply(instance, MarsEnvironmentStage::new));
}