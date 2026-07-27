package com.marsproject.terraformingmars.event;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.environment.MarsEnvironmentManager;
import com.marsproject.terraformingmars.environment.MarsEnvironmentStage;
import com.marsproject.terraformingmars.environment.MarsTerraformProgress;
import com.marsproject.terraformingmars.screen.TeleportHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import com.marsproject.terraformingmars.registry.ModBlocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
@EventBusSubscriber(modid = TerraformingMarsMod.MODID)
public class MarsAirSupplyHandler {
    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        LivingEntity entity = event.getEntity();
        // Chỉ chạy server
        if (entity.level().isClientSide())
            return;
        // Chỉ chạy trên Mars
        if (!entity.level().dimension().equals(TeleportHelper.MARS_LEVEL_KEY))
            return;
        ServerLevel level = (ServerLevel) entity.level();
        float progress = MarsTerraformProgress
                .get(level)
                .getProgress();
        MarsEnvironmentStage stage =
                MarsEnvironmentManager.resolve(progress);
        if (hasLandingSuit(entity)) {
            event.setCanBreathe(true);
            return;
        }
        if (hasPoweredLifeSupport(level, entity.blockPosition())) {
            event.setCanBreathe(true);
            return;
        }
        // Mars chưa có khí quyển -> không thể thở
        if (!MarsEnvironmentManager.canLive(stage)) {
            event.setCanBreathe(false);
            // Có thể tăng tốc độ mất oxy:
            // event.setConsumeAirAmount(2);
        }
    }

    private static boolean hasLandingSuit(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(Items.NETHERITE_HELMET)
                && entity.getItemBySlot(EquipmentSlot.CHEST).is(Items.NETHERITE_CHESTPLATE)
                && entity.getItemBySlot(EquipmentSlot.LEGS).is(Items.NETHERITE_LEGGINGS)
                && entity.getItemBySlot(EquipmentSlot.FEET).is(Items.NETHERITE_BOOTS);
    }

    /**
     * Stage 0 power model: a life-support unit operates when a solar array is
     * within cable range. This keeps the first implementation deterministic
     * while leaving room for a persistent energy network in Stage 1.
     */
    private static boolean hasPoweredLifeSupport(ServerLevel level, BlockPos playerPos) {
        BlockPos lifeSupport = null;
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-8, -5, -8), playerPos.offset(8, 5, 8))) {
            if (level.getBlockState(pos).is(ModBlocks.LIFE_SUPPORT_UNIT.get())) {
                lifeSupport = pos.immutable();
                break;
            }
        }
        if (lifeSupport == null) {
            return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(lifeSupport.offset(-12, -6, -12), lifeSupport.offset(12, 6, 12))) {
            if (level.getBlockState(pos).is(ModBlocks.SOLAR_ARRAY.get()) && level.canSeeSky(pos.above())) {
                return true;
            }
        }
        return false;
    }
}
