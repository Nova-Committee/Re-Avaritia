package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

import java.util.function.Supplier;

/**
 * Name: Avaritia-forge / ModRecipeSerializers
 * @author cnlimiter
 * CreateTime: 2023/9/8 22:27
 * Description:
 */
public class ModRecipeSerializers {
    public static final RegistryEntries<RecipeSerializer<?>> SERIALIZERS = RegistryEntries.create(BuiltInRegistries.RECIPE_SERIALIZER, Const.MOD_ID);
    public static RegistryEntry<RecipeSerializer<?>> INFINITY_CATALYST_CRAFT_SERIALIZER = serializer("infinity_catalyst", InfinityCatalystCraftRecipe.Serializer::new);
    public static RegistryEntry<RecipeSerializer<?>> ETERNAL_SINGULARITY_CRAFT_SERIALIZER = serializer("eternal_singularity", EternalSingularityCraftRecipe.Serializer::new);
    public static RegistryEntry<RecipeSerializer<?>> SHAPED_CRAFT_SERIALIZER = serializer("shaped_table", ShapedTableCraftingRecipe.Serializer::new);
    public static RegistryEntry<RecipeSerializer<?>> SHAPELESS_CRAFT_SERIALIZER = serializer("shapeless_table", ShapelessTableCraftingRecipe.Serializer::new);
    public static RegistryEntry<RecipeSerializer<?>> COMPRESSOR_SERIALIZER = serializer("compressor", CompressorRecipe.Serializer::new);
    public static RegistryEntry<RecipeSerializer<?>> EXTREME_SMITHING_SERIALIZER = serializer("extreme_smithing", ExtremeSmithingRecipe.Serializer::new);
    public static RegistryEntry<RecipeSerializer<?>> FULL_MATTER_CLUSTER_SERIALIZER =
            serializer("full_matter_cluster", FullMatterClusterRecipe.Serializer::new);
    public static RegistryEntry<RecipeSerializer<?>> NO_CONSUME_CATALYST_SHAPED_SERIALIZER =
            serializer("no_consume_catalyst_shaped", NoConsumeCatalystShapedRecipe.Serializer::new);

    public static RegistryEntry<RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }


}
