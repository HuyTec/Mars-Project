package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.MarsDustBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All natural Mars terrain materials. Textures are supplied as resource assets. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TerraformingMarsMod.MODID);

    private static DeferredBlock<Block> solid(String name, MapColor color, float strength) {
        return solid(name, color, strength, SoundType.STONE);
    }

    private static DeferredBlock<Block> solid(String name, MapColor color, float strength, SoundType sound) {
        return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.of().mapColor(color)
                .strength(strength).requiresCorrectToolForDrops().sound(sound));
    }

    private static DeferredBlock<Block> falling(String name, MapColor color) {
        return falling(name, color, SoundType.SAND);
    }

    private static DeferredBlock<Block> falling(String name, MapColor color, SoundType sound) {
        return BLOCKS.register(name, () -> new MarsDustBlock(BlockBehaviour.Properties.of().mapColor(color)
                .strength(0.5F).sound(sound)));
    }

    // --- Bụi / regolith bề mặt ---
    public static final DeferredBlock<Block> DUST_DEPOSIT = falling("dust_deposit", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<Block> FINE_DUST = falling("fine_dust", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<Block> COMPACTED_DUST = solid("compacted_dust", MapColor.COLOR_BROWN, 0.8F, SoundType.SOUL_SAND);
    public static final DeferredBlock<Block> WIND_CRUST = solid("wind_crust", MapColor.COLOR_ORANGE, 0.8F, SoundType.SOUL_SAND);
    /** Oxidised, dust-contaminated parent rock between regolith and intact basalt. */
    public static final DeferredBlock<Block> DIRTY_STONE = solid("dirty_stone", MapColor.COLOR_BROWN, 1.5F, SoundType.TUFF);
    public static final DeferredBlock<Block> DUST_LAYER = BLOCKS.register("dust_layer", () -> new SnowLayerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.1F).sound(SoundType.SNOW).noOcclusion()));
    public static final DeferredBlock<Block> LOOSE_REGOLITH = falling("loose_regolith", MapColor.COLOR_ORANGE, SoundType.GRAVEL);
    public static final DeferredBlock<Block> COMPACTED_REGOLITH = solid("compacted_regolith", MapColor.COLOR_BROWN, 1.0F, SoundType.SOUL_SAND);
    public static final DeferredBlock<Block> ROCKY_REGOLITH = falling("rocky_regolith", MapColor.COLOR_BROWN, SoundType.GRAVEL);
    public static final DeferredBlock<Block> IRON_RICH_REGOLITH = falling("iron_rich_regolith", MapColor.COLOR_RED, SoundType.GRAVEL);
    public static final DeferredBlock<Block> ICE_RICH_REGOLITH = solid("ice_rich_regolith", MapColor.ICE, 1.2F, SoundType.POWDER_SNOW);

    // --- Basalt (núi lửa) ---
    public static final DeferredBlock<Block> BASALTIC_ROCK = solid("basaltic_rock", MapColor.COLOR_GRAY, 2.0F, SoundType.BASALT);
    public static final DeferredBlock<Block> WEATHERED_BASALT = solid("weathered_basalt", MapColor.COLOR_BROWN, 1.8F, SoundType.BASALT);
    public static final DeferredBlock<Block> FRACTURED_BASALT = solid("fractured_basalt", MapColor.COLOR_GRAY, 2.0F, SoundType.BASALT);
    public static final DeferredBlock<Block> VESICULAR_BASALT = solid("vesicular_basalt", MapColor.COLOR_BLACK, 2.0F, SoundType.BASALT);
    public static final DeferredBlock<Block> MASSIVE_BASALT = solid("massive_basalt", MapColor.COLOR_GRAY, 2.5F, SoundType.BASALT);
    public static final DeferredBlock<Block> FERRIC_BASALT = solid("ferric_basalt", MapColor.COLOR_RED, 2.2F, SoundType.BASALT);
    public static final DeferredBlock<Block> FROST_BASALT = solid("frost_basalt", MapColor.ICE, 2.1F, SoundType.BASALT);

    // --- Sulfat ---
    public static final DeferredBlock<Block> SULFATE_ROCK = solid("sulfate_rock", MapColor.COLOR_YELLOW, 1.5F, SoundType.TUFF);
    public static final DeferredBlock<Block> LAYERED_SULFATE = solid("layered_sulfate", MapColor.COLOR_YELLOW, 1.5F, SoundType.TUFF);

    // --- Carbonat ---
    public static final DeferredBlock<Block> CARBONATE_ROCK = solid("carbonate_rock", MapColor.QUARTZ, 1.5F, SoundType.CALCITE);
    public static final DeferredBlock<Block> LAYERED_CARBONATE = solid("layered_carbonate", MapColor.QUARTZ, 1.6F, SoundType.CALCITE);

    // --- Sét / bùn ---
    public static final DeferredBlock<Block> CLAYSTONE = solid("claystone", MapColor.TERRACOTTA_BROWN, 1.2F, SoundType.MUD);
    public static final DeferredBlock<Block> MUDSTONE = solid("mudstone", MapColor.COLOR_BROWN, 1.0F, SoundType.MUD);
    public static final DeferredBlock<Block> EVAPORITE = solid("evaporite", MapColor.QUARTZ, 1.4F, SoundType.CALCITE);

    // --- Sắt / hematit / magnetit ---
    public static final DeferredBlock<Block> IRONSTONE = solid("ironstone", MapColor.COLOR_BROWN, 3.0F, SoundType.DEEPSLATE);
    public static final DeferredBlock<Block> HEMATITE_LAYER = solid("hematite_layer", MapColor.COLOR_RED, 2.0F, SoundType.DEEPSLATE);
    public static final DeferredBlock<Block> OXIDIZED_ROCK = solid("oxidized_rock", MapColor.COLOR_ORANGE, 1.8F, SoundType.DEEPSLATE);
    public static final DeferredBlock<Block> MAGNETITE_ROCK = solid("magnetite_rock", MapColor.COLOR_BLACK, 2.5F, SoundType.ANCIENT_DEBRIS);

    // --- Băng giá ---
    public static final DeferredBlock<Block> CRYOTIC_ROCK = solid("cryotic_rock", MapColor.ICE, 2.0F, SoundType.POWDER_SNOW);
    public static final DeferredBlock<Block> FROZEN_DUST = falling("frozen_dust", MapColor.ICE, SoundType.POWDER_SNOW);
    public static final DeferredBlock<Block> PERMAFROST = solid("permafrost", MapColor.ICE, 1.0F, SoundType.POWDER_SNOW);

    public static final DeferredBlock<Block> CARBONATE_VEIN = solid("carbonate_vein", MapColor.QUARTZ, 2.0F, SoundType.CALCITE);
    public static final DeferredBlock<Block> WHITE_CARBONATE = solid("white_carbonate", MapColor.SNOW, 1.8F, SoundType.CALCITE);

    // --- Silica ---
    public static final DeferredBlock<Block> SILICA_DEPOSIT = solid("silica_deposit", MapColor.QUARTZ, 1.8F, SoundType.AMETHYST);
    public static final DeferredBlock<Block> OPALINE_SILICA = solid("opaline_silica", MapColor.QUARTZ, 1.5F, SoundType.AMETHYST);
    public static final DeferredBlock<Block> SILICA_CRUST = solid("silica_crust", MapColor.QUARTZ, 1.5F, SoundType.AMETHYST);
    public static final DeferredBlock<Block> SILICA_VEIN = solid("silica_vein", MapColor.QUARTZ, 2.0F, SoundType.AMETHYST);

    /** Rare phosphate vein in iron_highlands: Ca5(PO4)3(OH,F,Cl). */
    public static final DeferredBlock<Block> APATITE_VEIN = solid("apatite_vein", MapColor.COLOR_GREEN, 2.2F, SoundType.CALCITE);
    /** Rare feldspar vein in crater_field: K-rich feldspar. */
    public static final DeferredBlock<Block> FELDSPAR_VEIN = solid("feldspar_vein", MapColor.COLOR_LIGHT_GRAY, 2.0F, SoundType.STONE);

    public static final DeferredBlock<Block> CLAY_RICH_ROCK = solid("clay_rich_rock", MapColor.CLAY, 1.2F, SoundType.MUD);
    public static final DeferredBlock<Block> SMECTITE = solid("smectite", MapColor.TERRACOTTA_BROWN, 1.2F, SoundType.MUD);
    public static final DeferredBlock<Block> BENTONITE = solid("bentonite", MapColor.TERRACOTTA_BROWN, 1.2F, SoundType.MUD);

    // --- Muối ---
    public static final DeferredBlock<Block> CHLORIDE_DEPOSIT = solid("chloride_deposit", MapColor.SNOW, 1.4F, SoundType.CALCITE);
    public static final DeferredBlock<Block> SALT_CRUST = falling("salt_crust", MapColor.SNOW);
    public static final DeferredBlock<Block> SALT_LAYER = BLOCKS.register("salt_layer", () -> new SnowLayerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.1F).sound(SoundType.SNOW).noOcclusion()));

    // --- Vein kim loại ---
    public static final DeferredBlock<Block> MARS_IRON_VEIN = solid("mars_iron_vein", MapColor.COLOR_RED, 3.0F, SoundType.DEEPSLATE);
    public static final DeferredBlock<Block> HEMATITE_VEIN = solid("hematite_vein", MapColor.COLOR_RED, 3.0F, SoundType.DEEPSLATE);
    public static final DeferredBlock<Block> SULFATE_VEIN = solid("sulfate_vein", MapColor.COLOR_YELLOW, 2.0F, SoundType.TUFF);

    // --- Gravel / cobble / boulder ---
    public static final DeferredBlock<Block> RED_GRAVEL = falling("red_gravel", MapColor.COLOR_RED, SoundType.GRAVEL);
    public static final DeferredBlock<Block> IRON_GRAVEL = falling("iron_gravel", MapColor.COLOR_BROWN, SoundType.GRAVEL);
    public static final DeferredBlock<Block> BASALT_GRAVEL = falling("basalt_gravel", MapColor.COLOR_GRAY, SoundType.BASALT);
    public static final DeferredBlock<Block> BASALT_COBBLE = solid("basalt_cobble", MapColor.COLOR_GRAY, 2.0F, SoundType.BASALT);
    public static final DeferredBlock<Block> IRONSTONE_COBBLE = solid("ironstone_cobble", MapColor.COLOR_BROWN, 2.5F, SoundType.DEEPSLATE);
    public static final DeferredBlock<Block> SULFATE_COBBLE = solid("sulfate_cobble", MapColor.COLOR_YELLOW, 1.5F, SoundType.TUFF);
    public static final DeferredBlock<Block> BASALT_BOULDER = solid("basalt_boulder", MapColor.COLOR_GRAY, 3.0F, SoundType.BASALT);
    public static final DeferredBlock<Block> IRON_BOULDER = solid("iron_boulder", MapColor.COLOR_BROWN, 3.0F, SoundType.DEEPSLATE);
    public static final DeferredBlock<Block> CRYOTIC_BOULDER = solid("cryotic_boulder", MapColor.ICE, 3.0F, SoundType.POWDER_SNOW);
    public static final DeferredBlock<Block> SULFATE_BOULDER = solid("sulfate_boulder", MapColor.COLOR_YELLOW, 2.0F, SoundType.TUFF);

    public static void register() { }
}