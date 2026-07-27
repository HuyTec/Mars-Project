package com.marsproject.terraformingmars.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public final class MachineRecipeSerializer implements RecipeSerializer<MachineRecipe> {
    private static final MapCodec<OutputData> OUTPUT_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(OutputData::item),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(OutputData::count)
            ).apply(instance, OutputData::new));

    private static final MapCodec<MachineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    net.minecraft.resources.ResourceLocation.CODEC.fieldOf("machine_type")
                            .forGetter(MachineRecipe::machineType),
                    Ingredient.LIST_CODEC_NONEMPTY.fieldOf("inputs")
                            .forGetter(MachineRecipe::inputs),
                    OUTPUT_CODEC.fieldOf("output")
                            .forGetter(recipe -> new OutputData(
                                    recipe.output().getItem(), recipe.output().getCount())),
                    Codec.INT.fieldOf("processing_time")
                            .forGetter(MachineRecipe::processingTimeTicks),
                    Codec.INT.fieldOf("power_cost")
                            .forGetter(MachineRecipe::powerCostWatts)
            ).apply(instance, (type, inputs, output, time, power) ->
                    new MachineRecipe(type, inputs, output.stack(), time, power)));

    private static final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> STREAM_CODEC =
            StreamCodec.of(MachineRecipeSerializer::encode, MachineRecipeSerializer::decode);

    @Override
    public MapCodec<MachineRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, MachineRecipe recipe) {
        buffer.writeResourceLocation(recipe.machineType());
        buffer.writeVarInt(recipe.inputs().size());
        recipe.inputs().forEach(input -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, input));
        ItemStack.STREAM_CODEC.encode(buffer, recipe.output());
        buffer.writeVarInt(recipe.processingTimeTicks());
        buffer.writeVarInt(recipe.powerCostWatts());
    }

    private static MachineRecipe decode(RegistryFriendlyByteBuf buffer) {
        net.minecraft.resources.ResourceLocation type = buffer.readResourceLocation();
        int size = buffer.readVarInt();
        List<Ingredient> inputs = java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer))
                .toList();
        ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
        return new MachineRecipe(type, inputs, output,
                buffer.readVarInt(), buffer.readVarInt());
    }

    private record OutputData(Item item, int count) {
        private ItemStack stack() {
            return new ItemStack(item, count);
        }
    }
}
