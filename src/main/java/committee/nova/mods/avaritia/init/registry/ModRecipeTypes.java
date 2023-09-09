package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.recipe.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:19
 * Version: 1.0
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Static.MOD_ID);

    public static final @NotNull RegistryObject<RecipeType<ICraftRecipe>> EXTREME_CRAFT_RECIPE = recipe("extreme_craft_recipe", () -> RecipeType.simple(new ResourceLocation(Static.MOD_ID, "extreme_craft_recipe")));
    //public static final @NotNull RegistryObject<RecipeType<ICraftRecipe>> INFINITY_CATALYST_RECIPE = recipe("infinity_catalyst_recipe", () -> RecipeType.simple(new ResourceLocation(Static.MOD_ID, "infinity_catalyst_recipe")));
    public static final @NotNull RegistryObject<RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE = recipe("compressor_recipe", () -> RecipeType.simple(new ResourceLocation(Static.MOD_ID, "compressor_recipe")));




    public static <T extends Recipe<Container>> RegistryObject<RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }

}
