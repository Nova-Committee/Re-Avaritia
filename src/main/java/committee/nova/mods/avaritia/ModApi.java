package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/1/13 20:53
 * @Description:
 */
public class ModApi {
    @ApiStatus.Experimental
    public static ShapelessTableCraftingRecipe addModShapelessRecipe(ItemStack result, List<ItemStack> ingredients, int tier) {
        List<ItemStack> arraylist = new ArrayList<>();

        for (ItemStack stack : ingredients) {
            if (stack != null) {
                arraylist.add(stack.copy());
            } else {
                throw new RuntimeException("Invalid shapeless recipes!");
            }
        }

        return new ShapelessTableCraftingRecipe(getList(arraylist), result, tier);
    }

    private static NonNullList<Ingredient> getList(List<ItemStack> arrayList) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack stack : arrayList) {
            ingredients.add(Ingredient.of(stack));
        }
        return ingredients;
    }
}
