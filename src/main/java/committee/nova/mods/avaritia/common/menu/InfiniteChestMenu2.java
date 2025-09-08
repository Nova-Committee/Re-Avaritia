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

import javax.annotation.Nonnull;

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
        // 这里应该从 buf 中读取方块实体位置，但为了简化，我们假设已经传递了正确的方块实体
        return new InfiniteChestMenu2(containerId, inventory, buf.readBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
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

    // 分页控制方法
    public void nextPage() {
        if (getTileEntity() != null) {
            int currentPage = getTileEntity().getItemHandler().getCurrentPage();
            getTileEntity().getItemHandler().setCurrentPage(currentPage + 1);
            for (int i = 0; i < SLOTS_PER_PAGE; i++) {
                slots.get(i).setChanged();
            }
            broadcastChanges();
        }
    }

    public void previousPage() {
        if (getTileEntity() != null) {
            int currentPage = getTileEntity().getItemHandler().getCurrentPage();
            if (currentPage > 0) {
                getTileEntity().getItemHandler().setCurrentPage(currentPage - 1);
                for (int i = 0; i < SLOTS_PER_PAGE; i++) {
                    slots.get(i).setChanged();
                }
                broadcastChanges();
            }
        }
    }

    public int getCurrentPage() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getCurrentPage() : 0;
    }

    public int getTotalPages() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getTotalPages() : 0;
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
            broadcastChanges();
        }
    }

    public InfiniteItemHandler.SortType getSortType() {
        return getTileEntity() != null ? getTileEntity().getItemHandler().getSortType() : InfiniteItemHandler.SortType.NONE;
    }

    // 自动整理
    public void toggleAutoOrganize() {
        if (getTileEntity() != null) {
            boolean current = getTileEntity().getItemHandler().isAutoOrganize();
            getTileEntity().getItemHandler().setAutoOrganize(!current);
            broadcastChanges();
        }
    }

    public boolean isAutoOrganize() {
        return getTileEntity() != null && getTileEntity().getItemHandler().isAutoOrganize();
    }

    // 手动整理
    public void organizeItems() {
        if (getTileEntity() != null) {
            getTileEntity().getItemHandler().organizeItems();
            broadcastChanges();
        }
    }
}
