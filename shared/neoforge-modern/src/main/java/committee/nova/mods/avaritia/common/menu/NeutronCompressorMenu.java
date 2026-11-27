package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.crafting.ShapelessCraftingInput;
import committee.nova.mods.avaritia.api.common.menu.BaseTileMenu;
import committee.nova.mods.avaritia.api.common.slot.ItemStackWrapperSlot;
import committee.nova.mods.avaritia.api.common.slot.OutputSlot;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
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
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MATERIAL_COUNT = 1;
    public static final int DATA_MATERIALS_REQUIRED = 2;
    public static final int DATA_TIME_REQUIRED = 3;
    public static final int DATA_COUNT = 4;

    private final ContainerData data;

    public NeutronCompressorMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, NeutronCompressorTile.createInventoryHandler(), buffer.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public NeutronCompressorMenu(int id, Inventory playerInventory, ItemStackWrapper inventory, BlockPos pos, ContainerData data) {
        super(ModMenus.neutron_compressor.get(), id, playerInventory, pos);
        this.data = data;
        this.addDataSlots(data);
        inventory.setCanInsert((integer, stack) -> {
            if (integer == 1 && level instanceof ServerLevel serverLevel) {
                // 获取压缩器实例检查锁定状态
                if (serverLevel.getBlockEntity(pos) instanceof NeutronCompressorTile compressor) {
                    if (compressor.isRecipeLocked() && compressor.getLockedRecipe() != null) {
                        // 锁定状态下，只接受锁定配方的材料
                        var input = compressor.getLockedRecipe().getInput();
                        if (!input.isEmpty()) {
                            return input.test(stack);
                        }
                        return false;
                    }else {
                        var compressorRecipe = serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.COMPRESSOR_RECIPE.get(), new ShapelessCraftingInput(List.of(stack)), level).map(RecipeHolder::value).orElse(null);
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
        this.addSlot(new OutputSlot(inventory, inventory::set, 0, 120, 35));
        this.addSlot(new ItemStackWrapperSlot(inventory, inventory::set, 1, 39, 35));
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
        return this.data.get(DATA_PROGRESS);
    }

    public int getMaterialCount() {
        return this.data.get(DATA_MATERIAL_COUNT);
    }

    public int getMaterialsRequired() {
        return this.data.get(DATA_MATERIALS_REQUIRED);
    }

    public int getTimeRequired() {
        return this.data.get(DATA_TIME_REQUIRED);
    }

}
