package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.marsproject.terraformingmars.client.EnvironmentHudViewModel.Severity;

/**
 * Resolves the active dimension to a complete HUD model.
 * Static presets never touch Mars SavedData or request network synchronization.
 */
public final class EnvironmentHudResolver {
    private static final String HUD_KEY = "hud." + TerraformingMarsMod.MODID + ".";

    private static final ResourceLocation ICON_RADIATION = icon("radiation");
    private static final ResourceLocation ICON_PRESSURE = icon("pressure");
    private static final ResourceLocation ICON_OXYGEN = icon("oxygen");
    private static final ResourceLocation ICON_TEMPERATURE = icon("temperature");
    private static final ResourceLocation ICON_WATER = icon("water");
    private static final ResourceLocation ICON_BIOLOGY = icon("biology");

    /*
     * Earth HUD preset sourced from:
     * data/terraforming_mars/mars_environment/stage_01.json
     * Keep these values together here if Stage 01 changes.
     */
    private static final double EARTH_RADIATION = 0.05;
    private static final double EARTH_PRESSURE = 100.0;
    private static final double EARTH_OXYGEN = 21.0;
    private static final double EARTH_TEMPERATURE = 15.0;
    private static final double EARTH_WATER = 70.0;
    private static final double EARTH_BIOLOGY = 100.0;

    private EnvironmentHudResolver() {
    }

    public static EnvironmentHudViewModel resolve(
            ResourceKey<Level> dimension,
            MarsEnvironmentSyncPayload marsData
    ) {
        if (dimension.equals(TeleportHelper.MARS_LEVEL_KEY)) {
            return mars(marsData);
        }
        if (dimension.equals(Level.OVERWORLD)) {
            return earth();
        }
        if (dimension.equals(Level.NETHER)) {
            return nether();
        }
        if (dimension.equals(Level.END)) {
            return end();
        }
        return unknown(dimension.location());
    }

    private static EnvironmentHudViewModel mars(MarsEnvironmentSyncPayload data) {
        if (data == null) {
            return unknownModel(
                    EnvironmentHudProfile.MARS_DYNAMIC,
                    Component.translatable(HUD_KEY + "title.mars"),
                    Component.translatable(HUD_KEY + "status.calibrating")
            );
        }

        List<EnvironmentHudViewModel.StatRow> rows = List.of(
                radiationRow(data.radiation()),
                pressureRow(data.atmospherePressurePercent()),
                oxygenRow(data.oxygenPercent()),
                temperatureRow(data.temperatureCelsius()),
                waterRow(data.waterPercent()),
                biologyRow(data.biologyPercent())
        );
        List<EnvironmentHudViewModel.Warning> warnings = warningsFrom(rows);
        return new EnvironmentHudViewModel(
                EnvironmentHudProfile.MARS_DYNAMIC,
                Component.translatable(HUD_KEY + "title.mars"),
                rows,
                warnings,
                Component.translatable(HUD_KEY + (data.canLive() ? "status.safe" : "status.unsafe")),
                data.canLive() ? Severity.SAFE : Severity.CRITICAL
        );
    }

    private static EnvironmentHudViewModel earth() {
        List<EnvironmentHudViewModel.StatRow> rows = List.of(
                numericRow(ICON_RADIATION, "radiation", EARTH_RADIATION, "mSv/h", "low", Severity.SAFE),
                numericRow(ICON_PRESSURE, "pressure", EARTH_PRESSURE, "%", "normal", Severity.SAFE),
                numericRow(ICON_OXYGEN, "oxygen", EARTH_OXYGEN, "%", "normal", Severity.SAFE),
                numericRow(ICON_TEMPERATURE, "temperature", EARTH_TEMPERATURE, "\u00b0C", "normal", Severity.SAFE),
                numericRow(ICON_WATER, "water", EARTH_WATER, "%", "abundant", Severity.SAFE),
                numericRow(ICON_BIOLOGY, "biology", EARTH_BIOLOGY, "%", "thriving", Severity.SAFE)
        );
        return new EnvironmentHudViewModel(
                EnvironmentHudProfile.EARTH_STAGE_01,
                Component.translatable(HUD_KEY + "title.earth"),
                rows,
                List.of(),
                Component.translatable(HUD_KEY + "status.safe"),
                Severity.SAFE
        );
    }

