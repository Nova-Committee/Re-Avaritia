package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/23 12:43
 * @Description:
 */
public class ExtremeAnvilMenu extends ItemCombinerMenu {
    private String itemName;
    public int repairItemCountCost;
    public ExtremeAnvilMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }

    public ExtremeAnvilMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(ModMenus.extreme_anvil.get(), pContainerId, pPlayerInventory, pAccess);
    }

    @Override
    protected @NotNull ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 27, 47, (itemStack) -> true)
                .withSlot(1, 76, 47, (itemStack) -> true)
                .withResultSlot(2, 134, 47).build();
    }

    @Override
    protected boolean isValidBlock(BlockState pState) {
        return pState.is(BlockTags.ANVIL);
    }

    @Override
    protected boolean mayPickup(Player pPlayer, boolean pHasStack) {
        return true;
    }
    private int calculateXpCost() {
        ItemStack itemstack = this.inputSlots.getItem(0);
        ItemStack itemstack1 = this.inputSlots.getItem(1);
        if (itemstack.isEmpty()) {
            return -1;
        } else {
            int i = 0;
            int k = 0;
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);

            if (!itemstack1.isEmpty()) {
                boolean flag = itemstack1.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(itemstack1).isEmpty();
                if (itemstack.isDamageableItem() && itemstack.getItem().isValidRepairItem(itemstack, itemstack1)) {
                    int l2 = Math.min(itemstack.getDamageValue(), itemstack.getMaxDamage() / 4);
                    if (l2 <= 0) {
                        return -1;
                    }

                    int i3;
                    for(i3 = 0; l2 > 0 && i3 < itemstack1.getCount(); ++i3) {
                        int j3 = itemstack.getDamageValue() - l2;
                        itemstack.setDamageValue(j3);
                        ++i;
                        l2 = Math.min(itemstack.getDamageValue(), itemstack.getMaxDamage() / 4);
                    }
                } else {
                    if (!flag && (!itemstack.is(itemstack1.getItem()) || !itemstack.isDamageableItem())) {
                        return -1;
                    }

                    if (itemstack.isDamageableItem() && !flag) {
                        int l = itemstack.getMaxDamage() - itemstack.getDamageValue();
                        int i1 = itemstack1.getMaxDamage() - itemstack1.getDamageValue();
                        int j1 = i1 + itemstack.getMaxDamage() * 12 / 100;
                        int k1 = l + j1;
                        int l1 = itemstack.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < itemstack.getDamageValue()) {
                            i += 2;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack1);
                    for(Enchantment enchantment1 : map1.keySet()) {
                        if (enchantment1 != null) {
                            int i2 = map.getOrDefault(enchantment1, 0);
                            int j2 = map1.get(enchantment1);
                            j2 = i2 + j2;
                            map.put(enchantment1, j2);
                            int k3 = 0;
                            switch (enchantment1.getRarity()) {
                                case COMMON -> k3 = 1;
                                case UNCOMMON -> k3 = 2;
                                case RARE -> k3 = 4;
                                case VERY_RARE -> k3 = 8;
                            }

                            if (flag) {
                                k3 = Math.max(1, k3 / 2);
                            }

                            i += k3 * j2;
                            if (itemstack.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }

                }
            }

            if (this.itemName != null && !Util.isBlank(this.itemName)) {
                if (!this.itemName.equals(itemstack.getHoverName().getString())) {
                    k = 1;
                    i += k;
                }
            } else if (itemstack.hasCustomHoverName()) {
                k = 1;
                i += k;
            }

            return i;
        }
    }
    @Override
    protected void onTake(@NotNull Player pPlayer, @NotNull ItemStack pStack) {
        this.inputSlots.setItem(0, ItemStack.EMPTY);
        if (this.repairItemCountCost > 0) {
            ItemStack itemstack = this.inputSlots.getItem(1);
            if (!itemstack.isEmpty() && itemstack.getCount() > this.repairItemCountCost) {
                itemstack.shrink(this.repairItemCountCost);
                this.inputSlots.setItem(1, itemstack);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
        }

        this.access.execute((level, blockPos) -> {
            level.playSound(null, blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        });
    }

    @Override
    public void createResult() {
        ItemStack itemstack = this.inputSlots.getItem(0);
        int i = 0;
        int k = 0;
        if (itemstack.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = this.inputSlots.getItem(1);
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
            this.repairItemCountCost = 0;
            boolean flag = false;

            if (!itemstack2.isEmpty()) {
                flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(itemstack2).isEmpty();
                if (itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(itemstack, itemstack2)) {
                    int l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    if (l2 <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        return;
                    }

                    int i3;
                    for(i3 = 0; l2 > 0 && i3 < itemstack2.getCount(); ++i3) {
                        int j3 = itemstack1.getDamageValue() - l2;
                        itemstack1.setDamageValue(j3);
                        ++i;
                        l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    }
                    this.repairItemCountCost = i3;
                } else {
                    if (!flag && (!itemstack1.is(itemstack2.getItem()) || !itemstack1.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        return;
                    }

                    if (itemstack1.isDamageableItem() && !flag) {
                        int l = itemstack.getMaxDamage() - itemstack.getDamageValue();
                        int i1 = itemstack2.getMaxDamage() - itemstack2.getDamageValue();
                        int j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                        int k1 = l + j1;
                        int l1 = itemstack1.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < itemstack1.getDamageValue()) {
                            itemstack1.setDamageValue(l1);
                            i += 2;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
                    for(Enchantment enchantment1 : map1.keySet()) {
                        if (enchantment1 != null) {
                            int i2 = map.getOrDefault(enchantment1, 0);
                            int j2 = map1.get(enchantment1);
                            j2 = i2 + j2;
                            map.put(enchantment1, j2);
                            int k3 = 0;
                            switch (enchantment1.getRarity()) {
                                case COMMON -> k3 = 1;
                                case UNCOMMON -> k3 = 2;
                                case RARE -> k3 = 4;
                                case VERY_RARE -> k3 = 8;
                            }

                            if (flag) {
                                k3 = Math.max(1, k3 / 2);
                            }

                            i += k3 * j2;
                            if (itemstack.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }
                }
            }

            if (this.itemName != null && !Util.isBlank(this.itemName)) {
                if (!this.itemName.equals(itemstack.getHoverName().getString())) {
                    k = 1;
                    i += k;
                    itemstack1.setHoverName(Component.literal(this.itemName));
                }
            } else if (itemstack.hasCustomHoverName()) {
                k = 1;
                i += k;
                itemstack1.resetHoverName();
            }

            if (flag && !itemstack1.isBookEnchantable(itemstack2)) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (i <= 0) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (!itemstack1.isEmpty()) {
                EnchantmentHelper.setEnchantments(map, itemstack1);
            }

            this.resultSlots.setItem(0, itemstack1);
            this.broadcastChanges();
        }

    }

    public static int calculateIncreasedRepairCost(int pOldRepairCost) {
        return pOldRepairCost * 2 + 1;
    }

    public boolean setItemName(String pItemName) {
        String s = validateName(pItemName);
        if (s != null && !s.equals(this.itemName)) {
            this.itemName = s;
            if (this.getSlot(2).hasItem()) {
                ItemStack itemstack = this.getSlot(2).getItem();
                if (Util.isBlank(s)) {
                    itemstack.resetHoverName();
                } else {
                    itemstack.setHoverName(Component.literal(s));
                }
            }

            this.createResult();
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private static String validateName(String pItemName) {
        String s = SharedConstants.filterText(pItemName);
        return s.length() <= 100 ? s : null;
    }
}
