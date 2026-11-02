package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;


/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:13
 * Version: 1.0
 */
public class ModArmorMaterial {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, Const.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> infinity_armor = ARMOR_MATERIALS.register("infinity_armor", () ->
                    new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266655_) -> {
                        p_266655_.put(ArmorItem.Type.BOOTS, 10);
                        p_266655_.put(ArmorItem.Type.LEGGINGS, 10);
                        p_266655_.put(ArmorItem.Type.CHESTPLATE, 10);
                        p_266655_.put(ArmorItem.Type.HELMET, 10);
                    }), 20, SoundEvents.ARMOR_EQUIP_DIAMOND,
                            () -> Ingredient.of(ModItems.infinity_ingot.get()), List.of(new ArmorMaterial.Layer(Const.rl("infinity_armor"))),1.0f, 1.0f)
            );
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> infinity_horse_armor = ARMOR_MATERIALS.register("infinity_horse_armor", () ->
            new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266655_) -> {
                p_266655_.put(ArmorItem.Type.BOOTS, 5);
                p_266655_.put(ArmorItem.Type.LEGGINGS, 5);
                p_266655_.put(ArmorItem.Type.CHESTPLATE, 5);
                p_266655_.put(ArmorItem.Type.HELMET, 5);
                p_266655_.put(ArmorItem.Type.BODY, 50);
            }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
                    () -> Ingredient.of(ModItems.infinity_ingot.get()), List.of(new ArmorMaterial.Layer(Const.rl("infinity_armor"))),1.0f, 1.0f)
    );

    /**
     * 中子战狼铠材质：仅身体槽 50 点护甲，韧性 10。狼铠的实体贴图由
     * {@link net.minecraft.world.item.AnimalArmorItem.BodyType#CANINE} 固定解析为
     * {@code textures/entity/wolf/wolf_armor.png}，因此这里不需要额外 Layer。
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> neutron_wolf_armor = ARMOR_MATERIALS.register("neutron_wolf_armor", () ->
            new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> map.put(ArmorItem.Type.BODY, 50)),
                    10, SoundEvents.ARMOR_EQUIP_WOLF,
                    () -> Ingredient.of(ModItems.neutron_ingot.get()), List.of(), 10.0f, 0.0f)
    );

    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266653_) -> {
        p_266653_.put(ArmorItem.Type.BOOTS, 13);
        p_266653_.put(ArmorItem.Type.LEGGINGS, 15);
        p_266653_.put(ArmorItem.Type.CHESTPLATE, 16);
        p_266653_.put(ArmorItem.Type.HELMET, 11);
    });

}
