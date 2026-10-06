package committee.nova.mods.avaritia.api.util;

import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.core.io.StorageAccess;
import committee.nova.mods.avaritia.init.compat.trinkets.TrinketsIntegration;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class InventoryUtils {
    public static ItemStack tryInsert(ItemStack itemInv, ItemStack stack) {
        ItemHandler handler = StorageAccess.items(itemInv).orElse(null);
        return handler == null ? stack.copy() : ContainerUtils.insertItem(handler, stack, false);
    }

    public static ItemStack tryFilteredInsert(ItemStack itemInv, ItemStack stack) {
        ItemHandler handler = StorageAccess.items(itemInv).orElse(null);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).getItem() == stack.getItem()) {
                    return ContainerUtils.insertItem(handler, stack, false);
                }
            }
        }
        return stack;
    }

    public static ItemStack findFirstItem(Player player, Item consumeFrom) {
        return player.getInventory().items.stream()
                .filter(stack -> !stack.isEmpty() && stack.getItem() == consumeFrom)
                .findFirst().orElse(ItemStack.EMPTY);
    }

    public static int getFirstSlotWithStack(ItemStack itemInv, ItemStack stack) {
        ItemHandler handler = StorageAccess.items(itemInv).orElse(null);
        int slot = -1;
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).getItem() == stack.getItem()) {
                    slot = i;
                }
            }
        }
        return slot;
    }

    public static List<Integer> getAllSlotsWithStack(Player player, Predicate<ItemStack> action) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (action.test(player.getInventory().getItem(i))) {
                slots.add(i);
            }
        }
        return slots;
    }

    /** Main hand, offhand, then player inventory. */
    public static ItemStack findItemInInv(Player player, Predicate<ItemStack> predicate) {
        if (predicate.test(player.getMainHandItem())) return player.getMainHandItem();
        if (predicate.test(player.getOffhandItem())) return player.getOffhandItem();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (predicate.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack findItemInInv(Player player, Predicate<ItemStack> predicate,
                                        Function<ItemStack, ItemStack> map) {
        ItemStack stack = TrinketsIntegration.find(player, predicate);
        if (stack.isEmpty()) stack = findItemInInv(player, predicate);
        return stack.isEmpty() ? ItemStack.EMPTY : map.apply(stack);
    }
}
