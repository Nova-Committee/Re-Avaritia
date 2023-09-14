package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.registry.ModCreativeModeTabs;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 19:41
 * Version: 1.0
 */
public class SwordInfinityItem extends SwordItem {
    public SwordInfinityItem() {
        super(Tier.INFINITY_SWORD, 0, -2.8F, (new Properties())
                .stacksTo(1)
                .tab(ModCreativeModeTabs.TAB)
                .fireResistant());
    }


    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity victim, LivingEntity livingEntity) {
        var level = livingEntity.getCommandSenderWorld();
        if (level.isClientSide) {
            return true;
        }

        if (victim instanceof EnderDragon dragon && livingEntity instanceof Player player1) {
            dragon.hurt(dragon.head, new ModDamageTypes(player1), Float.POSITIVE_INFINITY);
            dragon.setHealth(0);//fix
        } else if (victim instanceof Player pvp) {
            if (InfinityHandler.isInfinite(pvp)) {
                victim.hurt(new ModDamageTypes(livingEntity).bypassArmor(), 4.0F);
            } else
                victim.hurt(new ModDamageTypes(livingEntity).bypassArmor(), Float.POSITIVE_INFINITY);

        } else
            victim.hurt(new ModDamageTypes(livingEntity).bypassArmor(), Float.POSITIVE_INFINITY);

        victim.lastHurtByPlayerTime = 60;
        victim.getCombatTracker().recordDamage(new ModDamageTypes(livingEntity), victim.getHealth(), victim.getHealth());

        if(victim instanceof Player victimP && InfinityHandler.isInfinite(victimP)) {
            victimP.getCommandSenderWorld().explode(livingEntity, victimP.getBlockX(), victimP.getBlockY(), victimP.getBlockZ(), 25.0f, Explosion.BlockInteraction.NONE);
            // 玩家身着无尽甲则只造成爆炸伤害
        	return true;
        }
        this.sweepAttack(level, livingEntity, victim);

        victim.setHealth(0);
        victim.die(new ModDamageTypes(livingEntity));
        return true;
    }

    public void sweepAttack(Level level, LivingEntity livingEntity, LivingEntity victim) {
        if (livingEntity instanceof Player player){
            for(LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, victim))) {
                double entityReachSq = Mth.square(player.getReachDistance()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
                if (!player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand)livingentity).isMarker()) && player.distanceToSqr(livingentity) < entityReachSq) {
                    livingentity.knockback(0.6F, Mth.sin(player.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(player.getYRot() * ((float)Math.PI / 180F)));
                    victim.setHealth(0);
                    victim.die(new ModDamageTypes(player));
                }
            }
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, livingEntity.getSoundSource(), 1.0F, 1.0F);
            double d0 = -Mth.sin(player.getYRot() * ((float)Math.PI / 180F));
            double d1 = Mth.cos(player.getYRot() * ((float)Math.PI / 180F));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (!level.isClientSide) {
            attackAOE(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), player.isCrouching() && ModConfig.isSwordAttackAnimal.get());
            player.getCooldowns().addCooldown(heldItem.getItem(), 20);
        }
        level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResultHolder.success(heldItem);
    }

    protected void attackAOE(Player player, float range, float damage, boolean type) {
        if (player.getCommandSenderWorld().isClientSide) return;
        AABB aabb = player.getBoundingBox().deflate(range);
        List<Entity> toAttack = player.getCommandSenderWorld().getEntities(player, aabb);
        DamageSource src = new ModDamageTypes(player);
        for (Entity entity : toAttack) {
            if (type) {
                if (entity instanceof LivingEntity) {
                    entity.hurt(src, damage);
                }
            } else {
                if (entity instanceof Mob) {
                    if (entity instanceof EnderDragon drageon) {
                        drageon.hurt(drageon.head, src, Float.POSITIVE_INFINITY);
                    } else if (entity instanceof WitherBoss wither) {
                        wither.setInvulnerableTicks(0);
                        wither.hurt(src, damage);
                    } else entity.hurt(src, damage);
                }
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!entity.getCommandSenderWorld().isClientSide && entity instanceof Player victim) {
            if (victim.isCreative() && !victim.isDeadOrDying() && victim.getHealth() > 0 && !InfinityHandler.isInfinite(victim)) {
                victim.getCombatTracker().recordDamage(new ModDamageTypes(player), victim.getHealth(), victim.getHealth());
                victim.setHealth(0);
                victim.die(new ModDamageTypes(player));
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }


    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }
}
