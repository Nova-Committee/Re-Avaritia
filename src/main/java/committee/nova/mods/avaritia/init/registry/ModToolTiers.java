package committee.nova.mods.avaritia.init.registry;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.function.Supplier;

public final class ModToolTiers {
    public static final Tier BLAZE = new NativeTier(7777, 7777, 25F, 25F, 77,
            () -> Ingredient.of(ModItems.blaze_cube.get()));
    public static final Tier CRYSTAL = new NativeTier(8888, 8888, 50F, 50F, 888,
            () -> Ingredient.of(ModItems.crystal_matrix_ingot.get()));
    public static final Tier INFINITY = new NativeTier(9999, 9999, 100F, 100F, 9999,
            () -> Ingredient.of(ModItems.infinity_ingot.get()));

    private record NativeTier(int level, int uses, float speed, float damage, int enchantment,
                              Supplier<Ingredient> repair) implements Tier {
        public int getLevel() { return level; }
        public int getUses() { return uses; }
        public float getSpeed() { return speed; }
        public float getAttackDamageBonus() { return damage; }
        public int getEnchantmentValue() { return enchantment; }
        public Ingredient getRepairIngredient() { return repair.get(); }
    }
}
