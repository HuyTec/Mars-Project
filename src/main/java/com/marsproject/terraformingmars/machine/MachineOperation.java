package com.marsproject.terraformingmars.machine;

import com.marsproject.terraformingmars.gas.GasType;

/** Data-driven operation performed by the shared machine block entity. */
public enum MachineOperation {
    OXYGEN_GENERATION(GasType.OXYGEN, 100, 0, 0, true),
    NITROGEN_GENERATION(GasType.NITROGEN, 100, 0, 0, true),
    AIR_CREATION(GasType.BREATHABLE_AIR, 100, 21, 79, false);

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
}
