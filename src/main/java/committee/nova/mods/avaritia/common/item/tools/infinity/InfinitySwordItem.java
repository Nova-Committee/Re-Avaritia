package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IToolTransform;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 19:41
 * Version: 1.0
 */
public class InfinitySwordItem extends SwordItem implements InitEnchantItem, ISwitchable, IUndamageable, IToolTransform {
    public InfinitySwordItem() {
        super(ModToolTiers.INFINITY, 900, 0F, (new Properties())
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        var level = player.level();
        var endlessDamage = ModConfig.isSwordAttackEndless.get();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && entity instanceof LivingEntity victim) {
            var damageSource = player.damageSources().source(ModDamageTypes.INFINITY, victim, player);
            ToolUtils.sweepAttack(serverLevel, player, victim);//横扫
            if (victim instanceof EnderDragon dragon) {
                dragon.hurt(dragon.head, damageSource, endlessDamage ? Float.MAX_VALUE : this.getTier().getAttackDamageBonus());
            } else if (victim instanceof Player pvp) {
                if (ToolUtils.isInfinite(pvp)) {
                    // 玩家身着无尽甲则只造成爆炸伤害
                    serverLevel.explode(player, pvp.getBlockX(), pvp.getBlockY(), pvp.getBlockZ(), 25.0F, Level.ExplosionInteraction.MOB);
                    return true;//直接返回
                } else {
                    this.hurt(victim, damageSource, endlessDamage ? Float.MAX_VALUE : this.getTier().getAttackDamageBonus());
                }

            } else {
                this.hurt(victim, damageSource, endlessDamage ? Float.MAX_VALUE : this.getTier().getAttackDamageBonus());
            }

            if (endlessDamage) {
                if (victim.isDeadOrDying()) {
                    victim.setHealth(0);//设置血量为零
                    this.die(victim, damageSource);//修正设置死亡
                    player.killedEntity(serverLevel, victim);//添加至信息统计
                    //player.getCombatTracker().recordDamage(damageSource, victim.getHealth());//添加至伤害记录
                }
            }
            return true;
        }
        return false;
    }

