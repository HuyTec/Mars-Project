package com.marsproject.terraformingmars.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class MarsDustParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private MarsDustParticle(ClientLevel level, double x, double y, double z,
                             double velocityX, double velocityY, double velocityZ,
                             SpriteSet sprites) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.sprites = sprites;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.hasPhysics = false;
        this.lifetime = 28 + random.nextInt(28);
        this.quadSize = 0.18F + random.nextFloat() * 0.32F;
        this.alpha = 0.35F + random.nextFloat() * 0.35F;
        this.setColor(
                0.75F + random.nextFloat() * 0.2F,
                0.28F + random.nextFloat() * 0.12F,
                0.10F + random.nextFloat() * 0.06F
        );
        // Particle's vanilla constructor randomizes/scales the supplied speed.
        // Restore the server-synced wind vector so dust travels horizontally.
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        // Keep vertical drift subtle; the storm's dominant motion is horizontal.
        yd *= 0.92D;
        setSpriteFromAge(sprites);
        if (age > lifetime * 0.7F) {
            alpha *= 0.9F;
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
            return new MarsDustParticle(
                    level, x, y, z, velocityX, velocityY, velocityZ, sprites
            );
        }
    }
}
