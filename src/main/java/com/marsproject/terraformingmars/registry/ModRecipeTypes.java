package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.machine.MachineRecipe;
import com.marsproject.terraformingmars.machine.MachineRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TerraformingMarsMod.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, TerraformingMarsMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer>
            MACHINE_SERIALIZER = SERIALIZERS.register(
                    "machine_recipe", MachineRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>>
            MACHINE_TYPE = TYPES.register("machine_recipe", () -> new RecipeType<MachineRecipe>() {
                @Override
                public String toString() {
                    return TerraformingMarsMod.MODID + ":machine_recipe";
                }
            });

    private ModRecipeTypes() {
    }
}
