package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.common.component.InfinityChestReference;
import committee.nova.mods.avaritia.common.container.InfinityChestContainer;
import committee.nova.mods.avaritia.common.net.chest.ChannelAction;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.core.chest.ChestHandler;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import committee.nova.mods.avaritia.core.chest.ServerChestHandler;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.Locale;

/** 由全局通道提供数据、由 15×7 只读虚拟槽提供视图的无尽箱菜单。 */
public class InfinityChestMenu extends AbstractContainerMenu {
    public static final int PLAYER_SLOT_COUNT = 36;
    public static final int CONTAINER_SLOT_START = PLAYER_SLOT_COUNT;
    public static final int CONTAINER_SLOT_SIZE = InfinityChestContainer.SIZE;

    private final Player player;
    private final BlockPos blockPos;
    private final @Nullable InfinityChestTile tile;
    private final ChestHandler chest;
    private final InfinityChestContainer chestContainer;
    private final UUID owner;
    private final UUID channelId;
    private boolean locked;
    private String filter;
    private byte sortType;

    public InfinityChestMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, readInitialData(buffer));
    }

    private InfinityChestMenu(int id, Inventory inventory, InitialData initial) {
        this(id, inventory, initial.pos(),
                inventory.player.level().getBlockEntity(initial.pos()) instanceof InfinityChestTile found ? found : null,
                initial.owner(), initial.locked(), initial.filter(), initial.sortType(), initial.channelId());
    }

    public InfinityChestMenu(int id, Inventory inventory, InfinityChestTile tile) {
        this(id, inventory, tile.getBlockPos(), tile,
                tile.getOwner() == null ? inventory.player.getUUID() : tile.getOwner(),
                tile.isLocked(), tile.getFilter(), tile.getSortType(), tile.getChannelID());
    }

    private InfinityChestMenu(int id, Inventory inventory, BlockPos blockPos, @Nullable InfinityChestTile tile,
                              UUID owner, boolean locked, String filter, byte sortType, UUID channelId) {
        super(ModMenus.infinity_chest.get(), id);
        this.player = inventory.player;
        this.blockPos = blockPos;
        this.tile = tile;
        this.owner = owner;
        this.locked = locked;
        this.filter = InfinityChestReference.sanitizeFilter(filter).toLowerCase(Locale.ROOT);
        this.sortType = sanitizeSort(sortType);
        this.channelId = channelId;
        this.chestContainer = new InfinityChestContainer(this);
        this.chest = player instanceof ServerPlayer && tile != null
                ? tile.getChest()
                : ClientChestManager.getInstance().getChest(chestContainer);

        addPlayerSlots(inventory);
        for (int row = 0; row < InfinityChestContainer.HEIGHT; row++) {
            for (int column = 0; column < InfinityChestContainer.WIDTH; column++) {
                addSlot(new VirtualSlot(chestContainer, column + row * InfinityChestContainer.WIDTH,
                        8 + column * 18, 16 + row * 18));
            }
        }

        if (player instanceof ServerPlayer serverPlayer && chest instanceof ServerChestHandler serverChest) {
            serverChest.addListener(serverPlayer);
            if (tile != null) tile.startOpen(player);
        }
        chestContainer.refresh(true);
    }

    private void addPlayerSlots(Inventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 62 + column * 18, 214));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 62 + column * 18, 156 + row * 18));
            }
        }
    }

    public @Nullable InfinityChestTile getTile() {
        return tile;
    }

    public ChestHandler getChest() {
        return chest;
    }

    public InfinityChestContainer getChestContainer() {
        return chestContainer;
    }

    public int virtualIndex(Slot slot) {
        return virtualIndexForMenuSlot(slots.indexOf(slot));
    }

    static int virtualIndexForMenuSlot(int menuSlot) {
        return menuSlot >= CONTAINER_SLOT_START && menuSlot < CONTAINER_SLOT_START + CONTAINER_SLOT_SIZE
                ? menuSlot - CONTAINER_SLOT_START
                : -1;
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getFilter() {
        return filter;
    }

    public byte getSortType() {
        return sortType;
    }

    public boolean contains(ItemResource resource) {
        return resource != null && !resource.isEmpty() && chest.amount(resource) > 0L;
    }

    public boolean canPlayerModify(Player candidate) {
        if (!candidate.level().isClientSide() && tile != null) {
            return tile.canPlayerModify(candidate);
        }
        return InfinityChestReference.canModify(owner, locked, candidate.getUUID());
    }

    public void setFilterFromClient(String nextFilter) {
        if (!canPlayerModify(player)) return;
        filter = InfinityChestReference.sanitizeFilter(nextFilter).toLowerCase(Locale.ROOT);
        if (tile != null && player instanceof ServerPlayer) tile.setFilter(filter);
        chestContainer.refresh(true);
    }

    public void nextSort() {
        sortType = (byte) ((sortType + 2) % 8);
        if (tile != null && player instanceof ServerPlayer) tile.setSortType(sortType);
        chestContainer.refresh(true);
    }

    public void reverseSort() {
        sortType = (byte) (sortType % 2 == 0 ? sortType + 1 : sortType - 1);
        if (tile != null && player instanceof ServerPlayer) tile.setSortType(sortType);
        chestContainer.refresh(true);
    }

    public void toggleLockClient() {
        if (owner.equals(player.getUUID())) {
            locked = !locked;
        }
    }

    @Override
    public boolean clickMenuButton(Player candidate, int id) {
        if (!canPlayerModify(candidate)) return false;
        switch (id) {
            case 0 -> {
                if (!owner.equals(candidate.getUUID()) || tile == null) return false;
                locked = !locked;
                tile.setLocked(locked);
            }
            case 1 -> nextSort();
            case 2 -> reverseSort();
            default -> { return false; }
        }
        return true;
    }

    /** 仅由已通过载荷守卫的服务端调用。 */
    public void action(ChannelAction action, ItemResource selected) {
        if (!(player instanceof ServerPlayer) || !canPlayerModify(player)
                || !contains(selected) && !(selected.isEmpty() && !getCarried().isEmpty())) return;
        ItemAccess cursor = ItemAccess.forPlayerCursor(player, this);
        ResourceHandler<ItemResource> carriedContainer = cursor.getCapability(Capabilities.Item.ITEM);
        switch (action) {
            case LEFT_CLICK -> {
                if (cursor.getResource().isEmpty()) withdraw(cursor, selected, selected.getMaxStackSize());
                else if (!selected.isEmpty() && cursor.getAmount() == 1 && carriedContainer != null) {
                    fillContainer(carriedContainer, selected);
                }
                else deposit(cursor, cursor.getAmount());
            }
            case RIGHT_CLICK -> {
                if (cursor.getResource().isEmpty()) withdraw(cursor, selected, (selected.getMaxStackSize() + 1) / 2);
                else if (cursor.getAmount() == 1 && carriedContainer != null) drainContainer(carriedContainer, false);
                else deposit(cursor, 1);
            }
            case LEFT_SHIFT -> {
                if (cursor.getResource().isEmpty()) withdraw(PlayerInventoryWrapper.of(player), selected, selected.getMaxStackSize());
                else if (!selected.isEmpty() && carriedContainer != null) fillContainer(carriedContainer, selected);
                else deposit(cursor, cursor.getAmount());
            }
            case RIGHT_SHIFT -> {
                if (cursor.getResource().isEmpty()) withdraw(PlayerInventoryWrapper.of(player), selected, 1);
                else if (carriedContainer != null) drainContainer(carriedContainer, true);
                else deposit(cursor, 1);
            }
            case THROW_ONE -> drop(selected, 1);
            case THROW_STACK -> drop(selected, selected.getMaxStackSize());
            case LEFT_DRAG -> {
                if (!cursor.getResource().isEmpty()) deposit(cursor, cursor.getAmount());
            }
            case RIGHT_DRAG -> {
                if (!cursor.getResource().isEmpty()) deposit(cursor, 1);
            }
            case CLONE -> {
                if (player.hasInfiniteMaterials()) setCarried(selected.toStack(selected.getMaxStackSize()));
            }
            case DRAG_CLONE -> {
                if (player.hasInfiniteMaterials() && !getCarried().isEmpty()) {
                    setCarried(getCarried().copyWithCount(getCarried().getMaxStackSize()));
                }
            }
        }
    }

    private int deposit(ItemAccess source, int requested) {
        ItemResource resource = source.getResource();
        int amount = Math.min(requested, source.getAmount());
        if (resource.isEmpty() || amount <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = chest.insert(resource, amount, transaction);
            if (inserted <= 0 || source.extract(resource, inserted, transaction) != inserted) return 0;
            transaction.commit();
            return inserted;
        }
    }

    private int withdraw(ResourceHandler<ItemResource> destination, ItemResource resource, int requested) {
        if (requested <= 0) return 0;
        int available = (int) Math.min(requested, chest.amount(resource));
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = destination.insert(resource, available, transaction);
            if (inserted <= 0 || chest.extract(resource, inserted, transaction) != inserted) return 0;
            transaction.commit();
            return inserted;
        }
    }

    private int withdraw(ItemAccess destination, ItemResource resource, int requested) {
        if (requested <= 0) return 0;
        int available = (int) Math.min(requested, chest.amount(resource));
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = chest.extract(resource, available, transaction);
            if (extracted <= 0 || destination.insert(resource, extracted, transaction) != extracted) return 0;
            transaction.commit();
            return extracted;
        }
    }

    private int fillContainer(ResourceHandler<ItemResource> destination, ItemResource resource) {
        int requested = (int) Math.min(Integer.MAX_VALUE, chest.amount(resource));
        if (requested <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = destination.insert(resource, requested, transaction);
            if (inserted <= 0 || chest.extract(resource, inserted, transaction) != inserted) return 0;
            transaction.commit();
            return inserted;
        }
    }

    private long drainContainer(ResourceHandler<ItemResource> source, boolean allSlots) {
        long moved = 0L;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int index = 0; index < source.size(); index++) {
                ItemResource resource = source.getResource(index);
                int amount = source.getAmountAsInt(index);
                if (resource.isEmpty() || amount <= 0) continue;
                int extracted = source.extract(index, resource, amount, transaction);
                if (extracted <= 0) continue;
                if (chest.insert(resource, extracted, transaction) != extracted) return 0;
                moved += extracted;
                if (!allSlots) break;
            }
            if (moved > 0) transaction.commit();
        }
        return moved;
    }

    private void drop(ItemResource resource, int requested) {
        int amount = (int) Math.min(requested, chest.amount(resource));
        if (amount <= 0) return;
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = chest.extract(resource, amount, transaction);
            if (extracted <= 0) return;
            transaction.commit();
            player.drop(resource.toStack(extracted), false);
        }
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player candidate) {
        if (slotId >= CONTAINER_SLOT_START) return;
        super.clicked(slotId, button, input, candidate);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player candidate, int slotId) {
        if (!(candidate instanceof ServerPlayer) || slotId < 0 || slotId >= PLAYER_SLOT_COUNT
                || !canPlayerModify(candidate)) return ItemStack.EMPTY;
        Slot slot = slots.get(slotId);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        ItemAccess access = ItemAccess.forPlayerSlot(candidate, slot.getContainerSlot());
        int moved = deposit(access, access.getAmount());
        slot.setChanged();
        return moved > 0 ? original : ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return virtualIndex(slot) < 0;
    }

    @Override
    public boolean stillValid(@NotNull Player candidate) {
        return tile != null && !tile.isRemoved() && tile.getLevel() != null
                && tile.getLevel().getBlockEntity(blockPos) == tile
                && tile.getLevel().getBlockState(blockPos).is(ModBlocks.infinity_chest.get())
                && candidate.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64.0D
                && canPlayerModify(candidate);
    }

    @Override
    public void removed(Player candidate) {
        super.removed(candidate);
        if (candidate instanceof ServerPlayer serverPlayer) {
            if (chest instanceof ServerChestHandler serverChest) serverChest.removeListener(serverPlayer);
            if (tile != null) tile.stopOpen(candidate);
        } else {
            ClientChestManager.getInstance().close(chestContainer);
        }
    }

    private static byte sanitizeSort(byte value) {
        return value >= 0 && value <= 7 ? value : InfinityChestReference.DEFAULT_SORT_TYPE;
    }

    private static InitialData readInitialData(FriendlyByteBuf buffer) {
        return new InitialData(buffer.readBlockPos(), buffer.readUUID(), buffer.readBoolean(),
                buffer.readUtf(InfinityChestReference.MAX_FILTER_LENGTH), buffer.readByte(), buffer.readUUID());
    }

    private record InitialData(BlockPos pos, UUID owner, boolean locked, String filter, byte sortType, UUID channelId) {
    }

    private static final class VirtualSlot extends Slot {
        private VirtualSlot(InfinityChestContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
        @Override public int getMaxStackSize() { return 1; }
    }
}
