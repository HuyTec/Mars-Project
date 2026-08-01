package com.marsproject.terraformingmars.atmosphere;

import com.marsproject.terraformingmars.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Bounded room scan used for initial base air, Air Creator filling and leak venting. */
public final class RoomAtmosphereManager {
    public static final int MAX_ROOM_VOLUME = 16_384;

    private RoomAtmosphereManager() {
    }

    public static int fillInitialRoom(ServerLevel level, BlockPos interiorPos) {
        RoomScan scan = scan(level, interiorPos);
        return scan.sealed() ? fill(level, scan.unfilledAir(), Integer.MAX_VALUE) : 0;
    }

    public static int fillFromMachine(ServerLevel level, BlockPos machinePos, int availableGas) {
        if (availableGas <= 0) {
            return 0;
        }
        Set<BlockPos> checkedStarts = new HashSet<>();
        for (Direction direction : Direction.values()) {
            BlockPos start = machinePos.relative(direction);
            if (!checkedStarts.add(start) || !isTraversable(level, start)) {
                continue;
            }
            RoomScan scan = scan(level, start);
            if (scan.sealed() && !scan.unfilledAir().isEmpty()) {
                return fill(level, scan.unfilledAir(), availableGas);
            }
        }
        return 0;
    }

    public static boolean hasBreathableAir(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(ModBlocks.BREATHABLE_AIR.get())
                || level.getBlockState(pos.above()).is(ModBlocks.BREATHABLE_AIR.get())) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(ModBlocks.BREATHABLE_AIR.get())) {
                return true;
            }
        }
        return false;
    }

    public static void ventIfExposed(ServerLevel level, BlockPos breathablePos) {
        if (!level.getBlockState(breathablePos).is(ModBlocks.BREATHABLE_AIR.get())) {
            return;
        }
        RoomScan scan = scan(level, breathablePos);
        if (scan.sealed()) {
            return;
        }
        for (BlockPos pos : scan.breathableAir()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private static RoomScan scan(ServerLevel level, BlockPos start) {
        if (!isTraversable(level, start)) {
            return RoomScan.blocked();
        }

        Queue<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> unfilled = new ArrayList<>();
        List<BlockPos> breathable = new ArrayList<>();
        open.add(start.immutable());
        boolean sealed = true;

        while (!open.isEmpty()) {
            BlockPos pos = open.remove();
            if (!visited.add(pos)) {
                continue;
            }
            if (visited.size() > MAX_ROOM_VOLUME
                    || pos.getY() <= level.getMinBuildHeight()
                    || pos.getY() >= level.getMaxBuildHeight() - 1) {
                sealed = false;
                break;
            }
            if (!level.hasChunkAt(pos)) {
                sealed = false;
                break;
            }

            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.BREATHABLE_AIR.get())) {
                breathable.add(pos.immutable());
            } else if (state.isAir()) {
                unfilled.add(pos.immutable());
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (!visited.contains(neighbor) && isTraversable(level, neighbor)) {
                    open.add(neighbor.immutable());
                }
            }
        }
        return new RoomScan(sealed && open.isEmpty(), List.copyOf(unfilled), List.copyOf(breathable));
    }

    private static boolean isTraversable(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        // Doors are intentionally treated as sealed even while open for forgiving base gameplay.
        // Trapdoors still create a leak whenever their OPEN property is true.
        if (state.getBlock() instanceof DoorBlock) {
            return false;
        }
        if (state.getBlock() instanceof TrapDoorBlock) {
            return state.hasProperty(BlockStateProperties.OPEN)
                    && state.getValue(BlockStateProperties.OPEN);
        }
        return false;
    }

    private static int fill(ServerLevel level, List<BlockPos> positions, int limit) {
        int count = Math.min(limit, positions.size());
        for (int i = 0; i < count; i++) {
            BlockPos pos = positions.get(i);
            if (level.getBlockState(pos).isAir()
                    && !level.getBlockState(pos).is(ModBlocks.BREATHABLE_AIR.get())) {
                level.setBlock(pos, ModBlocks.BREATHABLE_AIR.get().defaultBlockState(), 2);
            }
        }
        return count;
    }

    private record RoomScan(boolean sealed, List<BlockPos> unfilledAir,
                            List<BlockPos> breathableAir) {
        private static RoomScan blocked() {
            return new RoomScan(false, List.of(), List.of());
        }
    }
}
