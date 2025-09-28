package committee.nova.mods.avaritia.init.compat.emi.handler;

import committee.nova.mods.avaritia.init.compat.emi.category.tables.EndCraftingTableCategory;

public class EndCraftingRecipeHandler extends TierCraftMenuRecipeHandler {
    public EndCraftingRecipeHandler() {
        super(EndCraftingTableCategory.class, 49);
    }
}
