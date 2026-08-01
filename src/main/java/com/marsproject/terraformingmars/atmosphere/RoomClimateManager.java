package com.marsproject.terraformingmars.atmosphere;

import com.marsproject.terraformingmars.block.AirVentBlock;
import com.marsproject.terraformingmars.block.PipeBlock;
import com.marsproject.terraformingmars.event.PlayerSurvivalHandler;
import com.marsproject.terraformingmars.pipe.PipeNetworkScanner;
import com.marsproject.terraformingmars.pipe.PipeType;
import com.marsproject.terraformingmars.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;

/** Cached room-average climate; no per-block heat simulation is performed. */
public final class RoomClimateManager {
    private static final int MAX_VOLUME = RoomAtmosphereManager.MAX_ROOM_VOLUME;
    private static final long ACTIVE_GRACE_TICKS = 80;
    private static final Map<ServerLevel, Map<Long, HeatedZone>> ZONES = new WeakHashMap<>();

    private RoomClimateManager() {
    }

    public static void supplyHeat(ServerLevel level, BlockPos heaterPos, int heatUnits) {
        Set<BlockPos> room = findTargetRoom(level, heaterPos);
        if (room.isEmpty()) return;

        long anchor = room.stream().mapToLong(BlockPos::asLong).min().orElse(heaterPos.asLong());
        Map<Long, HeatedZone> zones = ZONES.computeIfAbsent(level, ignored -> new HashMap<>());
        HeatedZone zone = zones.computeIfAbsent(anchor, ignored -> new HeatedZone(room));
        if (!zone.positions.equals(room)) zone.positions = room;

        long now = level.getGameTime();
        double outside = PlayerSurvivalHandler.ambientTemperature(level, heaterPos);
        double elapsedSeconds = Math.max(1.0, (now - zone.lastUpdateTick) / 20.0);
        zone.temperature += (outside - zone.temperature) * Math.min(0.25, 0.002 * elapsedSeconds);
        zone.temperature += heatUnits * 0.20 / Math.max(1, zone.positions.size());
        zone.temperature = Math.max(outside, Math.min(35.0, zone.temperature));
        zone.lastUpdateTick = now;
        zone.lastSuppliedTick = now;
    }

    public static OptionalDouble temperatureAt(ServerLevel level, BlockPos pos) {
        Map<Long, HeatedZone> zones = ZONES.get(level);
        if (zones == null) return OptionalDouble.empty();
        long now = level.getGameTime();
        zones.values().removeIf(zone -> now - zone.lastSuppliedTick > ACTIVE_GRACE_TICKS * 4);
        for (HeatedZone zone : zones.values()) {
            if (now - zone.lastSuppliedTick <= ACTIVE_GRACE_TICKS && zone.positions.contains(pos)) {
                return OptionalDouble.of(zone.temperature);
            }
        }
        return OptionalDouble.empty();
    }

    public static OptionalDouble temperatureNear(ServerLevel level, BlockPos pos) {
        OptionalDouble direct = temperatureAt(level, pos);
        if (direct.isPresent()) return direct;
        for (Direction direction : Direction.values()) {
            OptionalDouble adjacent = temperatureAt(level, pos.relative(direction));
            if (adjacent.isPresent()) return adjacent;
        }
        return OptionalDouble.empty();
    }

    public static boolean isClimateControlled(ServerLevel level, BlockPos pos) {
        OptionalDouble temperature = temperatureAt(level, pos);
        return temperature.isPresent()
                && temperature.getAsDouble() >= 10.0
                && temperature.getAsDouble() <= 35.0
                && RoomAtmosphereManager.hasBreathableAir(level, pos);
    }

    private static Set<BlockPos> findTargetRoom(ServerLevel level, BlockPos heaterPos) {
        for (Direction direction : Direction.values()) {
            BlockPos candidate = heaterPos.relative(direction);
            Set<BlockPos> room = collectBreathable(level, candidate);
            if (!room.isEmpty()) return room;
        }

        for (Direction direction : Direction.values()) {
            BlockPos pipePos = heaterPos.relative(direction);
            if (!(level.getBlockState(pipePos).getBlock() instanceof PipeBlock pipe)
                    || pipe.getPipeType() != PipeType.HEAT) continue;
            var network = PipeNetworkScanner.scan(level, pipePos);
            for (BlockPos endpoint : network.connectedMachines()) {
                if (!(level.getBlockState(endpoint).getBlock() instanceof AirVentBlock)) continue;
                BlockPos sample = AirVentBlock.samplePos(endpoint, level.getBlockState(endpoint));
                Set<BlockPos> room = collectBreathable(level, sample);
                if (!room.isEmpty()) return room;
            }
        }
        return Set.of();
    }

    private static Set<BlockPos> collectBreathable(ServerLevel level, BlockPos start) {
        if (!level.getBlockState(start).is(ModBlocks.BREATHABLE_AIR.get())) return Set.of();
        Queue<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        open.add(start.immutable());
        while (!open.isEmpty() && visited.size() < MAX_VOLUME) {
            BlockPos pos = open.remove();
            if (!visited.add(pos)) continue;
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!visited.contains(next)
                        && level.getBlockState(next).is(ModBlocks.BREATHABLE_AIR.get())) {
                    open.add(next.immutable());
                }
            }
        }
        return visited.size() >= MAX_VOLUME ? Set.of() : Set.copyOf(visited);
    }

    private static final class HeatedZone {
        private Set<BlockPos> positions;
        private double temperature = 18.0;
        private long lastUpdateTick;
        private long lastSuppliedTick;

        private HeatedZone(Set<BlockPos> positions) {
            this.positions = positions;
        }
    }
}
