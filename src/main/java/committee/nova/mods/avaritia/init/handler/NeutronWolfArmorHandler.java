package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.WolfArmorState;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

/**
 * 中子战狼铠的专属机制。
 *
 * <p>四条逻辑，全部以「主人在不在身边」为轴：</p>
 * <ul>
 *   <li><b>护卫</b>：羁绊半径内伤害被完全抵消，半径外线性衰减到保底减伤；</li>
 *   <li><b>调停</b>：穿甲 / 无视无敌的机制伤害只吃 {@code wolf_hax_reduction}，不给绝对免伤；</li>
 *   <li><b>羁绊</b>：主人被一击打死时，半径内的狼替主人吃下这一击（冷却见配置）；</li>
 *   <li><b>图腾</b>：狼自己被一击打死时消耗铠甲耐久免死。</li>
 * </ul>
 * <p>冷却记在狼实体上而不是铠甲物品上，避免脱穿铠甲刷新冷却。</p>
 */
public final class NeutronWolfArmorHandler {
    private static final double EPSILON = 1.0E-4D;
    private static final int TICK_INTERVAL = 20;
    private static final Identifier HEALTH_MODIFIER_ID = Const.rl("neutron_wolf_armor_health");

    private NeutronWolfArmorHandler() {
    }

    /** Incoming guard runs before vanilla armor wear and hurt feedback. */
    public static boolean preventDamage(LivingEntity victim, DamageSource source) {
        if (victim.level().isClientSide() || !ModConfig.neutronWolfArmorEnabled.get()
                || source.is(ModDamageTypes.INFINITY)) return false;
        if (isArmoredWolf(victim) && !isHaxDamage(source) && reductionAt(ownerDistance(victim)) >= 1.0D - EPSILON) {
            playGuardFeedback(victim, 0);
            return true;
        }
        return false;
    }

    /** Called after vanilla armor/enchantment reduction, before absorption, on both damage paths. */
    public static float reduceDamage(LivingEntity victim, DamageSource source, float amount) {
        if (victim.level().isClientSide() || !ModConfig.neutronWolfArmorEnabled.get()
                || source.is(ModDamageTypes.INFINITY)) return amount;
        if (victim instanceof ServerPlayer player) {
            if (amount >= player.getHealth() + player.getAbsorptionAmount() && absorbLethalHit(player, source, amount)) {
                player.hurtTime = 0;
                return 0;
            }
            return amount;
        }
        if (!isArmoredWolf(victim)) return amount;
        double reduction = reductionAt(ownerDistance(victim));
        if (isHaxDamage(source)) reduction = Math.min(reduction, ModConfig.neutronWolfHaxReduction.get());
        double remaining = amount * Math.max(0.0D, 1.0D - reduction);
        if (remaining >= victim.getHealth() - EPSILON && tryTotem(victim)) return 0;
        return (float) Math.max(0.0D, remaining);
    }

    /** Every 20 server ticks, add or remove the transient max-health modifier. */
    public static void tick(LivingEntity wolf) {
        if (!wolf.level().isClientSide() && wolf.getType() == EntityTypes.WOLF && wolf.tickCount % TICK_INTERVAL == 0) {
            syncHealthBonus(wolf);
        }
    }

    private static boolean absorbLethalHit(ServerPlayer player, DamageSource source, float amount) {
        if (ToolUtils.isInfinite(player)) {
            return false;
        }
        LivingEntity wolf = findBondWolf(player);
        if (wolf == null) {
            return false;
        }
        long gameTime = player.level().getGameTime();
        WolfArmorState state = stateOf(wolf);
        if (!state.isBondReady(gameTime)) {
            return false;
        }
        setState(wolf, state.withBondCooldown(gameTime, cooldownTicks(ModConfig.neutronWolfBondCooldownSeconds.get())));
        if (wolf.level() instanceof ServerLevel level) {
            wolf.hurtServer(level, source, amount);
        }
        playGuardFeedback(wolf, 2);
        playGuardFeedback(player, 2);
        return true;
    }

    /**
     * 狼自己被一击打死时免死一次，代价是损耗铠甲耐久。
     * <p>耐久不足以支付代价时图腾不生效，狼照常承受这一击。</p>
     */
    private static boolean tryTotem(LivingEntity wolf) {
        if (!(wolf.level() instanceof ServerLevel level)) {
            return false;
        }
        ItemStack armor = wolf.getItemBySlot(EquipmentSlot.BODY);
        int cost = ModConfig.neutronWolfTotemDurabilityCost.get();
        if (cost > 0) {
            if (!armor.isDamageableItem()) {
                return false;
            }
            int remaining = armor.getMaxDamage() - armor.getDamageValue();
            if (remaining < cost) {
                return false;
            }
            armor.hurtAndBreak(cost, wolf, EquipmentSlot.BODY);
        }
        wolf.setHealth(wolf.getMaxHealth());
        playGuardFeedback(wolf, 1);
        return true;
    }

