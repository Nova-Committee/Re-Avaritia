package nova.committee.avaritia.common.item.tools;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.init.config.ModConfig;
import nova.committee.avaritia.init.handler.InfinityHandler;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModTab;
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
                .tab(ModTab.TAB)
                .stacksTo(1)
                .fireResistant());
        setRegistryName("infinity_sword");
    }


    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity victim, LivingEntity player) {
        if (player.level.isClientSide) {
            return true;
        }

        if (victim instanceof EnderDragon drageon && player instanceof Player player1) {
            drageon.hurt(drageon.head, new DamageSourceInfinitySword(player1), Float.POSITIVE_INFINITY);
        } else if (victim instanceof Player pvp) {
            if (InfinityHandler.isInfinite(pvp)) {
                victim.hurt(new DamageSourceInfinitySword(player).bypassArmor(), 4.0F);
            } else victim.hurt(new DamageSourceInfinitySword(player).bypassArmor(), Float.POSITIVE_INFINITY);

        } else victim.hurt(new DamageSourceInfinitySword(player).bypassArmor(), Float.POSITIVE_INFINITY);

        victim.lastHurtByPlayerTime = 60;
        victim.getCombatTracker().recordDamage(new DamageSourceInfinitySword(player), victim.getHealth(), victim.getHealth());

        if(victim instanceof Player victimP && InfinityHandler.isInfinite(victimP)) {
        	victimP.level.explode(player, victimP.getBlockX(), victimP.getBlockY(), victimP.getBlockZ(), 25.0f, Explosion.BlockInteraction.NONE);
        	return true;
        }

        victim.setHealth(0);
        victim.die(new DamageSourceInfinitySword(player));
        return true;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (!level.isClientSide) {
            attackAOE(player, ModConfig.SERVER.swordAttackRange.get(), ModConfig.SERVER.swordRangeDamage.get(), player.isCrouching() && ModConfig.SERVER.isSwordAttackAnimal.get());
            player.getCooldowns().addCooldown(heldItem.getItem(), 20);
        }
        level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResultHolder.success(heldItem);
    }

    protected void attackAOE(Player player, float range, float damage, boolean type) {
        if (player.level.isClientSide) return;
        AABB aabb = player.getBoundingBox().deflate(range);
        List<Entity> toAttack = player.getLevel().getEntities(player, aabb);
        DamageSource src = new DamageSourceInfinitySword(player);
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
        if (!entity.level.isClientSide && entity instanceof Player victim) {
            if (victim.isCreative() && !victim.isDeadOrDying() && victim.getHealth() > 0 && !InfinityHandler.isInfinite(victim)) {
                victim.getCombatTracker().recordDamage(new DamageSourceInfinitySword(player), victim.getHealth(), victim.getHealth());
                victim.setHealth(0);
                victim.die(new DamageSourceInfinitySword(player));
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
    public int getItemEnchantability(ItemStack stack) {
        return 0;
    }


    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }
}
