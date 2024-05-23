package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import committee.nova.mods.avaritia.util.registry.RegistryHolder;
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
    public static final RegistryHolder<RecipeType<?>> RECIPES = FabricRegistry.INSTANCE.createRecipeTypeRegistryHolder();
    public static void init() {}

    public static final @NotNull Supplier<RecipeType<ISpecialRecipe>> EXTREME_CRAFT_RECIPE = recipe("extreme_craft_recipe", () -> simple(new ResourceLocation(Static.MOD_ID, "extreme_craft_recipe")));
    public static final @NotNull Supplier<RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE = recipe("compressor_recipe", () -> simple(new ResourceLocation(Static.MOD_ID, "compressor_recipe")));

    public static <T extends Recipe<Container>> Supplier<RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
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
