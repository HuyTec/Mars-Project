package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.MarsDustBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TerraformingMarsMod.MODID);

    public static final DeferredBlock<Block> DUST_DEPOSIT = BLOCKS.register(
            "dust_deposit",
            () -> new MarsDustBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.5f)
                    .sound(SoundType.SAND)) {
            }
    );

    public static final DeferredBlock<Block> HEMATITE_LAYER = BLOCKS.registerSimpleBlock(
            "hematite_layer", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.STONE));

    public static final DeferredBlock<Block> SULFATE_ROCK = BLOCKS.registerSimpleBlock(
            "sulfate_rock", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(1.5f).requiresCorrectToolForDrops().sound(SoundType.STONE));

    public static final DeferredBlock<Block> CRYOTIC_ROCK = BLOCKS.registerSimpleBlock(
            "cryotic_rock", BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.ANCIENT_DEBRIS));

    public static final DeferredBlock<Block> IRONSTONE = BLOCKS.registerSimpleBlock(
            "ironstone", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(3.0f).requiresCorrectToolForDrops().sound(SoundType.STONE));

    // Phương thức rỗng, chỉ để gọi từ TerraformingMarsMod nhằm kích hoạt class này load
    public static void register() {}
}