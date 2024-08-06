package committee.nova.mods.avaritia.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/6 下午1:43
 * @Description:
 */
public class InventoryUtils {
    /**
     *
     * @param itemInv 有容器的物品
     * @param stack 需要存入的物品
     * @return 存入完返回的剩余物品
     */
    public static ItemStack tryInsert(ItemStack itemInv, ItemStack stack) {
        AtomicReference<ItemStack> returnStack = new AtomicReference<>(ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()));
        itemInv.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            returnStack.set(ItemHandlerHelper.insertItem(h, stack, false));
        });
        return returnStack.get();
    }

    public static ItemStack tryFilteredInsert(ItemStack itemInv, ItemStack stack) {
        if (itemInv.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent() && itemInvHasItem(itemInv, stack)) {
            return tryInsert(itemInv, stack);
        }
        return stack;
    }

    private static boolean itemInvHasItem(ItemStack itemInv, ItemStack stack) {
        AtomicBoolean hasItem = new AtomicBoolean(false);
        itemInv.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            for (int i = 0; i < h.getSlots(); i++) {
                if (h.getStackInSlot(i).getItem() == stack.getItem()) {
                    hasItem.set(true);
                }
            }
        });
        return hasItem.get();
    }

    public static int getFirstSlotWithStack(ItemStack itemInv, ItemStack stack) {
        AtomicInteger slot = new AtomicInteger(-1);
        itemInv.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            for (int i = 0; i < h.getSlots(); i++) {
                if (h.getStackInSlot(i).getItem() == stack.getItem()) {
                    slot.set(i);
                }
            }
        });
        return slot.get();
    }

    private static int getLastSlotWithStack(ItemStack itemInv, ItemStack stack) {
        AtomicInteger slot = new AtomicInteger(-1);
        itemInv.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            for (int i = h.getSlots() - 1; i >= 0; i--) {
                if (h.getStackInSlot(i).getItem() == stack.getItem()) {
                    slot.set(i);
                }
            }
        });
        return slot.get();
    }

    /**
     *
     * @param player 玩家
     * @param action 匹配的物品
     * @return 所以给定物品的slot
     */
    public static List<Integer> getAllSlotsWithStack(Player player, Predicate<ItemStack> action) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (action.test(player.getInventory().getItem(i))) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static ItemStack findInInv(Player player, Inventory playerInventory, Item item) {
        if (player.getMainHandItem().getItem() == item) {
            return player.getMainHandItem();
        }
        else if (player.getOffhandItem().getItem() == item) {
            return player.getOffhandItem();
        }
        else {
            for (int x = 0; x < playerInventory.getContainerSize(); x++) {
                ItemStack stack = playerInventory.getItem(x);
                if (stack.getItem() == item) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
