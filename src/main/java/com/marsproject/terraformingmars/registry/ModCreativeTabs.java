package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Creative inventory tab that lists every Mars block/item added by this mod. */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TerraformingMarsMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MARS_TAB =
            CREATIVE_MODE_TABS.register("mars_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TerraformingMarsMod.MODID + ".mars_tab"))
                    .icon(() -> new ItemStack(ModBlocks.IRONSTONE.get()))
                    .displayItems((parameters, output) -> {
                        // Tự động thêm TẤT CẢ block đã đăng ký trong ModBlocks
                        ModBlocks.BLOCKS.getEntries().forEach(entry -> output.accept(entry.get()));
                    })
                    .build());

    public static void register() { }
}