    private static EnvironmentHudViewModel nether() {
        List<EnvironmentHudViewModel.StatRow> rows = List.of(
                textRow(ICON_RADIATION, "radiation", "extreme", "extreme", Severity.CRITICAL),
                textRow(ICON_PRESSURE, "pressure", "extreme", "extreme", Severity.CRITICAL),
                numericRow(ICON_OXYGEN, "oxygen", 0.0, "%", "critical", Severity.CRITICAL),
                numericRow(ICON_TEMPERATURE, "temperature", 450.0, "\u00b0C", "extreme_heat", Severity.CRITICAL),
                numericRow(ICON_WATER, "water", 0.0, "%", "none", Severity.SAFE),
                numericRow(ICON_BIOLOGY, "biology", 50.0, "%", "established", Severity.SAFE)
        );
        return new EnvironmentHudViewModel(
                EnvironmentHudProfile.NETHER_EXTREME,
                Component.translatable(HUD_KEY + "title.nether"),
                rows,
                warningsFrom(rows),
                Component.translatable(HUD_KEY + "status.lethal"),
                Severity.CRITICAL
        );
    }

    private static EnvironmentHudViewModel end() {
        List<EnvironmentHudViewModel.StatRow> rows = specialRows("error", Severity.ERROR);
        return new EnvironmentHudViewModel(
                EnvironmentHudProfile.END_ERROR,
                Component.translatable(HUD_KEY + "title.end"),
                rows,
                List.of(new EnvironmentHudViewModel.Warning(
                        Component.translatable(HUD_KEY + "warning.corrupted"),
                        Severity.ERROR
                )),
                Component.translatable(HUD_KEY + "status.error"),
                Severity.ERROR
        );
    }

    private static EnvironmentHudViewModel unknown(ResourceLocation dimensionId) {
        String translationKey = "dimension." + dimensionId.getNamespace() + "." + dimensionId.getPath();
        Component dimensionName = I18n.exists(translationKey)
                ? Component.translatable(translationKey)
                : Component.literal(readablePath(dimensionId.getPath()));
        return unknownModel(
                EnvironmentHudProfile.UNKNOWN,
                Component.translatable(HUD_KEY + "title.generic", dimensionName),
                Component.translatable(HUD_KEY + "status.uncalibrated")
        );
    }

    private static EnvironmentHudViewModel unknownModel(
            EnvironmentHudProfile profile,
            Component title,
            Component status
    ) {
        return new EnvironmentHudViewModel(
                profile,
                title,
                specialRows("unknown", Severity.WARNING),
                List.of(),
                status,
                Severity.WARNING
        );
    }

    private static List<EnvironmentHudViewModel.StatRow> specialRows(String valueKey, Severity severity) {
        Component value = Component.translatable(HUD_KEY + "value." + valueKey);
        return List.of(
                specialRow(ICON_RADIATION, "radiation", value, severity),
                specialRow(ICON_PRESSURE, "pressure", value, severity),
                specialRow(ICON_OXYGEN, "oxygen", value, severity),
                specialRow(ICON_TEMPERATURE, "temperature", value, severity),
                specialRow(ICON_WATER, "water", value, severity),
                specialRow(ICON_BIOLOGY, "biology", value, severity)
        );
    }

    private static EnvironmentHudViewModel.StatRow specialRow(
            ResourceLocation icon,
            String label,
            Component value,
            Severity severity
    ) {
        return new EnvironmentHudViewModel.StatRow(
                icon,
                label(label),
                value.copy(),
                value.copy(),
                severity
        );
    }

    private static EnvironmentHudViewModel.StatRow radiationRow(double value) {
        if (value < 5) return numericRow(ICON_RADIATION, "radiation", value, "mSv/h", "low", Severity.SAFE);
        if (value < 10) return numericRow(ICON_RADIATION, "radiation", value, "mSv/h", "elevated", Severity.WARNING);
        if (value < 15) return numericRow(ICON_RADIATION, "radiation", value, "mSv/h", "high", Severity.WARNING);
        return numericRow(ICON_RADIATION, "radiation", value, "mSv/h", "critical", Severity.CRITICAL);
    }

