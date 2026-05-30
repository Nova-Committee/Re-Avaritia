package com.avaritia.common.menu;

import com.avaritia.init.registry.ModMenus;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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
        super(ModMenus.extreme_anvil.get(), pContainerId, pPlayerInventory, pAccess, createInputSlotDefinitions());
    }


    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 27, 47, itemStack -> true)
                .withSlot(1, 76, 47, itemStack -> true)
                .withResultSlot(2, 134, 47)
                .build();
    }

    @Override
    protected boolean isValidBlock(BlockState pState) {
        return pState.is(BlockTags.ANVIL);
    }

    @Override
    protected boolean mayPickup(Player pPlayer, boolean pHasStack) {
        return pPlayer.hasInfiniteMaterials();
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
    }

    @Override
    public void createResult() {
        ItemStack input = this.inputSlots.getItem(0);
        int price = 0;
        int k = 0;
        if(!input.isEmpty() && EnchantmentHelper.canStoreEnchantments(input)){
            ItemStack result = input.copy();
            ItemStack addition = this.inputSlots.getItem(1);
            ItemEnchantments.Mutable itemenchantments$mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));
            this.repairItemCountCost = 0;
            boolean flag = false;

            if (!addition.isEmpty()) {
                flag = addition.has(DataComponents.STORED_ENCHANTMENTS);
                if (result.isDamageableItem() && input.isValidRepairItem(addition)) {
                    int repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
                    if (repairAmount <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        return;
                    }

                    int count;
                    for(count = 0; repairAmount > 0 && count < addition.getCount(); ++count) {
                        int j3 = result.getDamageValue() - repairAmount;
                        result.setDamageValue(j3);
                        price++;
                        repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
                    }
                    this.repairItemCountCost = count;
                } else {
                    if (!flag && (!result.is(addition.getItem()) || !result.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        return;
                    }

                    if (result.isDamageableItem() && !flag) {
                        int l = input.getMaxDamage() - input.getDamageValue();
                        int i1 = addition.getMaxDamage() - addition.getDamageValue();
                        int j1 = i1 + result.getMaxDamage() * 12 / 100;
                        int k1 = l + j1;
                        int l1 = result.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < result.getDamageValue()) {
                            result.setDamageValue(l1);
                            price += 2;
                        }
                    }

                    ItemEnchantments itemenchantments = EnchantmentHelper.getEnchantmentsForCrafting(addition);
                    boolean flag2 = false;
                    boolean flag3 = false;
                    for(Object2IntMap.Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
                        Holder<Enchantment> holder = entry.getKey();
                        int i2 = itemenchantments$mutable.getLevel(holder);
                        int j2 = entry.getIntValue();
                        j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
                        itemenchantments$mutable.set(holder, j2);
                        price += j2;
                    }

                }
            }

            if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
                if (!this.itemName.equals(input.getHoverName().getString())) {
                    k = 1;
                    price += k;
                    result.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
                }
            } else if (input.has(DataComponents.CUSTOM_NAME)) {
                k = 1;
                price += k;
                result.remove(DataComponents.CUSTOM_NAME);
            }

            if (price <= 0) {
                result = ItemStack.EMPTY;
            }

            if (!result.isEmpty()) {
                EnchantmentHelper.setEnchantments(result, itemenchantments$mutable.toImmutable());
            }

            this.resultSlots.setItem(0, result);
            this.broadcastChanges();
        }
        else {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
        ItemStack leftInput = this.inputSlots.getItem(0);
        ItemStack rightInput = this.inputSlots.getItem(1);
        //net.neoforged.neoforge.common.CommonHooks.onAnvilUpdate(this, leftInput, rightInput, resultSlots, itemName, this.player);
    }

    public boolean setItemName(String pItemName) {
        String s = validateName(pItemName);
        if (s != null && !s.equals(this.itemName)) {
            this.itemName = s;
            if (this.getSlot(2).hasItem()) {
                ItemStack itemstack = this.getSlot(2).getItem();
                if (StringUtil.isBlank(s)) {
                    itemstack.remove(DataComponents.CUSTOM_NAME);
                } else {
                    itemstack.set(DataComponents.CUSTOM_NAME, Component.literal(s));
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
        String s = StringUtil.filterText(pItemName);
        return s.length() <= 50 ? s : null;
    }
}
