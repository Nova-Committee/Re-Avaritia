package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 19:41
 * Version: 1.0
 */
public class InfinitySwordItem extends SwordItem {
    public InfinitySwordItem() {
        super(ModToolTiers.INFINITY_SWORD, 0, -2.8F, (new Properties())
                .stacksTo(1)
                .fireResistant());
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity victim, LivingEntity livingEntity) {
        var level = livingEntity.level();
        if (level.isClientSide) {
            return true;
        }

        if (victim instanceof EnderDragon dragon && livingEntity instanceof Player player) {
            dragon.hurt(dragon.head, player.damageSources().source(ModDamageTypes.INFINITY, player, victim), Float.POSITIVE_INFINITY);
            dragon.setHealth(0);//fix
        } else if (victim instanceof Player pvp) {
            if (ToolUtils.isInfinite(pvp)) {
                victim.hurt(livingEntity.damageSources().source(ModDamageTypes.INFINITY, livingEntity, victim), 4.0F);
            } else
                victim.hurt(livingEntity.damageSources().source(ModDamageTypes.INFINITY, livingEntity, victim), Float.POSITIVE_INFINITY);

        } else
            victim.hurt(livingEntity.damageSources().source(ModDamageTypes.INFINITY, livingEntity, victim), Float.POSITIVE_INFINITY);

        victim.lastHurtByPlayerTime = 60;
        victim.getCombatTracker().recordDamage(livingEntity.damageSources().source(ModDamageTypes.INFINITY, livingEntity, victim), victim.getHealth());

        if(victim instanceof Player victimP && ToolUtils.isInfinite(victimP)) {
            victimP.level().explode(livingEntity, victimP.getBlockX(), victimP.getBlockY(), victimP.getBlockZ(), 25.0f, Level.ExplosionInteraction.BLOCK);
            // 玩家身着无尽甲则只造成爆炸伤害
        	return true;
        }
        ToolUtils.sweepAttack(level, livingEntity, victim);

        victim.setHealth(0);
        victim.die(livingEntity.damageSources().source(ModDamageTypes.INFINITY, livingEntity, victim));
        return true;
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), ModConfig.isSwordAttackAnimal.get(), ModConfig.isSwordAttackLightning.get());
            player.getCooldowns().addCooldown(heldItem.getItem(), 20);
        }
        level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResultHolder.success(heldItem);
    }



    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!entity.level().isClientSide && entity instanceof Player victim) {
            if (!victim.isCreative() && !victim.isDeadOrDying() && victim.getHealth() > 0 && !ToolUtils.isInfinite(victim)) {
                victim.getCombatTracker().recordDamage(player.damageSources().source(ModDamageTypes.INFINITY, player, victim), victim.getHealth());
                victim.setHealth(0);
                victim.die(player.damageSources().source(ModDamageTypes.INFINITY, player, victim));
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return ModRarities.COSMIC;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }
}
