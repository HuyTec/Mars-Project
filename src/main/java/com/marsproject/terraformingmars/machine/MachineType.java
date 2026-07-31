package com.marsproject.terraformingmars.machine;

import com.marsproject.terraformingmars.block.MultiblockPart;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record MachineType(
        ResourceLocation machineTypeId,
        int inputSlotCount,
        int outputSlotCount,
        int energyPerOperation,
        int operationIntervalTicks,
        MachineOperation operation,
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
        if (energyPerOperation < 0) {
            throw new IllegalArgumentException("Machine energy per operation cannot be negative");
        }
        if (operationIntervalTicks <= 0) {
            throw new IllegalArgumentException("Machine operation interval must be positive");
        }
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(machineTypeId, "machineTypeId");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(texture, "texture");
        Objects.requireNonNull(animation, "animation");
        Objects.requireNonNull(parts, "parts");
        Objects.requireNonNull(idleAnimation, "idleAnimation");
        Objects.requireNonNull(workingAnimation, "workingAnimation");
        Objects.requireNonNull(noPowerAnimation, "noPowerAnimation");
        parts = List.copyOf(parts);
    }
}
