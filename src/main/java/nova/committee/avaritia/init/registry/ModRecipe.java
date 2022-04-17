package nova.committee.avaritia.init.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.recipe.CompressorRecipe;
import nova.committee.avaritia.common.recipe.ICompressorRecipe;
import nova.committee.avaritia.common.recipe.ICraftRecipe;
import nova.committee.avaritia.common.recipe.ShapedExtremeCraftingRecipe;

import java.util.Optional;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:19
 * Version: 1.0
 */
@Mod.EventBusSubscriber(modid = Static.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipe {
    public static RecipeSerializer<ShapedExtremeCraftingRecipe> EXTREME_CRAFT_RECIPE = new ShapedExtremeCraftingRecipe.Serializer();
    public static RecipeSerializer<CompressorRecipe> COMPRESSOR_RECIPE = new CompressorRecipe.Serializer();

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<RecipeSerializer<?>> event) {
        final IForgeRegistry<RecipeSerializer<?>> registry = event.getRegistry();

        registry.registerAll(
                EXTREME_CRAFT_RECIPE.setRegistryName(new ResourceLocation(Static.MOD_ID, "shaped_craft")),
                COMPRESSOR_RECIPE.setRegistryName(new ResourceLocation(Static.MOD_ID, "compressor"))
        );

    }

    public static class RecipeTypes {
        public static final RecipeType<ICraftRecipe> CRAFTING = new RecipeType<>() {
            @Override
            public <C extends Container> Optional<ICraftRecipe> tryMatch(Recipe<C> recipe, Level world, C inv) {
                return recipe.matches(inv, world) ? Optional.of((ICraftRecipe) recipe) : Optional.empty();
            }
        };

        public static final RecipeType<ICompressorRecipe> COMPRESSOR = new RecipeType<ICompressorRecipe>() {
            @Override
            public <C extends Container> Optional<ICompressorRecipe> tryMatch(Recipe<C> recipe, Level world, C inv) {
                return recipe.matches(inv, world) ? Optional.of((ICompressorRecipe) recipe) : Optional.empty();
            }
        };
    }
}
