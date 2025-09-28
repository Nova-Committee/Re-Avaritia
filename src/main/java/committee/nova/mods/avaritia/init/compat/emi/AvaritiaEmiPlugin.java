package committee.nova.mods.avaritia.init.compat.emi;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ITierCraftingRecipe;
import committee.nova.mods.avaritia.init.compat.emi.category.CompressorCategory;
import committee.nova.mods.avaritia.init.compat.emi.category.ExtremeSmithingRecipeCategory;
import committee.nova.mods.avaritia.init.compat.emi.category.tables.EndCraftingTableCategory;
import committee.nova.mods.avaritia.init.compat.emi.category.tables.ExtremeCraftingTableCategory;
import committee.nova.mods.avaritia.init.compat.emi.category.tables.NetherCraftingTableCategory;
import committee.nova.mods.avaritia.init.compat.emi.category.tables.SculkCraftingTableCategory;
import committee.nova.mods.avaritia.init.compat.emi.handler.EndCraftingRecipeHandler;
import committee.nova.mods.avaritia.init.compat.emi.handler.ExtremeCraftingRecipeHandler;
import committee.nova.mods.avaritia.init.compat.emi.handler.NetherCraftingRecipeHandler;
import committee.nova.mods.avaritia.init.compat.emi.handler.SculkCraftingRecipeHandler;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.SingularityUtils;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

@EmiEntrypoint
public class AvaritiaEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.setDefaultComparison(ModItems.singularity.get(), Comparison.of((stack1, stack2) -> SingularityUtils.getSingularity(stack1.getItemStack()) == SingularityUtils.getSingularity(stack2.getItemStack())));

        registry.addWorkstation(VanillaEmiRecipeCategories.ANVIL_REPAIRING, EmiStack.of(ModBlocks.extreme_anvil.get()));

        registry.addCategory(CompressorCategory.CATEGORY);
        registry.addWorkstation(CompressorCategory.CATEGORY, CompressorCategory.WORKSTATION);
        for (ICompressorRecipe recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.COMPRESSOR_RECIPE.get()))
            registry.addRecipe(new CompressorCategory(recipe));

        registry.addCategory(ExtremeSmithingRecipeCategory.CATEGORY);
        registry.addWorkstation(ExtremeSmithingRecipeCategory.CATEGORY, ExtremeSmithingRecipeCategory.WORKSTATION);
        for (ExtremeSmithingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get()))
            registry.addRecipe(new ExtremeSmithingRecipeCategory(recipe));

        registry.addCategory(SculkCraftingTableCategory.CATEGORY);
        registry.addWorkstation(SculkCraftingTableCategory.CATEGORY, SculkCraftingTableCategory.WORKSTATION);
        registry.addCategory(NetherCraftingTableCategory.CATEGORY);
        registry.addWorkstation(NetherCraftingTableCategory.CATEGORY, NetherCraftingTableCategory.WORKSTATION);
        registry.addCategory(EndCraftingTableCategory.CATEGORY);
        registry.addWorkstation(EndCraftingTableCategory.CATEGORY, EndCraftingTableCategory.WORKSTATION);
        registry.addCategory(ExtremeCraftingTableCategory.CATEGORY);
        registry.addWorkstation(ExtremeCraftingTableCategory.CATEGORY, ExtremeCraftingTableCategory.WORKSTATION);
        for (ITierCraftingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get()))
            registry.addRecipe(switch (recipe.getTier()) {
                case 1 -> new SculkCraftingTableCategory(recipe);
                case 2 -> new NetherCraftingTableCategory(recipe);
                case 3 -> new EndCraftingTableCategory(recipe);
                case 4 -> new ExtremeCraftingTableCategory(recipe);
                default -> throw new UnsupportedOperationException("Unsupported tier " + recipe.getTier());
            });

        registry.addRecipe(new EmiInfoRecipe(List.of(EmiIngredient.of(Ingredient.of(ModBlocks.neutron_collector.get()))), List.of(Component.translatable("emi.tooltip.avaritia.neutron_collector")), ResourceLocation.tryBuild(Const.MOD_ID, "/info_collector")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiIngredient.of(Ingredient.of(ModItems.neutron_pile.get()))), List.of(Component.translatable("emi.tooltip.avaritia.neutron_pile")), ResourceLocation.tryBuild(Const.MOD_ID, "/info_pile")));
        registry.addRecipeHandler(ModMenus.sculk_crafting_tile_table.get(), new SculkCraftingRecipeHandler());
        registry.addRecipeHandler(ModMenus.nether_crafting_tile_table.get(), new NetherCraftingRecipeHandler());
        registry.addRecipeHandler(ModMenus.end_crafting_tile_table.get(), new EndCraftingRecipeHandler());
        registry.addRecipeHandler(ModMenus.extreme_crafting_table.get(), new ExtremeCraftingRecipeHandler());

    }
}
