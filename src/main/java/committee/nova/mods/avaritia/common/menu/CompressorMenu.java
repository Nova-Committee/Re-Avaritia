package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseTileMenu;
import committee.nova.mods.avaritia.api.common.slot.ItemStackWrapperSlot;
import committee.nova.mods.avaritia.api.common.slot.OutputSlot;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 18:09
 * Version: 1.0
 */
public class CompressorMenu extends BaseTileMenu<NeutronCompressorTile> {
    private final ContainerData progressData;

    public CompressorMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, NeutronCompressorTile.createInventoryHandler(), buffer.readBlockPos(), new SimpleContainerData(1));
    }

    public CompressorMenu(int id, Inventory playerInventory, ItemStackWrapper inventory, BlockPos pos, ContainerData data) {
        super(ModMenus.compressor.get(), id, playerInventory, pos);
        this.progressData = data;
        inventory.setSlotValidator((integer, itemStack) -> {
            if (integer == 1) {
                var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.COMPRESSOR_RECIPE.get());
                if (recipes.isEmpty()) return true;

                for (var recipe : recipes) {
                    var ingredients = recipe.getIngredients();
                    if (!ingredients.isEmpty()) {
                        var ingredient = ingredients.get(0);
                        var items = ingredient.getItems();
                        if (items.length > 0 && itemStack.is(items[0].getItem())) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                return true;
            }
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

    @OnlyIn(Dist.CLIENT)
    public int getProgress() {
        return this.progressData.get(0);
    }

}
