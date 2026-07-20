package com.marsproject.terraformingmars.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

public class MarsSkyRenderer {

    private static final ResourceLocation MILKY_WAY =
            new ResourceLocation("terraforming_mars", "textures/environment/milky_way.png");

    private static final float MILKY_WAY_RADIUS = 120F;
    private static final int LAT_SEGMENTS = 32;
    private static final int LON_SEGMENTS = 64;

    private static final long STAR_SEED = 20260615L; // cố định để pattern sao không đổi mỗi lần load
    private static final int STAR_COUNT = 1200;
    private static final float STAR_RADIUS = 100F; // nhỏ hơn dải Milky Way -> sao nằm "phía trước"
    private static final float STAR_MIN_SIZE = 0.15F;
    private static final float STAR_MAX_SIZE = 0.55F;

    private static VertexBuffer milkyWayBuffer;
    private static VertexBuffer starBuffer;

    /** Gọi một lần khi client load. */
    public static void init() {
        if (milkyWayBuffer != null) return;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();

//        milkyWayBuffer = upload(buildMilkyWay(builder));
        starBuffer = upload(buildStarField(builder, RandomSource.create(STAR_SEED)));
    }

    private static VertexBuffer upload(BufferBuilder.RenderedBuffer rendered) {
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(rendered);
        VertexBuffer.unbind();
        return buffer;
    }

    // ---------- Milky Way band (giữ nguyên logic cũ) ----------

//    private static BufferBuilder.RenderedBuffer buildMilkyWay(BufferBuilder builder) {
//        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//
//        for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
//            float v0 = 1F - (float) lat / LAT_SEGMENTS;
//            float v1 = 1F - (float) (lat + 1) / LAT_SEGMENTS;
//            double theta0 = Math.PI * lat / LAT_SEGMENTS;
//            double theta1 = Math.PI * (lat + 1) / LAT_SEGMENTS;
//
//            for (int lon = 0; lon < LON_SEGMENTS; lon++) {
//                float u0 = (float) lon / LON_SEGMENTS;
//                float u1 = (float) (lon + 1) / LON_SEGMENTS;
//                double phi0 = Math.PI * 2 * lon / LON_SEGMENTS;
//                double phi1 = Math.PI * 2 * (lon + 1) / LON_SEGMENTS;
//
//                float[] a = spherePoint(MILKY_WAY_RADIUS, theta0, phi0);
//                float[] b = spherePoint(MILKY_WAY_RADIUS, theta0, phi1);
//                float[] c = spherePoint(MILKY_WAY_RADIUS, theta1, phi1);
//                float[] d = spherePoint(MILKY_WAY_RADIUS, theta1, phi0);
//
//                // Đảo thứ tự: sphere nhìn từ bên trong
//                addTex(builder, d, u0, v1);
//                addTex(builder, c, u1, v1);
//                addTex(builder, b, u1, v0);
//                addTex(builder, a, u0, v0);
//            }
//        }
//
//        return builder.end();
//    }

    private static float[] spherePoint(float radius, double theta, double phi) {
        float x = (float) (radius * Math.sin(theta) * Math.cos(phi));
        float y = (float) (radius * Math.cos(theta));
        float z = (float) (radius * Math.sin(theta) * Math.sin(phi));
        return new float[]{x, y, z};
    }

    private static void addTex(BufferBuilder builder, float[] pos, float u, float v) {
        builder.vertex(pos[0], pos[1], pos[2]).uv(u, v).endVertex();
    }

    // ---------- Star field (mới) ----------

    private static BufferBuilder.RenderedBuffer buildStarField(BufferBuilder builder, RandomSource random) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < STAR_COUNT; i++) {
            addStarQuad(builder, random);
        }

        return builder.end();
    }

    private static void addStarQuad(BufferBuilder builder, RandomSource random) {
        // 1. Điểm ngẫu nhiên phân bố đều trên mặt cầu
        double theta = Math.acos(2 * random.nextFloat() - 1);
        double phi = 2 * Math.PI * random.nextFloat();

        float px = (float) (STAR_RADIUS * Math.sin(theta) * Math.cos(phi));
        float py = (float) (STAR_RADIUS * Math.cos(theta));
        float pz = (float) (STAR_RADIUS * Math.sin(theta) * Math.sin(phi));

        float nx = px / STAR_RADIUS, ny = py / STAR_RADIUS, nz = pz / STAR_RADIUS;

        // 2. Cơ sở tiếp tuyến vuông góc với pháp tuyến (nx,ny,nz)
        float upX = 0F, upY = 1F, upZ = 0F;
        if (Math.abs(ny) > 0.99F) { // tránh song song ở 2 cực
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

        // 3. Xoay ngẫu nhiên quanh pháp tuyến để các sao không đồng dạng
        float angle = random.nextFloat() * (float) (Math.PI * 2);
        float cos = Mth.cos(angle), sin = Mth.sin(angle);
        float rx = t1x * cos + t2x * sin, ry = t1y * cos + t2y * sin, rz = t1z * cos + t2z * sin;
        float sx = -t1x * sin + t2x * cos, sy = -t1y * sin + t2y * cos, sz = -t1z * sin + t2z * cos;

        // 4. Kích thước & độ sáng ngẫu nhiên cho từng sao
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

    // ---------- Render ----------

    public static void render(ClientLevel level, float partialTick, Matrix4f modelView, Matrix4f projection, Camera camera) {
        if (milkyWayBuffer == null || starBuffer == null) return;

        float brightness = level.getStarBrightness(partialTick);
        if (brightness <= 0.01F) return;

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Dải Milky Way vẽ trước (nền), sao riêng lẻ vẽ sau (nổi lên trên)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MILKY_WAY);
        RenderSystem.setShaderColor(1F, 1F, 1F, brightness * 0.45F);
        milkyWayBuffer.drawWithShader(modelView, projection, RenderSystem.getShader());

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(brightness, brightness, brightness, brightness);
        starBuffer.drawWithShader(modelView, projection, RenderSystem.getShader());

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }
}