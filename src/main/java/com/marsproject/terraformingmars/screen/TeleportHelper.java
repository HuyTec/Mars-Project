package com.marsproject.terraformingmars.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.marsproject.terraformingmars.world.MarsBaseManager;

import java.util.Set;

public class TeleportHelper {

    public static final ResourceKey<Level> MARS_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("terraforming_mars", "mars"));

    public static void teleportToMars(ServerPlayer serverPlayer) {
        ServerLevel targetLevel = serverPlayer.server.getLevel(MARS_LEVEL_KEY);

        if (targetLevel != null) {
            var landingPos = MarsBaseManager.ensureGenerated(targetLevel)
                    .orElseGet(() -> new net.minecraft.core.BlockPos(
                            serverPlayer.getBlockX(), 100, serverPlayer.getBlockZ()));
            serverPlayer.teleportTo(targetLevel,
                    landingPos.getX() + 0.5,
                    landingPos.getY(),
                    landingPos.getZ() + 0.5,
                    Set.<RelativeMovement>of(), serverPlayer.getYRot(), serverPlayer.getXRot());
            giveLandingKit(serverPlayer);
        }
    }

    private static void giveLandingKit(ServerPlayer player) {
        if (player.getPersistentData().getBoolean("terraforming_mars_stage0_kit")) {
            return;
        }
        player.getPersistentData().putBoolean("terraforming_mars_stage0_kit", true);
        player.getInventory().add(new ItemStack(Items.STONE_PICKAXE));
        player.getInventory().add(new ItemStack(Items.STONE_SHOVEL));
        player.getInventory().add(new ItemStack(Items.LEATHER_HELMET));
        player.getInventory().add(new ItemStack(Items.LEATHER_CHESTPLATE));
        player.getInventory().add(new ItemStack(Items.BREAD, 32));
        player.displayClientMessage(Component.translatable("message.terraforming_mars.landing_kit"), false);
    }
}
