package committee.nova.mods.avaritia.init.compat.emi.category.handler;


import committee.nova.mods.avaritia.init.compat.emi.category.tables.ExtremeCraftingTableCategory;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;

public class ExtremeCraftingRecipeHandler extends TierCraftMenuRecipeHandler {
    public ExtremeCraftingRecipeHandler() {
        super(ExtremeCraftingTableCategory.CATEGORY, ModCraftTier.EXTREME);
    }
}