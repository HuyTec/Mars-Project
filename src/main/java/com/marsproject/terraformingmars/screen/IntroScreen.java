package com.marsproject.terraformingmars;

import com.marsproject.terraformingmars.network.IntroFinishedPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class IntroScreen extends Screen {

    private static final ResourceLocation PLACEHOLDER_IMAGE =
            new ResourceLocation("terraforming_mars", "textures/gui/background.png");

    private static final int DURATION_TICKS = 60; // 3 giây x 20 tick/giây

    private int ticksElapsed = 0;

    public IntroScreen() {
        super(Component.literal("Intro"));
    }

    @Override
    public void tick() {
        super.tick();
        ticksElapsed++;

        if (ticksElapsed >= DURATION_TICKS) {
            PacketDistributor.sendToServer(new IntroFinishedPayload());
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(PLACEHOLDER_IMAGE, 0, 0, this.width, this.height, 0, 0, 1920, 1080, 1920, 1080);

        float scale = 3.0f; // Chữ to gấp 3 lần mặc định, chỉnh số này để tăng/giảm

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);

        guiGraphics.drawCenteredString(this.font,
                "Coming to Mars...",
                (int) ((this.width / 2) / scale),
                (int) ((this.height - 60) / scale),
                0xFFFFFF);

        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}