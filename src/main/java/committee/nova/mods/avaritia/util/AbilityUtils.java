package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.item.InfinityArmorItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/1/7 23:53
 * @Description:
 */

public class AbilityUtils {
    public static void sweepAttack(Level level, LivingEntity livingEntity, LivingEntity victim) {
        if (livingEntity instanceof Player player){
            for(LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, victim))) {
                double entityReachSq = Mth.square(player.getEntityReach()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
                if (!player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand)livingentity).isMarker()) && player.distanceToSqr(livingentity) < entityReachSq) {
                    livingentity.knockback(0.6F, Mth.sin(player.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(player.getYRot() * ((float)Math.PI / 180F)));
                    victim.setHealth(0);
                    victim.die(player.damageSources().source(ModDamageTypes.INFINITY, player, victim));
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

    public static void attackAOE(Player player, float range, float damage, boolean hurtLiving) {
        if (player.level().isClientSide) return;
        AABB aabb = player.getBoundingBox().deflate(range);
        List<Entity> toAttack = player.level().getEntities(player, aabb);
        DamageSource src = player.damageSources().source(ModDamageTypes.INFINITY, player, player);
        for (var entity : toAttack) {
            if (hurtLiving) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.hurt(src, damage);
                }
            } else if (entity instanceof Mob) {

                    if (entity instanceof EnderDragon drageon) {
                        drageon.hurt(drageon.head, src, Float.POSITIVE_INFINITY);
                    } else if (entity instanceof WitherBoss wither) {
                        wither.setInvulnerableTicks(0);
                        wither.hurt(src, damage);
                    } else entity.hurt(src, damage);

            }
        }
    }

    public static boolean isPlayerWearing(LivingEntity entity, EquipmentSlot slot, Predicate<Item> predicate) {
        ItemStack stack = entity.getItemBySlot(slot);
        return !stack.isEmpty() && predicate.test(stack.getItem());
    }

    public static boolean isInfinite(LivingEntity player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof InfinityArmorItem)) {
                return false;
            }
        }
        return true;
    }
}
