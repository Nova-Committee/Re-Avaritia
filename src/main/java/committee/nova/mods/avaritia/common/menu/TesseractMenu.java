package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.slot.FakeSlot;
import committee.nova.mods.avaritia.api.util.game.CraftingRecipeGridIndexGetter;
import committee.nova.mods.avaritia.api.util.math.InvItemCounter;
import committee.nova.mods.avaritia.common.container.DummyChannelContainer;
import committee.nova.mods.avaritia.common.menu.provider.ChannelSelectMenuProvider;
import committee.nova.mods.avaritia.common.net.channel.C2SChannelActionPack;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import committee.nova.mods.avaritia.core.channel.*;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.util.StorageUtils;
import committee.nova.mods.avaritia.util.StorageUtils.Action;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/4 14:41
 * @Description:
 */
public class TesseractMenu extends AbstractContainerMenu {

    public final Channel channel;
    public final UUID owner;
    public final Player player;
    public final Level level;
    private final BlockPos blockPos;
    /**
     * 便携终端所在物品槽位
     */
    private final int panelItemSlotIndex;
    private final ItemStack panelItem;
    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    public DummyChannelContainer dummyChannelContainer = new DummyChannelContainer(this);
    public TesseractTile tesseractTile;
    public boolean locked;
    public UUID channelOwner;
    public int channelID;
    public boolean craftingMode;
    public String filter;
    public byte sortType;
    public byte viewType;
    public boolean LShifting = false;
    public Runnable craftModeSetter = () -> {
    };
    private CraftingRecipe lastCraftingRecipe = null;


