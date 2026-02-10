package committee.nova.mods.avaritia.common.container.chest;

import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class InfinityChestContainer extends AbstractContainerMenu {
    protected InfinityChestTile chestTile;


    protected InfinityChestContainer(@Nullable MenuType<?> type, int id) {
        super(type, id);
        chestTile = null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }


    @Override
    public boolean stillValid(Player player) {
        return this.chestTile.stillValid(player);
    }

    /**
     * shift 移动物品
     * @param stackIn 物品
     * @param index 开始查找序号
     * @param index0 结束序号
     * @param flagIn 查找顺序
     * @return 结果
     */
    @Override
    public boolean moveItemStackTo(ItemStack stackIn, int index, int index0, boolean flagIn) {
        boolean flag = false;
        int i = index;
        if (flagIn) { //从后往前
            i = index0 - 1;
        }

        Slot slot1;
        ItemStack itemstack; //空物品
        if (stackIn.isStackable()) { //可堆叠的
            while(!stackIn.isEmpty()) {
                if (flagIn) {
                    if (i < index) {
                        break;
                    }
                } else if (i >= index0) {
                    break;
                }

                slot1 = this.slots.get(i);
                itemstack = slot1.getItem(); //当前格物品
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(stackIn, itemstack)) { //相同
                    int j = itemstack.getCount() + stackIn.getCount(); //总数量
                    int maxSize = Math.max(slot1.getMaxStackSize(), stackIn.getMaxStackSize()); //允许的最大堆叠数量
                    if (j <= maxSize) {
                        stackIn.setCount(0);
                        itemstack.setCount(j);
                        slot1.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stackIn.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot1.setChanged();
                        flag = true;
                    }
                }

                if (flagIn) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stackIn.isEmpty()) { //不为空
            if (flagIn) {
                i = index0 - 1;
            } else {
                i = index;
            }

            while(true) {
                if (flagIn) {
                    if (i < index) {
                        break;
                    }
                } else if (i >= index0) {
                    break;
                }

                slot1 = this.slots.get(i);
                itemstack = slot1.getItem();
                if (itemstack.isEmpty() && slot1.mayPlace(stackIn)) {
                    if (stackIn.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stackIn.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(stackIn.split(stackIn.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (flagIn) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public void clicked(int slot, int button, ClickType clickType, Player player) {
        try {
            this.doClick(slot, button, clickType, player);
        } catch (Exception var8) {
            CrashReport crashreport = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
            crashreportcategory.setDetail("Menu Type", () -> Objects.requireNonNull(BuiltInRegistries.MENU.getKey(this.getType())).toString());
            crashreportcategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashreportcategory.setDetail("Slot Count", this.slots.size());
            crashreportcategory.setDetail("Slot", slot);
            crashreportcategory.setDetail("Button", button);
            crashreportcategory.setDetail("Type", clickType);
            throw new ReportedException(crashreport);
        }
    }

    /**
     * 玩家点击一个格子
     * @param slot 点击的容器格子
     * @param button 鼠标按键0左键1右键
     * @param clickType 点击类型
     * @param player 玩家
     */
    private void doClick(int slot, int button, ClickType clickType, Player player) {
        Inventory inventory = player.getInventory();
        Slot slot7;
        ItemStack itemstack9; //玩家操作的物品
        ItemStack itemstack2;
        int i1;
        int k1;

        int l2;
            if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
                ClickAction clickaction = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
                if (slot == -999) {
                    if (!this.getCarried().isEmpty()) {
                        if (clickaction == ClickAction.PRIMARY) {
                            player.drop(this.getCarried(), true);
                            this.setCarried(ItemStack.EMPTY);
                        } else {
                            player.drop(this.getCarried().split(1), true);
                        }
                    }
                } else if (clickType == ClickType.QUICK_MOVE) {
                    if (slot < 0) {
                        return;
                    }

                    slot7 = (Slot)this.slots.get(slot);
                    if (!slot7.mayPickup(player)) {
                        return;
                    }

                    for(itemstack9 = this.quickMoveStack(player, slot); !itemstack9.isEmpty() && ItemStack.isSameItem(slot7.getItem(), itemstack9); itemstack9 = this.quickMoveStack(player, slot)) {
                    }
                } else { //从容器拿放
                    if (slot < 0) {
                        return; //放入物品容器
                    }

                    slot7 = this.slots.get(slot);
                    itemstack9 = slot7.getItem(); //物品格
                    ItemStack itemstack10 = this.getCarried(); //鼠标物品
                    player.updateTutorialInventoryAction(itemstack10, slot7.getItem(), clickaction);
                    if (!this.tryItemClickBehaviourOverride(player, clickaction, slot7, itemstack9, itemstack10) && !CommonHooks.onItemStackedOn(itemstack9, itemstack10, slot7, clickaction, player, this.createCarriedSlotAccess())) {
                        if (itemstack9.isEmpty()) { //物品格为空
                            if (!itemstack10.isEmpty()) {
                                l2 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1; //左键 还是右键  左键放完 右键放一个
                                if (l2 > slot7.getMaxStackSize(itemstack10))
                                    l2 = slot7.getMaxStackSize(itemstack10); //超过堆叠数

                                this.setCarried(slot7.safeInsert(itemstack10, l2)); //只放64 有剩余
                            }
                        } else if (slot7.mayPickup(player)) { //容器物品格不为空 拿取
                            if (itemstack10.isEmpty()) { //空物品
                                l2 = clickaction == ClickAction.PRIMARY ? itemstack9.getMaxStackSize() : (itemstack9.getCount() + 1) / 2; //右键取一半物品
                                Optional<ItemStack> optional1 = slot7.tryRemove(l2, Integer.MAX_VALUE, player);
                                optional1.ifPresent((stack) -> {
                                    this.setCarried(stack);
                                    slot7.onTake(player, stack);
                                });
                            } else if (slot7.mayPlace(itemstack10)) { //能否放进此物品
                                if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) {
                                    l2 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                    if (l2 > slot7.getMaxStackSize(itemstack10) - itemstack9.getCount())  //物品相同 超过则放部分
                                        l2 = slot7.getMaxStackSize(itemstack10) - itemstack9.getCount();

                                    this.setCarried(slot7.safeInsert(itemstack10, l2));
                                } else if (itemstack10.getCount() <= slot7.getMaxStackSize(itemstack10)) { //否则全放
                                    this.setCarried(itemstack9);
                                    slot7.setByPlayer(itemstack10);
                                }
                            } else if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) { //不能放入但是容器有物品 则合并到玩家鼠标物品
                                int count = itemstack9.getCount(); //容器物品数量
                                Optional<ItemStack> optional = slot7.tryRemove(count, itemstack10.getMaxStackSize() - itemstack10.getCount(), player);
                                optional.ifPresent((stack) -> {
                                    itemstack10.grow(stack.getCount());
                                    slot7.onTake(player, stack);
                                });
                            }
                        }
                    }

                    slot7.setChanged();
                }
            } else {
                Slot slot2;
                int k2;
                if (clickType == ClickType.SWAP) { //交换物品
                    slot2 = this.slots.get(slot);
                    itemstack2 = inventory.getItem(button);
                    itemstack9 = slot2.getItem();
                    if (!itemstack2.isEmpty() || !itemstack9.isEmpty()) {
                        if (itemstack2.isEmpty()) {
                            if (slot2.mayPickup(player)) {
                                inventory.setItem(button, itemstack9);
                                slot2.setByPlayer(ItemStack.EMPTY);
                                slot2.onTake(player, itemstack9);
                            }
                        } else if (itemstack9.isEmpty()) {
                            if (slot2.mayPlace(itemstack2)) {
                                k2 = slot2.getMaxStackSize(itemstack2);
                                if (itemstack2.getCount() > k2) {
                                    slot2.setByPlayer(itemstack2.split(k2));
                                } else {
                                    inventory.setItem(button, ItemStack.EMPTY);
                                    slot2.setByPlayer(itemstack2);
                                }
                            }
                        } else if (slot2.mayPickup(player) && slot2.mayPlace(itemstack2)) {
                            k2 = slot2.getMaxStackSize(itemstack2);
                            if (itemstack2.getCount() > k2) {
                                slot2.setByPlayer(itemstack2.split(k2));
                                slot2.onTake(player, itemstack9);
                                if (!inventory.add(itemstack9)) {
                                    player.drop(itemstack9, true);
                                }
                            } else {
                                inventory.setItem(button, itemstack9);
                                slot2.setByPlayer(itemstack2);
                                slot2.onTake(player, itemstack9);
                            }
                        }
                    }
                } else if (clickType == ClickType.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && slot >= 0) {
                    slot2 = this.slots.get(slot);//创造模式复制物品
                    if (slot2.hasItem()) {
                        itemstack2 = slot2.getItem();
                        this.setCarried(itemstack2.copyWithCount(itemstack2.getMaxStackSize()));
                    }
                } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && slot >= 0) {
                    slot2 = this.slots.get(slot); //丢出物品
                    i1 = button == 0 ? 1 : slot2.getItem().getCount();
                    itemstack9 = slot2.safeTake(i1, Integer.MAX_VALUE, player);
                    player.drop(itemstack9, true);
                } else if (clickType == ClickType.PICKUP_ALL && slot >= 0) {
                    slot2 = this.slots.get(slot);  //合并所有相同物品
                    itemstack2 = this.getCarried(); //玩家物品
                    if (!itemstack2.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(player))) {
                        k1 = button == 0 ? 0 : this.slots.size() - 1;
                        k2 = button == 0 ? 1 : -1;

                        for(l2 = 0; l2 < 2; ++l2) {
                            for(int l3 = k1; l3 >= 0 && l3 < this.slots.size() && itemstack2.getCount() < itemstack2.getMaxStackSize(); l3 += k2) {
                                Slot slot8 = this.slots.get(l3);
                                if (slot8.hasItem() && canItemQuickReplace(slot8, itemstack2, true) && slot8.mayPickup(player) && this.canTakeItemForPickAll(itemstack2, slot8)) {
                                    ItemStack itemstack11 = slot8.getItem();
                                    if (l2 != 0 || itemstack11.getCount() != itemstack11.getMaxStackSize()) {
                                        ItemStack itemstack12 = slot8.safeTake(itemstack11.getCount(), itemstack2.getMaxStackSize() - itemstack2.getCount(), player);
                                        itemstack2.grow(itemstack12.getCount());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    private boolean tryItemClickBehaviourOverride(Player player, ClickAction clickAction, Slot slot, ItemStack stack, ItemStack itemStack) {
        FeatureFlagSet featureflagset = player.level().enabledFeatures();
        if (itemStack.isItemEnabled(featureflagset) && itemStack.overrideStackedOnOther(slot, clickAction, player)) {
            return true;
        } else {
            return stack.isItemEnabled(featureflagset) && stack.overrideOtherStackedOnMe(itemStack, slot, clickAction, player, this.createCarriedSlotAccess());
        }
    }

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess() {
            public @NotNull ItemStack get() {
                return InfinityChestContainer.this.getCarried();
            }

            public boolean set(@NotNull ItemStack stack) {
                InfinityChestContainer.this.setCarried(stack);
                return true;
            }
        };
    }

    public Container getContainer() {
        return this.chestTile;
    }
}