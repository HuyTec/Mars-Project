package com.marsproject.terraformingmars.survival;

import com.marsproject.terraformingmars.atmosphere.RoomAtmosphereManager;
import com.marsproject.terraformingmars.atmosphere.RoomClimateManager;
import com.marsproject.terraformingmars.block.PipeBlock;
import com.marsproject.terraformingmars.block.entity.MachineBlockEntity;
import com.marsproject.terraformingmars.gas.GasType;
import com.marsproject.terraformingmars.machine.ResourceStorage;
import com.marsproject.terraformingmars.pipe.PipeNetworkScanner;
import com.marsproject.terraformingmars.pipe.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Set;

public final class MarsAgricultureService {
    private static final int IRRIGATION_SEARCH_RADIUS = 3;

    private MarsAgricultureService() {
    }

    public static boolean canPlant(ServerLevel level, BlockPos pos) {
        return hasEnvironment(level, pos) && consumeIrrigationWater(level, pos, 5);
    }

    public static boolean canGrow(ServerLevel level, BlockPos pos) {
        return hasEnvironment(level, pos) && consumeIrrigationWater(level, pos, 1);
    }

    private static boolean hasEnvironment(ServerLevel level, BlockPos pos) {
        return RoomAtmosphereManager.hasBreathableAir(level, pos)
                && RoomClimateManager.isClimateControlled(level, pos)
                && level.getMaxLocalRawBrightness(pos) >= 9;
    }

    private static boolean consumeIrrigationWater(ServerLevel level, BlockPos pos, int amount) {
        Set<BlockPos> checkedPipes = new HashSet<>();
        for (BlockPos candidate : BlockPos.betweenClosed(
                pos.offset(-IRRIGATION_SEARCH_RADIUS, -1, -IRRIGATION_SEARCH_RADIUS),
                pos.offset(IRRIGATION_SEARCH_RADIUS, 1, IRRIGATION_SEARCH_RADIUS))) {
            if (!(level.getBlockState(candidate).getBlock() instanceof PipeBlock pipe)
                    || pipe.getPipeType() != PipeType.FLUID
                    || !checkedPipes.add(candidate.immutable())) continue;
            var network = PipeNetworkScanner.scan(level, candidate);
            checkedPipes.addAll(network.pipes());
            for (BlockPos devicePos : network.connectedMachines()) {
                if (!(level.getBlockEntity(devicePos) instanceof ResourceStorage storage)
                        || storage.getStoredResource(GasType.WATER) < amount) continue;
                if (storage instanceof MachineBlockEntity machine
                        && !machine.isOutputNetwork(GasType.WATER, network.pipes())) continue;
                return storage.extractResource(GasType.WATER, amount) == amount;
            }
        }
        return false;
    }
}
