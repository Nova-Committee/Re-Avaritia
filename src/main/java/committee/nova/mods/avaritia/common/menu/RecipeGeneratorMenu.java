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
                this.addSlot(new Slot(getTileEntity().containers, row * 9 + col, 8 + col * 18, 18 + row * 18));
            }
        }

        // 添加输出槽位 (81)，放在GUI右侧
        this.addSlot(new Slot(getTileEntity().containers, 81, 202, 89));
    }

    public static RecipeGeneratorMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        return new RecipeGeneratorMenu(containerId, inventory, buf.readBlockPos());
    }

    // 获取指定槽位的物品
    public ItemStack getSlotItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            return this.slots.get(slotIndex).getItem();
        }
        return ItemStack.EMPTY;
    }

}
