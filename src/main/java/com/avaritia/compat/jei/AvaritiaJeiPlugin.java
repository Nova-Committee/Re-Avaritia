package com.avaritia.compat.jei;

import com.avaritia.Avaritia;
import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.client.screen.ExtremeAnvilScreen;
import com.avaritia.client.screen.ExtremeSmithingScreen;
import com.avaritia.client.screen.NeutronCompressorScreen;
import com.avaritia.client.screen.craft.EndCraftScreen;
import com.avaritia.client.screen.craft.ExtremeCraftScreen;
import com.avaritia.client.screen.craft.NetherCraftScreen;
import com.avaritia.client.screen.craft.SculkCraftScreen;
import com.avaritia.common.menu.ExtremeAnvilMenu;
import com.avaritia.common.menu.ExtremeSmithingMenu;
import com.avaritia.common.menu.NeutronCompressorMenu;
import com.avaritia.common.menu.TierCraftMenu;
import com.avaritia.compat.jei.category.CompressorCategory;
import com.avaritia.compat.jei.category.ExtremeSmithingRecipeCategory;
import com.avaritia.compat.jei.category.tables.EndCraftingTableCategory;
import com.avaritia.compat.jei.category.tables.ExtremeCraftingTableCategory;
import com.avaritia.compat.jei.category.tables.NetherCraftingTableCategory;
import com.avaritia.compat.jei.category.tables.SculkCraftingTableCategory;
import com.avaritia.compat.jei.handler.JeiContainerHandler;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModMenus;
import com.avaritia.init.registry.ModRecipeTypes;
import com.avaritia.util.SingularityUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JeiPlugin
public class AvaritiaJeiPlugin implements IModPlugin {
    public static final Identifier UID = Identifier.of(Avaritia.MOD_ID, "jei_plugin");

    @Override
    public @NotNull Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CompressorCategory(helper));
        registration.addRecipeCategories(new SculkCraftingTableCategory(helper));
        registration.addRecipeCategories(new NetherCraftingTableCategory(helper));
        registration.addRecipeCategories(new EndCraftingTableCategory(helper));
        registration.addRecipeCategories(new ExtremeCraftingTableCategory(helper));
        registration.addRecipeCategories(new ExtremeSmithingRecipeCategory(helper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        var manager = level.getRecipeManager();
        registration.addRecipes(CompressorCategory.RECIPE_TYPE, manager.byType(ModRecipeTypes.COMPRESSOR_RECIPE.get()).stream().toList());
        registration.addRecipes(ExtremeSmithingRecipeCategory.RECIPE_TYPE, manager.byType(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get()).stream().toList());

        var recipes = Stream.of(1, 2, 3, 4).collect(Collectors.toMap(tier -> tier, tier ->
                manager.byType(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get())
                        .stream()
                        .filter(recipe -> recipe.value().hasRequiredTier() ? tier == recipe.value().getTier() : tier >= recipe.value().getTier())
                        .toList()
        ));

        registration.addRecipes(SculkCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(1, new ArrayList<>()));
        registration.addRecipes(NetherCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(2, new ArrayList<>()));
        registration.addRecipes(EndCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(3, new ArrayList<>()));
        registration.addRecipes(ExtremeCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(4, new ArrayList<>()));

        registration.addIngredientInfo(ModBlocks.neutron_collector.get(), Component.translatable("jei.tooltip.avaritia.neutron_collector"));
        registration.addIngredientInfo(ModItems.neutron_pile.get(), Component.translatable("jei.tooltip.avaritia.neutron_pile"));
        registration.addIngredientInfo(ModItems.crystal_pickaxe.get(), Component.translatable("jei.tooltip.avaritia.crystal_pickaxe"));
        registration.addIngredientInfo(ModItems.full_matter_cluster.get(), Component.translatable("jei.tooltip.avaritia.full_matter_cluster"));
        registration.addIngredientInfo(ModItems.refined_coal.get(), Component.translatable("jei.tooltip.avaritia.refined_coal"));
        registration.addIngredientInfo(Items.BEDROCK, Component.translatable("jei.tooltip.avaritia.bedrock"));
        registration.addIngredientInfo(Items.END_PORTAL_FRAME, Component.translatable("jei.tooltip.avaritia.end_portal_frame"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(CompressorCategory.RECIPE_TYPE, ModBlocks.neutron_compressor.get());
        registration.addCraftingStation(SculkCraftingTableCategory.RECIPE_TYPE, ModBlocks.sculk_crafting_table.get());
        registration.addCraftingStation(NetherCraftingTableCategory.RECIPE_TYPE, ModBlocks.nether_crafting_table.get());
        registration.addCraftingStation(EndCraftingTableCategory.RECIPE_TYPE, ModBlocks.end_crafting_table.get());
        registration.addCraftingStation(ExtremeCraftingTableCategory.RECIPE_TYPE, ModBlocks.extreme_crafting_table.get());
        registration.addCraftingStation(ExtremeSmithingRecipeCategory.RECIPE_TYPE, ModBlocks.extreme_smithing_table.get());
        registration.addCraftingStation(RecipeTypes.ANVIL, ModBlocks.extreme_anvil.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(NeutronCompressorMenu.class, ModMenus.neutron_compressor.get(), CompressorCategory.RECIPE_TYPE, 1, 1, 2, 36);
        registration.addRecipeTransferHandler(TierCraftMenu.class, ModMenus.sculk_crafting_tile_table.get(), SculkCraftingTableCategory.RECIPE_TYPE, 1, 9, 10, 36);
        registration.addRecipeTransferHandler(TierCraftMenu.class, ModMenus.nether_crafting_tile_table.get(), NetherCraftingTableCategory.RECIPE_TYPE, 1, 25, 26, 36);
        registration.addRecipeTransferHandler(TierCraftMenu.class, ModMenus.end_crafting_tile_table.get(), EndCraftingTableCategory.RECIPE_TYPE, 1, 49, 50, 36);
        registration.addRecipeTransferHandler(TierCraftMenu.class, ModMenus.extreme_crafting_table.get(), ExtremeCraftingTableCategory.RECIPE_TYPE, 1, 81, 82, 36);
        registration.addRecipeTransferHandler(ExtremeSmithingMenu.class, ModMenus.extreme_smithing_table.get(), ExtremeSmithingRecipeCategory.RECIPE_TYPE, 1, 5, 6, 36);
        registration.addRecipeTransferHandler(ExtremeAnvilMenu.class, ModMenus.extreme_anvil.get(), RecipeTypes.ANVIL, 0, 2, 3, 36);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(NeutronCompressorScreen.class, 84, 35, 31, 12, CompressorCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(SculkCraftScreen.class, 90, 40, 22, 12, SculkCraftingTableCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(NetherCraftScreen.class, 105, 58, 22, 12, NetherCraftingTableCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(EndCraftScreen.class, 135, 76, 22, 12, EndCraftingTableCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(ExtremeCraftScreen.class, 174, 90, 22, 12, ExtremeCraftingTableCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(ExtremeSmithingScreen.class, 86, 40, 22, 12, ExtremeSmithingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(ExtremeAnvilScreen.class, 102, 48, 22, 15, RecipeTypes.ANVIL);
        registration.addGenericGuiContainerHandler(BaseContainerScreen.class, new JeiContainerHandler());
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.singularity.get(), (stack, context) -> {
            var singularity = SingularityUtils.getSingularity(stack);
            return singularity != null ? singularity.getRegistryName().toString() : "";
        });
    }
}
