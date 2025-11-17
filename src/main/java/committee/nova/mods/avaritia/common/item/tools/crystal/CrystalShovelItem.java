package committee.nova.mods.avaritia.common.item.tools.crystal;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
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
        super(ModToolTiers.CRYSTAL,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .attributes(createAttributes(ModToolTiers.CRYSTAL, 0, ModToolTiers.BLAZE.getSpeed()))
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
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide && pEntity instanceof Player player) {
            if (pIsSelected) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 2, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 2, false, true));
                List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
                for (MobEffectInstance potion : Collections2
                        .filter(effects, potion ->

                                (
                                        potion.getEffect().equals(MobEffects.MOVEMENT_SLOWDOWN)
                                                || potion.getEffect().equals(MobEffects.DIG_SLOWDOWN)
                                )
                        )
                ) {
                    player.removeEffect(potion.getEffect());
                }
            } else {
                player.removeEffect(MobEffects.DIG_SPEED);
                player.removeEffect(MobEffects.MOVEMENT_SPEED);
            }
        }
    }
}
