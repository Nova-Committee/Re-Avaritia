package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.slot.FakeSlot;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestActionPack;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.core.chest.ChestHandler;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import committee.nova.mods.avaritia.core.chest.InfinityChestContainer;
import committee.nova.mods.avaritia.core.chest.ServerChestHandler;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author cnlimiter
 */
public class InfinityChestMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SLOT_START = 36;
    public static final int CONTAINER_SLOT_SIZE = 15 * 9;
    public final ChestHandler chest;
    public final Player player;
    public final Level level;
    public final BlockPos blockPos;

    public InfinityChestTile chestTile;
    public InfinityChestContainer chestContainer = new InfinityChestContainer(this);
    public boolean LShifting = false;

    public final UUID owner;
    public boolean locked;
    public String filter;
    public byte sortType;
    public UUID channelID;


    //???????
    public InfinityChestMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ModMenus.infinity_chest.get(), containerId);
        this.player = playerInv.player;
        this.level = playerInv.player.level();
        this.blockPos = extraData.readBlockPos();

        this.owner = extraData.readUUID();
        this.locked = extraData.readBoolean();
        this.filter = extraData.readUtf(64);
        this.sortType = extraData.readByte();
        this.channelID = extraData.readUUID();

        addSlots(playerInv.player, playerInv);

        this.chestContainer = new InfinityChestContainer(this);
        this.chest = ClientChestManager.getInstance().getChest(chestContainer);
        //???????41 ~ 118
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 15; j++) {
                this.addSlot(new FakeSlot(chestContainer, i * 15 + j, 8 + j * 18, 16 + i * 18)
                {
                    @Override
                    public ItemStack getItem()
                    {
                        ItemStack itemStack = super.getItem();
                        itemStack.setCount(1);
                        return itemStack;
                    }
                });
            }
        }
    }

    //??????
    public InfinityChestMenu(int containerId, Player player, InfinityChestTile blockEntity) {
        super(ModMenus.infinity_chest.get(), containerId);
        this.player = player;
        this.level = player.level();
        this.blockPos = blockEntity.getBlockPos();
        this.chestTile = blockEntity;

        this.owner = blockEntity.getOwner() == null ? player.getUUID() : blockEntity.getOwner();
        this.locked = blockEntity.isLocked();
        this.filter = blockEntity.getFilter();
        this.sortType = blockEntity.getSortType();
        this.channelID = blockEntity.getChannelID();
        this.chest = blockEntity.getChannel();
        if (!chest.isRemoved() && player instanceof ServerPlayer serverPlayer) {
            ((ServerChestHandler) this.chest).addListener(serverPlayer);
        }
        addSlots(player, player.getInventory());
    }


    private void addSlots(Player player, Inventory playerInv) {
        //???0~8
        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInv, l, 62 + l * 18, 250));
        }

        //??9~35
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInv, i1 + k * 9 + 9, 62 + i1 * 18, 192 + k * 18));
            }
        }
    }

    //????
    @Override
    @ParametersAreNonnullByDefault
    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> {
                if (owner.equals(player.getUUID())) {
                    locked = !locked;
                    chestTile.setLocked(locked);
                    if (locked) saveBlock();
                }
            }
            case 1 -> nextSort();
            case 2 -> reverseSort();
        }
        return pId < 3;
    }


    //????
    public void action(int actionId, String id) {
        switch (actionId) {
            case StorageUtils.Action.LEFT_CLICK_DUMMY_SLOT -> onLeftClickDummySlot(id);
            case StorageUtils.Action.Right_CLICK_DUMMY_SLOT -> onRightClickDummySlot(id);
            case StorageUtils.Action.LEFT_SHIFT_DUMMY_SLOT -> onLeftShiftDummySlot(id);
            case StorageUtils.Action.Right_SHIFT_DUMMY_SLOT -> onRightShiftDummySlot(id);
            case StorageUtils.Action.THROW_ONE -> tryThrowOneFromDummySlot(id);
            case StorageUtils.Action.THROW_STICK -> tryThrowStickFromDummySlot(id);
            case StorageUtils.Action.LEFT_DRAG -> onLeftDragDummySlot(id);
            case StorageUtils.Action.RIGHT_DRAG -> onRightDragDummySlot(id);
            case StorageUtils.Action.CLONE -> onCloneFormDummySlot(id);
            case StorageUtils.Action.DRAG_CLONE -> onDragCloneDummySlot(id);
        }
    }

    public void onLeftClickDummySlot(String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            setCarried(chest.saveTakeItem(id, false));
        } else {
            //????1?????????????
            if (carried.getCount() > 1) {
                chest.addItem(carried);
                return;
            }
            {

                //????
                AtomicBoolean canal = new AtomicBoolean(false);

                carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                    if (!chest.storageItems.containsKey(id)) return;
                    int slots = iItemHandler.getSlots();
                    for (int i = 0; i < slots; i++) {
                        ItemStack tryInsertItem = new ItemStack(StorageUtils.getItem(id));
                        if (!ItemStack.isSameItemSameTags(tryInsertItem, iItemHandler.getStackInSlot(i)) && !iItemHandler.getStackInSlot(i).isEmpty())
                            continue;
                        int remainingSlotSpace = iItemHandler.getSlotLimit(i) - iItemHandler.getStackInSlot(i).getCount();
                        if (remainingSlotSpace <= 0) continue;
                        int transmitAmount = (int) Math.min(Integer.MAX_VALUE, chest.storageItems.get(id) / 2);
                        transmitAmount = Math.max(transmitAmount, 64000);
                        transmitAmount = (int) Math.min(transmitAmount, chest.storageItems.get(id));
                        transmitAmount = Math.min(transmitAmount, remainingSlotSpace);
                        int markAmount = transmitAmount;
                        tryInsertItem.setCount(transmitAmount);
                        for (int j = 0; j < 64; j++) {
                            ItemStack remainingItem = iItemHandler.insertItem(i, tryInsertItem, false);
                            transmitAmount = remainingItem.getCount();
                            if (transmitAmount <= 0) break;
                            tryInsertItem.setCount(transmitAmount);
                        }
                        markAmount -= transmitAmount;
                        if (markAmount > 0) {
                            chest.takeItem(id, markAmount);
                            canal.set(true);
                            return;
                        }
                    }
                });
                if (canal.get()) return;
                chest.addItem(carried);
            }
        }
    }

    public void onRightClickDummySlot(String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            setCarried(chest.saveTakeItem(id, true));
        } else {
            if (carried.getCount() > 1) {
                chest.fillItemStack(carried, -1);
                return;
            }
            AtomicBoolean canal = new AtomicBoolean(false);
            carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                int slots = iItemHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack itemStack = iItemHandler.getStackInSlot(i);
                    if (itemStack.isEmpty() || itemStack.hasTag()) continue;
                    int maxExtractAmount = chest.canStorageAmount(itemStack);
                    itemStack = iItemHandler.extractItem(i, maxExtractAmount, false);
                    if (itemStack.isEmpty()) continue;
                    chest.addItem(itemStack);
                    canal.set(true);
                    break;
                }
            });
            if (canal.get()) return;
            chest.addItem(carried);
        }
    }

    public void onLeftShiftDummySlot(String id) {
        if (id.equals("minecraft:air")) return;
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (!chest.storageItems.containsKey(id)) return;
            ItemStack itemStack = new ItemStack(StorageUtils.getItem(id));
            itemStack.setCount((int) Math.min(itemStack.getMaxStackSize(), chest.storageItems.get(id)));
            int i = itemStack.getCount();
            moveItemStackTo(itemStack, 41, 50, false);
            i = i - itemStack.getCount();
            if (i > 0) {
                itemStack.setCount(i);
                chest.removeItem(itemStack);
            }

        } else {
            if (carried.getCount() > 1) {
                chest.fillItemStack(carried, -1);
                return;
            }
            {
                AtomicBoolean canal = new AtomicBoolean(false);

                carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                    if (!chest.storageItems.containsKey(id)) return;
                    int transmitAmount = (int) Math.min(Integer.MAX_VALUE, chest.storageItems.get(id) / 2);
                    transmitAmount = Math.max(transmitAmount, 64000);
                    transmitAmount = (int) Math.min(transmitAmount, chest.storageItems.get(id));
                    int markAmount = transmitAmount;
                    ItemStack tryInsertItem = new ItemStack(StorageUtils.getItem(id), transmitAmount);
                    int slots = iItemHandler.getSlots();
                    for (int i = 0; i < slots; i++) {
                        for (int j = 0; j < 64; j++) {
                            ItemStack remainingItem = iItemHandler.insertItem(i, tryInsertItem, false);
                            if (remainingItem.getCount() == transmitAmount) break;
                            transmitAmount = remainingItem.getCount();
                            if (transmitAmount <= 0) break;
                            tryInsertItem.setCount(transmitAmount);
                        }
                    }
                    markAmount -= transmitAmount;
                    if (markAmount > 0) {
                        chest.takeItem(id, markAmount);
                        canal.set(true);
                    }
                });
                if (canal.get()) return;
                chest.addItem(carried);
            }
        }
    }

    public void onRightShiftDummySlot(String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (!chest.storageItems.containsKey(id)) return;
            ItemStack itemStack = new ItemStack(StorageUtils.getItem(id));
            moveItemStackTo(itemStack, 41, 50, false);
            if (itemStack.isEmpty()) chest.takeItem(id, 1);
        } else {
            if (carried.getCount() > 1) {
                chest.fillItemStack(carried, -1);
                return;
            }
            AtomicBoolean canal = new AtomicBoolean(false);
            carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                int slots = iItemHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack itemStack = iItemHandler.getStackInSlot(i);
                    if (itemStack.isEmpty() || itemStack.hasTag()) continue;
                    int maxExtractAmount = chest.canStorageAmount(itemStack);
                    itemStack = iItemHandler.extractItem(i, maxExtractAmount, false);
                    if (itemStack.isEmpty()) continue;
                    chest.addItem(itemStack);
                    canal.set(true);
                }
            });
            if (canal.get()) return;
            chest.addItem(carried);
        }
    }

    public void tryThrowOneFromDummySlot(String id) {
        if (id.equals("minecraft:air")) return;
        if (!chest.storageItems.containsKey(id)) return;
        ItemStack itemStack = chest.takeItem(id, 1);
        player.drop(itemStack, false);
    }

    public void tryThrowStickFromDummySlot(String id) {
        if (id.equals("minecraft:air")) return;
        if (!chest.storageItems.containsKey(id)) return;
        ItemStack itemStack = chest.saveTakeItem(id, false);
        player.drop(itemStack, false);
    }

    public void onLeftDragDummySlot(String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        chest.addItem(carried);
    }

    public void onRightDragDummySlot(String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        chest.fillItemStack(carried, -1);
    }

    public void onCloneFormDummySlot(String id) {
        if (id.equals("minecraft:air") || !player.isCreative()) return;
        chest.addItem(id, Long.max(chest.getRealItemAmount(id), 64L));
    }

    public void onDragCloneDummySlot(String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        ItemStack itemStack = carried.copy();
        itemStack.setCount(itemStack.getMaxStackSize());
        chest.addItem(itemStack);
    }

    public void nextSort() {
        sortType += 2;
        if (sortType > 7) sortType %= 8;
        if (level.isClientSide) chestContainer.refreshContainer(true);
    }

    public void reverseSort() {
        if (sortType % 2 == 0) sortType++;
        else sortType--;
        if (level.isClientSide) chestContainer.refreshContainer(true);
    }

    private void saveBlock() {
        chestTile.setFilter(filter);
        chestTile.setSortType(sortType);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        if (pSlotId >= CONTAINER_SLOT_START) {
            //???????
            String object;
            if (pSlotId - CONTAINER_SLOT_START < chestContainer.viewingObject.size())
                object = chestContainer.viewingObject.get(pSlotId - CONTAINER_SLOT_START);
            else object = "minecraft:air";

            switch (pButton) {
                case 0 -> {
                    switch (pClickType) {
                        case QUICK_MOVE -> {
                            //??shift
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SInfinityChestActionPack(containerId, StorageUtils.Action.LEFT_SHIFT_DUMMY_SLOT, object));
                            onLeftShiftDummySlot(object);
                        }
                        case PICKUP -> {
                            //????
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SInfinityChestActionPack(containerId, StorageUtils.Action.LEFT_CLICK_DUMMY_SLOT, object));
                            onLeftClickDummySlot(object);
                        }
                        case THROW -> {
                            //???
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SInfinityChestActionPack(containerId, StorageUtils.Action.THROW_ONE, object));
                            tryThrowOneFromDummySlot(object);
                        }
                    }
                }
                case 1 -> {
                    switch (pClickType) {
                        case PICKUP -> {
                            //????
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SInfinityChestActionPack(containerId, StorageUtils.Action.Right_CLICK_DUMMY_SLOT, object));
                            onRightClickDummySlot(object);
                        }
                        case QUICK_MOVE -> {
                            //??shift ?????
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SInfinityChestActionPack(containerId, StorageUtils.Action.Right_SHIFT_DUMMY_SLOT, object));
                            onRightShiftDummySlot(object);
                        }
                        case QUICK_CRAFT -> {
                            //????
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SInfinityChestActionPack(containerId, StorageUtils.Action.LEFT_DRAG, object));
                            onLeftDragDummySlot(object);
                        }
                        case THROW -> {
                            //???
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SInfinityChestActionPack(containerId, StorageUtils.Action.THROW_STICK, object));
                            tryThrowStickFromDummySlot(object);
                        }
                    }
                }
                case 4 -> {
                    if (pClickType == ClickType.CLONE) {
                        //??
                        if (object.equals("minecraft:air")) return;
                        NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SInfinityChestActionPack(containerId, StorageUtils.Action.CLONE, object));
                        onCloneFormDummySlot(object);
                    }
                }
                case 5 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //????
                        NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SInfinityChestActionPack(containerId, StorageUtils.Action.RIGHT_DRAG, object));
                        onRightDragDummySlot(object);
                    }
                }
                case 9 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //????
                        NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SInfinityChestActionPack(containerId, StorageUtils.Action.DRAG_CLONE, object));
                        onDragCloneDummySlot(object);
                    }
                }
            }
            //???SWAP???(hot bar????)
        } else super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull ItemStack quickMoveStack(Player player, int slotId) {
        //empty???????????
        ItemStack itemStack = ItemStack.EMPTY;
        if (slotId < 0 || slotId >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            ItemStack movingStack = slot.getItem();
            itemStack = movingStack.copy();
            if (slotId >=0 && slotId <= 35) {
                chest.addItem(movingStack);
                return ItemStack.EMPTY;
            } else {
                Const.LOGGER.warn("Ohh! Who trigger the quickMoveStack() when slotId >= 36 + CONTAINER_SLOT_SIZE in server side ?");
            }

            if (movingStack.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (movingStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, movingStack);
        }
        return itemStack;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.index <= 35;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean stillValid(Player player) {
        return (chestTile == null || !chestTile.isRemoved()) &&
                player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void removed(Player player) {
        super.removed(player);
        if (level.isClientSide || chestTile == null) {
            return;
        }
        if (!chest.isRemoved() && player instanceof ServerPlayer serverPlayer) {
            ((ServerChestHandler) chest).removeListener(serverPlayer);
        }
        if (!chestTile.isLocked()) {
            saveBlock();
        }
    }
}
