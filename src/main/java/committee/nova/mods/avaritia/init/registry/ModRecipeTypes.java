package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public static final LazyRegistrar<RecipeType<?>> RECIPES = LazyRegistrar.create(BuiltInRegistries.RECIPE_TYPE, Static.MOD_ID);
    public static void init() {
        Static.LOGGER.info("Registering Mod Recipe Types...");
        RECIPES.register();
    }

    public static final @NotNull RegistryObject<RecipeType<ISpecialRecipe>> EXTREME_CRAFT_RECIPE = recipe("extreme_craft_recipe", () -> simple(new ResourceLocation(Static.MOD_ID, "extreme_craft_recipe")));
    public static final @NotNull RegistryObject<RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE = recipe("compressor_recipe", () -> simple(new ResourceLocation(Static.MOD_ID, "compressor_recipe")));

    public static <T extends Recipe<Container>> RegistryObject<RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }


    public static <T extends Recipe<?>> RecipeType<T> simple(ResourceLocation name) {
        final String toString = name.toString();
        return new RecipeType<T>() {
            public String toString() {
                return toString;
            }
        };
    }
}
