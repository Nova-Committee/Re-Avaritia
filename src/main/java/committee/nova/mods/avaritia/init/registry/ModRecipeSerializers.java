package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.common.crafting.recipe.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 注册模组中的所有配方序列化器。
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Const.MOD_ID);

    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfinityCatalystCraftRecipe>> INFINITY_CATALYST_CRAFT_SERIALIZER = serializer("infinity_catalyst", () -> InfinityCatalystCraftRecipe.Serializer.SERIALIZER);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EternalSingularityCraftRecipe>> ETERNAL_SINGULARITY_CRAFT_SERIALIZER = serializer("eternal_singularity", () -> EternalSingularityCraftRecipe.Serializer.SERIALIZER);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedTableCraftingRecipe>> SHAPED_CRAFT_SERIALIZER = serializer("shaped_table", () -> ShapedTableCraftingRecipe.Serializer.SERIALIZER);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapelessTableCraftingRecipe>> SHAPELESS_CRAFT_SERIALIZER = serializer("shapeless_table", () -> ShapelessTableCraftingRecipe.Serializer.SERIALIZER);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressorRecipe>> COMPRESSOR_SERIALIZER = serializer("compressor", () -> CompressorRecipe.Serializer.SERIALIZER);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ExtremeSmithingRecipe>> EXTREME_SMITHING_SERIALIZER = serializer("extreme_smithing", () -> ExtremeSmithingRecipe.SERIALIZER);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FullMatterClusterRecipe>> FULL_MATTER_CLUSTER_SERIALIZER =
            serializer("full_matter_cluster", () -> FullMatterClusterRecipe.Serializer.SERIALIZER);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NoConsumeCatalystShapedRecipe>> NO_CONSUME_CATALYST_SHAPED_SERIALIZER =
            serializer("no_consume_catalyst_shaped", () -> NoConsumeCatalystShapedRecipe.Serializer.SERIALIZER);

    public static <T extends Recipe<?>> DeferredHolder<RecipeSerializer<?>, RecipeSerializer<T>> serializer(String name, Supplier<RecipeSerializer<T>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }
}
