package committee.nova.mods.avaritia.common.crafting.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** State implemented on vanilla Ingredient by the native serialization/matching hook. */
public interface NbtIngredientAccess {
    void avaritia$setNbtStack(ItemStack stack);
    void avaritia$setAlternatives(Ingredient[] alternatives);
}
