package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.client.renderer.MarsSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;

public class MarsDimensionEffects extends DimensionSpecialEffects {

    private final float[] colorData = new float[4];

    // Mau suong mu: do dam dac (barren) -> xanh xam nhat hon khi khi quyen day len
    private static final Vec3 FOG_BARREN = new Vec3(0.78D, 0.48D, 0.32D);
    private static final Vec3 FOG_TERRAFORMED = new Vec3(0.55D, 0.62D, 0.72D);
    private static final double fogStrength = 0.1D;

    public MarsDimensionEffects() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    /** Tra ve progress 0.0 - 1.0, mac dinh 0 (barren) neu chua co du lieu tu server. */
    private static float getProgress01() {
        var data = ClientMarsEnvironmentData.get();
        if (data == null) return 0F;
        return Mth.clamp((float) (data.terraformProgress()), 0F, 1F);
    }

    @Override
    public float getCloudHeight() {
        return Float.NaN;
    }

    @Nullable
    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTick) {
        float threshold = 0.4F;
        float angle = Mth.cos(timeOfDay * ((float) Math.PI * 2F));

        if (angle >= -threshold && angle <= threshold) {
            float t = (angle + threshold) / (threshold * 2F);
            float alpha = Mth.sin(t * (float) Math.PI);
            alpha *= alpha;

            colorData[0] = 0.30F;
            colorData[1] = 0.55F;
            colorData[2] = 0.90F;
            colorData[3] = alpha * 0.85F;

            return colorData;
        }

        return null;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        // Sương bụi đỏ của Sao Hỏa
        return new Vec3(
                0.78D * (0.35D + brightness * 0.65D),
                0.48D * (0.35D + brightness * 0.65D),
                0.32D * (0.35D + brightness * 0.65D)
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
        setupFog.run();

        MarsSkyRenderer.init();
        MarsSkyRenderer.render(level, partialTick, modelViewMatrix, projectionMatrix, camera);

        return false;
    }
}