package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import committee.nova.mods.avaritia.util.registry.RegistryHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

/**
 * Name: Avaritia-forge / ModRecipeSerializers
 * Author: cnlimiter
 * CreateTime: 2023/9/8 22:27
 * Description:
 */
public class ModRecipeSerializers {
    public static final RegistryHolder<RecipeSerializer<?>> SERIALIZERS = FabricRegistry.INSTANCE.createRecipeSerializerRegistryHolder();
    public static Supplier<RecipeSerializer<?>> INFINITY_SERIALIZER = serializer("infinity_catalyst_craft", InfinityCatalystCraftRecipe.Serializer::new);
    public static Supplier<RecipeSerializer<?>> SHAPED_EXTREME_CRAFT_SERIALIZER = serializer("shaped_extreme_craft", ShapedExtremeCraftingRecipe.Serializer::new);
    public static Supplier<RecipeSerializer<?>> SHAPELESS_EXTREME_CRAFT_SERIALIZER = serializer("shapeless_extreme_craft", ShapelessExtremeCraftingRecipe.Serializer::new);
    public static Supplier<RecipeSerializer<?>> COMPRESSOR_SERIALIZER = serializer("compressor", CompressorRecipe.Serializer::new);

    public static void init() {}

    public static Supplier<RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }

//    @SubscribeEvent
//    public static void registerRecipeSerializers(RegisterEvent event){
//        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)){
//            ConditionalRecipe.register(InfinityCatalystCondition.Serializer.INSTANCE);
//        }
//    }

}