    public boolean hurt(LivingEntity victim, DamageSource pSource, float pAmount) {
        if (victim.level().isClientSide) {
            return false;
        } else if (victim.isDeadOrDying()) {
            return false;
        } else {
            if (victim.isSleeping() && !victim.level().isClientSide) {
                victim.stopSleeping();
            }

            boolean flag = false;

            victim.setNoActionTime(0);
            victim.walkAnimation.setSpeed(1.5F);
            victim.lastHurt = pAmount;
            victim.invulnerableTime = 20;
            victim.getCombatTracker().recordDamage(pSource, pAmount);
            victim.setHealth(victim.getHealth() - pAmount);
            victim.gameEvent(GameEvent.ENTITY_DAMAGE);
            victim.hurtDuration = 10;
            victim.hurtTime = victim.hurtDuration;


            Entity entity1 = pSource.getEntity();
            if (entity1 != null) {
                if (entity1 instanceof LivingEntity livingentity1) {
                    if (!pSource.is(DamageTypeTags.NO_ANGER)) {
                        victim.setLastHurtByMob(livingentity1);
                    }
                }

                if (entity1 instanceof Player player1) {
                    victim.lastHurtByPlayerTime = 100;
                    victim.setLastHurtByPlayer(player1);
                } else if (entity1 instanceof net.minecraft.world.entity.TamableAnimal tamableEntity) {
                    if (tamableEntity.isTame()) {
                        victim.lastHurtByPlayerTime = 100;
                        LivingEntity livingentity2 = tamableEntity.getOwner();
                        if (livingentity2 instanceof Player player2) {
                            victim.setLastHurtByPlayer(player2);
                        } else {
                            victim.setLastHurtByPlayer(null);
                        }
                    }
                }
            }

            victim.level().broadcastDamageEvent(victim, pSource);

            if (!pSource.is(DamageTypeTags.NO_IMPACT)) {
                victim.hurtMarked = true;
            }

            if (entity1 != null && !pSource.is(DamageTypeTags.IS_EXPLOSION)) {
                double d0 = entity1.getX() - victim.getX();

                double d1;
                for (d1 = entity1.getZ() - victim.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                    d0 = (Math.random() - Math.random()) * 0.01D;
                }

                victim.knockback(0.4F, d0, d1);
                if (!flag) {
                    victim.indicateDamage(d0, d1);
                }
            }

            if (victim.isDeadOrDying()) {
                this.die(victim, pSource);
            } else {
                SoundEvent soundevent = SoundEvents.GENERIC_HURT;
                victim.playSound(soundevent, 2F, victim.getVoicePitch());
            }

            boolean flag2 = true;
            victim.lastDamageSource = pSource;
            victim.lastDamageStamp = victim.level().getGameTime();

            if (victim instanceof ServerPlayer) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) victim, pSource, pAmount, pAmount, flag);
            }

            if (entity1 instanceof ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) entity1, victim, pSource, pAmount, pAmount, flag);
            }

            return flag2;
        }
    }

    public void die(LivingEntity victim, DamageSource pDamageSource) {
        if (!victim.isRemoved() && !victim.dead) {
            Entity entity = pDamageSource.getEntity();
            LivingEntity livingentity = victim.getKillCredit();
            if (victim.deathScore >= 0 && livingentity != null) {
                livingentity.awardKillScore(victim, victim.deathScore, pDamageSource);
            }

            if (victim.isSleeping()) {
                victim.stopSleeping();
            }

            if (!victim.level().isClientSide && victim.hasCustomName()) {
                Const.LOGGER.info("Named entity {} died: {}", this, victim.getCombatTracker().getDeathMessage().getString());
            }

            victim.dead = true;
            victim.getCombatTracker().recheckStatus();
            Level level = victim.level();
            if (level instanceof ServerLevel serverlevel) {
                if (entity == null || entity.killedEntity(serverlevel, victim)) {
                    victim.gameEvent(GameEvent.ENTITY_DIE);
                    victim.dropAllDeathLoot(pDamageSource);
                    this.createWitherRose(victim, livingentity);
                }

                victim.level().broadcastEntityEvent(victim, (byte) 3);
            }

            victim.setPose(Pose.DYING);
        }
    }

    protected void createWitherRose(LivingEntity victim, @Nullable LivingEntity pEntitySource) {
        if (!victim.level().isClientSide) {
            boolean flag = false;
            if (pEntitySource instanceof WitherBoss) {
                BlockPos blockpos = victim.blockPosition();
                BlockState blockstate = Blocks.WITHER_ROSE.defaultBlockState();
                if (victim.level().isEmptyBlock(blockpos) && blockstate.canSurvive(victim.level(), blockpos)) {
                    victim.level().setBlock(blockpos, blockstate, 3);
                    flag = true;
                }


                if (!flag) {
                    ItemEntity itementity = new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(), new ItemStack(Items.WITHER_ROSE));
                    victim.level().addFreshEntity(itementity);
                }
            }

        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        var heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "infinity_sword_kill");
            return InteractionResultHolder.success(itemstack);
        }
        if (!level.isClientSide) {
            if (isActive(itemstack, "infinity_sword_kill")) {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), true, ModConfig.isSwordAttackLightning.get());
            } else {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), false, ModConfig.isSwordAttackLightning.get());
            }
            player.getCooldowns().addCooldown(heldItem.getItem(), 20);
        }
        level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResultHolder.success(heldItem);
    }


    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
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
    public int getInitEnchantLevel(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.MOB_LOOTING ? 10 : 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantments.MOB_LOOTING.getFullname(10)).build());

        if (isActive(stack, "infinity_sword_kill")) {
            tooltipComponents.add(Component.translatable("tooltip.avaritia.sword_kill_mode.active").withStyle(net.minecraft.ChatFormatting.RED));
        }
    }
}
