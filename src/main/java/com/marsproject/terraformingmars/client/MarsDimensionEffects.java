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

        return new Vec3(
                baseFog.x * (0.35D + brightness * 0.65D),
                baseFog.y * (0.35D + brightness * 0.65D),
                baseFog.z * (0.35D + brightness * 0.65D)
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

        System.out.println("MarsDimensionEffects.renderSky()");
        setupFog.run();

        MarsSkyRenderer.init();
        MarsSkyRenderer.render(level, partialTick, modelViewMatrix, projectionMatrix, camera);

        return false;
    }
}