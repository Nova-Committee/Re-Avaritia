package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:19
 * Version: 1.0
 */
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_TYPE, Static.MOD_ID);

    public static final @NotNull DeferredHolder<RecipeType<?>, RecipeType<ISpecialRecipe>> EXTREME_CRAFT_RECIPE = RECIPES.register("extreme_craft_recipe", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return "extreme_craft_recipe";
        }
    });
    public static final @NotNull DeferredHolder<RecipeType<?>, RecipeType<ICompressorRecipe>> COMPRESSOR_RECIPE = RECIPES.register("compressor_recipe", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return "compressor_recipe";
        }
    });


}
