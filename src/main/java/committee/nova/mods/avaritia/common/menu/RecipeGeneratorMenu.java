package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseTileMenu;
import committee.nova.mods.avaritia.api.common.slot.CopySlot;
import committee.nova.mods.avaritia.api.common.slot.ForbidSlot;
import committee.nova.mods.avaritia.api.common.wrapper.BaseItemWrapper;
import committee.nova.mods.avaritia.common.tile.RecipeGeneratorTile;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: cnlimiter
 */
public class RecipeGeneratorMenu extends BaseTileMenu<RecipeGeneratorTile> {
    public RecipeGeneratorMenu(int id, Inventory playerInventory, @NotNull BlockPos blockPos) {
        super(ModMenus.recipe_generator.get(), id, playerInventory, blockPos);
        // 添加9x9输入槽位 (0-80)
        for(int row = 0; row < 9; ++row) {
            for(int col = 0; col < 9; ++col) {
                // 在GUI中的位置: 8 + col*18, 18 + row*18
                this.addSlot(new Slot(getTileEntity().containers, row * 9 + col, 87 + col * 18, 18 + row * 18));
            }
        }

        // 添加输出槽位 (81)，放在GUI右侧
        this.addSlot(new Slot(getTileEntity().containers, 81, 202, 89));
    }

    public static RecipeGeneratorMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        return new RecipeGeneratorMenu(containerId, inventory, buf.readBlockPos());
    }

    // 根据等级获取可用槽位数量
    public int getAvailableSlotsForTier(int tier) {
        return switch (tier) {
            case 1 -> 9;   // 3x3
            case 2 -> 25;  // 5x5
            case 3 -> 49;  // 7x7
            case 4 -> 81;  // 9x9
            default -> 81;
        };
    }

    // 检查指定槽位是否在当前等级的可用范围内（从中心向外扩散）
    public boolean isSlotAvailableForTier(int slotIndex, int tier) {
        if (slotIndex >= 81) return slotIndex == 81; // 输出槽位总是可用

        // 获取中心向外扩散的可用槽位集合
        Set<Integer> availableSlots = getAvailableSlotsSetForTier(tier);
        return availableSlots.contains(slotIndex);
    }

    // 获取指定等级的可用槽位集合（从中心向外扩散）
    public Set<Integer> getAvailableSlotsSetForTier(int tier) {
        Set<Integer> availableSlots = new HashSet<>();

        // 根据等级确定网格大小
        int gridSize = switch (tier) {
            case 1 -> 3;   // 3x3
            case 2 -> 5;   // 5x5
            case 3 -> 7;   // 7x7
            case 4 -> 9;   // 9x9
            default -> 9;
        };

        // 计算起始位置（从中心向外扩散）
        int startRow = (9 - gridSize) / 2;
        int startCol = (9 - gridSize) / 2;

        // 添加可用槽位
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int slotIndex = (startRow + row) * 9 + (startCol + col);
                if (slotIndex < 81) {
                    availableSlots.add(slotIndex);
                }
            }
        }

        return availableSlots;
    }


    // 获取指定槽位的物品
    public ItemStack getSlotItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            return this.slots.get(slotIndex).getItem();
        }
        return ItemStack.EMPTY;
    }

}
