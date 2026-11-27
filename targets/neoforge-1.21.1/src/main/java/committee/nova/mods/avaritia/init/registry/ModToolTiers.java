package committee.nova.mods.avaritia.init.registry;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 11:37
 * @Description:
 */
public class ModToolTiers {
    public static final Tier BLAZE =new SimpleTier(ModTags.NEEDS_BLAZE_TOOL,7777, 25f, 25f,  77,
            () -> Ingredient.of(ModItems.blaze_cube.get()));
    public static final Tier CRYSTAL =new SimpleTier(ModTags.NEEDS_CRYSTAL_TOOL,8888, 50f, 50f,  888,
            () -> Ingredient.of(ModItems.crystal_matrix_ingot.get()));
    public static final Tier INFINITY =new SimpleTier(ModTags.NEEDS_INFINITY_TOOL,9999, 100f, 100f,  9999,
            () -> Ingredient.of(ModItems.infinity_ingot.get()));
}
