package com.marsproject.terraformingmars.registry;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

/** Armor material shared by the four sealed space-suit pieces. */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, TerraformingMarsMod.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SPACE_SUIT =
            ARMOR_MATERIALS.register("space_suit", () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), defense -> {
                        defense.put(ArmorItem.Type.BOOTS, 2);
                        defense.put(ArmorItem.Type.LEGGINGS, 5);
                        defense.put(ArmorItem.Type.CHESTPLATE, 6);
                        defense.put(ArmorItem.Type.HELMET, 2);
                        defense.put(ArmorItem.Type.BODY, 5);
                    }),
                    10,
                    SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(Items.IRON_INGOT),
                    List.of(new ArmorMaterial.Layer(new ResourceLocation(
                            TerraformingMarsMod.MODID, "space_suit"))),
                    1.0F,
                    0.0F
            ));

    private ModArmorMaterials() {
    }
}
