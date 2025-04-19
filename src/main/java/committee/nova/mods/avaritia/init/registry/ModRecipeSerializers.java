package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.condition.InfinityCatalystCondition;
import committee.nova.mods.avaritia.common.crafting.recipe.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

/**
 * Name: Avaritia-forge / ModRecipeSerializers
 * Author: cnlimiter
 * CreateTime: 2023/9/8 22:27
 * Description:
 */
@EventBusSubscriber
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Static.MOD_ID);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> INFINITY_CATALYST_CRAFT_SERIALIZER = serializer("infinity_catalyst", InfinityCatalystCraftRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> ETERNAL_SINGULARITY_CRAFT_SERIALIZER = serializer("eternal_singularity", EternalSingularityCraftRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPED_CRAFT_SERIALIZER = serializer("shaped_table", ShapedTableCraftingRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPELESS_CRAFT_SERIALIZER = serializer("shapeless_table", ShapelessTableCraftingRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> COMPRESSOR_SERIALIZER = serializer("compressor", CompressorRecipe.Serializer::new);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> EXTREME_SMITHING_SERIALIZER = serializer("extreme_smithing", ExtremeSmithingRecipe.Serializer::new);


    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(InfinityCatalystCondition.Serializer.INSTANCE);
        }
    }

}
