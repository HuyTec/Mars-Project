package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.screen.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;

/** Removes exposed liquid water in Mars' low-pressure atmosphere. */
@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public final class MarsWaterEvaporationHandler {
    private MarsWaterEvaporationHandler() {
    }

    @SubscribeEvent
    public static void onWaterBucketUse(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();
        ItemStack held = player.getItemInHand(event.getHand());
        if (!held.is(Items.WATER_BUCKET)
                || !isLowPressureExposure(level, event.getPos())) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        if (level.isClientSide()) {
            return;
        }

        if (!player.hasInfiniteMaterials()) {
            player.setItemInHand(event.getHand(), new ItemStack(Items.BUCKET));
        }
        player.awardStat(Stats.ITEM_USED.get(Items.WATER_BUCKET));
        BlockPos vaporPos = event.getPos().relative(event.getFace());
        vaporize((ServerLevel) level, vaporPos);
        player.displayClientMessage(Component.translatable(
                "message.terraforming_mars.water_evaporated"), true);
    }

    @SubscribeEvent
    public static void onWaterNeighborUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !isLowPressureExposure(level, event.getPos())) {
            return;
        }

        BlockState state = event.getState();
        if (!state.getFluidState().is(FluidTags.WATER)) {
            return;
        }

        BlockState dryState = state.hasProperty(BlockStateProperties.WATERLOGGED)
                ? state.setValue(BlockStateProperties.WATERLOGGED, false)
                : Blocks.AIR.defaultBlockState();
        level.setBlock(event.getPos(), dryState, Block.UPDATE_ALL);
        vaporize(level, event.getPos());
    }

    @SubscribeEvent
    public static void onWaterSourceCreation(CreateFluidSourceEvent event) {
        if (event.getFluidState().is(FluidTags.WATER)
                && isLowPressureExposure(event.getLevel(), event.getPos())) {
            event.setCanConvert(false);
        }
    }

    /**
     * Replace this dimension-wide rule with a sealed-room atmosphere query
     * once pressurized habitat volumes are available.
     */
    private static boolean isLowPressureExposure(Level level, BlockPos pos) {
        return level.dimension().equals(TeleportHelper.MARS_LEVEL_KEY);
    }

    private static void vaporize(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.5F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        level.sendParticles(ParticleTypes.CLOUD,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                8,
                0.25,
                0.25,
                0.25,
                0.02);
    }
}
