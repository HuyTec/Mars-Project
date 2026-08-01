package com.marsproject.terraformingmars.gas;

import com.marsproject.terraformingmars.pipe.PipeType;

/** Process resources transported by typed machine networks. */
public enum GasType {
    OXYGEN(PipeType.GAS),
    NITROGEN(PipeType.GAS),
    HYDROGEN(PipeType.GAS),
    CARBON_DIOXIDE(PipeType.GAS),
    METHANE(PipeType.GAS),
    BREATHABLE_AIR(PipeType.GAS),
    WATER(PipeType.FLUID),
    HEAT(PipeType.HEAT);

    private final PipeType pipeType;

    GasType(PipeType pipeType) {
        this.pipeType = pipeType;
    }

    public PipeType pipeType() {
        return pipeType;
    }
}
