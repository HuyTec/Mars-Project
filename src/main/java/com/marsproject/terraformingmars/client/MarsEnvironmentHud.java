package com.marsproject.terraformingmars.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MarsEnvironmentHud implements LayeredDraw.Layer {

    public static boolean visible = true; // bat/tat bang phim X (ModKeyMappings)

    private static final String MODID = "terraforming_mars";
    private static ResourceLocation icon(String name) {
        return new ResourceLocation(MODID, "textures/gui/hud/icon_" + name + ".png");
    }

    private static final ResourceLocation ICON_RADIATION = icon("radiation");
    private static final ResourceLocation ICON_PRESSURE = icon("pressure");
    private static final ResourceLocation ICON_OXYGEN = icon("oxygen");
    private static final ResourceLocation ICON_TEMPERATURE = icon("temperature");
    private static final ResourceLocation ICON_WATER = icon("water");
    private static final ResourceLocation ICON_BIOLOGY = icon("biology");

    private static final int ICON_SIZE = 16;

    private static final int BG_COLOR = 0x88001018;
    private static final int BORDER_COLOR = 0xFF33CCFF;
    private static final int TITLE_COLOR = 0xFF66FFFF;
    private static final int LABEL_COLOR = 0xFF66CCFF;
    private static final int VALUE_COLOR = 0xFFFFFFFF;

    private static final int COLOR_SAFE = 0xFF55FF55;
    private static final int COLOR_WARN = 0xFFFFAA33;
    private static final int COLOR_CRITICAL = 0xFFFF5555;

    private enum Severity { SAFE, WARNING, CRITICAL }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!visible) return; // <-- dong bi thieu trong ban truoc, phim X gio moi thuc su co tac dung

        var data = ClientMarsEnvironmentData.get();
        if (data == null) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 3;
        int rowHeight = Math.max(lineHeight, ICON_SIZE + 2);

        // NOTE: nguong (min/max) la GIA DINH TAM - can ban tu chinh theo
        // logic that cua canLive()/MarsEnvironmentStage.
        List<StatRow> rows = List.of(
                radiationRow(data.radiation()),
                pressureRow(data.atmospherePressurePercent()),
                oxygenRow(data.oxygenPercent()),
                temperatureRow(data.temperatureCelsius()),
                waterRow(data.waterPercent()),
                biologyRow(data.biologyPercent())
        );

        int x = 10;
        int y = 10;
        int panelWidth = 190;
        int titleBlock = lineHeight + 2;
        int footerBlock = lineHeight + 10; // dong STATUS tong quat

        int panelHeight = titleBlock + rows.size() * rowHeight + footerBlock + 12;

        int left = x - 6;
        int top = y - 6;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawPanel(guiGraphics, left, top, right, bottom);

        int line = y;
        guiGraphics.drawString(font, Component.literal("MARS ENVIRONMENT"), x, line, TITLE_COLOR);
        line += titleBlock;

        int iconX = x;
        int textX = x + ICON_SIZE + 4;

        for (StatRow row : rows) {
            guiGraphics.blit(row.icon(), iconX, line, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            drawEntry(guiGraphics, font, textX, line + (ICON_SIZE - font.lineHeight) / 2,
                    row.label(), String.format("%.1f %s", row.rawValue(), row.unit()));
            line += rowHeight;
        }

        BlockPos pos = player.blockPosition();
        drawEntry(guiGraphics, font, x, line, "Position",
                pos.getX() + "  " + pos.getY() + "  " + pos.getZ());
        line += lineHeight + 2;

        boolean canLive = data.canLive();
        int statusColor = canLive ? COLOR_SAFE : COLOR_CRITICAL;
        guiGraphics.drawString(font, Component.literal(
                canLive ? "STATUS : STABLE" : "STATUS : ENVIRONMENT UNSAFE"), x, line, statusColor);

        // Canh bao chi tiet - NAM NGOAI bang, ben PHAI
        drawWarnings(guiGraphics, font, right + 10, top, rows);
    }

    private void drawWarnings(GuiGraphics g, Font font, int x, int y, List<StatRow> rows) {
        List<StatRow> warnings = new ArrayList<>();
        for (StatRow row : rows) {
            if (row.severity() != Severity.SAFE) warnings.add(row);
        }
        if (warnings.isEmpty()) return;

        int lineHeight = font.lineHeight + 3;
        int line = y;

        g.drawString(font, Component.literal("! WARNING"), x, line, COLOR_CRITICAL);
        line += lineHeight + 1;

        for (StatRow row : warnings) {
            int color = row.severity() == Severity.CRITICAL ? COLOR_CRITICAL : COLOR_WARN;
            String text = row.label().toUpperCase() + ": " + row.statusText();
            g.drawString(font, Component.literal(text), x, line, color);
            line += lineHeight;
        }
    }

    private void drawEntry(GuiGraphics g, Font font, int x, int y, String label, String value) {
        g.drawString(font, Component.literal(label + " : "), x, y, LABEL_COLOR);
        int offset = font.width(label + " : ");
        g.drawString(font, Component.literal(value), x + offset, y, VALUE_COLOR);
    }

    private void drawPanel(GuiGraphics g, int left, int top, int right, int bottom) {
        g.fill(left, top, right, bottom, BG_COLOR);

        g.fill(left, top, right, top + 1, BORDER_COLOR);
        g.fill(left, bottom - 1, right, bottom, BORDER_COLOR);
        g.fill(left, top, left + 1, bottom, BORDER_COLOR);
        g.fill(right - 1, top, right, bottom, BORDER_COLOR);

        // Goc cong nghe
        g.fill(left, top, left + 12, top + 2, BORDER_COLOR);
        g.fill(left, top, left + 2, top + 12, BORDER_COLOR);
        g.fill(right - 12, top, right, top + 2, BORDER_COLOR);
        g.fill(right - 2, top, right, top + 12, BORDER_COLOR);
        g.fill(left, bottom - 2, left + 12, bottom, BORDER_COLOR);
        g.fill(left, bottom - 12, left + 2, bottom, BORDER_COLOR);
        g.fill(right - 12, bottom - 2, right, bottom, BORDER_COLOR);
        g.fill(right - 2, bottom - 12, right, bottom, BORDER_COLOR);
    }

    // ---- Cac ham xac dinh trang thai + muc do nghiem trong tung chi so ----
    // NOTE: nguong la GIA DINH TAM, can chinh theo thiet ke that cua ban.

    private static StatRow radiationRow(double v) {
        if (v < 5) return new StatRow(ICON_RADIATION, "Radiation", v, "mSv/h", "LOW", Severity.SAFE);
        if (v < 10) return new StatRow(ICON_RADIATION, "Radiation", v, "mSv/h", "ELEVATED", Severity.WARNING);
        if (v < 15) return new StatRow(ICON_RADIATION, "Radiation", v, "mSv/h", "HIGH", Severity.WARNING);
        return new StatRow(ICON_RADIATION, "Radiation", v, "mSv/h", "CRITICAL", Severity.CRITICAL);
    }

    private static StatRow pressureRow(double v) {
        if (v < 15) return new StatRow(ICON_PRESSURE, "Pressure", v, "%", "CRITICAL", Severity.CRITICAL);
        if (v < 30) return new StatRow(ICON_PRESSURE, "Pressure", v, "%", "LOW", Severity.WARNING);
        if (v <= 70) return new StatRow(ICON_PRESSURE, "Pressure", v, "%", "NORMAL", Severity.SAFE);
        return new StatRow(ICON_PRESSURE, "Pressure", v, "%", "HIGH", Severity.WARNING);
    }

    private static StatRow oxygenRow(double v) {
        if (v < 8) return new StatRow(ICON_OXYGEN, "Oxygen", v, "%", "CRITICAL", Severity.CRITICAL);
        if (v < 16) return new StatRow(ICON_OXYGEN, "Oxygen", v, "%", "LOW", Severity.WARNING);
        if (v <= 25) return new StatRow(ICON_OXYGEN, "Oxygen", v, "%", "NORMAL", Severity.SAFE);
        return new StatRow(ICON_OXYGEN, "Oxygen", v, "%", "HIGH", Severity.SAFE);
    }

    private static StatRow temperatureRow(double v) {
        if (v < -20) return new StatRow(ICON_TEMPERATURE, "Temperature", v, "\u00b0C", "FREEZING", Severity.CRITICAL);
        if (v < 0) return new StatRow(ICON_TEMPERATURE, "Temperature", v, "\u00b0C", "COLD", Severity.WARNING);
        if (v <= 15) return new StatRow(ICON_TEMPERATURE, "Temperature", v, "\u00b0C", "NORMAL", Severity.SAFE);
        return new StatRow(ICON_TEMPERATURE, "Temperature", v, "\u00b0C", "HOT", Severity.WARNING);
    }

    // Water/Biology la chi so TIEN DO terraform, khong phai nguy hiem sinh ton
    // -> luon SAFE, khong bao gio bi liet vao danh sach WARNING ben phai.
    private static StatRow waterRow(double v) {
        String status = v < 5 ? "NONE" : v < 10 ? "TRACE" : v < 50 ? "PRESENT" : "ABUNDANT";
        return new StatRow(ICON_WATER, "Water", v, "%", status, Severity.SAFE);
    }

    private static StatRow biologyRow(double v) {
        String status = v < 5 ? "NONE" : v < 15 ? "EMERGING" : v < 40 ? "ESTABLISHED" : "THRIVING";
        return new StatRow(ICON_BIOLOGY, "Biology", v, "%", status, Severity.SAFE);
    }

    private record StatRow(ResourceLocation icon, String label, double rawValue, String unit,
                           String statusText, Severity severity) {}
}