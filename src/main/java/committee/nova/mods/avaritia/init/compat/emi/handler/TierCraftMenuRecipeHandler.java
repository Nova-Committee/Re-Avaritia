package committee.nova.mods.avaritia.init.compat.emi.handler;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
//Fuck you emi.--cu6, cnlimiter
public class TierCraftMenuRecipeHandler implements StandardRecipeHandler<TierCraftMenu> {
    private final Class<? extends EmiRecipe> recipeClass;
    private final ModCraftTier tier;

    public TierCraftMenuRecipeHandler(Class<? extends EmiRecipe> recipeClass, ModCraftTier tier) {
        this.recipeClass = recipeClass;
        this.tier = tier;
    }


    @Override
    public List<Slot> getInputSources(TierCraftMenu handler) {
        List<Slot> list = Lists.newArrayList();
        for (int i = 1; i < 10; i++) {
            list.add(handler.getSlot(i));
        }
        int invStart = 10;
        for (int i = invStart; i < invStart + 36; i++) {
            list.add(handler.getSlot(i));
        }
        return list;
    }

    @Override
    public List<Slot> getCraftingSlots(TierCraftMenu handler) {
        List<Slot> list = Lists.newArrayList();
        for (int i = 1; i < tier.size * tier.size; i++) {
            list.add(handler.getSlot(i));
        }
        return list;
    }

    @Override
    public @Nullable Slot getOutputSlot(TierCraftMenu handler) {
        return handler.getSlot(tier.size * tier.size);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipeClass.isInstance(recipe);
    }
}
