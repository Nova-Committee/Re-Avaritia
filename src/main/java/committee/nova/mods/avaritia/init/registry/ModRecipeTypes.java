package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.BaseTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/4/2 11:37
 * @Description:
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Const.MOD_ID);

    public static final @NotNull DeferredHolder<RecipeType<?>, RecipeType<BaseTableCraftingRecipe>> CRAFTING_TABLE_RECIPE = recipe("crafting_table_recipe", () -> RecipeType.simple(Const.rl( "crafting_table_recipe")));
    public static final @NotNull DeferredHolder<RecipeType<?>, RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE = recipe("compressor_recipe", () -> RecipeType.simple(Const.rl( "compressor_recipe")));
    public static final @NotNull DeferredHolder<RecipeType<?>, RecipeType<ExtremeSmithingRecipe>> EXTREME_SMITHING_RECIPE = recipe("extreme_smithing_recipe", () -> RecipeType.simple(Const.rl( "extreme_smithing_recipe")));


    public static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }

}
