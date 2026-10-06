package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ITierCraftingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 9:19
 * Version: 1.0
 */
public class ModRecipeTypes {
    public static final RegistryEntries<RecipeType<?>> RECIPES = RegistryEntries.create(BuiltInRegistries.RECIPE_TYPE, Const.MOD_ID);

    public static final @NotNull RegistryEntry<RecipeType<ITierCraftingRecipe>> CRAFTING_TABLE_RECIPE = recipe("crafting_table_recipe", () -> named("crafting_table_recipe"));
    public static final @NotNull RegistryEntry<RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE = recipe("compressor_recipe", () -> named("compressor_recipe"));
    public static final @NotNull RegistryEntry<RecipeType<ExtremeSmithingRecipe>> EXTREME_SMITHING_RECIPE = recipe("extreme_smithing_recipe", () -> named("extreme_smithing_recipe"));


    public static <T extends Recipe<Container>> RegistryEntry<RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }
    private static <T extends Recipe<?>> RecipeType<T> named(String path) {
        return new RecipeType<>() {
            @Override public String toString() { return Const.MOD_ID + ":" + path; }
        };
    }

}
