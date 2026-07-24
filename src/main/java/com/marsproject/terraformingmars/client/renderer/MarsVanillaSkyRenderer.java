package com.marsproject.terraformingmars.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

/**
 * Sky renderer cho dimension Mars.
 * Chỉ chịu trách nhiệm: sky dome (sáng/tối), sao, mặt trời, mặt trăng, end-sky fallback.
 * Không đụng tới chunk / entity / particle / weather rendering — đó là việc của LevelRenderer gốc,
 * không phải của class này.
 */
public class MarsVanillaSkyRenderer {

    private static final ResourceLocation SUN_LOCATION = new ResourceLocation("terraforming_mars","textures/environment/sun.png");
    private static final ResourceLocation MOON_LOCATION = new ResourceLocation("terraforming_mars","textures/environment/moon_phases.png");
    private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("terraforming_mars","textures/environment/end_sky.png");

    private static final float SKY_DISC_RADIUS = 512.0F;
    private static final float STAR_DISTANCE = 100.0F;
    private static final float SUN_HALF_SIZE = 30.0F;
    private static final float MOON_HALF_SIZE = 20.0F;
    private static final int STAR_COUNT = 1500;
    private static final long STAR_SEED = 10842L;
    private static final ResourceLocation MILKY_WAY_LOCATION =
            new ResourceLocation("terraforming_mars", "textures/environment/milkyway.png");
    private final Minecraft minecraft;
    @Nullable
    private VertexBuffer milkyWayBuffer;
    @Nullable
    private ClientLevel level;
    private int ticks;

    @Nullable
    private VertexBuffer starBuffer;
    @Nullable
    private VertexBuffer skyBuffer;
    @Nullable
    private VertexBuffer darkBuffer;

