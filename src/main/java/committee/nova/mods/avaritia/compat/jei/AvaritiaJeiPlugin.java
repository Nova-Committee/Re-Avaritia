package committee.nova.mods.avaritia.compat.jei;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.client.screen.ExtremeAnvilScreen;
import committee.nova.mods.avaritia.client.screen.ExtremeSmithingScreen;
import committee.nova.mods.avaritia.client.screen.NeutronCompressorScreen;
import committee.nova.mods.avaritia.client.screen.craft.EndCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.ExtremeCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.NetherCraftScreen;
import committee.nova.mods.avaritia.client.screen.craft.SculkCraftScreen;
import committee.nova.mods.avaritia.api.common.crafting.ITierCraftingRecipe;
import committee.nova.mods.avaritia.common.menu.ExtremeAnvilMenu;
import committee.nova.mods.avaritia.common.menu.ExtremeSmithingMenu;
import committee.nova.mods.avaritia.common.menu.NeutronCompressorMenu;
import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import committee.nova.mods.avaritia.compat.ClientRecipeMaps;
import committee.nova.mods.avaritia.compat.jei.category.CompressorCategory;
import committee.nova.mods.avaritia.compat.jei.category.ExtremeSmithingRecipeCategory;
import committee.nova.mods.avaritia.compat.jei.category.tables.EndCraftingTableCategory;
import committee.nova.mods.avaritia.compat.jei.category.tables.ExtremeCraftingTableCategory;
import committee.nova.mods.avaritia.compat.jei.category.tables.NetherCraftingTableCategory;
import committee.nova.mods.avaritia.compat.jei.category.tables.SculkCraftingTableCategory;
import committee.nova.mods.avaritia.compat.jei.handler.JeiContainerHandler;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JeiPlugin
public class AvaritiaJeiPlugin implements IModPlugin {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(Const.MOD_ID, "jei_plugin");

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
        RecipeMap recipeMap = ClientRecipeMaps.get();
        if (recipeMap.values().isEmpty()) {
            Const.LOGGER.warn("Avaritia JEI recipes skipped: client recipe map is empty.");
            return;
        }

        var compressorRecipes = recipeMap.byType(ModRecipeTypes.COMPRESSOR_RECIPE.get()).stream().toList();
        var extremeSmithingRecipes = recipeMap.byType(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get()).stream().toList();
        var craftingRecipes = recipeMap.byType(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get()).stream().toList();

        // 客户端配方同步失败时，JEI 分类会存在但没有内容；这里记录数量方便直接从 latest.log 判断问题。
        Const.LOGGER.info("Avaritia JEI recipes: crafting={}, compressor={}, extreme_smithing={}",
                craftingRecipes.size(), compressorRecipes.size(), extremeSmithingRecipes.size());

        registration.addRecipes(CompressorCategory.RECIPE_TYPE, compressorRecipes);
        registration.addRecipes(ExtremeSmithingRecipeCategory.RECIPE_TYPE, extremeSmithingRecipes);

        Map<Integer, List<RecipeHolder<ITierCraftingRecipe>>> recipes = Stream.of(1, 2, 3, 4).collect(Collectors.toMap(tier -> tier, tier ->
                craftingRecipes.stream()
                        .filter(recipe -> recipe.value().hasRequiredTier() ? tier == recipe.value().getTier() : tier >= recipe.value().getTier())
                        .toList()
        ));

        registration.addRecipes(SculkCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(1, List.of()));
        registration.addRecipes(NetherCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(2, List.of()));
        registration.addRecipes(EndCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(3, List.of()));
        registration.addRecipes(ExtremeCraftingTableCategory.RECIPE_TYPE, recipes.getOrDefault(4, List.of()));

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
        registration.addRecipeTransferHandler(
                ExtremeSmithingMenu.class,
                ModMenus.extreme_smithing_table.get(),
                ExtremeSmithingRecipeCategory.RECIPE_TYPE,
                ExtremeSmithingMenu.TEMPLATE_SLOT,
                ExtremeSmithingMenu.INPUT_SLOT_COUNT,
                ExtremeSmithingMenu.PLAYER_INVENTORY_SLOT_START,
                ExtremeSmithingMenu.PLAYER_INVENTORY_SLOT_COUNT
        );
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
            var singularityId = stack.get(ModDataComponents.SINGULARITY_ID.get());
            return singularityId != null ? singularityId.toString() : "";
        });
    }
}
