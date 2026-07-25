package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TerraformingMarsMod.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MARS_DUST =
            PARTICLE_TYPES.register("mars_dust", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DRY_ICE_CRYSTAL =
            PARTICLE_TYPES.register("dry_ice_crystal", () -> new SimpleParticleType(false));

    private ModParticles() {
    }
}
