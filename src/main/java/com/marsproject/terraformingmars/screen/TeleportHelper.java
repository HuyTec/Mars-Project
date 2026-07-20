package com.marsproject.terraformingmars.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;

import java.util.Set;

public class TeleportHelper {

    public static void teleportToMars(ServerPlayer serverPlayer) {
        ServerLevel targetLevel = serverPlayer.server.getLevel(
                ResourceKey.create(Registries.DIMENSION,
                        new ResourceLocation("terraforming_mars", "mars")));

        if (targetLevel != null) {
            serverPlayer.teleportTo(targetLevel, serverPlayer.getX(), 100, serverPlayer.getZ(),
                    Set.<RelativeMovement>of(), serverPlayer.getYRot(), serverPlayer.getXRot());
        }
    }
}