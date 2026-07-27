package com.marsproject.terraformingmars.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** Single environment HUD renderer. Dimension-specific behavior lives in {@link EnvironmentHudResolver}. */
public class MarsEnvironmentHud implements LayeredDraw.Layer {
    public static boolean visible = true;

    private static final int ICON_SIZE = 16;
    private static final int MIN_PANEL_WIDTH = 190;
    private static final int PANEL_PADDING = 6;
    private static final int TEXT_GAP = 6;

    private static final int BG_COLOR = 0x88001018;
    private static final int BORDER_COLOR = 0xFF33CCFF;
    private static final int TITLE_COLOR = 0xFF66FFFF;
    private static final int LABEL_COLOR = 0xFF66CCFF;
    private static final int VALUE_COLOR = 0xFFFFFFFF;
    private static final int COLOR_SAFE = 0xFF55FF55;
    private static final int COLOR_WARN = 0xFFFFAA33;
    private static final int COLOR_CRITICAL = 0xFFFF5555;
    private static final int COLOR_ERROR = 0xFFCC66FF;

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!visible) return;

        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;

        EnvironmentHudViewModel model = EnvironmentHudResolver.resolve(
                player.level().dimension(),
                ClientMarsEnvironmentData.get()
        );

        Font font = minecraft.font;
        int lineHeight = font.lineHeight + 3;
        int rowHeight = Math.max(lineHeight, ICON_SIZE + 2);
        int titleBlock = lineHeight + 2;
        int footerBlock = lineHeight + 10;
        int x = 10;
        int y = 10;

        Component position = positionText(player.blockPosition());
        int panelWidth = calculatePanelWidth(font, model, position);
        int panelHeight = titleBlock + model.rows().size() * rowHeight + footerBlock + 12;
        int left = x - PANEL_PADDING;
        int top = y - PANEL_PADDING;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawPanel(guiGraphics, left, top, right, bottom);

        int line = y;
        guiGraphics.drawString(font, model.title(), x, line, titleColor(model));
        line += titleBlock;

        int textX = x + ICON_SIZE + 4;
        for (EnvironmentHudViewModel.StatRow row : model.rows()) {
            guiGraphics.blit(row.icon(), x, line, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            drawEntry(guiGraphics, font, textX, line + (ICON_SIZE - font.lineHeight) / 2,
                    row.label(), row.value());
            line += rowHeight;
        }

        guiGraphics.drawString(font, position, x, line, VALUE_COLOR);
        line += lineHeight + 2;
        guiGraphics.drawString(font, model.status(), x, line, color(model.statusSeverity()));

        drawWarnings(guiGraphics, font, right + 10, top, model);
    }

    private static int calculatePanelWidth(
            Font font,
            EnvironmentHudViewModel model,
            Component position
    ) {
        int contentWidth = Math.max(font.width(model.title()), font.width(model.status()));
        contentWidth = Math.max(contentWidth, font.width(position));
        for (EnvironmentHudViewModel.StatRow row : model.rows()) {
            int rowWidth = ICON_SIZE + 4 + font.width(row.label()) + font.width(" : ")
                    + font.width(row.value());
            contentWidth = Math.max(contentWidth, rowWidth);
        }
        return Math.max(MIN_PANEL_WIDTH, contentWidth + PANEL_PADDING * 2 + TEXT_GAP);
    }

    private static Component positionText(BlockPos pos) {
        return Component.translatable(
                "hud.terraforming_mars.position",
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    private static void drawWarnings(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            EnvironmentHudViewModel model
    ) {
        if (model.warnings().isEmpty()) return;

        int lineHeight = font.lineHeight + 3;
        int line = y;
        graphics.drawString(
                font,
                Component.translatable("hud.terraforming_mars.warning.header"),
                x,
                line,
                color(model.profile() == EnvironmentHudProfile.END_ERROR
                        ? EnvironmentHudViewModel.Severity.ERROR
                        : EnvironmentHudViewModel.Severity.CRITICAL)
        );
        line += lineHeight + 1;

        for (EnvironmentHudViewModel.Warning warning : model.warnings()) {
            graphics.drawString(font, warning.text(), x, line, color(warning.severity()));
            line += lineHeight;
        }
    }

    private static void drawEntry(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            Component label,
            Component value
    ) {
        Component prefix = Component.translatable("hud.terraforming_mars.entry", label);
        graphics.drawString(font, prefix, x, y, LABEL_COLOR);
        graphics.drawString(font, value, x + font.width(prefix), y, VALUE_COLOR);
    }

    private static int titleColor(EnvironmentHudViewModel model) {
        return model.profile() == EnvironmentHudProfile.END_ERROR ? COLOR_ERROR : TITLE_COLOR;
    }

    private static int color(EnvironmentHudViewModel.Severity severity) {
        return switch (severity) {
            case SAFE -> COLOR_SAFE;
            case WARNING -> COLOR_WARN;
            case CRITICAL -> COLOR_CRITICAL;
            case ERROR -> COLOR_ERROR;
        };
    }

    private static void drawPanel(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, BG_COLOR);
        graphics.fill(left, top, right, top + 1, BORDER_COLOR);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR);
        graphics.fill(left, top, left + 12, top + 2, BORDER_COLOR);
        graphics.fill(left, top, left + 2, top + 12, BORDER_COLOR);
        graphics.fill(right - 12, top, right, top + 2, BORDER_COLOR);
        graphics.fill(right - 2, top, right, top + 12, BORDER_COLOR);
        graphics.fill(left, bottom - 2, left + 12, bottom, BORDER_COLOR);
        graphics.fill(left, bottom - 12, left + 2, bottom, BORDER_COLOR);
        graphics.fill(right - 12, bottom - 2, right, bottom, BORDER_COLOR);
        graphics.fill(right - 2, bottom - 12, right, bottom, BORDER_COLOR);
    }
}
