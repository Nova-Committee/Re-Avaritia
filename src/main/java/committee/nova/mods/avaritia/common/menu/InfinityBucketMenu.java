package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.api.common.menu.BaseMenu;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Server-authoritative Infinity Bucket menu bound to a specific inventory slot and nonce.
 * Direct carried-stack transfers; no user-facing transfer slot.
 */
public class InfinityBucketMenu extends BaseMenu {
    public static final int ACTION_INSERT_CARRIED = 3;
    public static final int ACTION_CLEAR = 6;
    public static final int ACTION_SELECT_FLUID = 1000;
    public static final int ACTION_SELECT_CREATURE = 2000;
    public static final int ACTION_EXTRACT_FLUID = 3000;
    public static final int ACTION_DELETE_FLUID = 4000;
    public static final int ACTION_DELETE_CREATURE = 5000;
    public static final int ACTION_EXTRACT_CREATURE = 6000;

    public static final int PLAYER_INV_X = 49;
    public static final int PLAYER_INV_Y = 156;
    public static final int PLAYER_HOTBAR_Y = 214;
    private static final int HOTBAR_START = 27;

    public final int bucketSlot;
    private final UUID nonce;
    private final int offhandSyncSlot;

    public InfinityBucketMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory, buf.readInt(), buf.readUUID());
    }

    public InfinityBucketMenu(int id, Inventory playerInventory, int bucketSlot, UUID nonce) {
        super(ModMenus.infinity_bucket.get(), id, playerInventory);
        this.bucketSlot = bucketSlot;
        this.nonce = nonce;
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
            public boolean mayPickup(@NotNull Player clicker) {
                return inventorySlot != bucketSlot && super.mayPickup(clicker);
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
            public boolean mayPickup(@NotNull Player clicker) {
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

            @Override
            public boolean allowModification(@NotNull Player clicker) {
                return false;
            }

            @Override
            public boolean isHighlightable() {
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
        if (current.isEmpty() || !current.is(ModItems.infinity_bucket.get()) || current.getCount() != 1) {
            return false;
        }
        if (player.level().isClientSide()) {
            return true;
        }
        UUID onItem = current.get(ModDataComponents.INFINITY_BUCKET_MENU_NONCE.get());
        return nonce != null && nonce.equals(onItem);
    }

    @Override
    public boolean stillValid(@NotNull Player clicker) {
        return clicker == this.player && clicker.isAlive() && ownerValid();
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player clicker) {
        if (clicker != player || slotId == offhandSyncSlot
                || (input == ContainerInput.SWAP && button == bucketSlot)) {
            return;
        }
        super.clicked(slotId, button, input, clicker);
    }

    @Override
    public boolean canTakeItemForPickAll(@NotNull ItemStack stack, Slot target) {
        return target.index != offhandSyncSlot && super.canTakeItemForPickAll(stack, target);
    }

    @Override
    public boolean canDragTo(Slot target) {
        return target.index != offhandSyncSlot && super.canDragTo(target);
    }

    public boolean handleAction(ServerPlayer clicker, int action, int index, boolean flag) {
        return clickMenuButton(clicker, action);
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
        if (id >= ACTION_EXTRACT_CREATURE) {
            return extractCreature(bucket, id - ACTION_EXTRACT_CREATURE);
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

    private boolean transferCarried(ItemStack bucket, int fluidIndex) {
        ItemStack carried = getCarried();
        if (carried.isEmpty() || carried.is(ModItems.infinity_bucket.get())) {
            return false;
        }
        if (carried.getItem() instanceof MobBucketItem) {
            ItemStack one = carried.copyWithCount(1);
            if (fluidIndex >= 0 || !InfinityBucketItem.tryStoreMobBucket(bucket, one, player)) {
                return false;
            }
            finishCarriedTransfer(carried, new ItemStack(Items.BUCKET));
            return true;
        }
        ItemAccess containerAccess = ItemAccess.forPlayerCursor(player, this).oneByOne();
        ResourceHandler<FluidResource> container = containerAccess.getCapability(Capabilities.Fluid.ITEM);
        ItemAccess bucketAccess = ItemAccess.forPlayerSlot(player, bucketSlot);
        ResourceHandler<FluidResource> bucketHandler = bucketAccess.getCapability(Capabilities.Fluid.ITEM);
        if (container == null || bucketHandler == null) {
            return false;
        }
        int moved;
        try (Transaction tx = Transaction.openRoot()) {
            if (fluidIndex < 0) {
                moved = ResourceHandlerUtil.move(container, bucketHandler, resource -> true, Integer.MAX_VALUE, tx);
            } else {
                List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket);
                if (fluidIndex >= fluids.size()) {
                    return false;
                }
                FluidResource requested = FluidResource.of(fluids.get(fluidIndex));
                moved = ResourceHandlerUtil.move(bucketHandler, container, requested::equals, Integer.MAX_VALUE, tx);
            }
            if (moved <= 0) {
                return false;
            }
            tx.commit();
        }
        return true;
    }

    private boolean extractCreature(ItemStack bucket, int creatureIndex) {
        ItemStack carried = getCarried();
        if (carried.isEmpty() || carried.is(ModItems.infinity_bucket.get())) {
            return false;
        }
        ItemStack filled = InfinityBucketItem.tryExtractMobBucket(bucket, carried.copyWithCount(1), player, creatureIndex);
        if (filled.isEmpty()) {
            return false;
        }
        finishCarriedTransfer(carried, filled);
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
    public void removed(@NotNull Player clicker) {
        super.removed(clicker);
        ItemStack bucket = getBucket();
        if (!bucket.isEmpty() && nonce != null) {
            InfinityBucketItem.clearMenuNonce(bucket, nonce);
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
