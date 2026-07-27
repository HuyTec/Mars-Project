package com.marsproject.terraformingmars.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Complete, render-ready environment data. The HUD renderer contains no dimension rules. */
public record EnvironmentHudViewModel(
        EnvironmentHudProfile profile,
        Component title,
        List<StatRow> rows,
        List<Warning> warnings,
        Component status,
        Severity statusSeverity
) {
    public EnvironmentHudViewModel {
        rows = List.copyOf(rows);
        warnings = List.copyOf(warnings);
    }

    public enum Severity {
        SAFE,
        WARNING,
        CRITICAL,
        ERROR
    }

    public record StatRow(
            ResourceLocation icon,
            Component label,
            Component value,
            Component condition,
            Severity severity
    ) {
    }

    public record Warning(Component text, Severity severity) {
    }
}
