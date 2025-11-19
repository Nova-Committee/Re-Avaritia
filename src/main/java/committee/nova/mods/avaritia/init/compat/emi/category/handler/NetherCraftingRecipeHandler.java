package committee.nova.mods.avaritia.init.compat.emi.category.handler;

import committee.nova.mods.avaritia.init.compat.emi.category.tables.NetherCraftingTableCategory;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;

public class NetherCraftingRecipeHandler extends TierCraftMenuRecipeHandler {
    public NetherCraftingRecipeHandler() {
        super(NetherCraftingTableCategory.CATEGORY, ModCraftTier.NETHER);
    }
}