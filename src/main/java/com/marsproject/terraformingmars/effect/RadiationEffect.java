package com.marsproject.terraformingmars.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class RadiationEffect extends MobEffect {

    private static final int TICKS_PER_APPLICATION = 40; // 2 giây
    private static final float DAMAGE_PER_TICK = 1.0F;    // 0.5 tim

    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 0xF4D03F);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % TICKS_PER_APPLICATION == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().magic(), DAMAGE_PER_TICK);
        return true;
    }
}