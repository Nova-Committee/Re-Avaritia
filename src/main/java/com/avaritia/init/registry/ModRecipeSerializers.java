package com.avaritia.init.registry;

import com.avaritia.Avaritia;
import com.avaritia.common.crafting.recipe.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 注册模组中的所有配方序列化器。
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Avaritia.MOD_ID);

    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> INFINITY_CATALYST_CRAFT_SERIALIZER = serializer("infinity_catalyst", InfinityCatalystCraftRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> ETERNAL_SINGULARITY_CRAFT_SERIALIZER = serializer("eternal_singularity", EternalSingularityCraftRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPED_CRAFT_SERIALIZER = serializer("shaped_table", ShapedTableCraftingRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPELESS_CRAFT_SERIALIZER = serializer("shapeless_table", ShapelessTableCraftingRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> COMPRESSOR_SERIALIZER = serializer("compressor", CompressorRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> EXTREME_SMITHING_SERIALIZER = serializer("extreme_smithing", ExtremeSmithingRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> FULL_MATTER_CLUSTER_SERIALIZER =
            serializer("full_matter_cluster", FullMatterClusterRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> NO_CONSUME_CATALYST_SHAPED_SERIALIZER =
            serializer("no_consume_catalyst_shaped", NoConsumeCatalystShapedRecipe.Serializer::new);

    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }
}
