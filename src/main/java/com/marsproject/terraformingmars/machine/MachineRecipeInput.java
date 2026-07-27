package com.marsproject.terraformingmars.machine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public final class MachineRecipeInput extends SimpleContainer {
    private final ResourceLocation machineType;

    public MachineRecipeInput(ResourceLocation machineType, ItemStack[] inputs) {
        super(inputs);
        this.machineType = machineType;
    }

    public ResourceLocation machineType() {
        return machineType;
    }
}
