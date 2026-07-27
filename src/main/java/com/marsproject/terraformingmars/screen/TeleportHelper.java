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
import com.marsproject.terraformingmars.registry.ModItems;

import java.util.Set;

public class TeleportHelper {

    public static final ResourceKey<Level> MARS_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("terraforming_mars", "mars"));

    public static void teleportToMars(ServerPlayer serverPlayer) {
        ServerLevel targetLevel = serverPlayer.server.getLevel(MARS_LEVEL_KEY);

        if (targetLevel != null) {
            serverPlayer.teleportTo(targetLevel, serverPlayer.getX(), 100, serverPlayer.getZ(),
                    Set.<RelativeMovement>of(), serverPlayer.getYRot(), serverPlayer.getXRot());
            giveLandingKit(serverPlayer);
        }
    }

    private static void giveLandingKit(ServerPlayer player) {
        if (player.getPersistentData().getBoolean("terraforming_mars_stage0_kit")) {
            return;
        }
        player.getPersistentData().putBoolean("terraforming_mars_stage0_kit", true);
        player.getInventory().add(new ItemStack(ModItems.HABITAT_KIT.get()));
        player.getInventory().add(new ItemStack(ModItems.SOLAR_ARRAY_ITEM.get(), 2));
        player.getInventory().add(new ItemStack(ModItems.POWER_CABLE_ITEM.get(), 24));
        player.getInventory().add(new ItemStack(ModItems.ATMOSPHERIC_SAMPLER_ITEM.get()));
        player.getInventory().add(new ItemStack(Items.IRON_DOOR, 2));
        player.getInventory().add(new ItemStack(Items.NETHERITE_PICKAXE));
        player.getInventory().add(new ItemStack(Items.NETHERITE_SHOVEL));
        player.getInventory().add(new ItemStack(Items.NETHERITE_HELMET));
        player.getInventory().add(new ItemStack(Items.NETHERITE_CHESTPLATE));
        player.getInventory().add(new ItemStack(Items.NETHERITE_LEGGINGS));
        player.getInventory().add(new ItemStack(Items.NETHERITE_BOOTS));
        player.getInventory().add(new ItemStack(Items.COOKED_BEEF, 32));
        player.displayClientMessage(Component.translatable("message.terraforming_mars.landing_kit"), false);
    }
}
