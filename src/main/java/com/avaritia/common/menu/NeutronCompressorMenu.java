package com.avaritia.common.menu;

import com.avaritia.api.common.crafting.ShapelessCraftingInput;
import com.avaritia.api.common.menu.BaseTileMenu;
import com.avaritia.api.common.slot.ItemStackWrapperSlot;
import com.avaritia.api.common.slot.OutputSlot;
import com.avaritia.api.common.wrapper.ItemStackWrapper;
import com.avaritia.common.tile.NeutronCompressorTile;
import com.avaritia.init.registry.ModMenus;
import com.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 18:09
 * Version: 1.0
 */
public class NeutronCompressorMenu extends BaseTileMenu<NeutronCompressorTile> {
    private final ContainerData progressData;
    public NeutronCompressorMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, NeutronCompressorTile.createInventoryHandler(), buffer.readBlockPos(), new SimpleContainerData(1));
    }

    public NeutronCompressorMenu(int id, Inventory playerInventory, ItemStackWrapper inventory, BlockPos pos, ContainerData data) {
        super(ModMenus.neutron_compressor.get(), id, playerInventory, pos);
        this.progressData = data;
        this.addDataSlots(progressData);
        inventory.setCanInsert((integer, stack) -> {
            if (integer == 1) {
                // 获取压缩器实例检查锁定状态
                if (level.getBlockEntity(pos) instanceof NeutronCompressorTile compressor) {
                    if (compressor.isRecipeLocked() && compressor.getLockedRecipe() != null) {
                        // 锁定状态下，只接受锁定配方的材料
                        var input = compressor.getLockedRecipe().getInput();
                        if (!input.isEmpty()) {
                            return input.test(stack);
                        }
                        return false;
                    }else {
                        var compressorRecipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.COMPRESSOR_RECIPE.get(), new ShapelessCraftingInput(List.of(stack)), level).map(RecipeHolder::value).orElse(null);
                        if (compressorRecipe != null) {
                            var input = compressorRecipe.getInput();
                            if (!input.isEmpty()) {
                                return input.test(stack);
                            }
                            return false;
                        }
                        return false;
                    }
                }
            }
            return true;
        });
        this.addSlot(new OutputSlot(inventory, 0, 120, 35));
        this.addSlot(new ItemStackWrapperSlot(inventory, 1, 39, 35));
        createInventorySlots(playerInventory);
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
                    } else if (!this.moveItemStackTo(itemstack1, 11, 29, false)) {
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
    public int getProgress() {
        return this.progressData.get(0);
    }

}
