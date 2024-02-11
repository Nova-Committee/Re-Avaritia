package committee.nova.mods.avaritia.init.compat.jei;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.screen.CompressorScreen;
import committee.nova.mods.avaritia.client.screen.ExtremeCraftingScreen;
import committee.nova.mods.avaritia.common.menu.ExtremeCraftingMenu;
import committee.nova.mods.avaritia.init.compat.jei.category.CompressorCategory;
import committee.nova.mods.avaritia.init.compat.jei.category.ExtremeCraftingTableCategory;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.SingularityUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:09
 * Version: 1.0
 */
@JeiPlugin
public class JeiCompat implements IModPlugin {
    public static final ResourceLocation UID = new ResourceLocation(Static.MOD_ID, "jei_plugin");
    public static final ResourceLocation ICONS = new ResourceLocation(Static.MOD_ID, "textures/gui/jei/icons.png");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CompressorCategory(helper));
        registration.addRecipeCategories(new ExtremeCraftingTableCategory(helper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        var world = Minecraft.getInstance().level;
        if (world != null) {
            var manager = world.getRecipeManager();
            registration.addRecipes(CompressorCategory.RECIPE_TYPE,
                    getRecipe(manager, ModRecipeTypes.COMPRESSOR_RECIPE.get()));
            registration.addRecipes(ExtremeCraftingTableCategory.RECIPE_TYPE,
                    getRecipe(manager, ModRecipeTypes.EXTREME_CRAFT_RECIPE.get()));

            registration.addIngredientInfo(new ItemStack(ModBlocks.neutron_collector.get().asItem()), VanillaTypes.ITEM_STACK, Component.translatable("jei.tooltip.avaritia.neutron_collector"));
            registration.addIngredientInfo(new ItemStack(ModItems.neutron_pile.get()), VanillaTypes.ITEM_STACK, Component.translatable("jei.tooltip.avaritia.neutron_pile"));

        }

    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.neutron_compressor.get()), CompressorCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.extreme_crafting_table.get()), ExtremeCraftingTableCategory.RECIPE_TYPE);

    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ExtremeCraftingMenu.class, ModMenus.extreme_crafting_table.get(), ExtremeCraftingTableCategory.RECIPE_TYPE, 1, 81, 82, 36);

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CompressorScreen.class, 89, 35, 21, 16, CompressorCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(ExtremeCraftingScreen.class, 174, 90, 21, 14, ExtremeCraftingTableCategory.RECIPE_TYPE);

    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
        ModItems.singularity.asOptional().ifPresent(item -> {
            registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, (stack, context) -> {
                var singularity = SingularityUtil.getSingularity(stack);
                return singularity != null ? singularity.getId().toString() : "";
            });
        });
    }


    public <C extends Container, T extends Recipe<C>> List<T> getRecipe(RecipeManager manager, RecipeType<T> recipeType){
        List<T> list = new ArrayList<>();
        manager.getAllRecipesFor(recipeType).forEach(tRecipeHolder -> {
            list.add(tRecipeHolder.value());
        });
        return list;
    }
}
