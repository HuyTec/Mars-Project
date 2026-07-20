package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TerraformingMarsMod.MODID);

    public static final DeferredItem<BlockItem> DUST_DEPOSIT_ITEM =
            ITEMS.registerSimpleBlockItem("dust_deposit", ModBlocks.DUST_DEPOSIT);
    public static final DeferredItem<BlockItem> HEMATITE_LAYER_ITEM =
            ITEMS.registerSimpleBlockItem("hematite_layer", ModBlocks.HEMATITE_LAYER);
    public static final DeferredItem<BlockItem> SULFATE_ROCK_ITEM =
            ITEMS.registerSimpleBlockItem("sulfate_rock", ModBlocks.SULFATE_ROCK);
    public static final DeferredItem<BlockItem> CRYOTIC_ROCK_ITEM =
            ITEMS.registerSimpleBlockItem("cryotic_rock", ModBlocks.CRYOTIC_ROCK);
    public static final DeferredItem<BlockItem> IRONSTONE_ITEM =
            ITEMS.registerSimpleBlockItem("ironstone", ModBlocks.IRONSTONE);

//    public static final DeferredItem<Item> RAW_HEMATITE = ITEMS.registerSimpleItem("raw_hematite");
//    public static final DeferredItem<Item> SULFATE_POWDER = ITEMS.registerSimpleItem("sulfate_powder");
//    public static final DeferredItem<Item> ICE_SHARD = ITEMS.registerSimpleItem("ice_shard");
//    public static final DeferredItem<Item> RAW_IRON_MARS = ITEMS.registerSimpleItem("raw_fragnent");

    public static void register() {}
}