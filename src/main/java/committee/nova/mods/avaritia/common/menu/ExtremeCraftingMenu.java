package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.common.container.ExtremeCraftingContainer;
import committee.nova.mods.avaritia.common.slot.ExtremeCraftingSlot;
import committee.nova.mods.avaritia.common.tile.ExtremeCraftingTile;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.item.BaseItemStackHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/2/19 19:42
 * Version: 1.0
 */
public class ExtremeCraftingMenu extends AbstractContainerMenu {

    private final Function<Player, Boolean> isUsableByPlayer;
    private final Level world;
    private final Container result;

    private ExtremeCraftingMenu(MenuType<?> type, int id, Inventory playerInventory) {
        this(type, id, playerInventory, p -> false, ExtremeCraftingTile.createInventoryHandler(null));
    }

    private ExtremeCraftingMenu(MenuType<?> type, int id, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory) {
        super(type, id);
        this.isUsableByPlayer = isUsableByPlayer;
        this.world = playerInventory.player.getCommandSenderWorld();
        this.result = new ResultContainer();

        var matrix = new ExtremeCraftingContainer(this, inventory, 9);

        this.addSlot(new ExtremeCraftingSlot(this, matrix, this.result, 0, 206, 89));

        int i, j;
        for (i = 0; i < 9; i++) {
            for (j = 0; j < 9; j++) {
                this.addSlot(new Slot(matrix, j + i * 9, 8 + j * 18, 18 + i * 18));
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

    public static ExtremeCraftingMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new ExtremeCraftingMenu(ModMenus.extreme_crafting_table.get(), windowId, playerInventory);
    }

    public static ExtremeCraftingMenu create(int windowId, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory) {
        return new ExtremeCraftingMenu(ModMenus.extreme_crafting_table.get(), windowId, playerInventory, isUsableByPlayer, inventory);
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
    public boolean stillValid(@NotNull Player player) {
        return this.isUsableByPlayer.apply(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotNumber) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(slotNumber);

        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotNumber == 0) {
                if (!this.moveItemStackTo(itemstack1, 82, 118, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (slotNumber >= 82 && slotNumber < 118) {
                if (!this.moveItemStackTo(itemstack1, 1, 82, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 82, 118, false)) {
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