    public MarsVanillaSkyRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.createStars();
        this.createLightSky();
        this.createDarkSky();
        this.createMilkyWay();
    }



    /** Gọi khi client join/leave dimension Mars để renderer biết level hiện tại. */
    public void setLevel(@Nullable ClientLevel level) {
        this.level = level;
    }

    /** Gọi mỗi client tick — cần cho animation phụ thuộc thời gian (sunrise, star brightness...). */
    public void tick() {
        this.ticks++;
    }

    /** Giải phóng GPU buffer, gọi khi renderer không còn dùng nữa. */
    public void close() {
        if (this.starBuffer != null) {
            this.starBuffer.close();
            this.starBuffer = null;
        }
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
            this.skyBuffer = null;
        }
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
            this.darkBuffer = null;
        }
    }

    private Vec3 spherePoint(float r, float angle, float height) {

        float theta = angle;

        float x = Mth.sin(theta) * r;
        float z = Mth.cos(theta) * r;

        float y = height * r;

        return new Vec3(x,y,z);
    }

    private void createMilkyWay() {

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        if (this.milkyWayBuffer != null) {
            this.milkyWayBuffer.close();
        }

        this.milkyWayBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);


        builder.begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);


        float radius = 300.0F;     // bán kính bầu trời
        int segments = 32;         // độ mượt
        float band = 0.25F;        // độ rộng dải ngân hà


        for (int i = 0; i < segments; i++) {


            float a0 = -1.2F + (i / (float)segments) * 2.4F;
            float a1 = -1.2F + ((i + 1) / (float)segments) * 2.4F;


            // vĩ độ trên cầu
            float y0 = band;
            float y1 = -band;


            Vec3 p1 = spherePoint(radius, a0, y0);
            Vec3 p2 = spherePoint(radius, a1, y0);
            Vec3 p3 = spherePoint(radius, a1, y1);
            Vec3 p4 = spherePoint(radius, a0, y1);


            float u0 = i / (float)segments;
            float u1 = (i + 1) / (float)segments;


            builder.vertex(
                            (float)p1.x,
                            (float)p1.y,
                            (float)p1.z)
                    .uv(u0,0)
                    .endVertex();


            builder.vertex(
                            (float)p2.x,
                            (float)p2.y,
                            (float)p2.z)
                    .uv(u1,0)
                    .endVertex();


            builder.vertex(
                            (float)p3.x,
                            (float)p3.y,
                            (float)p3.z)
                    .uv(u1,1)
                    .endVertex();


            builder.vertex(
                            (float)p4.x,
                            (float)p4.y,
                            (float)p4.z)
                    .uv(u0,1)
                    .endVertex();
        }


        BufferBuilder.RenderedBuffer rendered = builder.end();

        this.milkyWayBuffer.bind();
        this.milkyWayBuffer.upload(rendered);
        VertexBuffer.unbind();
    }

    private void drawMilkyWay(PoseStack poseStack, Matrix4f projectionMatrix, float brightness) {
        if (this.milkyWayBuffer == null) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MILKY_WAY_LOCATION);
        RenderSystem.setShaderColor(brightness, brightness, brightness, brightness);
        this.milkyWayBuffer.bind();
        this.milkyWayBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();
        // KHÔNG gọi enableBlend()/disableBlend() ở đây nữa — blend đã được bật từ trước (dòng RenderSystem.enableBlend() ở renderNormalSky) và sẽ được tắt ở cuối renderNormalSky
    }

    private void createStars() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }

        this.starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.RenderedBuffer renderedBuffer = this.drawStars(bufferbuilder);
        this.starBuffer.bind();
        this.starBuffer.upload(renderedBuffer);
        VertexBuffer.unbind();
    }

    private BufferBuilder.RenderedBuffer drawStars(BufferBuilder builder) {
        RandomSource random = RandomSource.create(STAR_SEED);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for (int i = 0; i < STAR_COUNT; ++i) {
            double x = (double) (random.nextFloat() * 2.0F - 1.0F);
            double y = (double) (random.nextFloat() * 2.0F - 1.0F);
            double z = (double) (random.nextFloat() * 2.0F - 1.0F);
            double size = (double) (0.15F + random.nextFloat() * 0.1F);
            double lenSq = x * x + y * y + z * z;

            if (lenSq < 1.0D && lenSq > 0.01D) {
                double invLen = 1.0D / Math.sqrt(lenSq);
                x *= invLen;
                y *= invLen;
                z *= invLen;

                double px = x * (double) STAR_DISTANCE;
                double py = y * (double) STAR_DISTANCE;
                double pz = z * (double) STAR_DISTANCE;

                double yaw = Math.atan2(x, z);
                double sinYaw = Math.sin(yaw);
                double cosYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sinPitch = Math.sin(pitch);
                double cosPitch = Math.cos(pitch);
                double spin = random.nextDouble() * Math.PI * 2.0D;
                double sinSpin = Math.sin(spin);
                double cosSpin = Math.cos(spin);

                for (int corner = 0; corner < 4; ++corner) {
                    double cx = (double) ((corner & 2) - 1) * size;
                    double cy = (double) (((corner + 1) & 2) - 1) * size;

                    double rx = cx * cosSpin - cy * sinSpin;
                    double ry = cy * cosSpin + cx * sinSpin;

                    double px1 = rx * sinPitch;
                    double py1 = -rx * cosPitch;

                    double fx = py1 * sinYaw - ry * cosYaw;
                    double fy = ry * sinYaw + py1 * cosYaw;

                    builder.vertex(px + fx, py + px1, pz + fy).endVertex();
                }
            }
        }

        return builder.end();
    }

    private void createLightSky() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }

        this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        this.buildSkyDisc(bufferbuilder, 16.0F);
        this.skyBuffer.bind();
        this.skyBuffer.upload(bufferbuilder.end());
        VertexBuffer.unbind();
    }

    private void createDarkSky() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
        }

        this.darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        this.buildSkyDisc(bufferbuilder, -16.0F);
        this.darkBuffer.bind();
        this.darkBuffer.upload(bufferbuilder.end());
        VertexBuffer.unbind();
    }

    private void buildSkyDisc(BufferBuilder builder, float y) {
        float radius = SKY_DISC_RADIUS;
        builder.vertex(0.0F, y, 0.0F).endVertex();
        for (int angle = -180; angle <= 180; angle += 45) {
            builder.vertex(
                    radius * Mth.cos((float) angle * ((float)Math.PI / 180F)),
                    y,
                    radius * Mth.sin((float) angle * ((float)Math.PI / 180F))
            ).endVertex();
        }
    }

    private boolean doesMobEffectBlockSky(Camera camera) {
        Entity entity = camera.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.hasEffect(MobEffects.BLINDNESS) || livingEntity.hasEffect(MobEffects.DARKNESS);
        }
        return false;
    }

    private void renderEndSky(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for (int i = 0; i < 6; ++i) {
            poseStack.pushPose();
            if (i == 1) poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (i == 2) poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            if (i == 3) poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            if (i == 4) poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            if (i == 5) poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));

            Matrix4f matrix4f = poseStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            tesselator.end();
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup) {

        if (this.level == null || this.starBuffer == null || this.skyBuffer == null || this.darkBuffer == null) {
            return;
        }

        skyFogSetup.run();
        if (isFoggy) {
            return;
        }

        FogType fogType = camera.getFluidInCamera();
        if (fogType == FogType.POWDER_SNOW || fogType == FogType.LAVA || this.doesMobEffectBlockSky(camera)) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(frustumMatrix);

        DimensionSpecialEffects.SkyType skyType = this.level.effects().skyType();
        if (skyType == DimensionSpecialEffects.SkyType.END) {
            this.renderEndSky(poseStack);
            return;
        }
        if (skyType != DimensionSpecialEffects.SkyType.NORMAL) {
            return;
        }

        this.renderNormalSky(poseStack, projectionMatrix, partialTick);
    }

    private void renderNormalSky(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        Vec3 skyColor = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), partialTick);
        float r = (float) skyColor.x;
        float g = (float) skyColor.y;
        float b = (float) skyColor.z;
        FogRenderer.levelFogColor();

        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(r, g, b, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        ShaderInstance shader = RenderSystem.getShader();
        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
        VertexBuffer.unbind();

        RenderSystem.enableBlend();
        float[] sunriseColor = this.level.effects().getSunriseColor(this.level.getTimeOfDay(partialTick), partialTick);
        if (sunriseColor != null) {
            this.renderSunriseGlow(poseStack, sunriseColor, partialTick);
        }

        float rainFade = 1.0F - this.level.getRainLevel(partialTick);

        float milkyWayBrightness = this.level.getStarBrightness(partialTick) * rainFade;
        if (milkyWayBrightness > 0.0F) {
            this.drawMilkyWay(poseStack, projectionMatrix, milkyWayBrightness * 0.5F);
        }

        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainFade);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(this.level.getTimeOfDay(partialTick) * 360.0F));
        Matrix4f celestialPose = poseStack.last().pose();

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        this.renderSun(bufferbuilder, celestialPose);
        this.renderMoon(bufferbuilder, celestialPose);

        float starBrightness = this.level.getStarBrightness(partialTick) * rainFade;
        if (starBrightness > 0.0F) {
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
            FogRenderer.setupNoFog();
            this.starBuffer.bind();
            this.starBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
            VertexBuffer.unbind();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.popPose();

        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        double eyeHeight = this.minecraft.player.getEyePosition(partialTick).y - this.level.getLevelData().getHorizonHeight(this.level);
        if (eyeHeight < 0.0D) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 12.0F, 0.0F);
            this.darkBuffer.bind();
            this.darkBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
            VertexBuffer.unbind();
            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private void renderSunriseGlow(PoseStack poseStack, float[] sunriseColor, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        float flip = Mth.sin(this.level.getSunAngle(partialTick)) < 0.0F ? 180.0F : 0.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(flip));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));

        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3]).endVertex();

        for (int j = 0; j <= 16; ++j) {
            float angle = (float) j * ((float) Math.PI * 2.0F) / 16.0F;
            float sin = Mth.sin(angle);
            float cos = Mth.cos(angle);
            bufferbuilder.vertex(matrix4f, sin * 120.0F, cos * 120.0F, -cos * 40.0F * sunriseColor[3])
                    .color(sunriseColor[0], sunriseColor[1], sunriseColor[2], 0.0F).endVertex();
        }

        BufferUploader.drawWithShader(bufferbuilder.end());
        poseStack.popPose();
    }

    private void renderSun(BufferBuilder bufferbuilder, Matrix4f matrix4f) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_LOCATION);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, -SUN_HALF_SIZE, 100.0F, -SUN_HALF_SIZE).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, SUN_HALF_SIZE, 100.0F, -SUN_HALF_SIZE).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, SUN_HALF_SIZE, 100.0F, SUN_HALF_SIZE).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, -SUN_HALF_SIZE, 100.0F, SUN_HALF_SIZE).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    private void renderMoon(BufferBuilder bufferbuilder, Matrix4f matrix4f) {
        // dùng chung shader "position_tex" đã set ở renderSun — chỉ đổi texture
        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        int phase = this.level.getMoonPhase();
        int col = phase % 4;
        int row = phase / 4 % 2;
        float u0 = (float) col / 4.0F;
        float v0 = (float) row / 2.0F;
        float u1 = (float) (col + 1) / 4.0F;
        float v1 = (float) (row + 1) / 2.0F;

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, -MOON_HALF_SIZE, -100.0F, MOON_HALF_SIZE).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, MOON_HALF_SIZE, -100.0F, MOON_HALF_SIZE).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix4f, MOON_HALF_SIZE, -100.0F, -MOON_HALF_SIZE).uv(u0, v0).endVertex();
        bufferbuilder.vertex(matrix4f, -MOON_HALF_SIZE, -100.0F, -MOON_HALF_SIZE).uv(u1, v0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}