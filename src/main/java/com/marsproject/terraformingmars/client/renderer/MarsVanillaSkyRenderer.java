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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

/**
 * Sky renderer cho dimension Mars.
 * Chịu trách nhiệm: sky disc (sáng/tối), star dome (ảnh panorama xoay theo thời gian),
 * mặt trời, mặt trăng, end-sky fallback.
 * Không đụng tới chunk / entity / particle / weather rendering — đó là việc của LevelRenderer gốc.
 */
public class MarsVanillaSkyRenderer {

    private static final ResourceLocation SUN_LOCATION =
            new ResourceLocation("terraforming_mars", "textures/environment/sun.png");
    private static final ResourceLocation MOON_LOCATION =
            new ResourceLocation("terraforming_mars", "textures/environment/moon_phases.png");
    private static final ResourceLocation END_SKY_LOCATION =
            new ResourceLocation("terraforming_mars", "textures/environment/end_sky.png");

    // Ảnh panorama 360° (equirectangular) dùng làm "bầu trời sao" xoay theo thời gian.
    private static final ResourceLocation STAR_DOME_LOCATION =
            new ResourceLocation("terraforming_mars", "textures/environment/milkyway.png");

    private static final float SKY_DISC_RADIUS = 512.0F;
    private static final float SUN_HALF_SIZE = 30.0F;
    private static final float MOON_HALF_SIZE = 20.0F;

    private final Minecraft minecraft;

    @Nullable
    private ClientLevel level;
    private int ticks;

    @Nullable
    private VertexBuffer skyBuffer;
    @Nullable
    private VertexBuffer darkBuffer;
    @Nullable
    private VertexBuffer starDomeBuffer;

    public MarsVanillaSkyRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.createLightSky();
        this.createDarkSky();
        this.createStarDome();
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
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
            this.skyBuffer = null;
        }
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
            this.darkBuffer = null;
        }
        if (this.starDomeBuffer != null) {
            this.starDomeBuffer.close();
            this.starDomeBuffer = null;
        }
    }

    // ---------------------------------------------------------------------
    // Sphere math
    // ---------------------------------------------------------------------

    private Vec3 spherePoint(float r, float theta, float phi) {
        // theta: kinh độ (0 -> 2PI), phi: vĩ độ (-PI/2 -> PI/2)
        float cosPhi = Mth.cos(phi);
        float x = Mth.sin(theta) * cosPhi * r;
        float z = Mth.cos(theta) * cosPhi * r;
        float y = Mth.sin(phi) * r;
        return new Vec3(x, y, z);
    }

    // ---------------------------------------------------------------------
    // Star dome (panorama 360°, xoay theo celestialPose cùng sun/moon)
    // ---------------------------------------------------------------------

    private void createStarDome() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        if (this.starDomeBuffer != null) {
            this.starDomeBuffer.close();
        }
        this.starDomeBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        final float radius = 300F;
        final int lonSegments = 128;
        final int latSegments = 64;

        for (int lon = 0; lon < lonSegments; lon++) {
            float theta0 = (float) (lon * Math.PI * 2 / lonSegments);
            float theta1 = (float) ((lon + 1) * Math.PI * 2 / lonSegments);

            float u0 = theta0 / ((float) Math.PI * 2F);
            float u1 = theta1 / ((float) Math.PI * 2F);

            for (int lat = 0; lat < latSegments; lat++) {
                float phi0 = (float) (-Math.PI / 2 + lat * Math.PI / latSegments);
                float phi1 = (float) (-Math.PI / 2 + (lat + 1) * Math.PI / latSegments);

                float v0 = (phi0 + (float) Math.PI / 2F) / (float) Math.PI;
                float v1 = (phi1 + (float) Math.PI / 2F) / (float) Math.PI;

                Vec3 p1 = spherePoint(radius, theta0, phi0);
                Vec3 p2 = spherePoint(radius, theta1, phi0);
                Vec3 p3 = spherePoint(radius, theta1, phi1);
                Vec3 p4 = spherePoint(radius, theta0, phi1);

                // Winding đảo để đúng mặt khi camera đứng trong sphere (nhìn từ trong ra).
                builder.vertex((float) p1.x, (float) p1.y, (float) p1.z).uv(u0, 1 - v0).endVertex();
                builder.vertex((float) p4.x, (float) p4.y, (float) p4.z).uv(u0, 1 - v1).endVertex();
                builder.vertex((float) p3.x, (float) p3.y, (float) p3.z).uv(u1, 1 - v1).endVertex();
                builder.vertex((float) p2.x, (float) p2.y, (float) p2.z).uv(u1, 1 - v0).endVertex();
            }
        }

        BufferBuilder.RenderedBuffer rendered = builder.end();
        this.starDomeBuffer.bind();
        this.starDomeBuffer.upload(rendered);
        VertexBuffer.unbind();
    }

    /**
     * Vẽ dome sao bằng celestialPose (cùng ma trận xoay với mặt trời/mặt trăng)
     * để bầu trời sao "quay" theo thời gian trong ngày.
     * Gọi bên trong khối đã enableBlend + disableCull.
     */
    private void renderStarDome(Matrix4f celestialPose, Matrix4f projectionMatrix, float brightness) {
        if (this.starDomeBuffer == null) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, STAR_DOME_LOCATION);
        RenderSystem.setShaderColor(brightness, brightness, brightness, brightness);

        this.starDomeBuffer.bind();
        this.starDomeBuffer.drawWithShader(celestialPose, projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();
    }

    // ---------------------------------------------------------------------
    // Sky disc (màu solid sáng/tối, giữ nguyên hành vi vanilla)
    // ---------------------------------------------------------------------

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
                    radius * Mth.cos((float) angle * ((float) Math.PI / 180F)),
                    y,
                    radius * Mth.sin((float) angle * ((float) Math.PI / 180F))
            ).endVertex();
        }
    }

    // ---------------------------------------------------------------------
    // Misc helpers
    // ---------------------------------------------------------------------

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

    // ---------------------------------------------------------------------
    // Main entry
    // ---------------------------------------------------------------------

    public void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera,
                          boolean isFoggy, Runnable skyFogSetup) {

        if (this.level == null || this.skyBuffer == null || this.darkBuffer == null) {
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

        // --- Sky disc màu solid (nền) ---
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(r, g, b, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        ShaderInstance shader = RenderSystem.getShader();
        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
        VertexBuffer.unbind();

        RenderSystem.enableBlend();

        // --- Sunrise/sunset glow ---
        float[] sunriseColor = this.level.effects().getSunriseColor(this.level.getTimeOfDay(partialTick), partialTick);
        if (sunriseColor != null) {
            this.renderSunriseGlow(poseStack, sunriseColor, partialTick);
        }

        float rainFade = 1.0F - this.level.getRainLevel(partialTick);

        // --- Khối "celestial": sun, moon, star dome — cùng xoay theo thời gian ---
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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
            FogRenderer.setupNoFog();
            RenderSystem.disableCull(); // camera đứng trong dome, cần thấy mặt trong
            float boosted = Math.min(starBrightness * 1.8F, 1.0F); // tăng độ sáng, giới hạn tối đa 1.
            this.renderStarDome(celestialPose, projectionMatrix, boosted);
            RenderSystem.enableCull();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.popPose();

        // --- Dark disc (khi mắt ở dưới đường chân trời) ---
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        double eyeHeight = this.minecraft.player.getEyePosition(partialTick).y
                - this.level.getLevelData().getHorizonHeight(this.level);
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
        bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F)
                .color(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3]).endVertex();

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