    /**
     * 距离 → 减伤比例：羁绊半径内 1.0，{@code wolf_max_range} 之外保底 {@code wolf_min_reduction}，中间线性。
     * 找不到主人（跨维度 / 已死亡）按最远处处理。
     *
     * @param distance 狼与主人的直线距离（格）
     * @return 0.0 ~ 1.0 的减伤比例
     */
    public static double reductionAt(double distance) {
        double bond = ModConfig.neutronWolfBondRadius.get();
        double max = Math.max(bond + EPSILON, ModConfig.neutronWolfMaxRange.get());
        double floor = ModConfig.neutronWolfMinReduction.get();
        if (distance == Double.MAX_VALUE || distance >= max) {
            return 1.0D - floor;
        }
        if (distance <= bond) {
            return 1.0D;
        }
        double progress = (distance - bond) / (max - bond);
        return 1.0D - floor * progress;
    }

    private static LivingEntity findBondWolf(ServerPlayer player) {
        double radius = ModConfig.neutronWolfBondRadius.get();
        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, area,
                wolf -> isArmoredWolf(wolf) && isOwnedBy(wolf, player));
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distance = candidate.distanceToSqr(player);
            if (distance <= radius * radius && distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private static boolean isOwnedBy(LivingEntity wolf, ServerPlayer player) {
        return wolf instanceof OwnableEntity ownable && ownable.getOwner() == player;
    }

    private static void syncHealthBonus(LivingEntity wolf) {
        AttributeInstance maxHealth = wolf.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        if (isArmoredWolf(wolf)) {
            boolean firstApplication = maxHealth.getModifier(HEALTH_MODIFIER_ID) == null;
            maxHealth.addOrUpdateTransientModifier(new AttributeModifier(HEALTH_MODIFIER_ID,
                    ModConfig.neutronWolfHealthBonus.get(), AttributeModifier.Operation.ADD_VALUE));
            if (firstApplication) {
                wolf.setHealth(wolf.getMaxHealth());
            }
        } else if (maxHealth.removeModifier(HEALTH_MODIFIER_ID)) {
            wolf.setHealth(Math.min(wolf.getHealth(), wolf.getMaxHealth()));
        }
    }

    private static boolean isArmoredWolf(LivingEntity entity) {
        return entity.isAlive()
                && entity.getType() == EntityTypes.WOLF
                && entity.getItemBySlot(EquipmentSlot.BODY).is(ModItems.neutron_wolf_armor.get());
    }

    /** 穿甲 / 无视无敌的机制伤害不给绝对免伤，只保留封顶减免。 */
    private static boolean isHaxDamage(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    private static double ownerDistance(LivingEntity wolf) {
        LivingEntity owner = ownerOf(wolf);
        return owner == null ? Double.MAX_VALUE : Math.sqrt(wolf.distanceToSqr(owner));
    }

    private static LivingEntity ownerOf(LivingEntity wolf) {
        if (!(wolf instanceof OwnableEntity ownable)) {
            return null;
        }
        LivingEntity owner = ownable.getOwner();
        if (owner == null || owner.isRemoved() || owner.isDeadOrDying() || owner.level() != wolf.level()) {
            return null;
        }
        return owner;
    }

    private static long cooldownTicks(int seconds) {
        return Math.max(0L, seconds) * 20L;
    }

    private static WolfArmorState stateOf(LivingEntity wolf) {
        WolfArmorState state = ((AttachmentTarget) wolf).getAttached(ModDataAttachments.WOLF_ARMOR_STATE);
        return state == null ? WolfArmorState.EMPTY : state;
    }

    private static void setState(LivingEntity wolf, WolfArmorState state) {
        ((AttachmentTarget) wolf).setAttached(ModDataAttachments.WOLF_ARMOR_STATE, state);
    }

    /**
     * 服务端反馈：粒子与音效直接由 {@link ServerLevel} 广播，不额外开网络包。
     *
     * @param action 0 护卫抵消，1 图腾，2 羁绊
     */
    private static void playGuardFeedback(LivingEntity entity, int action) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.5D;
        double z = entity.getZ();
        switch (action) {
            case 0 -> {
                level.sendParticles(ParticleTypes.ENCHANT, x, y, z, 8, 0.35D, 0.45D, 0.35D, 0.02D);
                level.playSound(null, x, y, z, SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 0.8F, 1.5F);
            }
            case 1 -> {
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 30, 0.4D, 0.6D, 0.4D, 0.3D);
                level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            default -> {
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 20, 0.3D, 0.5D, 0.3D, 0.25D);
                level.sendParticles(ParticleTypes.HEART, x, y + 0.6D, z, 4, 0.3D, 0.3D, 0.3D, 0.0D);
                level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.8F, 1.4F);
            }
        }
    }
}
