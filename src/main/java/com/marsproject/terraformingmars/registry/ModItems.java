package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/** Block items for the Mars geology palette. Kept in order for the creative tab. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TerraformingMarsMod.MODID);
    public static final List<DeferredItem<BlockItem>> GEOLOGY_ITEMS = new ArrayList<>();

    private static DeferredItem<BlockItem> blockItem(String name, DeferredBlock<?> block) {
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(name, block);
        GEOLOGY_ITEMS.add(item);
        return item;
    }

    public static final DeferredItem<BlockItem> DUST_DEPOSIT_ITEM = blockItem("dust_deposit", ModBlocks.DUST_DEPOSIT);
    public static final DeferredItem<BlockItem> FINE_DUST_ITEM = blockItem("fine_dust", ModBlocks.FINE_DUST);
    public static final DeferredItem<BlockItem> COMPACTED_DUST_ITEM = blockItem("compacted_dust", ModBlocks.COMPACTED_DUST);
    public static final DeferredItem<BlockItem> WIND_CRUST_ITEM = blockItem("wind_crust", ModBlocks.WIND_CRUST);
    public static final DeferredItem<BlockItem> DIRTY_STONE_ITEM = blockItem("dirty_stone", ModBlocks.DIRTY_STONE);
    public static final DeferredItem<BlockItem> DUST_LAYER_ITEM = blockItem("dust_layer", ModBlocks.DUST_LAYER);
    public static final DeferredItem<BlockItem> LOOSE_REGOLITH_ITEM = blockItem("loose_regolith", ModBlocks.LOOSE_REGOLITH);
    public static final DeferredItem<BlockItem> COMPACTED_REGOLITH_ITEM = blockItem("compacted_regolith", ModBlocks.COMPACTED_REGOLITH);
    public static final DeferredItem<BlockItem> ROCKY_REGOLITH_ITEM = blockItem("rocky_regolith", ModBlocks.ROCKY_REGOLITH);
    public static final DeferredItem<BlockItem> IRON_RICH_REGOLITH_ITEM = blockItem("iron_rich_regolith", ModBlocks.IRON_RICH_REGOLITH);
    public static final DeferredItem<BlockItem> ICE_RICH_REGOLITH_ITEM = blockItem("ice_rich_regolith", ModBlocks.ICE_RICH_REGOLITH);

    public static final DeferredItem<BlockItem> BASALTIC_ROCK_ITEM = blockItem("basaltic_rock", ModBlocks.BASALTIC_ROCK);
    public static final DeferredItem<BlockItem> WEATHERED_BASALT_ITEM = blockItem("weathered_basalt", ModBlocks.WEATHERED_BASALT);
    public static final DeferredItem<BlockItem> FRACTURED_BASALT_ITEM = blockItem("fractured_basalt", ModBlocks.FRACTURED_BASALT);
    public static final DeferredItem<BlockItem> VESICULAR_BASALT_ITEM = blockItem("vesicular_basalt", ModBlocks.VESICULAR_BASALT);
    public static final DeferredItem<BlockItem> MASSIVE_BASALT_ITEM = blockItem("massive_basalt", ModBlocks.MASSIVE_BASALT);
    public static final DeferredItem<BlockItem> FERRIC_BASALT_ITEM = blockItem("ferric_basalt", ModBlocks.FERRIC_BASALT);
    public static final DeferredItem<BlockItem> FROST_BASALT_ITEM = blockItem("frost_basalt", ModBlocks.FROST_BASALT);

    public static final DeferredItem<BlockItem> SULFATE_ROCK_ITEM = blockItem("sulfate_rock", ModBlocks.SULFATE_ROCK);
    public static final DeferredItem<BlockItem> LAYERED_SULFATE_ITEM = blockItem("layered_sulfate", ModBlocks.LAYERED_SULFATE);
    public static final DeferredItem<BlockItem> CARBONATE_ROCK_ITEM = blockItem("carbonate_rock", ModBlocks.CARBONATE_ROCK);
    public static final DeferredItem<BlockItem> LAYERED_CARBONATE_ITEM = blockItem("layered_carbonate", ModBlocks.LAYERED_CARBONATE);
    public static final DeferredItem<BlockItem> CLAYSTONE_ITEM = blockItem("claystone", ModBlocks.CLAYSTONE);
    public static final DeferredItem<BlockItem> MUDSTONE_ITEM = blockItem("mudstone", ModBlocks.MUDSTONE);
    public static final DeferredItem<BlockItem> EVAPORITE_ITEM = blockItem("evaporite", ModBlocks.EVAPORITE);

    public static final DeferredItem<BlockItem> IRONSTONE_ITEM = blockItem("ironstone", ModBlocks.IRONSTONE);
    public static final DeferredItem<BlockItem> HEMATITE_LAYER_ITEM = blockItem("hematite_layer", ModBlocks.HEMATITE_LAYER);
    public static final DeferredItem<BlockItem> OXIDIZED_ROCK_ITEM = blockItem("oxidized_rock", ModBlocks.OXIDIZED_ROCK);
    public static final DeferredItem<BlockItem> MAGNETITE_ROCK_ITEM = blockItem("magnetite_rock", ModBlocks.MAGNETITE_ROCK);
    public static final DeferredItem<BlockItem> CRYOTIC_ROCK_ITEM = blockItem("cryotic_rock", ModBlocks.CRYOTIC_ROCK);
    public static final DeferredItem<BlockItem> FROZEN_DUST_ITEM = blockItem("frozen_dust", ModBlocks.FROZEN_DUST);
    public static final DeferredItem<BlockItem> PERMAFROST_ITEM = blockItem("permafrost", ModBlocks.PERMAFROST);
    public static final DeferredItem<BlockItem> CARBONATE_VEIN_ITEM = blockItem("carbonate_vein", ModBlocks.CARBONATE_VEIN);
    public static final DeferredItem<BlockItem> WHITE_CARBONATE_ITEM = blockItem("white_carbonate", ModBlocks.WHITE_CARBONATE);
    public static final DeferredItem<BlockItem> SILICA_DEPOSIT_ITEM = blockItem("silica_deposit", ModBlocks.SILICA_DEPOSIT);
    public static final DeferredItem<BlockItem> OPALINE_SILICA_ITEM = blockItem("opaline_silica", ModBlocks.OPALINE_SILICA);
    public static final DeferredItem<BlockItem> SILICA_CRUST_ITEM = blockItem("silica_crust", ModBlocks.SILICA_CRUST);
    public static final DeferredItem<BlockItem> SILICA_VEIN_ITEM = blockItem("silica_vein", ModBlocks.SILICA_VEIN);
    public static final DeferredItem<BlockItem> APATITE_VEIN_ITEM = blockItem("apatite_vein", ModBlocks.APATITE_VEIN);
    public static final DeferredItem<BlockItem> FELDSPAR_VEIN_ITEM = blockItem("feldspar_vein", ModBlocks.FELDSPAR_VEIN);
    public static final DeferredItem<BlockItem> CLAY_RICH_ROCK_ITEM = blockItem("clay_rich_rock", ModBlocks.CLAY_RICH_ROCK);
    public static final DeferredItem<BlockItem> SMECTITE_ITEM = blockItem("smectite", ModBlocks.SMECTITE);
    public static final DeferredItem<BlockItem> BENTONITE_ITEM = blockItem("bentonite", ModBlocks.BENTONITE);
    public static final DeferredItem<BlockItem> CHLORIDE_DEPOSIT_ITEM = blockItem("chloride_deposit", ModBlocks.CHLORIDE_DEPOSIT);
    public static final DeferredItem<BlockItem> SALT_CRUST_ITEM = blockItem("salt_crust", ModBlocks.SALT_CRUST);
    public static final DeferredItem<BlockItem> SALT_LAYER_ITEM = blockItem("salt_layer", ModBlocks.SALT_LAYER);

    public static final DeferredItem<BlockItem> MARS_IRON_VEIN_ITEM = blockItem("mars_iron_vein", ModBlocks.MARS_IRON_VEIN);
    public static final DeferredItem<BlockItem> HEMATITE_VEIN_ITEM = blockItem("hematite_vein", ModBlocks.HEMATITE_VEIN);
    public static final DeferredItem<BlockItem> SULFATE_VEIN_ITEM = blockItem("sulfate_vein", ModBlocks.SULFATE_VEIN);
    public static final DeferredItem<BlockItem> RED_GRAVEL_ITEM = blockItem("red_gravel", ModBlocks.RED_GRAVEL);
    public static final DeferredItem<BlockItem> IRON_GRAVEL_ITEM = blockItem("iron_gravel", ModBlocks.IRON_GRAVEL);
    public static final DeferredItem<BlockItem> BASALT_GRAVEL_ITEM = blockItem("basalt_gravel", ModBlocks.BASALT_GRAVEL);
    public static final DeferredItem<BlockItem> BASALT_COBBLE_ITEM = blockItem("basalt_cobble", ModBlocks.BASALT_COBBLE);
    public static final DeferredItem<BlockItem> IRONSTONE_COBBLE_ITEM = blockItem("ironstone_cobble", ModBlocks.IRONSTONE_COBBLE);
    public static final DeferredItem<BlockItem> SULFATE_COBBLE_ITEM = blockItem("sulfate_cobble", ModBlocks.SULFATE_COBBLE);
    public static final DeferredItem<BlockItem> BASALT_BOULDER_ITEM = blockItem("basalt_boulder", ModBlocks.BASALT_BOULDER);
    public static final DeferredItem<BlockItem> IRON_BOULDER_ITEM = blockItem("iron_boulder", ModBlocks.IRON_BOULDER);
    public static final DeferredItem<BlockItem> CRYOTIC_BOULDER_ITEM = blockItem("cryotic_boulder", ModBlocks.CRYOTIC_BOULDER);
    public static final DeferredItem<BlockItem> SULFATE_BOULDER_ITEM = blockItem("sulfate_boulder", ModBlocks.SULFATE_BOULDER);

    public static void register() { }
}
