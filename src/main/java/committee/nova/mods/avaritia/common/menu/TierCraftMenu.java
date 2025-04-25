package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseTileMenu;
import committee.nova.mods.avaritia.common.container.ModCraftContainer;
import committee.nova.mods.avaritia.common.container.slot.ModCraftResultSlot;
import committee.nova.mods.avaritia.common.tile.TierCraftTile;
import committee.nova.mods.avaritia.init.registry.ModCraftTier;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/2/19 19:42
 * Version: 1.0
 */
public class TierCraftMenu extends BaseTileMenu<TierCraftTile> {
    private final Level world;
    private final ResultContainer result;
    private final Player player;
    private final ModCraftTier tier;
    private final ModCraftContainer matrix;

    private TierCraftMenu(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buf, ModCraftTier tier) {
        this(type, id, playerInventory, buf.readBlockPos(), tier);
    }

    public TierCraftMenu(MenuType<?> type, int id, Inventory playerInventory, BlockPos pos,
                         ModCraftTier tier
    ) {
        super(type, id, playerInventory, pos);
        this.player = playerInventory.player;
        this.world = playerInventory.player.level();
        this.result = new ResultContainer();
        this.tier = tier;

        this.matrix = new ModCraftContainer(this, getTileEntity().getInventory(), tier.size * tier.size);

        this.addSlot(new ResultSlot(this.player, matrix, this.result, 0, tier.outX, tier.outY));

        int i, j;
        for (i = 0; i < tier.size; i++) {
            for (j = 0; j < tier.size; j++) {
                this.addSlot(new Slot(matrix, j + i * tier.size, tier.mainX + j * 18, tier.mainY + i * 18));
            }
        }

        for (i = 0; i < 3; i++) {
            for (j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, tier.playerInvX + j * 18, tier.playerInvY + i * 18));
            }
        }

        for (j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, tier.hotBarX + j * 18, tier.hotBarY));
        }

        this.slotsChanged(matrix);
    }

    public static TierCraftMenu sculk(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new TierCraftMenu(ModMenus.sculk_crafting_tile_table.get(), windowId, playerInventory, buf, ModCraftTier.SCULK);
    }

    public static TierCraftMenu sculk(int windowId, Inventory playerInventory, BlockPos pos) {
        return new TierCraftMenu(ModMenus.sculk_crafting_tile_table.get(), windowId, playerInventory, pos, ModCraftTier.SCULK);
    }

    public static TierCraftMenu nether(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new TierCraftMenu(ModMenus.nether_crafting_tile_table.get(), windowId, playerInventory, buf, ModCraftTier.NETHER);
    }

    public static TierCraftMenu nether(int windowId, Inventory playerInventory, BlockPos pos) {
        return new TierCraftMenu(ModMenus.nether_crafting_tile_table.get(), windowId, playerInventory, pos, ModCraftTier.NETHER);
    }

    public static TierCraftMenu end(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new TierCraftMenu(ModMenus.end_crafting_tile_table.get(), windowId, playerInventory, buf, ModCraftTier.END);
    }

    public static TierCraftMenu end(int windowId, Inventory playerInventory, BlockPos pos) {
        return new TierCraftMenu(ModMenus.end_crafting_tile_table.get(), windowId, playerInventory, pos, ModCraftTier.END);
    }

    public static TierCraftMenu extreme(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new TierCraftMenu(ModMenus.extreme_crafting_table.get(), windowId, playerInventory, buf, ModCraftTier.EXTREME);
    }

    public static TierCraftMenu extreme(int windowId, Inventory playerInventory, BlockPos pos) {
        return new TierCraftMenu(ModMenus.extreme_crafting_table.get(), windowId, playerInventory, pos, ModCraftTier.EXTREME);
    }

    @Override
    public void slotsChanged(@NotNull Container matrix) {
        var inventory = this.matrix.asCraftInput();
        var recipe = this.world.getRecipeManager().getRecipeFor(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get(), inventory, this.world);

        if (recipe.isPresent()) {
            var result = recipe.get().value().assemble(inventory, this.world.registryAccess());
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

        var in_slots = tier.size * tier.size + 1;
        var inv_slots = tier.size * tier.size + 1 + 36;

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
