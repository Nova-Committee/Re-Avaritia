package committee.nova.mods.avaritia.init.compact.rei;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import committee.nova.mods.avaritia.init.compact.rei.category.ExtremeTableCategory;
import committee.nova.mods.avaritia.init.compact.rei.category.IButtonArea;
import committee.nova.mods.avaritia.init.compact.rei.category.NeutronButtonArea;
import committee.nova.mods.avaritia.init.compact.rei.category.CompressorCategory;
import committee.nova.mods.avaritia.init.compact.rei.display.ExtremeTableShapedDisplay;
import committee.nova.mods.avaritia.init.compact.rei.display.ExtremeTableShapelessDisplay;
import committee.nova.mods.avaritia.init.compact.rei.display.CompressorDisplay;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;


public class ClientREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ExtremeTableCategory());
        registry.add(new CompressorCategory());
        registry.setPlusButtonArea(ServerREIPlugin.COMPRESSOR, NeutronButtonArea.defaultArea());
        registry.setPlusButtonArea(ServerREIPlugin.EXTREME_TABLE, IButtonArea.defaultArea());
        registry.addWorkstations(ServerREIPlugin.EXTREME_TABLE, EntryStacks.of(ModBlocks.extreme_crafting_table.get()));
        registry.addWorkstations(ServerREIPlugin.COMPRESSOR, EntryStacks.of(ModBlocks.neutron_compressor.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(ShapelessExtremeCraftingRecipe.class, ExtremeTableShapelessDisplay::new);
        registry.registerFiller(ShapedExtremeCraftingRecipe.class, ExtremeTableShapedDisplay::new);
        registry.registerFiller(CompressorRecipe.class, CompressorDisplay::new);
    }
}
