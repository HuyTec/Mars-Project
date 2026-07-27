package com.marsproject.terraformingmars.machine;

import com.marsproject.terraformingmars.block.entity.MachineBlockEntity;
import com.marsproject.terraformingmars.registry.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MachineMenu extends AbstractContainerMenu {
    private final MachineBlockEntity machine;
    private final ContainerData data;
    private final int machineSlotCount;

    public MachineMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory,
                (MachineBlockEntity) inventory.player.level()
                        .getBlockEntity(buffer.readBlockPos()),
                new SimpleContainerData(3));
    }

    public MachineMenu(int containerId, Inventory inventory,
                       MachineBlockEntity machine, ContainerData data) {
        super(ModMenuTypes.MACHINE.get(), containerId);
        this.machine = machine;
        this.data = data;
        MachineType type = machine.getMachineType();
        machineSlotCount = type.inputSlotCount() + type.outputSlotCount();

        addMachineSlots(type);
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private void addMachineSlots(MachineType type) {
        int inputStartX = centeredStart(type.inputSlotCount());
        for (int slot = 0; slot < type.inputSlotCount(); slot++) {
            addSlot(new SlotItemHandler(machine.getItems(), slot,
                    inputStartX + slot * 18, 35));
        }

        int outputStartX = centeredStart(type.outputSlotCount());
        for (int offset = 0; offset < type.outputSlotCount(); offset++) {
            int slot = type.inputSlotCount() + offset;
            addSlot(new SlotItemHandler(machine.getItems(), slot,
                    outputStartX + offset * 18, 71) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
    }

    private static int centeredStart(int count) {
        return 88 - count * 18 / 2;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new net.minecraft.world.inventory.Slot(
                        inventory, column + row * 9 + 9,
                        8 + column * 18, 103 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new net.minecraft.world.inventory.Slot(
                    inventory, column, 8 + column * 18, 161));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        net.minecraft.world.inventory.Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < machineSlotCount) {
            if (!moveItemStackTo(stack, machineSlotCount, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0,
                machine.getMachineType().inputSlotCount(), false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return machine.getLevel() != null
                && player.canInteractWithBlock(machine.getBlockPos(), 4.0);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getProcessingTime() {
        return data.get(1);
    }

    public int getScaledProgress(int width) {
        int total = getProcessingTime();
        return total <= 0 ? 0 : Math.min(width, getProgress() * width / total);
    }

    public int getStatus() {
        return data.get(2);
    }
}
