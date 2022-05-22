package nova.committee.avaritia.common.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import nova.committee.avaritia.api.common.slot.BaseItemStackHandlerSlot;
import nova.committee.avaritia.api.common.slot.OutputSlot;
import nova.committee.avaritia.common.tile.CompressorTileEntity;
import nova.committee.avaritia.init.registry.ModMenus;
import nova.committee.avaritia.util.item.BaseItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 18:09
 * Version: 1.0
 */
public class CompressorMenu extends AbstractContainerMenu {
    private final Function<Player, Boolean> isUsableByPlayer;
    private final ContainerData data;
    private final BlockPos pos;

    private CompressorMenu(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(type, id, playerInventory, p -> false, CompressorTileEntity.createInventoryHandler(null), new SimpleContainerData(10), buffer.readBlockPos());
    }

    private CompressorMenu(MenuType<?> type, int id, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
        super(type, id);
        this.isUsableByPlayer = isUsableByPlayer;
        this.data = data;
        this.pos = pos;

        this.addSlot(new OutputSlot(inventory, 0, 120, 35));
        this.addSlot(new BaseItemStackHandlerSlot(inventory, 1, 39, 35));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(data);
    }

    public static CompressorMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new CompressorMenu(ModMenus.compressor, windowId, playerInventory, buffer);
    }

    public static CompressorMenu create(int windowId, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
        return new CompressorMenu(ModMenus.compressor, windowId, playerInventory, isUsableByPlayer, inventory, data, pos);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotNumber) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(slotNumber);

        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotNumber == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);

            } else if (slotNumber >= 2 && slotNumber < 38) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                    if (slotNumber < 29) {
                        if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 10, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.isUsableByPlayer.apply(player);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getProgress() {
        return data.get(0);
    }
}
