package committee.nova.mods.avaritia.init.registry;

import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:33
 * Version: 1.0
 */
public enum ModTiers implements net.minecraft.world.item.Tier {
    CRYSTAL_PICKAXE(18, 4888, 4888f, 6.0F, 0, () -> {
        return Ingredient.of(ModItems.crystal_matrix_ingot.get());
    }),
    SKULL_SWORD(18, 4888, 4888f, 10F, 0, () -> {
        return Ingredient.of(ModItems.infinity_ingot.get());
    }),
    INFINITY_PICKAXE(32, 9999, 9999f, 8.0F, 0, () -> {
        return Ingredient.of(ModItems.infinity_ingot.get());
    }),
    INFINITY_SWORD(32, 9999, 9999f, 10F, 0, () -> {
        return Ingredient.of(ModItems.infinity_ingot.get());
    }),
    INFINITY_SHOVEL(32, 9999, 9999f, 8.0F, 0, () -> {
        return Ingredient.of(ModItems.infinity_ingot.get());
    }),
    INFINITY_HOE(32, 9999, 9999f, 8.0F, 0, () -> {
        return Ingredient.of(ModItems.infinity_ingot.get());
    }),
    INFINITY_AXE(32, 9999, 9999f, 8.0F, 0, () -> {
        return Ingredient.of(ModItems.infinity_ingot.get());
    });


    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    private ModTiers(int pLevel, int pUses, float pSpeed, float pDamage, int pEnchantmentValue, Supplier<Ingredient> pRepairIngredient) {
        this.level = pLevel;
        this.uses = pUses;
        this.speed = pSpeed;
        this.damage = pDamage;
        this.enchantmentValue = pEnchantmentValue;
        this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
    }

    public int getUses() {
        return this.uses;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getAttackDamageBonus() {
        return this.damage;
    }

    public int getLevel() {
        return this.level;
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
