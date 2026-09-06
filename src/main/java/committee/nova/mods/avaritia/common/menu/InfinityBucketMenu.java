package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfinityBucketMenu extends BaseMenu {
    public static final int ACTION_INSERT_CARRIED = 3;
    public static final int ACTION_CLEAR = 6;
    public static final int ACTION_SELECT_FLUID = 1000;
    public static final int ACTION_SELECT_CREATURE = 2000;
    public static final int ACTION_EXTRACT_FLUID = 3000;
    public static final int ACTION_DELETE_FLUID = 4000;
    public static final int ACTION_DELETE_CREATURE = 5000;

    public static final int PLAYER_INV_X = 49;
    public static final int PLAYER_INV_Y = 156;
    public static final int PLAYER_HOTBAR_Y = 214;
    private static final int HOTBAR_START = 27;

    public final int bucketSlot;
    private final ItemStack owningBucket;
    private final int offhandSyncSlot;

    public InfinityBucketMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory, buf.readInt());
    }

    public InfinityBucketMenu(int id, Inventory playerInventory, int bucketSlot) {
        super(ModMenus.infinity_bucket.get(), id, playerInventory);
        this.bucketSlot = bucketSlot;
        this.owningBucket = bucketSlot >= 0 && bucketSlot < playerInventory.getContainerSize()
                ? playerInventory.getItem(bucketSlot) : ItemStack.EMPTY;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(playerSlot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(playerSlot(playerInventory, col, PLAYER_INV_X + col * 18, PLAYER_HOTBAR_Y));
        }
        this.offhandSyncSlot = addSlot(lockedOffhandSyncSlot(playerInventory)).index;
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
        return current == owningBucket && !current.isEmpty()
                && current.is(ModItems.infinity_bucket.get()) && current.getCount() == 1;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player == this.player && player.isAlive() && ownerValid();
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player clicker) {
        if (clicker != player || slotId == offhandSyncSlot
                || (clickType == ClickType.SWAP && button == bucketSlot)) {
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
        if (id == ACTION_INSERT_CARRIED) {
            return transferCarried(bucket, -1);
        }
        if (id == ACTION_CLEAR) {
            if (!InfinityBucketItem.trySetFluids(bucket, List.of()) || !InfinityBucketItem.trySetCreatures(bucket, List.of())) {
                InfinityBucketItem.notifyOverCapacity(clicker);
                return false;
            }
            InfinityBucketItem.setControl(bucket, InfinityBucketItem.getControl(bucket).selectFluid(0));
            return true;
        }
        if (id >= ACTION_DELETE_CREATURE) {
            return deleteEntry(bucket, true, id - ACTION_DELETE_CREATURE);
        }
        if (id >= ACTION_DELETE_FLUID) {
            return deleteEntry(bucket, false, id - ACTION_DELETE_FLUID);
        }
        if (id >= ACTION_EXTRACT_FLUID) {
            return transferCarried(bucket, id - ACTION_EXTRACT_FLUID);
        }
        if (id >= ACTION_SELECT_CREATURE) {
            int index = id - ACTION_SELECT_CREATURE;
            if (index >= InfinityBucketItem.getCreatures(bucket).size()) {
                return false;
            }
            InfinityBucketItem.setControl(bucket, InfinityBucketItem.getControl(bucket).selectCreature(index));
            return true;
        }
        if (id >= ACTION_SELECT_FLUID) {
            int index = id - ACTION_SELECT_FLUID;
            if (index >= InfinityBucketItem.getFluids(bucket).size()) {
                return false;
            }
            InfinityBucketItem.setControl(bucket, InfinityBucketItem.getControl(bucket).selectFluid(index));
            return true;
        }
        return false;
    }

    private boolean deleteEntry(ItemStack bucket, boolean creature, int index) {
        if (creature) {
            List<InfinityBucketCreature> creatures = InfinityBucketItem.getCreatures(bucket);
            if (index < 0 || index >= creatures.size()) {
                return false;
            }
            creatures.remove(index);
            return InfinityBucketItem.trySetCreatures(bucket, creatures);
        }
        List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket);
        if (index < 0 || index >= fluids.size()) {
            return false;
        }
        fluids.remove(index);
        return InfinityBucketItem.trySetFluids(bucket, fluids);
    }

    /** Transfers exactly one carried container; the caller has already validated the owning stack. */
    private boolean transferCarried(ItemStack bucket, int fluidIndex) {
        ItemStack carried = getCarried();
        if (carried.isEmpty() || carried.is(ModItems.infinity_bucket.get())) {
            return false;
        }
        ItemStack one = carried.copyWithCount(1);
        if (one.getItem() instanceof MobBucketItem) {
            if (fluidIndex >= 0 || !InfinityBucketItem.tryStoreMobBucket(bucket, one, player)) {
                return false;
            }
            finishCarriedTransfer(carried, new ItemStack(Items.BUCKET));
            return true;
        }
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(one).orElse(null);
        IFluidHandlerItem bucketHandler = FluidUtil.getFluidHandler(bucket).orElse(null);
        if (itemHandler == null || bucketHandler == null) {
            return false;
        }
        FluidStack moved;
        if (fluidIndex < 0) {
            moved = FluidUtil.tryFluidTransfer(bucketHandler, itemHandler, Integer.MAX_VALUE, false);
            if (moved.isEmpty()) {
                return false;
            }
            moved = FluidUtil.tryFluidTransfer(bucketHandler, itemHandler, Integer.MAX_VALUE, true);
        } else {
            List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket);
            if (fluidIndex >= fluids.size()) {
                return false;
            }
            FluidStack requested = fluids.get(fluidIndex);
            moved = FluidUtil.tryFluidTransfer(itemHandler, bucketHandler, requested, false);
            if (moved.isEmpty()) {
                return false;
            }
            moved = FluidUtil.tryFluidTransfer(itemHandler, bucketHandler, requested, true);
        }
        if (moved.isEmpty()) {
            return false;
        }
        finishCarriedTransfer(carried, itemHandler.getContainer());
        return true;
    }

    private void finishCarriedTransfer(ItemStack carried, ItemStack result) {
        carried.shrink(1);
        if (carried.isEmpty()) {
            setCarried(result);
        } else {
            setCarried(carried);
            if (!result.isEmpty()) {
                player.getInventory().placeItemBackInInventory(result);
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player clicker, int index) {
        if (clicker != player || (!clicker.level().isClientSide() && !stillValid(clicker))
                || index < 0 || index >= offhandSyncSlot) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(clicker)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        boolean moved = index < HOTBAR_START
                ? moveItemStackTo(stack, HOTBAR_START, offhandSyncSlot, false)
                : moveItemStackTo(stack, 0, HOTBAR_START, false);
        if (!moved) {
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
