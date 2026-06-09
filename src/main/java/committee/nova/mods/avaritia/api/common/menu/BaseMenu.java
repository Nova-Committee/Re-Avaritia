package committee.nova.mods.avaritia.api.common.menu;

import committee.nova.mods.avaritia.api.iface.IDataReceiver;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/1/6 13:07
 * @Description:
 */
public abstract class BaseMenu extends AbstractContainerMenu implements IDataReceiver {

    public final Level level;
    public final Player player;

    protected BaseMenu(MenuType<?> menu, int id, Inventory playerInventory) {
        super(menu, id);
        this.player = playerInventory.player;
        this.level = playerInventory.player.level();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public void sendMessage(CompoundTag compound) {
        NetworkHandler.sendNbtDataToServer(compound);
    }

    @Override
    public void receive(CompoundTag tag) {
    }

    protected void createInventorySlots(Inventory pInventory) {
        this.createInventorySlots(pInventory, 0, 0);
    }

    protected void createInventorySlots(Inventory pInventory, int xOffset, int yOffset) {
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pInventory, j + i * 9 + 9, 8 + j * 18 + xOffset, 84 + i * 18 + yOffset));
            }
        }
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pInventory, k, 8 + k * 18 + xOffset, 142 + yOffset));
        }
    }
}
