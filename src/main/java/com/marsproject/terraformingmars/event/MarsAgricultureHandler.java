package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import com.marsproject.terraformingmars.survival.MarsAgricultureService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

/** Temporarily disables Mars agriculture until controlled water and heat exist. */
@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public final class MarsAgricultureHandler {
    private MarsAgricultureHandler() {
    }

    @SubscribeEvent
    public static void onPlantPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !isMars(level)
                || !isAgriculturalPlant(event.getPlacedBlock())) {
            return;
        }

        if (MarsAgricultureService.canPlant(level, event.getPos())) return;

        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(
                    "message.terraforming_mars.agriculture_unavailable"), true);
        }
    }

    @SubscribeEvent
    public static void onCropGrowth(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isMars(level)) {
            return;
        }

        if (MarsAgricultureService.canGrow(level, event.getPos())) return;

        event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        level.destroyBlock(event.getPos(), true);
    }

    @SubscribeEvent
    public static void onFeatureGrowth(BlockGrowFeatureEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isMars(level)) {
            return;
        }

        if (MarsAgricultureService.canGrow(level, event.getPos())) return;
        event.setCanceled(true);
        level.destroyBlock(event.getPos(), true);
    }

    private static boolean isMars(ServerLevel level) {
        return level.dimension().equals(TeleportHelper.MARS_LEVEL_KEY);
    }

    private static boolean isAgriculturalPlant(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CropBlock
                || block instanceof StemBlock
                || block instanceof AttachedStemBlock
                || block instanceof NetherWartBlock
                || block instanceof CocoaBlock
                || block instanceof SweetBerryBushBlock
                || block instanceof SaplingBlock
                || block instanceof MushroomBlock;
    }
}
