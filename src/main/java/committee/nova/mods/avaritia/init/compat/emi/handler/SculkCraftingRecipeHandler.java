package committee.nova.mods.avaritia.init.compat.emi.handler;

import committee.nova.mods.avaritia.init.compat.emi.category.tables.SculkCraftingTableCategory;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;

public class SculkCraftingRecipeHandler extends TierCraftMenuRecipeHandler {
    public SculkCraftingRecipeHandler() {
        super(SculkCraftingTableCategory.CATEGORY, ModCraftTier.SCULK);
    }
}
