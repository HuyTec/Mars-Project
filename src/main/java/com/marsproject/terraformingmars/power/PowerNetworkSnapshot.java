package com.marsproject.terraformingmars.power;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Set;

public record PowerNetworkSnapshot(
        Set<BlockPos> cables,
        List<PowerSourceInfo> generators,
        int totalWatts,
        boolean truncated
) {
    public static PowerNetworkSnapshot empty() {
        return new PowerNetworkSnapshot(Set.of(), List.of(), 0, false);
    }
}
