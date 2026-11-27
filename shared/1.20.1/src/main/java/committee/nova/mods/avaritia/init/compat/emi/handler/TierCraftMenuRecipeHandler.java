package committee.nova.mods.avaritia.init.compat.emi.handler;

import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
//Fuck you emi.--cu6, cnlimiter
public class TierCraftMenuRecipeHandler implements StandardRecipeHandler<TierCraftMenu> {
    private final EmiRecipeCategory emiRecipeCategory;
    private final ModCraftTier tier;

    public TierCraftMenuRecipeHandler(EmiRecipeCategory emiRecipeCategory, ModCraftTier tier) {
        this.emiRecipeCategory = emiRecipeCategory;
        this.tier = tier;
    }


    @Override
    public List<Slot> getInputSources(TierCraftMenu handler) {
        return handler.slots.subList(handler.slots.size() - 36, handler.slots.size());
    }

    @Override
    public List<Slot> getCraftingSlots(TierCraftMenu handler) {
        return handler.slots.subList(1, tier.size * tier.size + 1);
    }

    @Override
    public @Nullable Slot getOutputSlot(TierCraftMenu handler) {
        return handler.getSlot(0);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == emiRecipeCategory && recipe.supportsRecipeTree();
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<TierCraftMenu> context) {
        List<ItemStack> stacks = EmiRecipeFiller.getStacks(this, recipe, context.getScreen(), context.getAmount());
        if (stacks != null) {
            Minecraft.getInstance().setScreen(context.getScreen());
            if (!EmiClient.onServer) {
                return EmiRecipeFiller.clientFill(this, recipe, context.getScreen(), stacks, context.getDestination());
            } else {
                EmiClient.sendFillRecipe(this, context.getScreen(), context.getScreenHandler().containerId, switch(context.getDestination()) {
                    case NONE -> 0;
                    case CURSOR -> 1;
                    case INVENTORY -> 2;
                }, stacks, recipe);
            }
            return true;
        }
        return false;
    }
}
