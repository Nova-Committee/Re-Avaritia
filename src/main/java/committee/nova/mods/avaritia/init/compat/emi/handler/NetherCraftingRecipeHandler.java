package committee.nova.mods.avaritia.init.compat.emi.handler;

import committee.nova.mods.avaritia.init.compat.emi.category.tables.NetherCraftingTableCategory;

public class NetherCraftingRecipeHandler extends TierCraftMenuRecipeHandler {
    public NetherCraftingRecipeHandler() {
        super(NetherCraftingTableCategory.class, 25);
    }
}