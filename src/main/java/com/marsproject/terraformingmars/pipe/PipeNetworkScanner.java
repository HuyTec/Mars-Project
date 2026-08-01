package com.marsproject.terraformingmars.pipe;

import com.marsproject.terraformingmars.block.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Read-only BFS for air pipes. It never traverses power cables or electrical
 * devices; future air machines must explicitly implement PipeConnectable.
 */
public final class PipeNetworkScanner {
    public static final int MAX_PIPES_PER_SCAN = 4096;

    private PipeNetworkScanner() {
    }

    public static PipeNetworkSnapshot scan(Level level, BlockPos startPipe) {
        if (!(level.getBlockState(startPipe).getBlock() instanceof PipeBlock)) {
            return PipeNetworkSnapshot.empty();
        }

        Queue<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> pipes = new HashSet<>();
        Set<BlockPos> machines = new HashSet<>();
        open.add(startPipe.immutable());
        PipeType pipeType = ((PipeBlock) level.getBlockState(startPipe).getBlock()).getPipeType();

        boolean truncated = false;
        while (!open.isEmpty()) {
            BlockPos pipePos = open.remove();
            if (!pipes.add(pipePos)) {
                continue;
            }
            if (pipes.size() >= MAX_PIPES_PER_SCAN) {
                truncated = !open.isEmpty();
                break;
            }

            BlockState pipeState = level.getBlockState(pipePos);
            for (Direction direction : Direction.values()) {
                if (!PipeBlock.isConnected(pipeState, direction)) {
                    continue;
                }

                BlockPos neighborPos = pipePos.relative(direction);
                if (!level.hasChunkAt(neighborPos)) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof PipeBlock neighborPipe) {
                    if (neighborPipe.getPipeType() == pipeType
                            && PipeBlock.isConnected(neighborState, direction.getOpposite())
                            && !pipes.contains(neighborPos)) {
                        open.add(neighborPos.immutable());
                    }
                } else if (neighborState.getBlock() instanceof PipeConnectable connectable
                        && connectable.canConnectPipe(
                                level, neighborPos, neighborState, pipePos)) {
                    machines.add(neighborPos.immutable());
                }
            }
        }

        return new PipeNetworkSnapshot(
                Set.copyOf(pipes),
                Set.copyOf(machines),
                truncated
        );
    }
}
