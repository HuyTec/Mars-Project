package com.marsproject.terraformingmars.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class DryIceParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private DryIceParticle(ClientLevel level, double x, double y, double z,
                           double velocityX, double velocityY, double velocityZ,
                           SpriteSet sprites) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.sprites = sprites;
        this.gravity = 0.12F;
        this.friction = 0.98F;
        this.hasPhysics = true;
        this.lifetime = 35 + random.nextInt(30);
        this.quadSize = 0.10F + random.nextFloat() * 0.16F;
        this.alpha = 0.75F + random.nextFloat() * 0.2F;
        this.setColor(0.82F, 0.95F, 1.0F);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
        if (onGround || age > lifetime * 0.8F) {
            alpha *= 0.82F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new DryIceParticle(level, x, y, z,
                    velocityX, velocityY, velocityZ, sprites);
        }
    }
}
