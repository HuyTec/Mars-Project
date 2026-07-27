package com.marsproject.terraformingmars.power;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Immutable result used later by the UPS menu and network sync payload. */
public record PowerSourceInfo(BlockPos position, ResourceLocation type, int watts) {
}
