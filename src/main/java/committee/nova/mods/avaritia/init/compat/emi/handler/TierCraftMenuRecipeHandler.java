package committee.nova.mods.avaritia.init.compat.emi.handler;

import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;
//Fuck you emi.--cu6
public class TierCraftMenuRecipeHandler implements EmiRecipeHandler<TierCraftMenu> {
    private final Class<? extends EmiRecipe> recipeClass;
    private final int craftingSlotCount;

    public TierCraftMenuRecipeHandler(Class<? extends EmiRecipe> recipeClass, int craftingSlotCount) {
        this.recipeClass = recipeClass;
        this.craftingSlotCount = craftingSlotCount;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<TierCraftMenu> screen) {
        TierCraftMenu menu = screen.getMenu();
        return new EmiPlayerInventory(
                menu.slots.subList(craftingSlotCount + 1, menu.slots.size()).stream()
                        .map(slot -> EmiStack.of(slot.getItem()))
                        .toList()
        );
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipeClass.isInstance(recipe);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<TierCraftMenu> context) {
        EmiPlayerInventory inventory = getInventory(context.getScreen());
        return inventory.canCraft(recipe);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<TierCraftMenu> context) {
        TierCraftMenu menu = context.getScreen().getMenu();

        List<Slot> craftingSlots = menu.slots.subList(1, craftingSlotCount + 1);

        craftingSlots.forEach(slot -> slot.set(ItemStack.EMPTY));

        List<Slot> playerSlots = menu.slots.subList(craftingSlotCount + 1, menu.slots.size());

        Map<ItemStack, Integer> playerInventoryCopy = new HashMap<>();
        for (Slot slot : playerSlots) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                playerInventoryCopy.put(stack, stack.getCount());
            }
        }

        List<EmiIngredient> inputs = recipe.getInputs();

        for (int i = 0; i < inputs.size() && i < craftingSlots.size(); i++) {
            EmiIngredient ingredient = inputs.get(i);
            if (!ingredient.isEmpty()) {

                ItemStack foundStack = findMatchingStackFromCopy(ingredient, playerInventoryCopy);
                if (!foundStack.isEmpty()) {

                    craftingSlots.get(i).set(foundStack.copy());

                    consumeItemFromCopy(foundStack, playerInventoryCopy);
                }
            }
        }

        updatePlayerSlots(playerSlots, playerInventoryCopy);

        if (!craftingSlots.isEmpty()) {
            menu.slotsChanged(craftingSlots.get(0).container);
        }
         

        menu.broadcastChanges();

        return true;
    }


    private ItemStack findMatchingStackFromCopy(EmiIngredient ingredient, Map<ItemStack, Integer> playerInventoryCopy) {
        for (Map.Entry<ItemStack, Integer> entry : playerInventoryCopy.entrySet()) {
            ItemStack stack = entry.getKey();
            int count = entry.getValue();
            if (!stack.isEmpty() && count > 0) {
                EmiStack emiStack = EmiStack.of(stack);
                if (ingredient.getEmiStacks().stream().anyMatch(ingredientStack ->
                        ingredientStack.isEqual(emiStack))) {
                    ItemStack result = stack.copy();
                    result.setCount((int) Math.min(stack.getCount(), ingredient.getAmount()));
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private void consumeItemFromCopy(ItemStack stackToConsume, Map<ItemStack, Integer> playerInventoryCopy) {
        for (Map.Entry<ItemStack, Integer> entry : playerInventoryCopy.entrySet()) {
            ItemStack slotStack = entry.getKey();
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, stackToConsume)) {
                int currentCount = entry.getValue();
                if (currentCount > 0) {
                    entry.setValue(currentCount - 1);
                    return;
                }
            }
        }
    }

    private void updatePlayerSlots(List<Slot> playerSlots, Map<ItemStack, Integer> playerInventoryCopy) {
        for (Slot slot : playerSlots) {
            ItemStack slotStack = slot.getItem();
            if (!slotStack.isEmpty()) {
                Integer newCount = playerInventoryCopy.get(slotStack);
                if (newCount != null) {
                    if (newCount <= 0) {
                        slot.set(ItemStack.EMPTY);
                    } else {
                        slotStack.setCount(newCount);
                        slot.set(slotStack);
                    }
                }
            }
        }
    }
}
