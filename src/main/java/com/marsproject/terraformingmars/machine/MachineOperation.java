package com.marsproject.terraformingmars.machine;

import com.marsproject.terraformingmars.gas.GasType;

/** Data-driven operation performed by the shared machine block entity. */
public enum MachineOperation {
    OXYGEN_GENERATION(GasType.OXYGEN, 100, 0, 0, true),
    NITROGEN_GENERATION(GasType.NITROGEN, 100, 0, 0, true),
    AIR_CREATION(GasType.BREATHABLE_AIR, 100, 21, 79, false),
    WATER_EXTRACTION(GasType.WATER, 225, 0, 0, false),
    ELECTROLYSIS(GasType.HYDROGEN, 2_000, 0, 0, false),
    CO2_COLLECTION(GasType.CARBON_DIOXIDE, 1_000, 0, 0, true),
    SABATIER_REACTION(GasType.METHANE, 1_000, 0, 0, false),
    METHANE_HEATING(GasType.HEAT, 100, 0, 0, false),
    METHANE_POWER(GasType.HEAT, 80, 0, 0, false);

    private final GasType outputGas;
    private final int outputAmount;
    private final int oxygenInput;
    private final int nitrogenInput;
    private final boolean requiresAirVent;

    MachineOperation(GasType outputGas, int outputAmount, int oxygenInput,
                     int nitrogenInput, boolean requiresAirVent) {
        this.outputGas = outputGas;
        this.outputAmount = outputAmount;
        this.oxygenInput = oxygenInput;
        this.nitrogenInput = nitrogenInput;
        this.requiresAirVent = requiresAirVent;
    }

    public GasType outputGas() {
        return outputGas;
    }

    public int outputAmount() {
        return outputAmount;
    }

    public int oxygenInput() {
        return oxygenInput;
    }

    public int nitrogenInput() {
        return nitrogenInput;
    }

    public boolean requiresAirVent() {
        return requiresAirVent;
    }

    public boolean isAirCreator() {
        return this == AIR_CREATION;
    }

    public boolean isDualInput() {
        return this == SABATIER_REACTION
                || this == METHANE_HEATING
                || this == METHANE_POWER;
    }

    public boolean isPowerGenerator() {
        return this == METHANE_POWER;
    }
}
