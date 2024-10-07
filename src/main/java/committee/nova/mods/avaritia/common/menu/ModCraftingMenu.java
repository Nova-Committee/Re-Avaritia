package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.item.BaseItemStackHandler;
import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.common.inventory.ExtremeInventory;
import committee.nova.mods.avaritia.common.inventory.slot.ExtremeResultSlot;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/2/19 19:42
 * Version: 1.0
 */
public class ModCraftingMenu extends BaseMenu {
    private final Level world;
    private final Container result;
    private final Player player;
    private final int size;
    public static ModCraftingMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf buf, int size) {
        return new ModCraftingMenu(ModMenus.extreme_crafting_table.get(), windowId, playerInventory, buf, size);
    }

    public static ModCraftingMenu create(int windowId, Inventory playerInventory, BaseItemStackHandler inventory, BlockPos pos, int size) {
        return new ModCraftingMenu(ModMenus.extreme_crafting_table.get(), windowId, playerInventory, inventory, pos, size);
    }


    private ModCraftingMenu(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buf, int size) {
        this(type, id, playerInventory, new BaseItemStackHandler(size), buf.readBlockPos(), size);
    }

    private ModCraftingMenu(MenuType<?> type, int id, Inventory playerInventory, BaseItemStackHandler inventory, BlockPos pos, int size) {
        super(type, id, pos);
        this.player = playerInventory.player;
        this.world = playerInventory.player.level();
        this.result = new ResultContainer();
        this.size = size;

        var matrix = new ExtremeInventory(this, inventory, size);

        this.addSlot(new ExtremeResultSlot(this.player,this, matrix, this.result, 0, 206, 89));

        int i, j;
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                this.addSlot(new Slot(matrix, j + i * size, 8 + j * 18, 18 + i * 18));
            }
        }

        for (i = 0; i < 3; i++) {
            for (j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 39 + j * 18, 196 + i * 18));
            }
        }

        for (j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 39 + j * 18, 254));
        }

        this.slotsChanged(matrix);
    }



    @Override
    public void slotsChanged(@NotNull Container matrix) {
        var recipe = this.world.getRecipeManager().getRecipeFor(ModRecipeTypes.EXTREME_CRAFT_RECIPE.get(), matrix, this.world);

        if (recipe.isPresent()) {
            var result = recipe.get().assemble(matrix, this.world.registryAccess());
            this.result.setItem(0, result);
        } else {
            this.result.setItem(0, ItemStack.EMPTY);
        }

        super.slotsChanged(matrix);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotNumber) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(slotNumber);

        var in_slots = size * size + 1;
        var inv_slots = size * size + 1 + 36;

        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotNumber == 0) {
                if (!this.moveItemStackTo(itemstack1, in_slots, inv_slots, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (slotNumber >= in_slots && slotNumber < inv_slots) {
                if (!this.moveItemStackTo(itemstack1, 1, in_slots, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, in_slots, inv_slots, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
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

}
