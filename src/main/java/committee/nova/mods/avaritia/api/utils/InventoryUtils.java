package committee.nova.mods.avaritia.api.utils;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import static committee.nova.mods.avaritia.Const.curios;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/6 下午1:43
 * @Description:
 */
public class InventoryUtils {
    /**
     * @param itemInv 有容器的物品
     * @param stack   需要存入的物品
     * @return 存入完返回的剩余物品
     */
    public static ItemStack tryInsert(ItemStack itemInv, ItemStack stack) {
        AtomicReference<ItemStack> returnStack = new AtomicReference<>(stack.copyWithCount(stack.getCount()));
        Optional.ofNullable(itemInv.getCapability(Capabilities.ItemHandler.ITEM)).ifPresent(h -> {
            returnStack.set(ItemHandlerHelper.insertItem(h, stack, false));
        });
        return returnStack.get();
    }

    public static ItemStack tryFilteredInsert(ItemStack itemInv, ItemStack stack) {
        if (Optional.ofNullable(itemInv.getCapability(Capabilities.ItemHandler.ITEM)).isPresent() && itemInvHasItem(itemInv, stack)) {
            return tryInsert(itemInv, stack);
        }
        return stack;
    }

    /**
     * 判断物品容器中是否有给定物品
     * @param itemInv 有容器的物品
     * @param stack 需要查找的物品
     * @return 是否有给定物品
     */
    private static boolean itemInvHasItem(ItemStack itemInv, ItemStack stack) {
        AtomicBoolean hasItem = new AtomicBoolean(false);
        Optional.ofNullable(itemInv.getCapability(Capabilities.ItemHandler.ITEM)).ifPresent(h -> {
            for (int i = 0; i < h.getSlots(); i++) {
                if (h.getStackInSlot(i).getItem() == stack.getItem()) {
                    hasItem.set(true);
                }
            }
        });
        return hasItem.get();
    }

    public static ItemStack findFirstItem(Player player, Item consumeFrom) {
        return player.getInventory().items.stream().filter((s) -> !s.isEmpty() && s.getItem() == consumeFrom).findFirst().orElse(ItemStack.EMPTY);
    }

    /**
     * 获取第一个给定物品的slot
     * @param itemInv 有容器的物品
     * @param stack 需要查找的物品
     * @return 第一个给定物品的slot
     */
    public static int getFirstSlotWithStack(ItemStack itemInv, ItemStack stack) {
        AtomicInteger slot = new AtomicInteger(-1);
        Optional.ofNullable(itemInv.getCapability(Capabilities.ItemHandler.ITEM)).ifPresent(h -> {
            for (int i = 0; i < h.getSlots(); i++) {
                if (h.getStackInSlot(i).getItem() == stack.getItem()) {
                    slot.set(i);
                }
            }
        });
        return slot.get();
    }

    /**
     * 获取最后一个给定物品的slot
     * @param itemInv 有容器的物品
     * @param stack 需要查找的物品
     * @return 最后一个给定物品的slot
     */
    private static int getLastSlotWithStack(ItemStack itemInv, ItemStack stack) {
        AtomicInteger slot = new AtomicInteger(-1);
        Optional.ofNullable(itemInv.getCapability(Capabilities.ItemHandler.ITEM)).ifPresent(h -> {
            for (int i = h.getSlots() - 1; i >= 0; i--) {
                if (h.getStackInSlot(i).getItem() == stack.getItem()) {
                    slot.set(i);
                }
            }
        });
        return slot.get();
    }

    /**
     * @param player 玩家
     * @param action 匹配的物品
     * @return 所有给定物品的slot
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

    /**
     * 在玩家身上寻找物品并返回
     * 有优先级 主手 > 副手 > 背包
     *
     * @param player 玩家
     * @param is 匹配值
     * @return 找到的值
     */

    public static ItemStack findItemInInv(Player player, Predicate<ItemStack> is) {
        if(is.test(player.getMainHandItem())) return player.getMainHandItem();
        if(is.test(player.getOffhandItem()))return player.getOffhandItem();
        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for(int i = 0;i<size;i++) {
            ItemStack s = inv.getItem(i);
            if(is.test(s)) {
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 在玩家身上寻找物品并返回（兼容curios）
     * 有优先级 饰品栏 > 主手 > 副手 > 背包
     *
     * @param player 玩家
     * @param is 匹配值
     * @param map 操作函数
     * @return 找到的值
     */
    public static ItemStack findItemInInv(Player player, Predicate<ItemStack> is, Function<ItemStack, ItemStack> map) {
        if(curios) {
            AtomicReference<List<SlotResult>> s = new AtomicReference<>(new ArrayList<>());
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                s.set(curiosInventory.findCurios(is));
            });
            if(!s.get().isEmpty())return map.apply(s.get().get(0).stack());
        }//从饰品栏中获取
        if(is.test(player.getMainHandItem()))return map.apply(player.getMainHandItem());
        if(is.test(player.getOffhandItem()))return map.apply(player.getOffhandItem());
        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for(int i = 0;i<size;i++) {
            ItemStack s = inv.getItem(i);
            if(is.test(s)) {
                return map.apply(s);
            }
        }
        return ItemStack.EMPTY;
    }
}
