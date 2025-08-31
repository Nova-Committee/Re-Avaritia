package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.condition.InfinityCatalystCondition;
import committee.nova.mods.avaritia.common.crafting.recipe.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Name: Avaritia-forge / ModRecipeSerializers
 * Author: cnlimiter
 * CreateTime: 2023/9/8 22:27
 * Description:
 */
@Mod.EventBusSubscriber
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Const.MOD_ID);
    public static RegistryObject<RecipeSerializer<?>> INFINITY_CATALYST_CRAFT_SERIALIZER = serializer("infinity_catalyst", InfinityCatalystCraftRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> ETERNAL_SINGULARITY_CRAFT_SERIALIZER = serializer("eternal_singularity", EternalSingularityCraftRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> SHAPED_CRAFT_SERIALIZER = serializer("shaped_table", ShapedTableCraftingRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> SHAPELESS_CRAFT_SERIALIZER = serializer("shapeless_table", ShapelessTableCraftingRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> COMPRESSOR_SERIALIZER = serializer("compressor", CompressorRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> EXTREME_SMITHING_SERIALIZER = serializer("extreme_smithing", ExtremeSmithingRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> FULL_MATTER_CLUSTER_SERIALIZER =
            serializer("full_matter_cluster", FullMatterClusterRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> NO_CONSUME_CATALYST_SHAPED_SERIALIZER =
            serializer("no_consume_catalyst_shaped", NoConsumeCatalystShapedRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(InfinityCatalystCondition.Serializer.INSTANCE);
        }
    }

}