    //客户端调用这个
    public TesseractMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ModMenus.tesseract.get(), containerId);
        this.level = playerInv.player.level();
        this.player = playerInv.player;

        this.blockPos = extraData.readBlockPos();
        this.panelItemSlotIndex = extraData.readInt();

        this.owner = extraData.readUUID();
        this.locked = extraData.readBoolean();
        this.craftingMode = extraData.readBoolean();
        this.filter = extraData.readUtf(64);
        this.sortType = extraData.readByte();
        this.viewType = extraData.readByte();
        this.channelOwner = extraData.readUUID();
        this.channelID = extraData.readInt();

        if (panelItemSlotIndex >= 0) this.panelItem = player.getInventory().getItem(panelItemSlotIndex);
        else this.panelItem = ItemStack.EMPTY;

        addSlots(playerInv.player, playerInv);
        this.channel = ClientChannelManager.getInstance().getChannel(dummyChannelContainer);
        //虚拟储存物品格51 ~ 149
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 11; j++) {
                this.addSlot(new FakeSlot(dummyChannelContainer, i * 11 + j, 7 + j * 17, 17 + i * 17));
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 11; j++) {
                this.addSlot(new FakeSlot(dummyChannelContainer, 77 + i * 11 + j, 7 + j * 17, 136 + i * 17) {
                    @Override
                    public boolean isActive() {
                        return !craftingMode;
                    }
                });
            }
        }
    }

    //服务端用这个
    public TesseractMenu(int containerId, Player player, TesseractTile blockEntity, int panelItemSlotIndex) {
        super(ModMenus.tesseract.get(), containerId);
        this.level = player.level();
        this.player = player;
        this.panelItemSlotIndex = panelItemSlotIndex;

        if (panelItemSlotIndex >= 0) {
            this.blockPos = BlockPos.ZERO;
            this.tesseractTile = null;
            this.panelItem = player.getInventory().getItem(panelItemSlotIndex);
            CompoundTag nbt = panelItem.getOrCreateTag();
            this.owner = nbt.contains("owner") ? nbt.getUUID("owner") : player.getUUID();
            this.locked = nbt.getBoolean("locked");
            this.craftingMode = nbt.getBoolean("craftingMode");
            this.filter = nbt.getString("filter");
            this.sortType = nbt.getByte("sortType");
            this.viewType = nbt.getByte("viewType");
            CompoundTag channel = nbt.getCompound("channel");
            if (!channel.isEmpty()) {
                this.channelOwner = channel.getUUID("channelOwner");
                this.channelID = channel.getInt("channelID");
            }
        } else {
            this.blockPos = blockEntity.getBlockPos();
            this.tesseractTile = blockEntity;
            this.owner = blockEntity.getOwner() == null ? player.getUUID() : blockEntity.getOwner();
            this.locked = blockEntity.isLocked();
            this.craftingMode = blockEntity.isCraftingMode();
            this.filter = blockEntity.getFilter();
            this.sortType = blockEntity.getSortType();
            this.viewType = blockEntity.getViewType();
            this.channelOwner = blockEntity.getChannelOwner();
            this.channelID = blockEntity.getChannelID();
            this.panelItem = ItemStack.EMPTY;
        }

        this.channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
        if (!channel.isRemoved()) ((ServerChannel) this.channel).addListener((ServerPlayer) player);

        addSlots(player, player.getInventory());
    }

    //按钮相关
    @Override
    @ParametersAreNonnullByDefault
    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> {
                if (owner.equals(player.getUUID()) || owner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
                    locked = !locked;
                    if (panelItemSlotIndex >= 0) {
                        CompoundTag nbt = panelItem.getTag();
                        //locked 的空值检测在物品上，保证到menu的不会空
                        nbt.putBoolean("locked", locked);
                        if (locked) {
                            nbt.putBoolean("craftingMode", craftingMode);
                            nbt.putString("filter", filter);
                            nbt.putInt("sortType", sortType);
                        }
                        panelItem.setTag(nbt);
                    } else {
                        tesseractTile.setLocked(locked);
                        if (locked) saveBlock();
                    }
                }
            }
            case 1 -> craftingMode = !craftingMode;
            case 2 -> nextSort();
            case 3 -> reverseSort();
            case 4 -> changeViewType();
            case 5 -> openChannelScreen();
            case 6 -> craftToChannel(1);
            case 7 -> craftToChannel(8);
            case 8 -> craftToChannel(64);
            case 9 -> craftToChannel(512);
            case 10 -> craftToInventory(1);
            case 11 -> craftToInventory(8);
            case 12 -> craftToInventory(64);
            case 13 -> craftToInventory(512);
            case 14 -> craftAndDrop(1);
            case 15 -> craftAndDrop(8);
            case 16 -> craftAndDrop(64);
            case 17 -> craftAndDrop(512);
        }
        return pId < 18;
    }


    //本类方法
    public void action(int actionId, String type, String id) {
        switch (actionId) {
            case Action.LEFT_CLICK_DUMMY_SLOT -> onLeftClickDummySlot(type, id);
            case Action.Right_CLICK_DUMMY_SLOT -> onRightClickDummySlot(type, id);
            case Action.LEFT_SHIFT_DUMMY_SLOT -> onLeftShiftDummySlot(type, id);
            case Action.Right_SHIFT_DUMMY_SLOT -> onRightShiftDummySlot(type, id);
            case Action.THROW_ONE -> tryThrowOneFromDummySlot(type, id);
            case Action.THROW_STICK -> tryThrowStickFromDummySlot(type, id);
            case Action.LEFT_DRAG -> onLeftDragDummySlot(type, id);
            case Action.RIGHT_DRAG -> onRightDragDummySlot(type, id);
            case Action.CLONE -> onCloneFormDummySlot(type, id);
            case Action.DRAG_CLONE -> onDragCloneDummySlot(type, id);
        }
    }

    public void onLeftClickDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (type.equals("item")) setCarried(channel.saveTakeItem(id, false));
            else if (type.equals("fluid")) {
                if (!channel.storageFluids.containsKey(id)) return;
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket"))
                    return;
                FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                channel.takeItem("minecraft:bucket", 1);
                setCarried(fluidBucket);
            }
        } else {
            //叠堆大于1不处理特殊操作，防止意外。
            if (carried.getCount() > 1) {
                channel.addItem(carried);
                return;
            }
            //特殊操作，比如取液体.
            //原版桶
            if (type.equals("fluid") && carried.getItem().equals(Items.BUCKET)) {
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME) return;
                FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                setCarried(fluidBucket);
            } else {
//                if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) {
//                    channel.addItem(carried);
//                    return;
//                }
                //其他容器
                AtomicBoolean canal = new AtomicBoolean(false);
                if (type.equals("fluid"))
                    carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                        if (!channel.storageFluids.containsKey(id)) return;
                        FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), (int) Math.min(FluidType.BUCKET_VOLUME, channel.storageFluids.get(id)));
                        int filledAmount = iFluidHandlerItem.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        if (filledAmount != 0) {
                            boolean succeedInput = true;
                            int tanks = iFluidHandlerItem.getTanks();
                            ItemStack testItem = carried.copy();
                            AtomicReference<FluidStack> testFluid = new AtomicReference<>(FluidStack.EMPTY);
                            for (int i = 0; i < tanks; i++) {
                                int finalI = i;
                                testItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(testFluidHandlerItem ->
                                        testFluid.set(testFluidHandlerItem.getFluidInTank(
                                                finalI)));
                                if (!testFluid.get().isFluidStackIdentical(iFluidHandlerItem.getFluidInTank(i))) {
                                    succeedInput = false;
                                    setCarried(getCarried().copy());
                                    break;
                                }
                            }
                            if (succeedInput) channel.takeFluid(id, filledAmount);
                        }
                        canal.set(true);
                    });
                else if (type.equals("energy") && id.equals("blackholestorage:forge_energy"))
                    carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                        if (!iEnergyStorage.canReceive() || channel.getFEAmount() == 0) return;
                        int maxInputAmount = Math.min(1000000, channel.getFEAmount());
                        int receiveEnergy = iEnergyStorage.receiveEnergy(maxInputAmount, false);
                        if (receiveEnergy == 0) return;
                        channel.removeEnergy((long) receiveEnergy);
                        canal.set(true);
                    });
                else if (type.equals("item"))
                    carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                        //TODO：这里需要做与流体一样的正确性检查防止刷物品，但需要一个有这个问题的容器才能做测试。
                        if (!channel.storageItems.containsKey(id)) return;
                        int slots = iItemHandler.getSlots();
                        for (int i = 0; i < slots; i++) {
                            ItemStack tryInsertItem = new ItemStack(StorageUtils.getItem(id));
                            if (!ItemStack.isSameItemSameTags(tryInsertItem, iItemHandler.getStackInSlot(i)) && !iItemHandler.getStackInSlot(i).isEmpty())
                                continue;
                            int remainingSlotSpace = iItemHandler.getSlotLimit(i) - iItemHandler.getStackInSlot(i).getCount();
                            if (remainingSlotSpace <= 0) continue;
                            int transmitAmount = (int) Math.min(Integer.MAX_VALUE, channel.storageItems.get(id) / 2);
                            transmitAmount = Math.max(transmitAmount, 64000);
                            transmitAmount = (int) Math.min(transmitAmount, channel.storageItems.get(id));
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
                                channel.takeItem(id, markAmount);
                                canal.set(true);
                                return;
                            }
                        }
                    });
                if (canal.get()) return;
                channel.addItem(carried);
            }
        }
    }

    public void onRightClickDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (type.equals("item")) setCarried(channel.saveTakeItem(id, true));
            if (type.equals("fluid")) {
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket"))
                    return;
                FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                channel.takeItem("minecraft:bucket", 1);
                setCarried(fluidBucket);
            }
        } else {
            if (carried.getCount() > 1) {
                channel.fillItemStack(carried, -1);
                return;
            }
            //if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) return;
            AtomicBoolean canal = new AtomicBoolean(false);
            carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                FluidStack resultFluidStack = iFluidHandlerItem.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
                if (iFluidHandlerItem instanceof FluidBucketWrapper) {
                    channel.addFluid(resultFluidStack);
                    setCarried(new ItemStack(Items.BUCKET));
                    canal.set(true);
                    return;
                }
                if (!resultFluidStack.isEmpty()) {
                    int canStoredAmount = channel.canStorageAmount(resultFluidStack);
                    if (canStoredAmount > 0) {
                        resultFluidStack.setAmount(Math.min(resultFluidStack.getAmount(), canStoredAmount));
                        resultFluidStack = iFluidHandlerItem.drain(resultFluidStack, IFluidHandler.FluidAction.EXECUTE);
                        if (!resultFluidStack.isEmpty()) {
                            int tanks = iFluidHandlerItem.getTanks();
                            ItemStack testItem = carried.copy();
                            AtomicReference<FluidStack> testFluid = new AtomicReference<>(FluidStack.EMPTY);
                            for (int i = 0; i < tanks; i++) {
                                int finalI = i;
                                testItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(testFluidHandlerItem ->
                                        testFluid.set(
                                                testFluidHandlerItem.getFluidInTank(
                                                        finalI)));
                                if (!testFluid.get().isFluidStackIdentical(iFluidHandlerItem.getFluidInTank(i))) {
                                    setCarried(getCarried().copy());
                                    return;
                                }
                            }
                            channel.addFluid(resultFluidStack);
                            canal.set(true);
                        }
                    }
                }
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                if (!iEnergyStorage.canExtract() || iEnergyStorage.getEnergyStored() == 0) return;
                int extractEnergy = iEnergyStorage.extractEnergy(Math.min(1000000, channel.canStorageFEAmount()), false);
                if (extractEnergy == 0) return;
                channel.addEnergy(extractEnergy);
                canal.set(true);
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                int slots = iItemHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack itemStack = iItemHandler.getStackInSlot(i);
                    if (itemStack.isEmpty() || itemStack.hasTag()) continue;
                    int maxExtractAmount = channel.canStorageAmount(itemStack);
                    itemStack = iItemHandler.extractItem(i, maxExtractAmount, false);
                    if (itemStack.isEmpty()) continue;
                    channel.addItem(itemStack);
                    canal.set(true);
                    break;
                }
            });
            if (canal.get()) return;
            channel.addItem(carried);
        }
    }

    public void onLeftShiftDummySlot(String type, String id) {
        if (id.equals("minecraft:air")) return;
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (type.equals("item")) {
                if (!channel.storageItems.containsKey(id)) return;
                ItemStack itemStack = new ItemStack(StorageUtils.getItem(id));
                itemStack.setCount((int) Math.min(itemStack.getMaxStackSize(), channel.storageItems.get(id)));
                int i = itemStack.getCount();
                if (craftingMode) moveItemStackTo(itemStack, 41, 50, false);
                else moveItemStackTo(itemStack, 0, 36, false);
                i = i - itemStack.getCount();
                if (i > 0) {
                    itemStack.setCount(i);
                    channel.removeItem(itemStack);
                }
            } else if (type.equals("fluid")) {
                if (!channel.storageFluids.containsKey(id)) return;
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket"))
                    return;
                FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                if (craftingMode) moveItemStackTo(fluidBucket, 41, 50, false);
                else moveItemStackTo(fluidBucket, 0, 36, false);
                if (fluidBucket.isEmpty()) {
                    channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                    channel.takeItem("minecraft:bucket", 1);
                }
            }
        } else {
            if (carried.getCount() > 1) {
                channel.fillItemStack(carried, -1);
                return;
            }
            if (type.equals("fluid") && carried.getItem().equals(Items.BUCKET)) {
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME) return;
                FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                setCarried(fluidBucket);
            } else {
                //if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) return;
                AtomicBoolean canal = new AtomicBoolean(false);
                //取一大堆，下限堆大小为64k桶，上限为存量的一半，防止败家行为。
                if (type.equals("fluid"))
                    carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                        if (!channel.storageFluids.containsKey(id)) return;
                        int tanks = iFluidHandlerItem.getTanks();
                        for (int i = 0; i < tanks; i++) {
                            FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1000);
                            if (!fluidStack.isFluidEqual(iFluidHandlerItem.getFluidInTank(i)) && !iFluidHandlerItem.getFluidInTank(i).isEmpty())
                                continue;
                            int remainingTankSpace = iFluidHandlerItem.getTankCapacity(i) - iFluidHandlerItem.getFluidInTank(i).getAmount();
                            if (remainingTankSpace <= 0) continue;
                            int transmitAmount = (int) Math.min(Integer.MAX_VALUE, channel.storageFluids.get(id) / 2);
                            transmitAmount = Math.max(transmitAmount, 64000000);
                            transmitAmount = (int) Math.min(transmitAmount, channel.storageFluids.get(id));
                            transmitAmount = Math.min(transmitAmount, remainingTankSpace);
                            int markAmount = transmitAmount;
                            fluidStack.setAmount(transmitAmount);
                            for (int j = 0; j < 1024; j++) {
                                int filledAmount = iFluidHandlerItem.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                if (j == 0) {
                                    ItemStack testItem = carried.copy();
                                    AtomicReference<FluidStack> testFluid = new AtomicReference<>(FluidStack.EMPTY);
                                    for (int k = 0; k < tanks; k++) {
                                        int finalI = k;
                                        testItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(testFluidHandlerItem ->
                                                testFluid.set(
                                                        testFluidHandlerItem.getFluidInTank(
                                                                finalI)));
                                        if (!testFluid.get().isFluidStackIdentical(iFluidHandlerItem.getFluidInTank(i))) {
                                            filledAmount = 0;
                                            setCarried(getCarried().copy());
                                            break;
                                        }
                                    }
                                }
                                if (filledAmount == 0) break;
                                transmitAmount -= filledAmount;
                                if (transmitAmount <= 0) break;
                                fluidStack.setAmount(transmitAmount);
                            }
                            markAmount -= transmitAmount;
                            if (markAmount > 0) {
                                channel.takeFluid(id, markAmount);
                                canal.set(true);
                            }
                            return;
                        }
                    });
                    //但电不需要防败家，因为不缺嘿嘿嘿。
                else if (type.equals("energy") && id.equals("blackholestorage:forge_energy"))
                    carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                        if (!iEnergyStorage.canReceive() || channel.getFEAmount() == 0) return;
                        int maxInputAmount = channel.getFEAmount();
                        int markAmount = maxInputAmount;
                        for (int i = 0; i < 1024; i++) {
                            int receiveEnergy = iEnergyStorage.receiveEnergy(maxInputAmount, false);
                            if (receiveEnergy == 0) break;
                            maxInputAmount -= receiveEnergy;
                            if (maxInputAmount == 0) break;
                        }
                        markAmount -= maxInputAmount;
                        if (markAmount > 0) {
                            channel.removeEnergy((long) markAmount);
                            canal.set(true);
                        }
                    });
                else if (type.equals("item"))
                    carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                        if (!channel.storageItems.containsKey(id)) return;
                        int transmitAmount = (int) Math.min(Integer.MAX_VALUE, channel.storageItems.get(id) / 2);
                        transmitAmount = Math.max(transmitAmount, 64000);
                        transmitAmount = (int) Math.min(transmitAmount, channel.storageItems.get(id));
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
                            channel.takeItem(id, markAmount);
                            canal.set(true);
                        }
                    });
                if (canal.get()) return;
                channel.addItem(carried);
            }
        }
    }

    public void onRightShiftDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (type.equals("item")) {
                if (!channel.storageItems.containsKey(id)) return;
                ItemStack itemStack = new ItemStack(StorageUtils.getItem(id));
                if (craftingMode) moveItemStackTo(itemStack, 41, 50, false);
                else moveItemStackTo(itemStack, 0, 36, false);
                if (itemStack.isEmpty()) channel.takeItem(id, 1);
            } else if (type.equals("fluid")) {
                if (!channel.storageFluids.containsKey(id)) return;
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket"))
                    return;
                FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                if (craftingMode) moveItemStackTo(fluidBucket, 41, 50, false);
                else moveItemStackTo(fluidBucket, 0, 36, false);
                if (fluidBucket.isEmpty()) {
                    channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                    channel.takeItem("minecraft:bucket", 1);
                }
            }
        } else {
            if (carried.getCount() > 1) {
                channel.fillItemStack(carried, -1);
                return;
            }
            // if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) return;
            AtomicBoolean canal = new AtomicBoolean(false);
            carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                FluidStack resultFluidStack = iFluidHandlerItem.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
                if (iFluidHandlerItem instanceof FluidBucketWrapper) {
                    channel.addFluid(resultFluidStack);
                    setCarried(new ItemStack(Items.BUCKET));
                    return;
                }
                if (!resultFluidStack.isEmpty()) {
                    String fluid = StorageUtils.getFluidId(resultFluidStack.getFluid());
                    long canStoredAmount = channel.canStorageRealAmount(resultFluidStack);
                    if (canStoredAmount > 0L) {
                        long removedAmount = 0L;
                        //规避限速，最大循环1024次，防止过度循环。
                        for (int i = 0; i < 1024; i++) {
                            resultFluidStack.setAmount((int) Math.min(Integer.MAX_VALUE, canStoredAmount));
                            resultFluidStack = iFluidHandlerItem.drain(resultFluidStack, IFluidHandler.FluidAction.EXECUTE);
                            if (resultFluidStack.isEmpty()) break;
                            if (i == 0) {
                                int tanks = iFluidHandlerItem.getTanks();
                                ItemStack testItem = carried.copy();
                                AtomicReference<FluidStack> testFluid = new AtomicReference<>(FluidStack.EMPTY);
                                for (int j = 0; j < tanks; j++) {
                                    int finalI = j;
                                    testItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(testFluidHandlerItem ->
                                            testFluid.set(
                                                    testFluidHandlerItem.getFluidInTank(
                                                            finalI)));
                                    if (!testFluid.get().isFluidStackIdentical(iFluidHandlerItem.getFluidInTank(j))) {
                                        setCarried(getCarried().copy());
                                        return;
                                    }
                                }
                            }
                            canStoredAmount -= resultFluidStack.getAmount();
                            removedAmount += resultFluidStack.getAmount();
                        }
                        if (removedAmount > 0) {
                            channel.addFluid(fluid, removedAmount);
                            canal.set(true);
                        }
                    }
                }
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                if (!iEnergyStorage.canExtract() || iEnergyStorage.getEnergyStored() == 0) return;
                int maxRemoveAmount = channel.canStorageFEAmount();
                int markAmount = maxRemoveAmount;
                for (int i = 0; i < 1024; i++) {
                    int extractEnergy = iEnergyStorage.extractEnergy(Math.min(iEnergyStorage.getEnergyStored(), maxRemoveAmount), false);
                    if (extractEnergy == 0) break;
                    maxRemoveAmount -= extractEnergy;
                    if (iEnergyStorage.getEnergyStored() <= 0) break;
                    if (maxRemoveAmount <= 0) break;
                }
                markAmount -= maxRemoveAmount;
                if (markAmount > 0) {
                    channel.addEnergy(markAmount);
                    canal.set(true);
                }
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                int slots = iItemHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack itemStack = iItemHandler.getStackInSlot(i);
                    if (itemStack.isEmpty() || itemStack.hasTag()) continue;
                    int maxExtractAmount = channel.canStorageAmount(itemStack);
                    itemStack = iItemHandler.extractItem(i, maxExtractAmount, false);
                    if (itemStack.isEmpty()) continue;
                    channel.addItem(itemStack);
                    canal.set(true);
                }
            });
            if (canal.get()) return;
            channel.addItem(carried);
        }
    }

    public void tryThrowOneFromDummySlot(String type, String id) {
        if (id.equals("minecraft:air")) return;
        if (type.equals("item")) {
            if (!channel.storageItems.containsKey(id)) return;
            ItemStack itemStack = channel.takeItem(id, 1);
            player.drop(itemStack, false);
        } else if (type.equals("fluid")) {
            //笑死，对于流体这种空槽根本不触发扔事件。
            if (!channel.storageFluids.containsKey(id)) return;
            if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket"))
                return;
            FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
            ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
            if (fluidBucket.isEmpty()) return;
            channel.takeFluid(id, FluidType.BUCKET_VOLUME);
            channel.takeItem("minecraft:bucket", 1);
            player.drop(fluidBucket, false);
        }
    }

    public void tryThrowStickFromDummySlot(String type, String id) {
        if (id.equals("minecraft:air")) return;
        if (type.equals("item")) {
            if (!channel.storageItems.containsKey(id)) return;
            ItemStack itemStack = channel.saveTakeItem(id, false);
            player.drop(itemStack, false);
        } else if (type.equals("fluid")) {
            if (!channel.storageFluids.containsKey(id)) return;
            if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket"))
                return;
            FluidStack fluidStack = new FluidStack(StorageUtils.getFluid(id), 1);
            ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
            if (fluidBucket.isEmpty()) return;
            channel.takeFluid(id, FluidType.BUCKET_VOLUME);
            channel.takeItem("minecraft:bucket", 1);
            player.drop(fluidBucket, false);
        }
    }

    public void onLeftDragDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        channel.addItem(carried);
    }

    public void onRightDragDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        channel.fillItemStack(carried, -1);
    }

    public void onCloneFormDummySlot(String type, String id) {
        if (id.equals("minecraft:air") || !player.isCreative()) return;
        switch (type) {
            case "item" -> channel.addItem(id, Long.max(channel.getRealItemAmount(id), 64L));
            case "fluid" -> channel.addFluid(id, Long.max(channel.getRealFluidAmount(id), 1000L));
            case "energy" -> channel.addEnergy(id, Long.max(channel.getRealEnergyAmount(id), 1000L));
        }
    }

    public void onDragCloneDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        ItemStack itemStack = carried.copy();
        itemStack.setCount(itemStack.getMaxStackSize());
        channel.addItem(itemStack);
    }

    private void addSlots(Player player, Inventory playerInv) {
        //快捷栏0~8
        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInv, l, 23 + l * 17, 258));
        }

        //背包9~35
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInv, i1 + k * 9 + 9, 23 + i1 * 17, 195 + k * 17));
            }
        }

        //护甲36~40
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.HEAD, 39, 7, 139));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.CHEST, 38, 7, 156));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.LEGS, 37, 58, 139));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.FEET, 36, 58, 156));
        this.addSlot(new Slot(playerInv, 40, 7, 173) {
            @Override
            public boolean isActive() {
                return craftingMode;
            }
        });

        //合成格41~50
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 3, 92 + j * 17, 139 + i * 17) {
                    @Override
                    public boolean isActive() {
                        return craftingMode;
                    }
                });
            }
        }
        this.addSlot(new ResultSlot(player, this.craftSlots, this.resultSlots, 0, 161, 156) {
            @Override
            public boolean isActive() {
                return craftingMode;
            }
        });
    }

    private Slot getArmorSlot(Player player, Inventory inventory, EquipmentSlot equipmentslot, int slotId, int x, int y) {
        return new Slot(inventory, slotId, x, y) {
            @Override
            @ParametersAreNonnullByDefault
            public void set(ItemStack itemStack) {
                ItemStack itemstack = this.getItem();
                super.set(itemStack);
                player.onEquipItem(equipmentslot, itemstack, itemStack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            @ParametersAreNonnullByDefault
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.canEquip(equipmentslot, player);
            }

            @Override
            @ParametersAreNonnullByDefault
            public boolean mayPickup(Player player1) {
                ItemStack itemstack = this.getItem();
                return (itemstack.isEmpty() || player1.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(player1);
            }

            @Override
            public boolean isActive() {
                return craftingMode;
            }
        };
    }

    protected void clearCraftSlots() {
        for (int j = 0; j < this.craftSlots.getContainerSize(); ++j) {
            ItemStack itemStack = this.craftSlots.removeItemNoUpdate(j);
            channel.addItem(itemStack);
            if (!itemStack.isEmpty()) pushToInventory(itemStack);
        }
    }

    public void nextSort() {
        sortType += 2;
        if (sortType > 7) sortType %= 8;
        if (level.isClientSide) dummyChannelContainer.refreshContainer(true);
    }

    public void reverseSort() {
        if (sortType % 2 == 0) sortType++;
        else sortType--;
        if (level.isClientSide) dummyChannelContainer.refreshContainer(true);
    }

    public void changeViewType() {
        if (viewType == 2) viewType = 0;
        else viewType++;
        if (level.isClientSide) dummyChannelContainer.onChangeViewType();
    }

    private void saveBlock() {
        tesseractTile.setCraftingMode(craftingMode);
        tesseractTile.setFilter(filter);
        tesseractTile.setSortType(sortType);
        tesseractTile.setViewType(viewType);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        if (pSlotId >= 51) {
            //仅客户端能触发
            String[] object;
            if (pSlotId - 51 < dummyChannelContainer.viewingObject.size())
                object = dummyChannelContainer.viewingObject.get(pSlotId - 51);
            else object = new String[]{"item", "minecraft:air"};

            switch (pButton) {
                case 0 -> {
                    switch (pClickType) {
                        case QUICK_MOVE -> {
                            //左键shift
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SChannelActionPack(containerId, Action.LEFT_SHIFT_DUMMY_SLOT, object));
                            onLeftShiftDummySlot(object[0], object[1]);
                        }
                        case PICKUP -> {
                            //左键点击
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SChannelActionPack(containerId, Action.LEFT_CLICK_DUMMY_SLOT, object));
                            onLeftClickDummySlot(object[0], object[1]);
                        }
                        case THROW -> {
                            //丢一个
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SChannelActionPack(containerId, Action.THROW_ONE, object));
                            tryThrowOneFromDummySlot(object[0], object[1]);
                        }
                    }
                }
                case 1 -> {
                    switch (pClickType) {
                        case PICKUP -> {
                            //右键点击
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SChannelActionPack(containerId, Action.Right_CLICK_DUMMY_SLOT, object));
                            onRightClickDummySlot(object[0], object[1]);
                        }
                        case QUICK_MOVE -> {
                            //右键shift 快速拿一个
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SChannelActionPack(containerId, Action.Right_SHIFT_DUMMY_SLOT, object));
                            onRightShiftDummySlot(object[0], object[1]);
                        }
                        case QUICK_CRAFT -> {
                            //左键拖动
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SChannelActionPack(containerId, Action.LEFT_DRAG, object));
                            onLeftDragDummySlot(object[0], object[1]);
                        }
                        case THROW -> {
                            //丢一组
                            NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                                    new C2SChannelActionPack(containerId, Action.THROW_STICK, object));
                            tryThrowStickFromDummySlot(object[0], object[1]);
                        }
                    }
                }
                case 4 -> {
                    if (pClickType == ClickType.CLONE) {
                        //复制
                        if (object[0].equals("item") && object[1].equals("minecraft:air")) return;
                        NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SChannelActionPack(containerId, Action.CLONE, object));
                        onCloneFormDummySlot(object[0], object[1]);
                    }
                }
                case 5 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //右键拖动
                        NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SChannelActionPack(containerId, Action.RIGHT_DRAG, object));
                        onRightDragDummySlot(object[0], object[1]);
                    }
                }
                case 9 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //拖动复制
                        NetworkHandler.CHANNEL.send(PacketDistributor.SERVER.noArg(), new C2SChannelActionPack(containerId, Action.DRAG_CLONE, object));
                        onDragCloneDummySlot(object[0], object[1]);
                    }
                }
            }
            //剩下的SWAP无视掉(hot bar的快捷键)
        } else if (pSlotId >= 41 && pSlotId <= 49 && pClickType.equals(ClickType.QUICK_MOVE)) {
            //合成格
            ItemStack itemStack = slots.get(pSlotId).getItem();
            if (pButton == 0) {
                moveItemStackTo(itemStack, 9, 36, false);
                if (!itemStack.isEmpty()) moveItemStackTo(itemStack, 0, 9, false);
                slotsChanged(craftSlots);
            } else if (pButton == 1) {
                channel.addItem(itemStack);
                slotsChanged(craftSlots);
            } else super.clicked(pSlotId, pButton, pClickType, pPlayer);
        } else if (pSlotId != panelItemSlotIndex) super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull ItemStack quickMoveStack(Player player, int slotId) {
        //empty由于退出调用的奇怪循环
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            ItemStack movingStack = slot.getItem();
            itemStack = movingStack.copy();
            if (slotId >= 0 && slotId <= 35) {
                if (craftingMode) {
                    if (!this.moveItemStackTo(movingStack, 41, 50, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    channel.addItem(movingStack);
                    return ItemStack.EMPTY;
                }
            } else if (slotId == 50) {
                movingStack.getItem().onCraftedBy(movingStack, level, player);
                if (!this.moveItemStackTo(movingStack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(movingStack, itemStack);
            } else if (slotId >= 41 && slotId <= 49) {
                //正常情况不会运行这里，因为上面已经拦下来了。
                if (!this.moveItemStackTo(movingStack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                Const.LOGGER.warn("Ohh! Who trigger the quickMoveStack() when slotId >= 51 in server side ?");
            }
            if (movingStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (movingStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, movingStack);
            if (slotId == 50) {
                player.drop(movingStack, false);
            }
        }
        return itemStack;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.index <= 49;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean stillValid(Player player) {
        if (channel.isRemoved()) {
            if (panelItemSlotIndex >= 0) {
                CompoundTag nbt = panelItem.getTag();
                nbt.remove("channel");
                panelItem.setTag(nbt);
            } else tesseractTile.setChannel(null, -1);
            openChannelScreen();
        }
        if (panelItemSlotIndex >= 0) return panelItem == player.getInventory().getItem(panelItemSlotIndex);
        else return !tesseractTile.isRemoved() &&
                player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 32.0D;
    }


    //覆写

    @Override
    @ParametersAreNonnullByDefault
    public void removed(Player player) {
        if (level.isClientSide) return;
        if (!channel.isRemoved()) ((ServerChannel) channel).removeListener((ServerPlayer) player);
        super.removed(player);
        clearCraftSlots();
        if (panelItemSlotIndex >= 0) {
            CompoundTag nbt = panelItem.getTag();
            //locked 的空值检测在物品上，保证到menu的不会空
            if (!nbt.getBoolean("locked")) {
                nbt.putBoolean("locked", locked);
                nbt.putBoolean("craftingMode", craftingMode);
                nbt.putString("filter", filter);
                nbt.putByte("sortType", sortType);
                nbt.putByte("viewType", viewType);
                if (channel.isRemoved()) nbt.remove("channel");
                panelItem.setTag(nbt);
            }
        } else {
            if (!tesseractTile.isLocked()) saveBlock();
        }
    }

    private void openChannelScreen() {
        if (locked) return;
        if (panelItemSlotIndex >= 0) {
            NetworkHooks.openScreen((ServerPlayer) player,
                    new ChannelSelectMenuProvider(new ItemChannelTerminal(player.getInventory(), panelItem, panelItemSlotIndex)),
                    buf -> {
                    }
            );
        } else NetworkHooks.openScreen((ServerPlayer) player, new ChannelSelectMenuProvider(tesseractTile), buf -> {
        });
    }


    @Override
    @ParametersAreNonnullByDefault
    public void slotsChanged(Container container) {
        if (level.isClientSide) return;
        if (lastCraftingRecipe != null && lastCraftingRecipe.matches(craftSlots, level)) {
            resultSlots.setRecipeUsed(level, (ServerPlayer) player, lastCraftingRecipe);
            resultSlots.setItem(0, lastCraftingRecipe.assemble(craftSlots, this.level.registryAccess()));
            return;
        }
        Optional<CraftingRecipe> optional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots, level);
        if (optional.isPresent()) {
            CraftingRecipe recipe = optional.get();
            if (resultSlots.setRecipeUsed(level, (ServerPlayer) player, recipe)) {
                resultSlots.setItem(0, recipe.assemble(craftSlots, this.level.registryAccess()));
            }
        } else resultSlots.setItem(0, ItemStack.EMPTY);
    }

    public void receivedRecipe(String recipeId, boolean maxTransfer) {
        if (level.getServer() == null) return;
        RecipeManager manager = level.getServer().getRecipeManager();
        Optional<? extends Recipe<?>> optional = manager.byKey(new ResourceLocation(recipeId));
        if (optional.isEmpty()) return;
        Inventory inventory = player.getInventory();
        if (!(optional.get() instanceof CraftingRecipe craftingRecipe)) return;

        craftingMode = true;
        clearCraftSlots();
        slotsChanged(craftSlots);

        NonNullList<Ingredient> ingredients = craftingRecipe.getIngredients();
        //无nbt物品的可用数量
        HashMap<Item, Long> itemAmount = new HashMap<>();
        //无nbt物品的份数
        HashMap<Item, Integer> itemP = new HashMap<>();
        //背包物品计数器
        InvItemCounter invItemCounter = null;
        //ingredients对应的首选物品,null无可用，air为nbt.
        ArrayList<Item> itemChosen = new ArrayList<>();
        //统计上面需要的内容
        for (Ingredient ingredient : ingredients) {
            //空槽跳过
            if (ingredient.isEmpty()) {
                itemChosen.add(null);
                continue;
            }
            //此槽允许的物品列
            ItemStack[] stacks = ingredient.getItems();
            long markCount = 0;
            Item markItem = null;
            boolean hasNbtItem = false;
            for (ItemStack stack : stacks) {
                //不统计nbt
                if (stack.hasTag()) {
                    hasNbtItem = true;
                } else {
                    //边统计边确认此槽可以使用的物品里数量最大的那一种
                    Item item = stack.getItem();
                    long count;
                    if (itemAmount.containsKey(item)) count = itemAmount.get(item);
                    else {
                        count = channel.getRealItemAmount(StorageUtils.getItemId(item));
                        //9个合成格极限这么多，够了就没必要继续统计了。
                        if (count < 576) {
                            if (invItemCounter == null) invItemCounter = new InvItemCounter(inventory);
                            count += invItemCounter.getCount(item);
                        }
                        itemAmount.put(item, count);
                    }
                    if (count > markCount) {
                        markCount = count;
                        markItem = item;
                    }
                    if (itemP.containsKey(item)) itemP.replace(item, itemP.get(item) + 1);
                    else itemP.put(item, 1);
                }
            }
            if (markItem == null && hasNbtItem) {
                itemChosen.add(Items.AIR);
            } else itemChosen.add(markItem);
        }

        //先填充一次，如果连填充单个都不够，那就不需要继续了。
        CraftingRecipeGridIndexGetter recipeSlotIndexGetter = new CraftingRecipeGridIndexGetter(craftingRecipe);
        for (int i = 0; i < itemChosen.size(); i++) {
            int gridIndex = recipeSlotIndexGetter.get();
            Item item = itemChosen.get(i);
            if (item == null) continue;
            if (item.equals(Items.AIR)) {
                //代表此处物品需要nbt
                Ingredient ingredient = ingredients.get(i);
                for (ItemStack stack : ingredient.getItems()) {
                    //无nbt的踢开
                    if (!stack.hasTag()) continue;
                    if (invItemCounter == null) invItemCounter = new InvItemCounter(inventory);
                    Integer[] itemIndex = invItemCounter.getNbtItemIndex();
                    //玩家库存物品与目标对比，flag用于跳出双层循环。
                    boolean flag = false;
                    for (Integer index : itemIndex) {
                        ItemStack stack2 = inventory.getItem(index);
                        if (stack2.isEmpty()) continue;
                        if (ItemStack.isSameItemSameTags(stack, stack2)) {
                            if (stack2.getCount() == 1) {
                                craftSlots.setItem(gridIndex, stack2);
                                inventory.setItem(index, ItemStack.EMPTY);
                            } else {
                                ItemStack newStack = stack2.copy();
                                newStack.setCount(1);
                                craftSlots.setItem(gridIndex, newStack);
                                stack2.grow(-1);
                            }
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                }
            } else {
                if (channel.getRealItemAmount(StorageUtils.getItemId(item)) > 0) {
                    craftSlots.setItem(gridIndex, new ItemStack(item));
                    channel.removeItem(StorageUtils.getItemId(item), 1);
                } else {
                    if (invItemCounter == null) invItemCounter = new InvItemCounter(inventory);
                    Integer[] itemIndex = invItemCounter.getNoNbtItemIndex();
                    for (Integer index : itemIndex) {
                        ItemStack stack = inventory.getItem(index);
                        if (stack.isEmpty()) continue;
                        if (stack.getItem().equals(item)) {
                            if (stack.getCount() == 1) {
                                craftSlots.setItem(gridIndex, stack);
                                inventory.setItem(index, ItemStack.EMPTY);
                            } else {
                                craftSlots.setItem(gridIndex, new ItemStack(item));
                                stack.grow(-1);
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (!craftingRecipe.matches(craftSlots, level)) return;
        if (maxTransfer) {
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = craftSlots.getItem(i);
                if (itemStack.isEmpty() || itemStack.hasTag() || itemStack.getMaxStackSize() == 1) continue;
                Item item = itemStack.getItem();
                String itemId = StorageUtils.getItemId(item);
                long amount = itemAmount.getOrDefault(item, 0L);
                int p = itemP.getOrDefault(item, 0);
                int targetCount = (int) Long.min(itemStack.getMaxStackSize(), amount / p);
                if (targetCount < 2) continue;
                int channelHas = channel.getItemAmount(itemId);
                if (channelHas >= targetCount) {
                    channel.removeItem(itemId, targetCount - itemStack.getCount());
                    itemStack.setCount(targetCount);
                } else {
                    if (channelHas > 0) {
                        channel.removeItem(itemId, channelHas);
                        itemStack.setCount(itemStack.getCount() + channelHas);
                    }
                    if (invItemCounter == null) invItemCounter = new InvItemCounter(inventory);
                    Integer[] itemIndex = invItemCounter.getNoNbtItemIndex();
                    for (Integer index : itemIndex) {
                        ItemStack invStack = inventory.getItem(index);
                        if (invStack.isEmpty() || invStack.hasTag()) continue;
                        if (item.equals(invStack.getItem())) {
                            int invCount = invStack.getCount();
                            if (itemStack.getCount() + invCount > targetCount) {
                                int j = itemStack.getCount() + invCount - targetCount;
                                invStack.setCount(j);
                                itemStack.setCount(targetCount);
                            } else {
                                inventory.setItem(index, ItemStack.EMPTY);
                                itemStack.grow(invCount);
                                if (itemStack.getCount() >= targetCount) break;
                            }
                        }
                    }
                }
            }
        }

        //解决合成表冲突
        Optional<CraftingRecipe> optional1 = manager.getRecipeFor(RecipeType.CRAFTING, craftSlots, level);
        if (optional1.isPresent()) {
            CraftingRecipe currentRecipe = optional1.get();
            if (currentRecipe != craftingRecipe) {
                if (craftingRecipe.matches(craftSlots, level)) {
                    if (resultSlots.setRecipeUsed(level, (ServerPlayer) player, craftingRecipe)) {
                        lastCraftingRecipe = craftingRecipe;
                        resultSlots.setItem(0, craftingRecipe.assemble(craftSlots, this.level.registryAccess()));
                    }
                }
            }
        } else resultSlots.setItem(0, ItemStack.EMPTY);
    }

    public void receivedRecipe(Map<String, Integer> itemNeed) {
        itemNeed.forEach((s, integer) -> {
            ItemStack itemStack = channel.takeItem(s, integer);
            if (itemStack.isEmpty()) return;
            savePushToInventory(itemStack);
        });
    }

    private void craftToChannel(int max) {
        ItemStack resultItem = resultSlots.getItem(0).copy();
        if (resultItem.isEmpty() || resultItem.hasTag()) return;

        Recipe<?> recipe = resultSlots.getRecipeUsed();
        if (recipe instanceof CraftingRecipe) {

            int maxTry = max % resultItem.getCount() > 0 ? max / resultItem.getCount() + 1 : max / resultItem.getCount();

            int count = resultItem.getCount() * doCraft(resultItem, maxTry);
            long notInCount = count - channel.addItem(StorageUtils.getItemId(resultItem.getItem()), count);
            if (notInCount > 0) {
                resultItem.setCount((int) notInCount);
                pushToInventory(resultItem);
            }
        }
    }

    //合成相关

    private void craftToInventory(int max) {
        ItemStack resultItem = resultSlots.getItem(0).copy();
        if (resultItem.isEmpty()) return;

        Recipe<?> recipe = resultSlots.getRecipeUsed();
        if (recipe instanceof CraftingRecipe) {

            int maxTry1 = max % resultItem.getCount() > 0 ? max / resultItem.getCount() + 1 : max / resultItem.getCount();
            int maxSpace = 0;
            int maxStackSize = resultItem.getMaxStackSize();
            for (int i = 0; i < 36; i++) {
                ItemStack slotStack = player.getInventory().getItem(i);
                if (slotStack.isEmpty()) maxSpace += maxStackSize;
                else if (maxStackSize > 1 && ItemStack.isSameItemSameTags(resultItem, slotStack)) {
                    maxSpace = maxSpace + maxStackSize - slotStack.getCount();
                }
            }
            if (maxSpace <= 0) return;
            int maxTry2 = maxSpace / resultItem.getCount();
            int maxTry = Integer.min(maxTry1, maxTry2);

            int count = resultItem.getCount() * doCraft(resultItem, maxTry);
            resultItem.setCount(count);
            savePushToInventory(resultItem);
        }
    }

    private void craftAndDrop(int max) {
        ItemStack resultItem = resultSlots.getItem(0).copy();
        if (resultItem.isEmpty()) return;

        Recipe<?> recipe = resultSlots.getRecipeUsed();
        if (recipe instanceof CraftingRecipe) {

            int maxTry = max % resultItem.getCount() > 0 ? max / resultItem.getCount() + 1 : max / resultItem.getCount();

            int count = resultItem.getCount() * doCraft(resultItem, maxTry);
            resultItem.setCount(count);
            player.drop(resultItem, false);
        }
    }

    /**
     * 进行合成，此处未产出产物。
     *
     * @param resultItem 产物
     * @param maxTry     最大合成次数
     * @return 成功合成的次数
     */
    private int doCraft(ItemStack resultItem, int maxTry) {

        int remainingCraftTry = maxTry - tryFastCraft(maxTry);

        if (remainingCraftTry > 0) {
            ArrayList<ItemStack> beforeItems = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                beforeItems.add(i, craftSlots.getItem(i).copy());
            }
            InvItemCounter invItemCounter = null;
            while (remainingCraftTry > 0) {
                //补充物品
                if (remainingCraftTry > 1) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack craftingStack = craftSlots.getItem(i);
                        int maxStackSize = craftingStack.getMaxStackSize();
                        if (craftingStack.isEmpty() || maxStackSize == 1) continue;
                        if (craftingStack.getCount() < 2) {
                            if (!craftingStack.hasTag()) {
                                int channelAmount = channel.getItemAmount(StorageUtils.getItemId(craftingStack.getItem()));
                                if (channelAmount >= maxStackSize - 1) {
                                    craftingStack.setCount(maxStackSize);
                                    channel.removeItem(StorageUtils.getItemId(craftingStack.getItem()), maxStackSize - 1);
                                } else {
                                    if (channelAmount > 0) {
                                        channel.removeItem(StorageUtils.getItemId(craftingStack.getItem()), channelAmount);
                                        craftingStack.setCount(channelAmount + 1);
                                    }
                                    if (invItemCounter == null)
                                        invItemCounter = new InvItemCounter(player.getInventory());
                                    if (invItemCounter.getCount(craftingStack.getItem()) <= 0) continue;
                                    Integer[] noNbtItemIndex = invItemCounter.getNoNbtItemIndex();
                                    for (Integer integer : noNbtItemIndex) {
                                        applyFromInvIndex(craftingStack, integer);
                                    }
                                }
                            }
                        }
                    }
                }
                //合成一次
                slots.get(50).onTake(player, resultItem);
                remainingCraftTry--;
                if (!isSameResul(resultItem)) {
                    if (!fixItems(resultItem, beforeItems)) break;
                }
            }
        }
        return maxTry - remainingCraftTry;
    }

    /**
     * 进行快速合成，此处会扣掉频道材料，但未产出合成物。
     *
     * @param maxTry 最大合成次数
     * @return 已经合成的次数
     */
    private int tryFastCraft(int maxTry) {
        HashMap<Item, Integer> itemMap = new HashMap<>();
        boolean canFastCraft = true;
        for (int i = 0; i < 9; i++) {
            ItemStack slotStack = craftSlots.getItem(i);
            if (slotStack.isEmpty()) continue;
            if (slotStack.hasTag() || slotStack.hasCraftingRemainingItem()) {
                canFastCraft = false;
                break;
            }
            if (itemMap.containsKey(slotStack.getItem()))
                itemMap.replace(slotStack.getItem(), itemMap.get(slotStack.getItem()) + 1);
            else itemMap.put(slotStack.getItem(), 1);
        }

        if (canFastCraft) {
            int canTry = maxTry;
            for (Map.Entry<Item, Integer> entry : itemMap.entrySet()) {
                Item item = entry.getKey();
                Integer integer = entry.getValue();
                int needAmount = integer * canTry;
                int has = channel.getItemAmount(StorageUtils.getItemId(item));
                if (has >= needAmount) continue;
                canTry = has / integer;
                if (canTry == 0) break;
            }
            if (canTry > 0) {
                for (Map.Entry<Item, Integer> entry : itemMap.entrySet()) {
                    Item item = entry.getKey();
                    Integer integer = entry.getValue();
                    channel.removeItem(StorageUtils.getItemId(item), (long) integer * canTry);
                }
            }
            return canTry;
        }
        return 0;
    }

    /**
     * 修复并补充物品到合成格，若返回失败应该中断合成。
     *
     * @param resultItem  期望结果
     * @param beforeItems 期望合成格
     * @return 是否成功
     */
    private boolean fixItems(ItemStack resultItem, ArrayList<ItemStack> beforeItems) {
        //补充物品到合成格
        for (int i = 0; i < 9; i++) {
            ItemStack craftingStack = craftSlots.getItem(i);
            ItemStack needStack = beforeItems.get(i);
            if (ItemStack.isSameItemSameTags(craftingStack, needStack)) continue;
            if (!craftingStack.isEmpty()) {
                channel.addItem(craftingStack);
                if (!craftingStack.isEmpty()) moveItemStackTo(craftingStack, 9, 36, false);
                if (!craftingStack.isEmpty()) moveItemStackTo(craftingStack, 0, 9, false);
                if (!craftingStack.isEmpty()) return false;
            }
            if (!needStack.isEmpty()) {
                if (needStack.hasTag()) moveSameItemToCraftingSlot(i, needStack);
                else {
                    int channelHas = channel.getItemAmount(StorageUtils.getItemId(needStack.getItem()));
                    if (channelHas >= needStack.getMaxStackSize()) {
                        craftSlots.setItem(i, channel.takeItem(StorageUtils.getItemId(needStack.getItem()), needStack.getMaxStackSize()));
                    } else if (channelHas == 0) {
                        moveSameItemToCraftingSlot(i, needStack);
                    } else {
                        craftSlots.setItem(i, channel.takeItem(StorageUtils.getItemId(needStack.getItem()), channelHas));
                        fillStackFromInventory(craftSlots.getItem(i));
                    }
                }
            }
            if (isSameResul(resultItem)) return true;
        }
        //如补充后还是不可以合，就均摊一下合成槽物品。
        if (!isSameResul(resultItem)) {
            ArrayList<ItemStack> itemMark = new ArrayList<>();
            ArrayList<ItemStack> itemAmount = new ArrayList<>();
            beforeItems.forEach(beforeItem -> {
                if (beforeItem.isEmpty()) return;
                if (itemMark.size() == 0) {
                    ItemStack inItem = beforeItem.copy();
                    inItem.setCount(1);
                    itemMark.add(inItem);
                } else {
                    boolean flag = true;
                    for (ItemStack itemStack : itemMark) {
                        if (ItemStack.isSameItemSameTags(beforeItem, itemStack)) {
                            itemStack.setCount(itemStack.getCount() + 1);
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        ItemStack inItem = beforeItem.copy();
                        inItem.setCount(1);
                        itemMark.add(inItem);
                    }
                }
            });
            for (int i = 0; i < 9; i++) {
                ItemStack slotItem = craftSlots.getItem(i);
                if (slotItem.isEmpty()) continue;
                if (itemAmount.size() == 0) {
                    ItemStack inItem = slotItem.copy();
                    itemAmount.add(inItem);
                } else {
                    boolean flag = true;
                    for (ItemStack itemStack : itemAmount) {
                        if (ItemStack.isSameItemSameTags(slotItem, itemStack)) {
                            itemStack.setCount(itemStack.getCount() + slotItem.getCount());
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        ItemStack inItem = slotItem.copy();
                        itemAmount.add(inItem);
                    }
                }
                craftSlots.setItem(i, ItemStack.EMPTY);
            }
            for (int i = 0; i < 9; i++) {
                ItemStack beforeStack = beforeItems.get(i);
                if (beforeStack.isEmpty()) continue;
                ItemStack p = ItemStack.EMPTY;
                ItemStack amount = ItemStack.EMPTY;
                for (ItemStack itemStack : itemMark) {
                    if (ItemStack.isSameItemSameTags(beforeStack, itemStack)) {
                        p = itemStack;
                        break;
                    }
                }
                for (ItemStack itemStack : itemAmount) {
                    if (ItemStack.isSameItemSameTags(beforeStack, itemStack)) {
                        amount = itemStack;
                        break;
                    }
                }
                if (p.isEmpty() || amount.isEmpty()) continue;
                ItemStack newStack = beforeStack.copy();
                int count = amount.getCount() / p.getCount();
                newStack.setCount(count);
                craftSlots.setItem(i, newStack);
                amount.setCount(amount.getCount() - count);
                p.setCount(p.getCount() - 1);
            }
        }
        //返回是否成功
        return isSameResul(resultItem);
    }

    private void pushToInventory(ItemStack itemStack) {
        moveItemStackTo(itemStack, 9, 36, false);
        if (!itemStack.isEmpty()) moveItemStackTo(itemStack, 0, 9, false);
        if (!itemStack.isEmpty()) player.drop(itemStack, false);
    }

    private void savePushToInventory(ItemStack itemStack) {
        int loops = itemStack.getCount() / itemStack.getMaxStackSize();
        for (int i = 0; i < loops; i++) {
            ItemStack newStack = itemStack.copy();
            newStack.setCount(itemStack.getMaxStackSize());
            itemStack.setCount(itemStack.getCount() - itemStack.getMaxStackSize());
            pushToInventory(newStack);
        }
        if (itemStack.getCount() > 0) pushToInventory(itemStack);
    }

    private void fillStackFromInventory(ItemStack stack) {
        int maxStackSize = stack.getMaxStackSize();
        for (int i = 9; i < 36; i++) {
            applyNbtFromInvIndex(stack, i);
            if (stack.getCount() >= maxStackSize) break;
        }
        if (stack.getCount() < maxStackSize) for (int i = 0; i < 9; i++) {
            applyNbtFromInvIndex(stack, i);
            if (stack.getCount() >= maxStackSize) break;
        }
    }

    private void applyFromInvIndex(ItemStack itemStack, int slotId) {
        if (itemStack.getItem().equals(player.getInventory().getItem(slotId).getItem())) {
            ItemStack otherStack = player.getInventory().getItem(slotId);
            int needAmount = itemStack.getMaxStackSize() - itemStack.getCount();
            if (otherStack.getCount() > needAmount) {
                itemStack.setCount(itemStack.getMaxStackSize());
                otherStack.setCount(otherStack.getCount() - needAmount);
            } else {
                itemStack.setCount(itemStack.getCount() + otherStack.getCount());
                player.getInventory().setItem(slotId, ItemStack.EMPTY);
            }
        }
    }

    private void applyNbtFromInvIndex(ItemStack itemStack, int slotId) {
        if (ItemStack.isSameItemSameTags(itemStack, player.getInventory().getItem(slotId))) {
            ItemStack otherStack = player.getInventory().getItem(slotId);
            int needAmount = itemStack.getMaxStackSize() - itemStack.getCount();
            if (otherStack.getCount() > needAmount) {
                itemStack.setCount(itemStack.getMaxStackSize());
                otherStack.setCount(otherStack.getCount() - needAmount);
            } else {
                itemStack.setCount(itemStack.getCount() + otherStack.getCount());
                player.getInventory().setItem(slotId, ItemStack.EMPTY);
            }
        }
    }

    private void moveSameItemToCraftingSlot(int slotIndex, ItemStack stack) {
        //目标通常叠堆为一
        boolean flag = true;
        for (int i = 9; i < 36; i++) {
            ItemStack otherStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(stack, otherStack)) {
                craftSlots.setItem(slotIndex, otherStack);
                player.getInventory().setItem(i, ItemStack.EMPTY);
                flag = false;
                break;
            }
        }
        if (flag) for (int i = 0; i < 9; i++) {
            ItemStack otherStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(stack, otherStack)) {
                craftSlots.setItem(slotIndex, otherStack);
                player.getInventory().setItem(i, ItemStack.EMPTY);
                break;
            }
        }
    }

    private boolean isSameResul(ItemStack itemStack) {
        return ItemStack.isSameItemSameTags(resultSlots.getItem(0), itemStack) && resultSlots.getItem(0).getCount() == itemStack.getCount();
    }

}
