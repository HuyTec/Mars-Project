package com.marsproject.terraformingmars.client;

import com.marsproject.terraformingmars.survival.PlayerSurvivalData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;

/** Unified bar-style HUD for the player's core survival statistics. */
public final class SurvivalBarsHud implements LayeredDraw.Layer {
    private static final int WIDTH = 81;
    private static final int HEIGHT = 9;
    private static final int ROW_GAP = 2;
    private static final int BACKGROUND = 0xB0000000;
    private static final int BORDER = 0xCCFFFFFF;
    private static final int HEALTH_COLOR = 0xFFE04444;
    private static final int HUNGER_COLOR = 0xFFE6A23C;
    private static final int AIR_COLOR = 0xFF66D9FF;
    private static final int THIRST_COLOR = 0xFF3F9FFF;
    private static final int NORMAL_TEMPERATURE_COLOR = 0xFF55DD66;
    private static final int COLD_COLOR = 0xFF55AAFF;
    private static final int HOT_COLOR = 0xFFFF7043;
    private static final int SUIT_OXYGEN_COLOR = 0xFF7DE7FF;
    private static final int ARMOR_COLOR = 0xFFB8C5D6;

    @Override
    public void render(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }

        int center = graphics.guiWidth() / 2;
        int bottomY = graphics.guiHeight() - 39;
        int leftX = center - 91;
        int rightX = center + 10;
        int rowStep = HEIGHT + ROW_GAP;

        float maxHealth = Math.max(1.0F, player.getMaxHealth());
        float health = Mth.clamp(player.getHealth(), 0.0F, maxHealth);
        drawBar(graphics, leftX, bottomY, health / maxHealth, HEALTH_COLOR,
                Component.translatable("hud.terraforming_mars.health",
                        Math.round(health), Math.round(maxHealth)));

        int food = Mth.clamp(player.getFoodData().getFoodLevel(), 0, 20);
        drawBar(graphics, rightX, bottomY, food / 20.0F, HUNGER_COLOR,
                Component.translatable("hud.terraforming_mars.hunger", food));

        int maxAir = Math.max(1, player.getMaxAirSupply());
        int air = Mth.clamp(player.getAirSupply(), 0, maxAir);
        drawBar(graphics, rightX, bottomY - rowStep, air / (float) maxAir, AIR_COLOR,
                Component.translatable("hud.terraforming_mars.air",
                        Math.round(air * 100.0F / maxAir)));

        float thirstRatio = ClientSurvivalData.thirst() / PlayerSurvivalData.MAX_THIRST;
        drawBar(graphics, rightX, bottomY - rowStep * 2, thirstRatio, THIRST_COLOR,
                Component.translatable("hud.terraforming_mars.thirst",
                        Math.round(thirstRatio * 100.0F)));

        double temperature = ClientSurvivalData.bodyTemperature();
        float temperatureRatio = (float) Mth.clamp((temperature - 25.0) / 20.0, 0.0, 1.0);
        int temperatureColor = temperature < 30.0
                ? COLD_COLOR
                : temperature > 39.5 ? HOT_COLOR : NORMAL_TEMPERATURE_COLOR;
        drawBar(graphics, rightX, bottomY - rowStep * 3, temperatureRatio, temperatureColor,
                Component.translatable("hud.terraforming_mars.body_temperature",
                        String.format(Locale.ROOT, "%.1f", temperature)));

        int armor = Mth.clamp(player.getArmorValue(), 0, 20);
        drawBar(graphics, leftX, bottomY - rowStep, armor / 20.0F, ARMOR_COLOR,
                Component.translatable("hud.terraforming_mars.armor", armor));

        if (ClientSurvivalData.suitOxygenCapacity() > 0) {
            float suitRatio = ClientSurvivalData.suitOxygen()
                    / (float) ClientSurvivalData.suitOxygenCapacity();
            drawBar(graphics, leftX, bottomY - rowStep * 2, suitRatio, SUIT_OXYGEN_COLOR,
                    Component.translatable("hud.terraforming_mars.suit_oxygen",
                            Math.round(suitRatio * 100.0F),
                            ClientSurvivalData.suitSealed()
                                    ? Component.translatable("hud.terraforming_mars.suit_sealed")
                                    : Component.translatable("hud.terraforming_mars.suit_unsealed")));
        }
    }

    private static void drawBar(
            GuiGraphics graphics,
            int x,
            int y,
            float ratio,
            int fillColor,
            Component label
    ) {
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, BORDER);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, BACKGROUND);
        int fillWidth = Math.round((WIDTH - 2) * Mth.clamp(ratio, 0.0F, 1.0F));
        if (fillWidth > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + HEIGHT - 1, fillColor);
        }

        var font = Minecraft.getInstance().font;
        int textX = x + (WIDTH - font.width(label)) / 2;
        graphics.drawString(font, label, textX, y, 0xFFFFFFFF, true);
    }
}
