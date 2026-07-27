package com.marsproject.terraformingmars.pipe;

import net.minecraft.core.BlockPos;

import java.util.Set;

/** Topology discovered during one read-only pipe scan. */
public record PipeNetworkSnapshot(
        Set<BlockPos> pipes,
        Set<BlockPos> connectedMachines,
        boolean truncated
) {
    public static PipeNetworkSnapshot empty() {
        return new PipeNetworkSnapshot(Set.of(), Set.of(), false);
    }
}
