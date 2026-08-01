package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.block.entity.SolarArrayBlockEntity;
import com.marsproject.terraformingmars.block.entity.CableBlockEntity;
import com.marsproject.terraformingmars.block.entity.UpsBlockEntity;
import com.marsproject.terraformingmars.block.entity.MultiblockPartBlockEntity;
import com.marsproject.terraformingmars.block.entity.PipeBlockEntity;
import com.marsproject.terraformingmars.block.entity.MachineBlockEntity;
import com.marsproject.terraformingmars.block.entity.ResourceTankBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TerraformingMarsMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarArrayBlockEntity>> SOLAR_ARRAY =
            BLOCK_ENTITY_TYPES.register("solar_array",
                    () -> BlockEntityType.Builder.of(
                            SolarArrayBlockEntity::new,
                            ModBlocks.SOLAR_ARRAYS.stream()
                                    .map(holder -> (Block) holder.get())
                                    .toArray(Block[]::new)
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CableBlockEntity>> POWER_CABLE =
            BLOCK_ENTITY_TYPES.register("power_cable",
                    () -> BlockEntityType.Builder.of(
                            CableBlockEntity::new,
                            ModBlocks.POWER_CABLE.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PipeBlockEntity>> AIR_PIPE =
            BLOCK_ENTITY_TYPES.register("air_pipe",
                    () -> BlockEntityType.Builder.of(
                            PipeBlockEntity::new,
                            ModBlocks.AIR_PIPE.get(),
                            ModBlocks.FLUID_PIPE.get(),
                            ModBlocks.HEAT_PIPE.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineBlockEntity>> MACHINE =
            BLOCK_ENTITY_TYPES.register("machine",
                    () -> BlockEntityType.Builder.of(
                            MachineBlockEntity::new,
                            ModBlocks.MACHINES.stream()
                                    .map(holder -> (Block) holder.get())
                                    .toArray(Block[]::new)
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResourceTankBlockEntity>> RESOURCE_TANK =
            BLOCK_ENTITY_TYPES.register("resource_tank",
                    () -> BlockEntityType.Builder.of(
                            ResourceTankBlockEntity::new,
                            ModBlocks.FLUID_TANK.get(), ModBlocks.GAS_TANK.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpsBlockEntity>> UPS =
            BLOCK_ENTITY_TYPES.register("ups",
                    () -> BlockEntityType.Builder.of(
                            UpsBlockEntity::new,
                            ModBlocks.UPS.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockPartBlockEntity>>
            MULTIBLOCK_PART = BLOCK_ENTITY_TYPES.register("multiblock_part",
                    () -> BlockEntityType.Builder.of(
                            MultiblockPartBlockEntity::new,
                            ModBlocks.MULTIBLOCK_PART.get()
                    ).build(null));

    private ModBlockEntities() {
    }
}
