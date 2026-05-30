package com.avaritia.common.item.tools.infinity;

import com.avaritia.Const;
import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.item.IUndamageable;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.api.iface.transform.IToolTransform;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.ModDamageTypes;
import com.avaritia.init.registry.ModEntities;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import com.avaritia.util.ToolUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 19:41
 * Version: 1.0
 */
public class InfinitySwordItem extends Item implements InitEnchantItem, ISwitchable, IUndamageable, IToolTransform {
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.LOOTING, 10);
    public InfinitySwordItem() {
        super(new Properties()
                        .rarity(ModRarities.COSMIC.getValue())
                        .stacksTo(1)
                        .fireResistant()
                        .sword(ModToolTiers.INFINITY, 0, ModToolTiers.INFINITY.speed())
                        .attributes(ItemAttributeModifiers.builder()
                                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.INFINITY.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.INFINITY.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                                .build()
                                .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("attack_range_modifier"), 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND))
        );
    }

    public ToolMaterial getTier() {
        return ModToolTiers.INFINITY;
    }


    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, Player player, @NotNull Entity entity) {
        var endlessDamage = ModConfig.isSwordAttackEndless.get();
        if (player.level() instanceof ServerLevel serverLevel && entity instanceof LivingEntity victim) {
            var damageSource = player.damageSources().source(ModDamageTypes.INFINITY, victim, player);
            ToolUtils.sweepAttack(serverLevel, player, victim);//横扫
            if (victim instanceof EnderDragon dragon ) {
                dragon.hurt(serverLevel, dragon.head, damageSource, endlessDamage ? Float.MAX_VALUE : ModToolTiers.INFINITY.attackDamageBonus());
            } else if (victim instanceof Player pvp) {
                if (ToolUtils.isInfinite(pvp)) {
                    // 玩家身着无尽甲则只造成爆炸伤害
                    serverLevel.explode(player, pvp.getBlockX(), pvp.getBlockY(), pvp.getBlockZ(), 25.0F, Level.ExplosionInteraction.MOB);
                    return true;//直接返回
                } else {
                    this.hurt(victim, damageSource, endlessDamage ? Float.MAX_VALUE : ModToolTiers.INFINITY.attackDamageBonus());
                }

            } else {
                this.hurt(victim, damageSource, endlessDamage ? Float.MAX_VALUE : ModToolTiers.INFINITY.attackDamageBonus());
            }

            if (!victim.isDeadOrDying() && endlessDamage) {
                victim.setHealth(0);
                //set health to 0�?
                this.die(victim, damageSource);//修正设置死亡
                player.killedEntity(serverLevel, victim, damageSource);
                //add to stats
                //player.getCombatTracker().recordDamage(damageSource, victim.getHealth());
                //record damage
                }
        }
        return false;
    }

    public boolean hurt(LivingEntity victim, DamageSource pSource, float pAmount) {
        if (victim.level().isClientSide() || victim.isDeadOrDying()) {
            return false;
        } else {
            if (victim.isMultipartEntity()) {
                for (Entity part :victim.getParts()) {
                    if (part instanceof PartEntity<?> partEntity && partEntity.getParent() == victim) {
                        part.hurt(pSource, pAmount);
                    }
                }
            }
            if (victim.isSleeping() && !victim.level().isClientSide()) {
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
                    victim.setLastHurtByPlayer(player1, 100);
                } else if (entity1 instanceof net.minecraft.world.entity.TamableAnimal tamableEntity) {
                    if (tamableEntity.isTame()) {
                        LivingEntity livingentity2 = tamableEntity.getOwner();
                        if (livingentity2 instanceof Player player2) {
                            victim.setLastHurtByPlayer(player2, 100);
                        } else {
                            victim.setLastHurtByPlayer((Player) null, 100);
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
                for(d1 = entity1.getZ() - victim.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
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
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)victim, pSource, pAmount, pAmount, flag);
            }

            if (entity1 instanceof ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity1, victim, pSource, pAmount, pAmount, flag);
            }

            return flag2;
        }

    }

    public void die(LivingEntity victim, DamageSource pDamageSource) {
        if (!victim.isRemoved() && !victim.dead) {
            Entity entity = pDamageSource.getEntity();
            LivingEntity livingentity = victim.getKillCredit();
            if (livingentity != null) {
                livingentity.awardKillScore(victim, pDamageSource);
            }

            if (victim.isSleeping()) {
                victim.stopSleeping();
            }

            if (!victim.level().isClientSide() && victim.hasCustomName()) {
                Const.LOGGER.info("Named entity {} died: {}", this, victim.getCombatTracker().getDeathMessage().getString());
            }

            victim.dead = true;
            victim.getCombatTracker().recheckStatus();
            Level level = victim.level();
            if (level instanceof ServerLevel serverlevel) {
                if (entity == null || entity.killedEntity(serverlevel, victim, pDamageSource)) {
                    victim.gameEvent(GameEvent.ENTITY_DIE);
                    victim.dropAllDeathLoot(serverlevel, pDamageSource);
                    this.createWitherRose(victim, livingentity);
                }

                victim.level().broadcastEntityEvent(victim, (byte) 3);
            }

            victim.setPose(Pose.DYING);
        }
    }

    protected void createWitherRose(LivingEntity victim, @Nullable LivingEntity pEntitySource) {
        if (!victim.level().isClientSide()) {
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
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "infinity_sword_kill");
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            if (isActive(heldItem, "infinity_sword_kill")) {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), true, ModConfig.isSwordAttackLightning.get());
            } else {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), false, ModConfig.isSwordAttackLightning.get());
            }
            player.getCooldowns().addCooldown(heldItem, 20);
        }
        level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResult.SUCCESS;
    }


    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }


    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
        return 0;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.LOOTING)) {
            return 10;
        }
        return 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        if (isActive(stack, "infinity_sword_kill")) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.sword_kill_mode.active").withStyle(net.minecraft.ChatFormatting.RED));
        }
    }
}
