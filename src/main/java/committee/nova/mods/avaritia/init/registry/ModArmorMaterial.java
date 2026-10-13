package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;


/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/21 15:13
 * Version: 1.0
 */
public class ModArmorMaterial {

    public static final TagKey<Item> REPAIRS_INFINITY_ARMOR = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(Const.MOD_ID, "repairs_infinity_armor"));
    public static final ResourceKey<EquipmentAsset> INFINITY_ARMOR_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity_armor"));

    public static final ArmorMaterial infinity_armor = new ArmorMaterial(15, Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 3);
        map.put(ArmorType.LEGGINGS, 6);
        map.put(ArmorType.CHESTPLATE, 8);
        map.put(ArmorType.HELMET, 3);
    }), 1000, SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0f, 1.0f, REPAIRS_INFINITY_ARMOR, INFINITY_ARMOR_ASSET);

    /** @deprecated 兼容旧引用，等价于 {@link #infinity_armor} */
    @Deprecated
    public static final ArmorMaterial INFINITY_ARMOR = infinity_armor;
}
