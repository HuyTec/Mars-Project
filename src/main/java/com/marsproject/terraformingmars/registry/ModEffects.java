package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import com.marsproject.terraformingmars.effect.RadiationEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, TerraformingMarsMod.MODID);

    public static final DeferredHolder<MobEffect, RadiationEffect> RADIATION =
            EFFECTS.register("radiation_attack", RadiationEffect::new);
}