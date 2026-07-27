package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.MarsDustBlock;
import com.marsproject.terraformingmars.block.DryIceLayerBlock;
import com.marsproject.terraformingmars.block.SolarArrayBlock;
import com.marsproject.terraformingmars.block.SolarArrayType;
import com.marsproject.terraformingmars.block.CableBlock;
import com.marsproject.terraformingmars.block.SevenLayerBlock;
import com.marsproject.terraformingmars.block.UpsBlock;
import com.marsproject.terraformingmars.block.MultiblockPart;
import com.marsproject.terraformingmars.block.MultiblockPartBlock;
import com.marsproject.terraformingmars.block.PipeBlock;
import com.marsproject.terraformingmars.block.MachineBlock;
import com.marsproject.terraformingmars.machine.MachineType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/** All natural Mars terrain materials. Textures are supplied as resource assets. */
public final class ModBlocks {
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TerraformingMarsMod.MODID);
        public static final List<DeferredBlock<SolarArrayBlock>> SOLAR_ARRAYS = new ArrayList<>();
        public static final List<DeferredBlock<MachineBlock>> MACHINES = new ArrayList<>();

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

        private static SolarArrayType solarType(int watts, float dustRate,
                                                float maxDustPenalty, float modelScale,
                                                double width, double height, double depth,
                                                ResourceLocation model, ResourceLocation texture,
                                                ResourceLocation animation,
                                                List<MultiblockPart> parts) {
                return new SolarArrayType(watts, dustRate, maxDustPenalty, modelScale,
                        width, height, depth, model, texture, animation, parts);
                }
                

        private static DeferredBlock<SolarArrayBlock> solarArray(String name, SolarArrayType type) {
                DeferredBlock<SolarArrayBlock> block = BLOCKS.register(name, () -> new SolarArrayBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_BLUE)
                                .strength(2.0F)
                                .sound(SoundType.METAL)
                                .requiresCorrectToolForDrops(),
                        type
                ));
                SOLAR_ARRAYS.add(block);
                return block;
        }

        private static DeferredBlock<MachineBlock> machine(String name, MachineType type) {
                DeferredBlock<MachineBlock> block = BLOCKS.register(name, () -> new MachineBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                                .strength(2.5F)
                                .sound(SoundType.METAL)
                                .requiresCorrectToolForDrops()
                                .noOcclusion(),
                        type
                ));
                MACHINES.add(block);
                return block;
        }

        // --- Bụi / regolith bề mặt ---
        public static final DeferredBlock<Block> DUST_DEPOSIT = falling("dust_deposit", MapColor.COLOR_ORANGE);
        public static final DeferredBlock<Block> FINE_DUST = falling("fine_dust", MapColor.COLOR_ORANGE);
        public static final DeferredBlock<Block> COMPACTED_DUST = solid("compacted_dust", MapColor.COLOR_BROWN, 0.8F, SoundType.SOUL_SAND);
        public static final DeferredBlock<Block> WIND_CRUST = solid("wind_crust", MapColor.COLOR_ORANGE, 0.8F, SoundType.SOUL_SAND);
        /** Oxidised, dust-contaminated parent rock between regolith and intact basalt. */
        public static final DeferredBlock<Block> DIRTY_STONE = solid("dirty_stone", MapColor.COLOR_BROWN, 1.5F, SoundType.TUFF);
        public static final DeferredBlock<Block> DUST_LAYER = BLOCKS.register("dust_layer", () -> new SevenLayerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.1F).sound(SoundType.SNOW).noOcclusion()));
        public static final DeferredBlock<Block> DRY_ICE_LAYER = BLOCKS.register("dry_ice_layer",
                () -> new DryIceLayerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.ICE)
                        .strength(0.15F)
                        .sound(SoundType.POWDER_SNOW)
                        .noOcclusion()
                        .randomTicks()));
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
        public static final DeferredBlock<Block> SALT_LAYER = BLOCKS.register("salt_layer", () -> new SevenLayerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.1F).sound(SoundType.SNOW).noOcclusion()));

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

        // --- Stage 0 landing infrastructure ---
        public static final DeferredBlock<SolarArrayBlock> SOLAR_ARRAY = solarArray(
                "solar_array",
                solarType(250, 0.0000025F, 0.70F, 1.0F, 1.0, 0.75, 1.0,
                        new ResourceLocation(TerraformingMarsMod.MODID, "geo/solar_array.geo.json"),
                                new ResourceLocation(TerraformingMarsMod.MODID, "textures/block/solar_array.png"),
                                new ResourceLocation(TerraformingMarsMod.MODID, "animations/solar_array.animation.json"),
                                List.of())
        );
        public static final DeferredBlock<SolarArrayBlock> ADVANCED_SOLAR_ARRAY = solarArray(
                        "advanced_solar_array",
                        solarType(1250, 0.0000015F, 0.55F, 1.15F, 1.15, 0.9, 1.15,
                                new ResourceLocation(TerraformingMarsMod.MODID, "geo/advanced_solar_array.geo.json"),
                                new ResourceLocation(TerraformingMarsMod.MODID, "textures/block/advanced_solar_array.png"),
                                new ResourceLocation(TerraformingMarsMod.MODID, "animations/advanced_solar_array.animation.json"),
                                List.of(
                                        new MultiblockPart(new BlockPos(0, 1, 0), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(0, 2, 0), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(0, 2, -1), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(0, 2, -2), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(0, 2, 1), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(0, 2, 2), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(1, 2, 0), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(1, 2, -1), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(1, 2, -2), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(1, 2, 1), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(1, 2, 2), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(-1, 2, 0), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(-1, 2, -1), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(-1, 2, -2), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(-1, 2, 1), Block.box(0, 0, 0, 16, 16, 16)),
                                        new MultiblockPart(new BlockPos(-1, 2, 2), Block.box(0, 0, 0, 16, 16, 16))
                                ))
                );

        public static final DeferredBlock<MachineBlock> OXYGEN_GENERATOR = machine(
                "oxygen_generator",
                new MachineType(
                        new ResourceLocation(TerraformingMarsMod.MODID, "oxygen_generator"),
                        1,
                        1,
                        new ResourceLocation(TerraformingMarsMod.MODID, "geo/ups.geo.json"),
                        new ResourceLocation(TerraformingMarsMod.MODID, "textures/block/ups.png"),
                        new ResourceLocation(TerraformingMarsMod.MODID, "animations/ups.animation.json"),
                        List.of(),
                        "idle",
                        "working",
                        "no_power"
                )
        );

        public static final DeferredBlock<Block> POWER_CABLE = BLOCKS.register("power_cable",
                () -> new CableBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_ORANGE)
                        .strength(0.8F)
                        .sound(SoundType.COPPER)
                        .noOcclusion()));
                        public static final DeferredBlock<Block> LIFE_SUPPORT_UNIT = BLOCKS.registerSimpleBlock("life_support_unit",
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(2.5F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops());
        public static final DeferredBlock<Block> ATMOSPHERIC_SAMPLER = BLOCKS.registerSimpleBlock("atmospheric_sampler",
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops());
        public static final DeferredBlock<Block> UPS = BLOCKS.register("ups",
                () -> new UpsBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_GRAY)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()));
        public static final DeferredBlock<Block> MULTIBLOCK_PART = BLOCKS.register("multiblock_part",
                () -> new MultiblockPartBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_GRAY)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()));
                public static final DeferredBlock<Block> AIR_PIPE = BLOCKS.register("air_pipe",
                        () -> new PipeBlock(BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                                .strength(0.8F)
                                .sound(SoundType.COPPER)
                                .noOcclusion()));
        public static void register() { }
}
