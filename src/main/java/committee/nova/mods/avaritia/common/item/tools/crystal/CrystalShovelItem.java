package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalShovelItem extends Item implements ITooltip {
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int HOTBAR_EFFECT_DURATION_TICKS = 4;

    public CrystalShovelItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .shovel(ModToolTiers.CRYSTAL, 0, ModToolTiers.CRYSTAL.speed()));
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull ServerLevel pLevel, @NotNull Entity pEntity, @Nullable EquipmentSlot pSlot) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlot);
        if (pEntity instanceof Player player && isInHotbar(player, pStack)) {
            applyPassiveEffects(player);
        }
    }

    public static void applyPassiveEffects(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, HOTBAR_EFFECT_DURATION_TICKS, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, HOTBAR_EFFECT_DURATION_TICKS, 2, false, true));
        player.removeEffect(MobEffects.SLOWNESS);
        player.removeEffect(MobEffects.MINING_FATIGUE);
    }

    //26.3 的 inventoryTick 只带 EquipmentSlot，改为直接检查物品是否位于热栏（保留 1.20.1 行为）
    private static boolean isInHotbar(Player player, ItemStack stack) {
        for (int slotId = 0; slotId < HOTBAR_SLOT_COUNT; slotId++) {
            if (player.getInventory().getItem(slotId) == stack) {
                return true;
            }
        }
        return false;
    }
}
