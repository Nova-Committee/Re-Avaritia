package committee.nova.mods.avaritia.common.container.chest;

import committee.nova.mods.avaritia.common.menu.slot.InfinityBoxSlot;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class InfinityBoxContainer extends InfinityChestContainer {
    private final Player player;
    private final Level world;

    public InfinityBoxContainer(int id, Inventory playerInventory, FriendlyByteBuf buf){
        this(id, playerInventory, (Container) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public InfinityBoxContainer(int id, Inventory playerInventory, Container tile) {
        super(ModMenus.infinity_chest.get(), id);
        this.chestTile = (InfinityChestTile) tile;
        this.player = playerInventory.player;
        this.world = player.level();
        chestTile.startOpen(this.player);

        for(int j = 0; j < 9; ++j) {
            for(int k = 0; k < 27; ++k) {
                this.addSlot(new InfinityBoxSlot(chestTile, k + j * 27, 8 + k * 18, 17 + j * 18));
            }
        }

        for(int l = 0; l < 3; ++l) {
            for(int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 170 + j1 * 18, 193 + l * 18));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 170 + i1 * 18, 251));
        }
    }

    @Override
    public void slotsChanged(Container pContainer) {
        if (world.isClientSide) return;
        ServerPlayer serverPlayer = (ServerPlayer)player;
        ItemStack itemStack = ItemStack.EMPTY;
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.getStateId(), 252, itemStack));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemstack = itemStack1.copy();

            if (index < 243) { //取出
                if (!super.moveItemStackTo(itemStack1, 256, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack1, 0, 243, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }
            slot.onTake(player, itemStack1);
        }

        return itemstack;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.chestTile.stopOpen(pPlayer);
    }

    /**
     * 获取箱子名称
     */
    public String getDisplayName(){
        return this.chestTile.getDisplayName().getString();
    }
}
