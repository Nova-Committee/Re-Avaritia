package nova.committee.avaritia.init;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.init.registry.ModItems;

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
            new SimpleArmorMaterial(Static.MOD_ID + ":" + "infinity_armor", 15, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266655_) -> {
                p_266655_.put(ArmorItem.Type.BOOTS, 3);
                p_266655_.put(ArmorItem.Type.LEGGINGS, 6);
                p_266655_.put(ArmorItem.Type.CHESTPLATE, 8);
                p_266655_.put(ArmorItem.Type.HELMET, 3);
            }), 1000,
                    SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0f, 1.0f, () -> Ingredient.of(ModItems.infinity_ingot));
    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266653_) -> {
        p_266653_.put(ArmorItem.Type.BOOTS, 13);
        p_266653_.put(ArmorItem.Type.LEGGINGS, 15);
        p_266653_.put(ArmorItem.Type.CHESTPLATE, 16);
        p_266653_.put(ArmorItem.Type.HELMET, 11);
    });

    public static class SimpleArmorMaterial implements ArmorMaterial {
        private final String name;
        private final int durabilityMultiplier;
        private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
        private final int enchantmentValue;
        private final SoundEvent sound;
        private final float toughness;
        private final float knockbackResistance;
        private final LazyLoadedValue<Ingredient> repairIngredient;

        public SimpleArmorMaterial(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> protectionFunctionForType, int enchantmentValue, SoundEvent sound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
            this.name = name;
            this.durabilityMultiplier = durabilityMultiplier;
            this.protectionFunctionForType = protectionFunctionForType;
            this.enchantmentValue = enchantmentValue;
            this.sound = sound;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
            this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
        }

        @Override
        public int getDurabilityForType(ArmorItem.Type slot) {
            return HEALTH_FUNCTION_FOR_TYPE.get(slot) * this.durabilityMultiplier;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type slot) {
            return this.protectionFunctionForType.get(slot);
        }

        @Override
        public int getEnchantmentValue() {
            return this.enchantmentValue;
        }

        @Override
        public SoundEvent getEquipSound() {
            return this.sound;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return this.repairIngredient.get();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public float getToughness() {
            return toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return knockbackResistance;
        }

    }

}
