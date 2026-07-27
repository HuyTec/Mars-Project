package com.marsproject.terraformingmars.client.screen;

import com.marsproject.terraformingmars.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 185;
        inventoryLabelY = 91;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.blit(TEXTURE, left, top, 0, 0, imageWidth, imageHeight, 256, 256);

        int width = menu.getScaledProgress(24);
        int color = switch (menu.getStatus()) {
            case 1 -> 0xFF55FF55;
            case 2 -> 0xFFFF5555;
            default -> 0xFF777777;
        };
        graphics.fill(left + 76, top + 58, left + 76 + width, top + 62, color);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
