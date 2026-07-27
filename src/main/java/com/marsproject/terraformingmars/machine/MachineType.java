package com.marsproject.terraformingmars.machine;

import com.marsproject.terraformingmars.block.MultiblockPart;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MachineType(
        ResourceLocation machineTypeId,
        int inputSlotCount,
        int outputSlotCount,
        ResourceLocation model,
        ResourceLocation texture,
        ResourceLocation animation,
        List<MultiblockPart> parts,
        String idleAnimation,
        String workingAnimation,
        String noPowerAnimation
) {
    public MachineType {
        if (inputSlotCount < 1 || outputSlotCount < 1) {
            throw new IllegalArgumentException("Machine slot counts must be positive");
        }
        parts = List.copyOf(parts);
    }
}
