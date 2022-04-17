package nova.committee.avaritia.common.item.tools;

import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.crafting.Ingredient;
import nova.committee.avaritia.init.registry.ModItems;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:33
 * Version: 1.0
 */
public enum Tier implements net.minecraft.world.item.Tier {
    INFINITY_PICKAXE(32, 9999, 9999f, 6.0F, 200, () -> {
        return Ingredient.of(ModItems.infinity_ingot);
    }),
    INFINITY_SWORD(32, 9999, 9999f, -3.0F, 200, () -> {
        return Ingredient.of(ModItems.infinity_ingot);
    }),
    SKULL_SWORD(18, 4888, 4888f, -1.0F, 200, () -> {
        return Ingredient.of(ModItems.infinity_ingot);
    });;


    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    private Tier(int pLevel, int pUses, float pSpeed, float pDamage, int pEnchantmentValue, Supplier<Ingredient> pRepairIngredient) {
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