    private static EnvironmentHudViewModel.StatRow pressureRow(double value) {
        if (value < 15) return numericRow(ICON_PRESSURE, "pressure", value, "%", "critical", Severity.CRITICAL);
        if (value < 30) return numericRow(ICON_PRESSURE, "pressure", value, "%", "low", Severity.WARNING);
        if (value <= 70) return numericRow(ICON_PRESSURE, "pressure", value, "%", "normal", Severity.SAFE);
        return numericRow(ICON_PRESSURE, "pressure", value, "%", "high", Severity.WARNING);
    }

    private static EnvironmentHudViewModel.StatRow oxygenRow(double value) {
        if (value < 8) return numericRow(ICON_OXYGEN, "oxygen", value, "%", "critical", Severity.CRITICAL);
        if (value < 16) return numericRow(ICON_OXYGEN, "oxygen", value, "%", "low", Severity.WARNING);
        if (value <= 25) return numericRow(ICON_OXYGEN, "oxygen", value, "%", "normal", Severity.SAFE);
        return numericRow(ICON_OXYGEN, "oxygen", value, "%", "high", Severity.SAFE);
    }

    private static EnvironmentHudViewModel.StatRow temperatureRow(double value) {
        if (value < -20) return numericRow(ICON_TEMPERATURE, "temperature", value, "\u00b0C", "freezing", Severity.CRITICAL);
        if (value < 0) return numericRow(ICON_TEMPERATURE, "temperature", value, "\u00b0C", "cold", Severity.WARNING);
        if (value <= 15) return numericRow(ICON_TEMPERATURE, "temperature", value, "\u00b0C", "normal", Severity.SAFE);
        return numericRow(ICON_TEMPERATURE, "temperature", value, "\u00b0C", "hot", Severity.WARNING);
    }

    private static EnvironmentHudViewModel.StatRow waterRow(double value) {
        String condition = value < 5 ? "none" : value < 10 ? "trace" : value < 50 ? "present" : "abundant";
        return numericRow(ICON_WATER, "water", value, "%", condition, Severity.SAFE);
    }

    private static EnvironmentHudViewModel.StatRow biologyRow(double value) {
        String condition = value < 5 ? "none" : value < 15 ? "emerging" : value < 40 ? "established" : "thriving";
        return numericRow(ICON_BIOLOGY, "biology", value, "%", condition, Severity.SAFE);
    }

    private static EnvironmentHudViewModel.StatRow numericRow(
            ResourceLocation icon,
            String label,
            double value,
            String unit,
            String condition,
            Severity severity
    ) {
        return new EnvironmentHudViewModel.StatRow(
                icon,
                label(label),
                Component.literal(String.format(Locale.ROOT, "%.1f %s", value, unit)),
                Component.translatable(HUD_KEY + "condition." + condition),
                severity
        );
    }

    private static EnvironmentHudViewModel.StatRow textRow(
            ResourceLocation icon,
            String label,
            String value,
            String condition,
            Severity severity
    ) {
        return new EnvironmentHudViewModel.StatRow(
                icon,
                label(label),
                Component.translatable(HUD_KEY + "value." + value),
                Component.translatable(HUD_KEY + "condition." + condition),
                severity
        );
    }

    private static List<EnvironmentHudViewModel.Warning> warningsFrom(
            List<EnvironmentHudViewModel.StatRow> rows
    ) {
        List<EnvironmentHudViewModel.Warning> warnings = new ArrayList<>();
        for (EnvironmentHudViewModel.StatRow row : rows) {
            if (row.severity() == Severity.SAFE) continue;
            warnings.add(new EnvironmentHudViewModel.Warning(
                    Component.translatable(HUD_KEY + "warning.entry", row.label(), row.condition()),
                    row.severity()
            ));
        }
        return warnings;
    }

    private static Component label(String name) {
        return Component.translatable(HUD_KEY + "label." + name);
    }

    private static ResourceLocation icon(String name) {
        return new ResourceLocation(TerraformingMarsMod.MODID, "textures/gui/hud/icon_" + name + ".png");
    }

    static String readablePath(String path) {
        String[] words = path.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.isEmpty() ? path : result.toString();
    }
}
