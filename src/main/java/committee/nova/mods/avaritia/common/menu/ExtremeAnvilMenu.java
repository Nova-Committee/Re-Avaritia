package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/12/23 12:43
 * @Description: Don't forget this code! The text color.
 */
public class ExtremeAnvilMenu extends AnvilMenu {
    private String itemName;
    private int repairItemCountCost;
    private final DataSlot maximumCost = DataSlot.standalone();

    private void setMaximumCost(int value) { this.maximumCost.set(value); }

    public ExtremeAnvilMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }

    public ExtremeAnvilMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(pContainerId, pPlayerInventory, pAccess);
        this.addDataSlot(this.maximumCost);
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(ModBlocks.extreme_anvil.get());
    }

    @Override
    public @NotNull MenuType<?> getType() {
        return ModMenus.extreme_anvil.get();
    }

    @Override
    protected boolean mayPickup(Player pPlayer, boolean pHasStack) {
        return true;
    }

    @Override
    public int getCost() {
        return this.maximumCost.get();
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

        this.repairItemCountCost = 0;
        this.setMaximumCost(0);
        this.access.execute((level, blockPos) ->
                level.playSound(null, blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F));
    }

    @Override
    public void createResult() {
        this.clearResult();
        ItemStack left = this.inputSlots.getItem(0);
        if (left.isEmpty() || !EnchantmentHelper.canStoreEnchantments(left)) {
            return;
        }

        ItemStack right = this.inputSlots.getItem(1);
        int baseCost = left.getOrDefault(DataComponents.REPAIR_COST, 0) + (right.isEmpty() ? 0 : right.getOrDefault(DataComponents.REPAIR_COST, 0));

        ItemStack result = left.copy();
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));
        int operationCost = 0;
        boolean enchantedBook = false;

        if (!right.isEmpty()) {
            enchantedBook = right.has(DataComponents.STORED_ENCHANTMENTS);
            if (result.isDamageableItem() && left.isValidRepairItem(right)) {
                int repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
                if (repairAmount <= 0) {
                    this.clearResult();
                    return;
                }

                int consumedMaterials;
                for (consumedMaterials = 0;
                     repairAmount > 0 && consumedMaterials < right.getCount();
                     ++consumedMaterials) {
                    result.setDamageValue(result.getDamageValue() - repairAmount);
                    ++operationCost;
                    repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
                }
                this.repairItemCountCost = consumedMaterials;
            } else {
                if (!enchantedBook && (!result.is(right.getItem()) || !result.isDamageableItem())) {
                    this.clearResult();
                    return;
                }

                if (result.isDamageableItem() && !enchantedBook) {
                    int leftDurability = left.getMaxDamage() - left.getDamageValue();
                    int rightDurability = right.getMaxDamage() - right.getDamageValue();
                    int combinedDurability = leftDurability + rightDurability + result.getMaxDamage() * 12 / 100;
                    int combinedDamage = Math.max(result.getMaxDamage() - combinedDurability, 0);
                    if (combinedDamage < result.getDamageValue()) {
                        result.setDamageValue(combinedDamage);
                        operationCost += 2;
                    }
                }

                ItemEnchantments addedEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(right);
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : addedEnchantments.entrySet()) {
                    Holder<Enchantment> enchantmentHolder = entry.getKey();
                    Enchantment enchantment = enchantmentHolder.value();

                    int mergedLevel = enchantments.getLevel(enchantmentHolder) + entry.getIntValue();
                    enchantments.set(enchantmentHolder, mergedLevel);
                    int enchantmentCost = enchantment.getAnvilCost();
                    if (enchantedBook) {
                        enchantmentCost = Math.max(1, enchantmentCost / 2);
                    }

                    operationCost += enchantmentCost * mergedLevel;
                    if (left.getCount() > 1) {
                        operationCost = 40;
                    }
                }
            }
        }

        if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
            if (!this.itemName.equals(left.getHoverName().getString())) {
                ++operationCost;
                result.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
            }
        } else if (left.has(DataComponents.CUSTOM_NAME)) {
            ++operationCost;
            result.remove(DataComponents.CUSTOM_NAME);
        }

        if (operationCost <= 0) {
            this.clearResult();
            return;
        }

        EnchantmentHelper.setEnchantments(result, enchantments.toImmutable());
        this.setMaximumCost(baseCost + operationCost);
        this.resultSlots.setItem(0, result);
        this.broadcastChanges();
    }

    private void clearResult() {
        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.repairItemCountCost = 0;
        this.setMaximumCost(0);
    }

    @Override
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
        return s.length() <= 100 ? s : null;
    }
}
