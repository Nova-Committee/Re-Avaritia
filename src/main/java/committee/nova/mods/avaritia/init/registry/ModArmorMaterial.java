package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.function.Supplier;


/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:13
 * Version: 1.0
 */
public class ModArmorMaterial {

    public static final ArmorMaterial infinite_armor =
            new ArmorMaterial(Static.MOD_ID + ":" + "infinity_armor", 15, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266655_) -> {
                p_266655_.put(ArmorItem.Type.BOOTS, 3);
                p_266655_.put(ArmorItem.Type.LEGGINGS, 6);
                p_266655_.put(ArmorItem.Type.CHESTPLATE, 8);
                p_266655_.put(ArmorItem.Type.HELMET, 3);
            }), 1000,
                    SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0f, 1.0f, () -> Ingredient.of(ModItems.infinity_ingot.get()));
    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266653_) -> {
        p_266653_.put(ArmorItem.Type.BOOTS, 13);
        p_266653_.put(ArmorItem.Type.LEGGINGS, 15);
        p_266653_.put(ArmorItem.Type.CHESTPLATE, 16);
        p_266653_.put(ArmorItem.Type.HELMET, 11);
    });

}
