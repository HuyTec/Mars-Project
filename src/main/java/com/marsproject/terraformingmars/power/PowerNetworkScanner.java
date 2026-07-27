package com.marsproject.terraformingmars.power;

import com.marsproject.terraformingmars.block.CableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Read-only BFS over connected cable blocks. It deliberately does not cache:
 * placement/removal is therefore always reflected in the next scan. A cached
 * network manager can replace this later without changing the UPS API.
 */
public final class PowerNetworkScanner {
    public static final int MAX_CABLES_PER_SCAN = 4096;

    private PowerNetworkScanner() {
    }

    public static PowerNetworkSnapshot scan(Level level, BlockPos startCable) {
        if (!(level.getBlockState(startCable).getBlock() instanceof CableBlock)) {
            return PowerNetworkSnapshot.empty();
        }

        Queue<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> cables = new HashSet<>();
        Set<BlockPos> seenGenerators = new HashSet<>();
        List<PowerSourceInfo> generators = new ArrayList<>();
        open.add(startCable.immutable());

        boolean truncated = false;
        while (!open.isEmpty()) {
            BlockPos cablePos = open.remove();
            if (!cables.add(cablePos)) {
                continue;
            }
            if (cables.size() >= MAX_CABLES_PER_SCAN) {
                truncated = !open.isEmpty();
                break;
            }

            BlockState cableState = level.getBlockState(cablePos);
            for (Direction direction : Direction.values()) {
                if (!CableBlock.isConnected(cableState, direction)) {
                    continue;
                }

                BlockPos neighborPos = cablePos.relative(direction);
                if (!level.hasChunkAt(neighborPos)) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof CableBlock) {
                    if (CableBlock.isConnected(neighborState, direction.getOpposite())
                            && !cables.contains(neighborPos)) {
                        open.add(neighborPos.immutable());
                    }
                    continue;
                }

                if (neighborState.getBlock() instanceof PowerGenerator generator
                        && seenGenerators.add(neighborPos)
                        && generator.canConnectCable(level, neighborPos, neighborState, cablePos)) {
                    int watts = Math.max(0,
                            generator.generatedWatts(level, neighborPos, neighborState));
                    generators.add(new PowerSourceInfo(
                            neighborPos.immutable(),
                            generator.generatorType(),
                            watts
                    ));
                }
            }
        }

        int totalWatts = generators.stream().mapToInt(PowerSourceInfo::watts).sum();
        return new PowerNetworkSnapshot(
                Set.copyOf(cables),
                List.copyOf(generators),
                totalWatts,
                truncated
        );
    }
}
