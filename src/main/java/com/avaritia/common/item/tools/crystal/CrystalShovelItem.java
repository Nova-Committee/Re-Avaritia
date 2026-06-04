package com.avaritia.common.item.tools.crystal;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.avaritia.api.iface.ITooltip;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalShovelItem extends ShovelItem implements ITooltip {
    public CrystalShovelItem() {
        super(ModToolTiers.CRYSTAL,0, ModToolTiers.BLAZE.speed(),
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
        );
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
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull ServerLevel pLevel, @NotNull Entity pEntity, EquipmentSlot pSlot) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlot);
        if (pEntity instanceof Player player) {
            if (pSlot == EquipmentSlot.MAINHAND && player.getMainHandItem() == pStack) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, -1, 2, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, -1, 2, false, true));
                List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
                for (MobEffectInstance potion : Collections2
                        .filter(effects, potion ->

                                (
                                        potion.getEffect().equals(MobEffects.SLOWNESS)
                                                || potion.getEffect().equals(MobEffects.MINING_FATIGUE)
                                )
                        )
                ) {
                    player.removeEffect(potion.getEffect());
                }
            } else {
                player.removeEffect(MobEffects.HASTE);
                player.removeEffect(MobEffects.SPEED);
            }
        }
    }
}
