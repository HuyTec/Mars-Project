package com.marsproject.terraformingmars.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarsEnvironmentManager {

    private static final double EARTH_PRESSURE_PERCENT = 100.0;
    private static final double EARTH_OXYGEN_PERCENT = 21.0;
    private static final double EARTH_NITROGEN_PERCENT = 78.0;
    private static final double BREATHABLE_TOLERANCE = 5.0;

    public static MarsEnvironmentStage resolve(float progress) {
        List<MarsEnvironmentStage> stages = MarsEnvironmentStageLoader.getStages();
        if (stages.isEmpty()) {
            throw new IllegalStateException("Chua co du lieu mars_environment nao duoc nap");
        }

        MarsEnvironmentStage lower = stages.get(0);
        MarsEnvironmentStage upper = stages.get(stages.size() - 1);

        for (int i = 0; i < stages.size() - 1; i++) {
            if (progress >= stages.get(i).progress() && progress <= stages.get(i + 1).progress()) {
                lower = stages.get(i);
                upper = stages.get(i + 1);
                break;
            }
        }

        if (lower == upper) return lower;

        float span = upper.progress() - lower.progress();
        float t = span <= 0f ? 0f : (progress - lower.progress()) / span;

        return new MarsEnvironmentStage(
                progress,
                lerp(lower.radiation(), upper.radiation(), t),
                lerp(lower.magneticFieldPercent(), upper.magneticFieldPercent(), t),
                lerp(lower.temperatureCelsius(), upper.temperatureCelsius(), t),
                lerp(lower.waterPercent(), upper.waterPercent(), t),
                lerp(lower.atmospherePressurePercent(), upper.atmospherePressurePercent(), t),
                lerpMap(lower.atmosphereComposition(), upper.atmosphereComposition(), t),
                lerp(lower.biologyPercent(), upper.biologyPercent(), t)
        );
    }

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }

    private static Map<String, Double> lerpMap(Map<String, Double> a, Map<String, Double> b, float t) {
        Map<String, Double> result = new HashMap<>();
        for (String gas : a.keySet()) {
            double from = a.getOrDefault(gas, 0.0);
            double to = b.getOrDefault(gas, 0.0);
            result.put(gas, lerp(from, to, t));
        }
        return result;
    }

    /** Ngưỡng "sống được" — tính từ giá trị đã nội suy, không lưu tay trong JSON. */
    public static boolean hasBreathableAir(MarsEnvironmentStage stage) {
        double oxygen = stage.atmosphereComposition().getOrDefault("oxygen", 0.0);
        double nitrogen = stage.atmosphereComposition().getOrDefault("nitrogen", 0.0);

        return Math.abs(stage.atmospherePressurePercent() - EARTH_PRESSURE_PERCENT) <= BREATHABLE_TOLERANCE
                && Math.abs(oxygen - EARTH_OXYGEN_PERCENT) <= BREATHABLE_TOLERANCE
                && Math.abs(nitrogen - EARTH_NITROGEN_PERCENT) <= BREATHABLE_TOLERANCE;
    }

    public static boolean canLive(MarsEnvironmentStage stage) {
        return stage.radiation() < 0.1
                && hasBreathableAir(stage)
                && stage.temperatureCelsius() > -20.0
                && stage.temperatureCelsius() < 45.0;
    }
}