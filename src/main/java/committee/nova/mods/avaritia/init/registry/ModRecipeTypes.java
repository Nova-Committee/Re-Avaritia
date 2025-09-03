package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ITierCraftingRecipe;
import dev.architectury.registry.registries.DeferredRegister;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:19
 * Version: 1.0
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Const.MOD_ID);

    public static final @NotNull RegistryObject<RecipeType<ITierCraftingRecipe>> CRAFTING_TABLE_RECIPE = recipe("crafting_table_recipe", () -> RecipeType.simple(new ResourceLocation(Const.MOD_ID, "crafting_table_recipe")));
    public static final @NotNull RegistryObject<RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE = recipe("compressor_recipe", () -> RecipeType.simple(new ResourceLocation(Const.MOD_ID, "compressor_recipe")));
    public static final @NotNull RegistryObject<RecipeType<ExtremeSmithingRecipe>> EXTREME_SMITHING_RECIPE = recipe("extreme_smithing_recipe", () -> RecipeType.simple(new ResourceLocation(Const.MOD_ID, "extreme_smithing_recipe")));


    public static <T extends Recipe<Container>> RegistryObject<RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }

}
