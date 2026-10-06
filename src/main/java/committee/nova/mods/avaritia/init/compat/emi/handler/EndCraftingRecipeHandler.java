package committee.nova.mods.avaritia.init.compat.emi.handler;

import committee.nova.mods.avaritia.init.compat.emi.category.tables.EndCraftingTableCategory;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;

public class EndCraftingRecipeHandler extends TierCraftMenuRecipeHandler {
    public EndCraftingRecipeHandler() {
        super(EndCraftingTableCategory.CATEGORY, ModCraftTier.END);
    }
}
