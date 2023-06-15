package nova.committee.avaritia.init.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.crafting.recipe.CompressorRecipe;
import nova.committee.avaritia.common.crafting.recipe.InfinityCatalystRecipe;
import nova.committee.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import nova.committee.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import nova.committee.avaritia.util.registry.RegistryUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:19
 * Version: 1.0
 */
public class ModRecipeTypes {

    public static final RecipeSerializer<?> INFINITY_SERIALIZER = RegistryUtil.serializer("infinity_catalyst", InfinityCatalystRecipe.Serializer::new).get();
    public static final @NotNull RecipeType<Recipe<Container>> EXTREME_CRAFT_RECIPE = RegistryUtil.recipe("extreme_craft_recipe", () -> RecipeType.simple(new ResourceLocation(Static.MOD_ID, "extreme_craft_recipe"))).get();
    public static final @NotNull RecipeType<Recipe<Container>> COMPRESSOR_RECIPE = RegistryUtil.recipe("compressor_recipe", () -> RecipeType.simple(new ResourceLocation(Static.MOD_ID, "compressor_recipe"))).get();
    public static final @NotNull RecipeType<Recipe<Container>> INFINITY_CATALYST = RegistryUtil.recipe("infinity_catalyst_recipe", () -> RecipeType.simple(new ResourceLocation(Static.MOD_ID, "infinity_catalyst_recipe"))).get();
    public static RecipeSerializer<?> SHAPED_EXTREME_CRAFT_SERIALIZER = RegistryUtil.serializer("shaped_extreme_craft", ShapedExtremeCraftingRecipe.Serializer::new).get();
    public static RecipeSerializer<?> SHAPELESS_EXTREME_CRAFT_SERIALIZER = RegistryUtil.serializer("shapeless_extreme_craft", ShapelessExtremeCraftingRecipe.Serializer::new).get();
    public static RecipeSerializer<?> COMPRESSOR_SERIALIZER = RegistryUtil.serializer("compressor", CompressorRecipe.Serializer::new).get();

}
