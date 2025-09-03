package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:33
 * Version: 1.0
 */
public class ModToolTiers {
    public static final Tier BLAZE = TierSortingRegistry.registerTier(new ForgeTier(7777, 7777, 25f, 25F, 77, ModTags.NEEDS_BLAZE_TOOL,
            () -> Ingredient.of(ModItems.blaze_cube.get())), Const.rl("blaze"), List.of(Tiers.DIAMOND), List.of());
    public static final Tier CRYSTAL = TierSortingRegistry.registerTier(new ForgeTier(8888, 8888, 50f, 50F, 888, ModTags.NEEDS_CRYSTAL_TOOL,
            () -> Ingredient.of(ModItems.crystal_matrix_ingot.get())), Const.rl("crystal"), List.of(Tiers.NETHERITE, BLAZE), List.of());
    public static final Tier INFINITY = TierSortingRegistry.registerTier(new ForgeTier(9999, 9999, 100f, 100F, 9999, ModTags.NEEDS_INFINITY_TOOL,
            () -> Ingredient.of(ModItems.infinity_ingot.get())), Const.rl("infinity"), List.of(ModToolTiers.CRYSTAL), List.of());
}
