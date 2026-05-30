package com.avaritia.common.item.tools.crystal;

import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.transform.IBowTransform;
import com.avaritia.common.entity.BladeSlashEntity;
import com.avaritia.common.entity.arrow.NeutronArrowEntity;
import com.avaritia.init.registry.ModEntities;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

public class CrystalBowItem extends BowItem implements ISwitchable, IBowTransform {
    public CrystalBowItem() {
        super(new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
        );
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isCrouching()) {
            switchMode(level, player, hand, "blade_slash");
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }
    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack pStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {
            InteractionHand hand = pEntityLiving.getUsedItemHand();
            ItemStack stack = player.getItemInHand(hand);
            boolean isBladeSlashActive = isActive(stack, "blade_slash");

            int chargeTime = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft;

            chargeTime = EventHooks.onArrowLoose(pStack, pLevel, player, chargeTime, true);
            if (chargeTime < 0) {
                return isBladeSlashActive;
            }

            float power = getPowerForTime(chargeTime);
            if (power >= 0.1) {
                if (!pLevel.isClientSide()) {
                    if (isBladeSlashActive) {
                        BladeSlashEntity bladeSlash = new BladeSlashEntity(pLevel, player);

                        float speed = BladeSlashEntity.defaultSpeed * (1.0F + power * 2.0F);
                        float inaccuracy = 0.0F;
                        float yawOffset = 0.0F;

                        bladeSlash.shootFromRotation(
                                player,
                                player.getXRot(),
                                player.getYRot() + yawOffset,
                                0.0F,
                                speed,
                                inaccuracy
                        );

                        float damageBoost = power * 5.0F;
                        bladeSlash.damage += damageBoost;
                        bladeSlash.duration += (int) (power * 20);
                        pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                                1.0F, 0.8F + (power * 0.4F));
                        pLevel.addFreshEntity(bladeSlash);
                    } else {
                        NeutronArrowEntity neutronArrow = new NeutronArrowEntity(ModEntities.NEUTRON_ARROW.get(), pLevel);
                        neutronArrow.setOwner(player);
                        neutronArrow.setPos(player.getX(), player.getEyeY() - 0.1F, player.getZ());
                        float speed = 3.0F;
                        float inaccuracy = 0.0F;
                        float yawOffset = 0.0F;

                        neutronArrow.shootFromRotation(
                                player,
                                player.getXRot(),
                                player.getYRot() + yawOffset,
                                0.0F,
                                speed,
                                inaccuracy
                        );

                        pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                                1.0F, 0.8F + (power * 0.4F));

                        pLevel.addFreshEntity(neutronArrow);
                    }

                    pStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                return true;
            }
        }
        return false;
    }


    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Player player) {
        stack.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.INFINITY),1);
        stack.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.MULTISHOT),1);
        super.onCraftedBy(stack, player);
    }
}
