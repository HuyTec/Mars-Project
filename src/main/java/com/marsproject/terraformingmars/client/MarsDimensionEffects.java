package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.client.renderer.MarsSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.marsproject.terraformingmars.registry.ModParticles;
import com.marsproject.terraformingmars.weather.MarsWeatherBiomes;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;

public class MarsDimensionEffects extends DimensionSpecialEffects {
    private final MarsSkyRenderer marsSkyRenderer = new MarsSkyRenderer(Minecraft.getInstance());
    private final float[] colorData = new float[4];
    // Mau suong mu: do dam dac (barren) -> xanh xam nhat hon khi khi quyen day len
    private static final Vec3 FOG_BARREN = new Vec3(0.78D, 0.48D, 0.32D);
    private static final Vec3 FOG_TERRAFORMED = new Vec3(0.55D, 0.62D, 0.72D);

    public MarsDimensionEffects() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    /** Tra ve progress 0.0 - 1.0, mac dinh 0 (barren) neu chua co du lieu tu server. */
    private static float getProgress01() {
        var data = ClientMarsEnvironmentData.get();
        if (data == null) return 0F;
        return Mth.clamp((float) (data.terraformProgress()), 0F, 1F);
    }

    @Nullable
    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTick) {
        System.out.println("SUNRISE CALLED");
        float threshold = 0.4F;
        float angle = Mth.cos(timeOfDay * ((float) Math.PI * 2F));

        if (angle >= -threshold && angle <= threshold) {
            float t = (angle + threshold) / (threshold * 2F);
            float alpha = Mth.sin(t * (float) Math.PI);
            alpha *= alpha;

            float progress = getProgress01();
            // Khi quyen cang day (progress cao) -> hoang hon ro/dam net hon
            float alphaScale = Mth.lerp(progress, 0.4F, 0.85F);

            colorData[0] = 0.30F;
            colorData[1] = 0.55F;
            colorData[2] = 0.90F;
            colorData[3] = alpha * alphaScale;

            return colorData;
        }

        return null;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        float progress = getProgress01();
        Vec3 baseFog = FOG_BARREN.lerp(FOG_TERRAFORMED, progress);

        // Hạ sàn từ 0.35 xuống gần 0 -> ban đêm (brightness thấp) fog gần như đen/trong suốt hòa vào bầu trời đêm
        float factor = 0.05F + brightness * 0.95F;

        return new Vec3(
                baseFog.x * factor,
                baseFog.y * factor,
                baseFog.z * factor
        );
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick,
                             Matrix4f modelViewMatrix, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy,
                             Runnable setupFog) {

        marsSkyRenderer.setLevel(level);
        marsSkyRenderer.tick();
        marsSkyRenderer.renderSky(modelViewMatrix, projectionMatrix, partialTick, camera, isFoggy, setupFog);

        return true;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick,
                                     LightTexture lightTexture, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        ClientMarsWeatherData.tick();
        Vec3 cameraPosition = camera.getPosition();

        if (ClientMarsWeatherData.isDryIceStorm()) {
            if (MarsWeatherBiomes.isCryotic(level, BlockPos.containing(cameraPosition))) {
                spawnDryIceParticles(level, cameraPosition, ClientMarsWeatherData.intensity());
            }
            return true;
        }
        if (!ClientMarsWeatherData.isDustStorm()) {
            return true;
        }

        int intensity = ClientMarsWeatherData.intensity();
        int particlesPerTick = switch (intensity) {
            case 1 -> 3;
            case 2 -> 7;
            default -> 12;
        };
        double windSpeed = switch (intensity) {
            case 1 -> 0.26D;
            case 2 -> 0.38D;
            default -> 0.52D;
        };

        for (int i = 0; i < particlesPerTick; i++) {
            double x = cameraPosition.x + (level.random.nextDouble() - 0.5D) * 36.0D;
            double y = cameraPosition.y + (level.random.nextDouble() - 0.5D) * 14.0D;
            double z = cameraPosition.z + (level.random.nextDouble() - 0.5D) * 36.0D;
            double velocityX = ClientMarsWeatherData.windX() * windSpeed
                    + (level.random.nextDouble() - 0.5D) * 0.04D;
            double velocityY = (level.random.nextDouble() - 0.5D) * 0.008D;
            double velocityZ = ClientMarsWeatherData.windZ() * windSpeed
                    + (level.random.nextDouble() - 0.5D) * 0.04D;

            level.addParticle(ModParticles.MARS_DUST.get(), x, y, z,
                    velocityX, velocityY, velocityZ);
        }
        return true;
    }

    private static void spawnDryIceParticles(ClientLevel level, Vec3 cameraPosition, int intensity) {
        int particlesPerTick = switch (intensity) {
            case 1 -> 4;
            case 2 -> 8;
            default -> 14;
        };

        for (int i = 0; i < particlesPerTick; i++) {
            double x = cameraPosition.x + (level.random.nextDouble() - 0.5D) * 30.0D;
            double y = cameraPosition.y + 7.0D + level.random.nextDouble() * 13.0D;
            double z = cameraPosition.z + (level.random.nextDouble() - 0.5D) * 30.0D;
            double velocityX = (level.random.nextDouble() - 0.5D) * 0.035D;
            double velocityY = -0.10D - intensity * 0.035D;
            double velocityZ = (level.random.nextDouble() - 0.5D) * 0.035D;

            level.addParticle(ModParticles.DRY_ICE_CRYSTAL.get(), x, y, z,
                    velocityX, velocityY, velocityZ);
        }
    }
}
