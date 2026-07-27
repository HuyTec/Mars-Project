package com.marsproject.terraformingmars.machine;

import com.marsproject.terraformingmars.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public record MachineRecipe(
        ResourceLocation machineType,
        List<Ingredient> inputs,
        ItemStack output,
        int processingTimeTicks,
        int powerCostWatts
) implements Recipe<MachineRecipeInput> {
    public MachineRecipe {
        inputs = List.copyOf(inputs);
        output = output.copy();
        if (inputs.isEmpty()) throw new IllegalArgumentException("inputs cannot be empty");
        if (processingTimeTicks <= 0) throw new IllegalArgumentException("processingTimeTicks must be positive");
        if (powerCostWatts < 0) throw new IllegalArgumentException("powerCostWatts must be non-negative");
    }

    @Override
    public boolean matches(MachineRecipeInput container, Level level) {
        if (!machineType.equals(container.machineType())
                || container.getContainerSize() < inputs.size()) {
            return false;
        }
        for (int slot = 0; slot < inputs.size(); slot++) {
            if (!inputs.get(slot).test(container.getItem(slot))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(MachineRecipeInput container, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= inputs.size();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(inputs);
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.MACHINE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.MACHINE_TYPE.get();
    }
}
