package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

/**
 * Name: Avaritia-forge / ModRecipeSerializers
 * Author: cnlimiter
 * CreateTime: 2023/9/8 22:27
 * Description:
 */
public class ModRecipeSerializers {
    public static final LazyRegistrar<RecipeSerializer<?>> SERIALIZERS = LazyRegistrar.create(BuiltInRegistries.RECIPE_SERIALIZER, Static.MOD_ID);
    public static RegistryObject<RecipeSerializer<?>> INFINITY_SERIALIZER = serializer("infinity_catalyst_craft", InfinityCatalystCraftRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> SHAPED_EXTREME_CRAFT_SERIALIZER = serializer("shaped_extreme_craft", ShapedExtremeCraftingRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> SHAPELESS_EXTREME_CRAFT_SERIALIZER = serializer("shapeless_extreme_craft", ShapelessExtremeCraftingRecipe.Serializer::new);
    public static RegistryObject<RecipeSerializer<?>> COMPRESSOR_SERIALIZER = serializer("compressor", CompressorRecipe.Serializer::new);

    public static void init() {
        Static.LOGGER.info("Registering Mod Recipe Serializers...");
        SERIALIZERS.register();
    }

    public static RegistryObject<RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }

//    @SubscribeEvent
//    public static void registerRecipeSerializers(RegisterEvent event){
//        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)){
//            ConditionalRecipe.register(InfinityCatalystCondition.Serializer.INSTANCE);
//        }
//    }

}
