package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.common.component.InfinityBucketControl;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfinityBucketMenu extends BaseMenu {
    public static final int ACTION_TOGGLE_FORCE = 2;
    public static final int ACTION_TRANSFER_IN = 3;
    public static final int ACTION_TRANSFER_OUT = 4;
    public static final int ACTION_DELETE = 5;
    public static final int ACTION_CLEAR = 6;
    public static final int ACTION_SELECT_FLUID = 1000;
    public static final int ACTION_SELECT_CREATURE = 2000;

    public final int bucketSlot;
    private static final int TRANSFER_SLOT = 0;
    private final SimpleContainer transfer = new SimpleContainer(1);
    private final ItemStack owningBucket;
    private final int offhandSyncSlot;

    public InfinityBucketMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory, buf.readInt());
    }

    public InfinityBucketMenu(int id, Inventory playerInventory, int bucketSlot) {
        super(ModMenus.infinity_bucket.get(), id, playerInventory);
        this.bucketSlot = bucketSlot;
        this.owningBucket = bucketSlot >= 0 && bucketSlot < playerInventory.getContainerSize()
                ? playerInventory.getItem(bucketSlot)
                : ItemStack.EMPTY;
        this.addSlot(new Slot(transfer, 0, 152, 128) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return !stack.is(ModItems.infinity_bucket.get()) && stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(playerSlot(playerInventory, col + row * 9 + 9, 8 + col * 18, 158 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(playerSlot(playerInventory, col, 8 + col * 18, 216));
        }
        this.offhandSyncSlot = this.addSlot(lockedOffhandSyncSlot(playerInventory)).index;
    }

    private Slot playerSlot(Inventory inventory, int inventorySlot, int x, int y) {
        return new Slot(inventory, inventorySlot, x, y) {
            @Override
            public boolean mayPickup(@NotNull Player player) {
                return inventorySlot != bucketSlot && super.mayPickup(player);
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return inventorySlot != bucketSlot && super.mayPlace(stack);
            }
        };
    }

    private static Slot lockedOffhandSyncSlot(Inventory inventory) {
        return new Slot(inventory, Inventory.SLOT_OFFHAND, 0, 0) {
            @Override
            public boolean mayPickup(@NotNull Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }
        };
    }

    public ItemStack getBucket() {
        if (bucketSlot < 0 || bucketSlot >= player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return player.getInventory().getItem(bucketSlot);
    }

    public boolean ownerValid() {
        ItemStack current = getBucket();
        return current == owningBucket
                && !current.isEmpty()
                && current.is(ModItems.infinity_bucket.get())
                && current.getCount() == 1;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player == this.player && player.isAlive() && ownerValid();
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            this.clearContainer(player, this.transfer);
        }
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player clicker) {
        if (slotId == this.offhandSyncSlot) {
            return;
        }
        super.clicked(slotId, button, clickType, clicker);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player clicker, int id) {
        if (clicker != player || clicker.level().isClientSide() || !stillValid(clicker)) {
            return false;
        }
        ItemStack bucket = getBucket();
        if (bucket != owningBucket) {
            return false;
        }
        if (id == ACTION_TOGGLE_FORCE) {
            InfinityBucketControl control = InfinityBucketItem.getControl(bucket);
            InfinityBucketItem.setControl(bucket, control.withForcePlacement(!control.forcePlacement()));
            return true;
        }
        if (id == ACTION_TRANSFER_IN) {
            return transfer(bucket, true);
        }
        if (id == ACTION_TRANSFER_OUT) {
            return transfer(bucket, false);
        }
        if (id == ACTION_DELETE) {
            return deleteSelected(bucket);
        }
        if (id == ACTION_CLEAR) {
            if (!InfinityBucketItem.trySetFluids(bucket, List.of()) || !InfinityBucketItem.trySetCreatures(bucket, List.of())) {
                InfinityBucketItem.notifyOverCapacity(clicker);
                return false;
            }
            InfinityBucketItem.setControl(bucket, InfinityBucketItem.getControl(bucket).selectFluid(0));
            return true;
        }
        if (id >= ACTION_SELECT_CREATURE) {
            int index = id - ACTION_SELECT_CREATURE;
            List<InfinityBucketCreature> creatures = InfinityBucketItem.getCreatures(bucket);
            if (index < 0 || index >= creatures.size()) {
                return false;
            }
            InfinityBucketItem.setControl(bucket, InfinityBucketItem.getControl(bucket).selectCreature(index));
            return true;
        }
        if (id >= ACTION_SELECT_FLUID) {
            int index = id - ACTION_SELECT_FLUID;
            List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket);
            if (index < 0 || index >= fluids.size()) {
                return false;
            }
            InfinityBucketItem.setControl(bucket, InfinityBucketItem.getControl(bucket).selectFluid(index));
            return true;
        }
        return false;
    }

    private boolean deleteSelected(ItemStack bucket) {
        InfinityBucketControl control = InfinityBucketItem.getControl(bucket);
        if (control.creatureSelected()) {
            List<InfinityBucketCreature> creatures = InfinityBucketItem.getCreatures(bucket);
            if (control.selectedIndex() < 0 || control.selectedIndex() >= creatures.size()) {
                return false;
            }
            creatures.remove(control.selectedIndex());
            return InfinityBucketItem.trySetCreatures(bucket, creatures);
        }
        List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket);
        if (control.selectedIndex() < 0 || control.selectedIndex() >= fluids.size()) {
            return false;
        }
        fluids.remove(control.selectedIndex());
        return InfinityBucketItem.trySetFluids(bucket, fluids);
    }

    private boolean transfer(ItemStack bucket, boolean intoBucket) {
        if (bucket != owningBucket || !ownerValid()) {
            return false;
        }
        ItemStack original = transfer.getItem(0);
        if (original.isEmpty()) {
            return false;
        }
        ItemStack one = original.copyWithCount(1);
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(one).orElse(null);
        IFluidHandlerItem bucketHandler = FluidUtil.getFluidHandler(bucket).orElse(null);
        if (itemHandler == null || bucketHandler == null) {
            return false;
        }
        FluidStack moved;
        if (intoBucket) {
            moved = FluidUtil.tryFluidTransfer(bucketHandler, itemHandler, Integer.MAX_VALUE, false);
            if (moved.isEmpty()) {
                return false;
            }
            moved = FluidUtil.tryFluidTransfer(bucketHandler, itemHandler, Integer.MAX_VALUE, true);
        } else {
            FluidStack selected = InfinityBucketItem.getSelectedFluid(bucket);
            if (selected.isEmpty()) {
                return false;
            }
            moved = FluidUtil.tryFluidTransfer(itemHandler, bucketHandler, selected, false);
            if (moved.isEmpty()) {
                return false;
            }
            moved = FluidUtil.tryFluidTransfer(itemHandler, bucketHandler, selected, true);
        }
        if (moved.isEmpty()) {
            return false;
        }
        ItemStack result = itemHandler.getContainer();
        original.shrink(1);
        if (original.isEmpty()) {
            transfer.setItem(0, result);
        } else {
            transfer.setItem(0, original);
            player.getInventory().placeItemBackInInventory(result);
        }
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (!stillValid(player) || index == this.offhandSyncSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return result;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index == TRANSFER_SLOT) {
            if (!this.moveItemStackTo(stack, 1, this.offhandSyncSlot, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, TRANSFER_SLOT, TRANSFER_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }
}
