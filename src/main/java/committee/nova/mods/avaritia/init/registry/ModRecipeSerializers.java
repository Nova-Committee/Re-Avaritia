package committee.nova.mods.avaritia.init.registry;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.condition.InfinityCatalystCondition;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Name: Avaritia-forge / ModRecipeSerializers
 * Author: cnlimiter
 * CreateTime: 2023/9/8 22:27
 * Description:
 */
public class ModRecipeSerializers {
    private static final DeferredRegister<Codec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Static.MOD_ID);
    public static final DeferredHolder<Codec<? extends ICondition>, Codec<InfinityCatalystCondition>> INFINITY_CATALYST_CONDITION = CONDITION_CODECS.register("infinity_catalyst_recipe", () -> InfinityCatalystCondition.CODEC);


    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Static.MOD_ID);
    public static DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> INFINITY_SERIALIZER = serializer("infinity_catalyst_craft", InfinityCatalystCraftRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> SHAPED_EXTREME_CRAFT_SERIALIZER = serializer("shaped_extreme_craft", ShapedExtremeCraftingRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> SHAPELESS_EXTREME_CRAFT_SERIALIZER = serializer("shapeless_extreme_craft", ShapelessExtremeCraftingRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> COMPRESSOR_SERIALIZER = serializer("compressor", CompressorRecipe.Serializer::new);


    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }


}
