package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/12/23 12:43
 * @Description: Don't forget this code! The text color.
 */
public class ExtremeAnvilMenu extends AnvilMenu {
    private String itemName;

    public ExtremeAnvilMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }

    public ExtremeAnvilMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(pContainerId, pPlayerInventory, pAccess);
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
    protected void onTake(@NotNull Player pPlayer, @NotNull ItemStack pStack) {
        // Preserve the loader event contract; the extreme anvil itself never takes damage.
        ForgeHooks.onAnvilRepair(pPlayer, pStack, this.inputSlots.getItem(0), this.inputSlots.getItem(1));
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
        if (left.isEmpty()) {
            return;
        }

        ItemStack right = this.inputSlots.getItem(1);
        int baseCost = left.getBaseRepairCost() + (right.isEmpty() ? 0 : right.getBaseRepairCost());
        // Loader-provided anvil recipes take precedence over the enhanced vanilla behavior below.
        if (!ForgeHooks.onAnvilChange(this, left, right, this.resultSlots, this.itemName, baseCost, this.player)) {
            return;
        }

        ItemStack result = left.copy();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        int operationCost = 0;
        boolean enchantedBook = false;

        if (!right.isEmpty()) {
            enchantedBook = right.getItem() == Items.ENCHANTED_BOOK
                    && !EnchantedBookItem.getEnchantments(right).isEmpty();
            if (result.isDamageableItem() && result.getItem().isValidRepairItem(left, right)) {
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

                Map<Enchantment, Integer> addedEnchantments = EnchantmentHelper.getEnchantments(right);
                for (Enchantment enchantment : addedEnchantments.keySet()) {
                    if (enchantment == null) {
                        continue;
                    }

                    int mergedLevel = enchantments.getOrDefault(enchantment, 0) + addedEnchantments.get(enchantment);
                    enchantments.put(enchantment, mergedLevel);
                    int enchantmentCost = switch (enchantment.getRarity()) {
                        case COMMON -> 1;
                        case UNCOMMON -> 2;
                        case RARE -> 4;
                        case VERY_RARE -> 8;
                    };
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

        if (this.itemName != null && !Util.isBlank(this.itemName)) {
            if (!this.itemName.equals(left.getHoverName().getString())) {
                ++operationCost;
                result.setHoverName(Component.literal(this.itemName));
            }
        } else if (left.hasCustomHoverName()) {
            ++operationCost;
            result.resetHoverName();
        }

        if (operationCost <= 0 || enchantedBook && !result.isBookEnchantable(right)) {
            this.clearResult();
            return;
        }

        EnchantmentHelper.setEnchantments(enchantments, result);
        this.setMaximumCost(baseCost + operationCost);
        this.resultSlots.setItem(0, result);
        this.broadcastChanges();
    }

    private void clearResult() {
        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.repairItemCountCost = 0;
        this.setMaximumCost(0);
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
