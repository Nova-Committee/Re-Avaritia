package com.avaritia.init.registry;

import com.avaritia.Avaritia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

/**
 * 注册模组中的所有护甲材质。
 *
 * <p>NeoForge 26.1.2 使用 1.21.5+ 的装备组件系统，护甲材质不再通过
 * {@code armor_material} 注册表注册，而是作为 {@link ArmorMaterial} 记录直接传给物品属性。</p>
 */
public class ModArmorMaterial {
    public static final TagKey<Item> REPAIRS_INFINITY_ARMOR = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(Const.MOD_ID, "repairs_infinity_armor"));
    public static final ResourceKey<EquipmentAsset> INFINITY_ARMOR_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity_armor"));

    public static final ArmorMaterial infinity_armor = new ArmorMaterial(1, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 10);
        map.put(ArmorType.LEGGINGS, 10);
        map.put(ArmorType.CHESTPLATE, 10);
        map.put(ArmorType.HELMET, 10);
    }), 20, SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0f, 1.0f, REPAIRS_INFINITY_ARMOR, INFINITY_ARMOR_ASSET);

    public static final ArmorMaterial infinity_horse_armor = new ArmorMaterial(1, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 5);
        map.put(ArmorType.LEGGINGS, 5);
        map.put(ArmorType.CHESTPLATE, 5);
        map.put(ArmorType.HELMET, 5);
        map.put(ArmorType.BODY, 50);
    }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0f, 1.0f, REPAIRS_INFINITY_ARMOR, INFINITY_ARMOR_ASSET);
}
