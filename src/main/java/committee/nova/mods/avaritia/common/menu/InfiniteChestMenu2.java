package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseTileMenu;
import committee.nova.mods.avaritia.common.tile.InfiniteChestBlockEntity;
import committee.nova.mods.avaritia.common.wrappers.InfiniteItemHandler;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static committee.nova.mods.avaritia.common.wrappers.InfiniteItemHandler.SLOTS_PER_PAGE;

/**
 * @author: cnlimiter
 */
public class InfiniteChestMenu2 extends BaseTileMenu<InfiniteChestBlockEntity> {

    public InfiniteChestMenu2(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenus.infinity_chest2.get(), containerId, inventory, pos);
        // 添加箱子槽位（6x9 = 54个槽位）
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new SlotItemHandler(getTileEntity().getItemHandler(), col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // 添加玩家物品栏
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 139 + row * 18));
            }
        }

        // 添加玩家快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 197));
        }
    }

    public static InfiniteChestMenu2 fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        return new InfiniteChestMenu2(containerId, inventory, buf.readBlockPos());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@Nonnull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = getSlot(index);

        if (slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();

            // 从箱子移动到玩家物品栏
            if (index < 54) {
                if (!this.moveItemStackTo(itemStack1, 54, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家物品栏移动到箱子
                if (!this.moveItemStackTo(itemStack1, 0, 54, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemStack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return getTileEntity() == null || getTileEntity().canAccess(player, true);
    }

    // 滚动控制
    public void scroll(int direction) {
        if (getTileEntity() != null) {
            InfiniteItemHandler handler = getTileEntity().getItemHandler();
            int currentPos = handler.getScrollPosition();
            int maxPos = handler.getMaxScrollPosition();
            int newPos = currentPos + direction;

            // 限制滚动范围
            newPos = Math.max(0, Math.min(newPos, maxPos));

            if (newPos != currentPos) {
                handler.setScrollPosition(newPos);
                // 强制刷新所有槽位
                for (int i = 0; i < 54; i++) {
                    slots.get(i).setChanged();
                }
                broadcastChanges();
            }
        }
    }

    // 搜索功能
    public void setSearchQuery(String query) {
        if (getTileEntity() != null) {
            getTileEntity().getItemHandler().setSearchQuery(query);
            broadcastChanges();
        }
    }

    public String getSearchQuery() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getSearchQuery() : "";
    }

    // 分类功能
    public void setSortType(InfiniteItemHandler.SortType type) {
        if (getTileEntity() != null) {
            getTileEntity().getItemHandler().setSortType(type);
            // 重置滚动位置
            getTileEntity().getItemHandler().setScrollPosition(0);
            broadcastChanges();
        }
    }

    public InfiniteItemHandler.SortType getSortType() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getSortType() : InfiniteItemHandler.SortType.NONE;
    }

    // 获取滚动信息
    public int getScrollPosition() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getScrollPosition() : 0;
    }

    public int getMaxScrollPosition() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getMaxScrollPosition() : 0;
    }

    // 获取模组分类
    public Map<String, List<ItemStack>> getItemsByMod() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getItemsByMod() : new HashMap<>();
    }
}
