package com.marsproject.terraformingmars.client.renderer;

import com.marsproject.terraformingmars.client.ClientMarsEnvironmentData;
import com.marsproject.terraformingmars.network.MarsEnvironmentSyncPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

public class MarsSkyRenderer {

    private static final int LAT_SEGMENTS = 32;
    private static final int LON_SEGMENTS = 64;

    private static final long STAR_SEED = 20260615L;
    private static final int STAR_COUNT = 1200;
    private static final float STAR_RADIUS = 100F;
    private static final float STAR_MIN_SIZE = 0.15F;
    private static final float STAR_MAX_SIZE = 0.55F;

    private static VertexBuffer starBuffer;
    private static VertexBuffer skyDomeBuffer;
    private static final float SKY_DOME_RADIUS = 140F;

    private static long lastDebug = 0;

    public static void init() {
        if (skyDomeBuffer != null) return;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        skyDomeBuffer = upload(buildSkyDome(builder));
        starBuffer = upload(buildStarField(builder, RandomSource.create(STAR_SEED)));
    }

    private static VertexBuffer upload(BufferBuilder.RenderedBuffer rendered) {
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(rendered);
        VertexBuffer.unbind();
        return buffer;
    }

    private static BufferBuilder.RenderedBuffer buildSkyDome(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
            double theta0 = Math.PI * lat / LAT_SEGMENTS;
            double theta1 = Math.PI * (lat + 1) / LAT_SEGMENTS;

            for (int lon = 0; lon < LON_SEGMENTS; lon++) {
                double phi0 = Math.PI * 2 * lon / LON_SEGMENTS;
                double phi1 = Math.PI * 2 * (lon + 1) / LON_SEGMENTS;

                float[] a = spherePoint(SKY_DOME_RADIUS, theta0, phi0);
                float[] b = spherePoint(SKY_DOME_RADIUS, theta0, phi1);
                float[] c = spherePoint(SKY_DOME_RADIUS, theta1, phi1);
                float[] d = spherePoint(SKY_DOME_RADIUS, theta1, phi0);

                addPos(builder, d);
                addPos(builder, c);
                addPos(builder, b);
                addPos(builder, a);
            }
        }

        return builder.end();
    }

    private static void addPos(BufferBuilder builder, float[] pos) {
        builder.vertex(pos[0], pos[1], pos[2]).endVertex();
    }

    private static float[] computeSkyColor(float timeOfDay, float progress) {
        float angle = Mth.cos(timeOfDay * ((float) Math.PI * 2F));
        float dayFactor = Mth.clamp(angle * 1.5F, -1F, 1F) * 0.5F + 0.5F;

        float[] dayColor    = {0.40F, 0.62F, 0.92F};
        float[] nightColor  = {0.03F, 0.04F, 0.09F};
        float[] sunsetColor = {0.85F, 0.45F, 0.20F};
        float[] dustColor   = {0.55F, 0.40F, 0.30F};

        float threshold = 0.4F;
        float sunsetWeight = 0F;
        if (angle >= -threshold && angle <= threshold) {
            float t = (angle + threshold) / (threshold * 2F);
            sunsetWeight = Mth.sin(t * (float) Math.PI);
        }

        float[] base = lerpColor(nightColor, dayColor, dayFactor);
        float[] withSunset = lerpColor(base, sunsetColor, sunsetWeight * 0.7F);

        return lerpColor(dustColor, withSunset, progress);
    }

    private static float[] lerpColor(float[] from, float[] to, float t) {
        return new float[]{
                Mth.lerp(t, from[0], to[0]),
                Mth.lerp(t, from[1], to[1]),
                Mth.lerp(t, from[2], to[2])
        };
    }

    private static float[] spherePoint(float radius, double theta, double phi) {
        float x = (float) (radius * Math.sin(theta) * Math.cos(phi));
        float y = (float) (radius * Math.cos(theta));
        float z = (float) (radius * Math.sin(theta) * Math.sin(phi));
        return new float[]{x, y, z};
    }

    private static BufferBuilder.RenderedBuffer buildStarField(BufferBuilder builder, RandomSource random) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < STAR_COUNT; i++) {
            addStarQuad(builder, random);
        }

        return builder.end();
    }

    private static void addStarQuad(BufferBuilder builder, RandomSource random) {
        double theta = Math.acos(2 * random.nextFloat() - 1);
        double phi = 2 * Math.PI * random.nextFloat();

        float px = (float) (STAR_RADIUS * Math.sin(theta) * Math.cos(phi));
        float py = (float) (STAR_RADIUS * Math.cos(theta));
        float pz = (float) (STAR_RADIUS * Math.sin(theta) * Math.sin(phi));

        float nx = px / STAR_RADIUS, ny = py / STAR_RADIUS, nz = pz / STAR_RADIUS;

        float upX = 0F, upY = 1F, upZ = 0F;
        if (Math.abs(ny) > 0.99F) {
            upX = 1F; upY = 0F;
        }

        float t1x = ny * upZ - nz * upY;
        float t1y = nz * upX - nx * upZ;
        float t1z = nx * upY - ny * upX;
        float t1len = Mth.sqrt(t1x * t1x + t1y * t1y + t1z * t1z);
        t1x /= t1len; t1y /= t1len; t1z /= t1len;

        float t2x = ny * t1z - nz * t1y;
        float t2y = nz * t1x - nx * t1z;
        float t2z = nx * t1y - ny * t1x;

        float angle = random.nextFloat() * (float) (Math.PI * 2);
        float cos = Mth.cos(angle), sin = Mth.sin(angle);
        float rx = t1x * cos + t2x * sin, ry = t1y * cos + t2y * sin, rz = t1z * cos + t2z * sin;
        float sx = -t1x * sin + t2x * cos, sy = -t1y * sin + t2y * cos, sz = -t1z * sin + t2z * cos;

        float size = STAR_MIN_SIZE + random.nextFloat() * (STAR_MAX_SIZE - STAR_MIN_SIZE);
        float brightness = 0.4F + random.nextFloat() * 0.6F;

        addColor(builder, px - rx * size - sx * size, py - ry * size - sy * size, pz - rz * size - sz * size, brightness);
        addColor(builder, px + rx * size - sx * size, py + ry * size - sy * size, pz + rz * size - sz * size, brightness);
        addColor(builder, px + rx * size + sx * size, py + ry * size + sy * size, pz + rz * size + sz * size, brightness);
        addColor(builder, px - rx * size + sx * size, py - ry * size + sy * size, pz - rz * size + sz * size, brightness);
    }

    private static void addColor(BufferBuilder builder, float x, float y, float z, float brightness) {
        builder.vertex(x, y, z).color(brightness, brightness, brightness, 1F).endVertex();
    }

    public static void render(ClientLevel level, float partialTick, Matrix4f modelView, Matrix4f projection, Camera camera) {
        if (skyDomeBuffer == null || starBuffer == null) return;

        float timeOfDay = level.getTimeOfDay(partialTick);
        MarsEnvironmentSyncPayload data = ClientMarsEnvironmentData.get();

        long now = System.currentTimeMillis();

        if (now - lastDebug > 1000) {
            lastDebug = now;

            System.out.println("========== MARS SKY ==========");
            System.out.println("Time      : " + level.getTimeOfDay(partialTick));
            System.out.println("Star      : " + level.getStarBrightness(partialTick));
            System.out.println("Camera    : " + camera.getPosition());

            if (data == null) {
                System.out.println("Payload   : NULL");
            } else {
                System.out.println("Progress  : " + data.terraformProgress());
            }

            System.out.println("SkyBuffer : " + (skyDomeBuffer != null));
            System.out.println("StarBuf   : " + (starBuffer != null));
        }

        float progress = 0F;

        if (data != null) {
            progress = Mth.clamp((float) data.terraformProgress(), 0F, 1F);
        }

        float[] skyColor = computeSkyColor(timeOfDay, progress);

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(skyColor[0], skyColor[1], skyColor[2], 1F);
        skyDomeBuffer.drawWithShader(modelView, projection, RenderSystem.getShader());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float starBrightness = level.getStarBrightness(partialTick);
        if (starBrightness > 0.01F) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
            starBuffer.drawWithShader(modelView, projection, RenderSystem.getShader());
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }
}